package Codify.result.service;

import Codify.result.domain.Codeline;
import Codify.result.domain.Result;
import Codify.result.domain.Submission;
import Codify.result.exception.*;
import Codify.result.repository.CodelineRepository;
import Codify.result.repository.ResultRepository;
import Codify.result.repository.SubmissionRepository;
import Codify.result.repository.UserRepository;
import Codify.result.web.dto.CompareResponseDto;
import Codify.result.web.dto.PlagiarismJudgeResponseDto;
import Codify.result.web.dto.SaveResultRequestDto;
import Codify.result.web.dto.*;
import Codify.result.web.dto.topology.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultService {

    private static  final double SIMILARITY_THRESHOLD = 0.8;

    private final ResultRepository resultRepository;
    private final CodelineRepository codelineRepository;
    private final S3FileService s3FileService;
    private final SubmissionInfoService submissionInfoService;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public CompareResponseDto getCompareResult(UUID userUuid, Long assignmentId, Long studentFromId, Long studentToId, Integer week) {

        // 사용자 검증
        if (userUuid == null) {
            throw new UnauthenticatedException();
        }
        if (!userRepository.existsById(userUuid)) {
            throw new UserNotFoundException();
        }

        // 기본 파라미터 검증
        if (studentFromId == null || studentFromId <= 0 || studentToId == null || studentToId <= 0) {
            throw new StudentNotFoundException();
        }
        if (week == null || week <= 0) {
            throw new InvalidWeekParameterException();
        }

        // 동일한 학생끼리 비교 방지
        if (studentFromId.equals(studentToId)) {
            throw new SameStudentComparisonException();
        }

        // 주차 검증 - Submission 테이블에서 해당 과제의 해당 주차가 존재하는지 확인
        if (!submissionRepository.existsByAssignmentIdAndWeek(assignmentId, week)) {
            throw new WeekNotFoundException();
        }

        // Result 테이블에서 결과 조회 (양방향 검색)
        Result result = findResultBidirectional(studentFromId, studentToId, assignmentId);

        // 각 학생별 표절된 코드 라인 정보 조회
        List<Codeline> student1Lines = codelineRepository.findByResultIdAndStudentId(
                result.getId(), studentFromId
        );
        List<Codeline> student2Lines = codelineRepository.findByResultIdAndStudentId(
                result.getId(), studentToId
        );

        // 제출 정보 조회 (실제 DB 저장 순서 고려)
        SubmissionInfoService.SubmissionInfo submission1 = submissionInfoService.getSubmissionInfo(
                result.getStudentFromId().equals(studentFromId) ? result.getSubmissionFromId() : result.getSubmissionToId()
        );
        SubmissionInfoService.SubmissionInfo submission2 = submissionInfoService.getSubmissionInfo(
                result.getStudentFromId().equals(studentFromId) ? result.getSubmissionToId() : result.getSubmissionFromId()
        );

        // 표절 라인 변환
        List<Integer> lines1 = convertToLineNumbers(student1Lines);
        List<Integer> lines2 = convertToLineNumbers(student2Lines);

        // S3에서 표절된 라인의 코드만 조회
        List<String> code1 = s3FileService.getSpecificLinesFromS3(submission1.getS3Key(), lines1);
        List<String> code2 = s3FileService.getSpecificLinesFromS3(submission2.getS3Key(), lines2);

        return CompareResponseDto.builder()
                .student1(CompareResponseDto.StudentCompareDto.builder()
                        .id(studentFromId.toString())
                        .name(submission1.getStudentName())
                        .fileName(submission1.getFileName())
                        .submissionTime(submission1.getSubmissionTime())
                        .code(code1)
                        .lines(lines1)
                        .build())
                .student2(CompareResponseDto.StudentCompareDto.builder()
                        .id(studentToId.toString())
                        .name(submission2.getStudentName())
                        .fileName(submission2.getFileName())
                        .submissionTime(submission2.getSubmissionTime())
                        .code(code2)
                        .lines(lines2)
                        .build())
                .build();
    }

    private List<Integer> convertToLineNumbers(List<Codeline> codelines) {
        return codelines.stream()
                .flatMapToInt(codeline ->
                        IntStream.rangeClosed(codeline.getStartLine(), codeline.getEndLine()))
                .distinct() // 중복 라인 번호 제거
                .sorted() // 오름차순 정렬
                .boxed()
                .toList();
    }

    @Transactional(readOnly = true)
    public PlagiarismJudgeResponseDto getPlagiarismJudgeResult(UUID userUuid, Long assignmentId, Long studentFromId, Long studentToId, Integer week) {

        // 사용자 검증
        if (userUuid == null) {
            throw new UnauthenticatedException();
        }
        if (!userRepository.existsById(userUuid)) {
            throw new UserNotFoundException();
        }

        // 기본 파라미터 검증
        if (studentFromId == null || studentFromId <= 0 || studentToId == null || studentToId <= 0) {
            throw new StudentNotFoundException();
        }
        if (week == null || week <= 0) {
            throw new InvalidWeekParameterException();
        }

        // 동일한 학생끼리 비교 방지
        if (studentFromId.equals(studentToId)) {
            throw new SameStudentComparisonException();
        }

        // 주차 검증 - Submission 테이블에서 해당 과제의 해당 주차가 존재하는지 확인
        if (!submissionRepository.existsByAssignmentIdAndWeek(assignmentId, week)) {
            throw new WeekNotFoundException();
        }

        // Result 테이블에서 결과 조회
        Result result = findResultBidirectional(studentFromId, studentToId, assignmentId);

        // 제출 정보 조회
        SubmissionInfoService.SubmissionInfo submission1 = submissionInfoService.getSubmissionInfo(
                result.getStudentFromId().equals(studentFromId) ? result.getSubmissionFromId() : result.getSubmissionToId()
        );
        SubmissionInfoService.SubmissionInfo submission2 = submissionInfoService.getSubmissionInfo(
                result.getStudentFromId().equals(studentFromId) ? result.getSubmissionToId() : result.getSubmissionFromId()
        );

        // 유사도 퍼센트로 변환
        Integer similarity = (int) Math.round(result.getAccumulateResult() * 100);

        return PlagiarismJudgeResponseDto.builder()
                .similarity(similarity)
                .student1(PlagiarismJudgeResponseDto.StudentInfoDto.builder()
                        .id(studentFromId.toString())
                        .name(submission1.getStudentName())
                        .submittedTime(submission1.getSubmissionTime())
                        .build())
                .student2(PlagiarismJudgeResponseDto.StudentInfoDto.builder()
                        .id(studentToId.toString())
                        .name(submission2.getStudentName())
                        .submittedTime(submission2.getSubmissionTime())
                        .build())
                .build();
    }
    
    private Result findResultBidirectional(Long studentFromId, Long studentToId, Long assignmentId) {
        return resultRepository.findByStudentFromIdAndStudentToIdAndAssignmentId(
                        studentFromId, studentToId, assignmentId)
                .or(() -> resultRepository.findByStudentFromIdAndStudentToIdAndAssignmentId(
                        studentToId, studentFromId, assignmentId))
                .orElseThrow(() -> new ComparisonResultNotFoundException());
    }

    @Transactional
    public void saveResult(UUID userUuid, Long assignmentId, Integer week, SaveResultRequestDto saveResultRequestDto) {
        // 사용자 검증
        if (userUuid == null) {
            throw new UnauthenticatedException();
        }
        if (!userRepository.existsById(userUuid)) {
            throw new UserNotFoundException();
        }
        
        // 주차 검증
        if (week == null || week <= 0) {
            throw new InvalidWeekParameterException();
        }
        
        // 주차 존재 여부 확인
        if (!submissionRepository.existsByAssignmentIdAndWeek(assignmentId, week)) {
            throw new WeekNotFoundException();
        }
        
        // student ID 검증
        Long studentFromId = saveResultRequestDto.getStudent1().getId();
        Long studentToId = saveResultRequestDto.getStudent2().getId();
        
        if (studentFromId == null || studentFromId <= 0 || studentToId == null || studentToId <= 0) {
            throw new StudentNotFoundException();
        }
        
        // 동일한 학생끼리 비교 방지
        if (studentFromId.equals(studentToId)) {
            throw new SameStudentComparisonException();
        }
        
        // 실제 제출물 데이터 검증
        validateSubmissionData(assignmentId, week, saveResultRequestDto.getStudent1(), studentFromId);
        validateSubmissionData(assignmentId, week, saveResultRequestDto.getStudent2(), studentToId);
        
        // Result 테이블에서 해당 비교 결과 찾기
        Result result = findResultBidirectional(studentFromId, studentToId, assignmentId);
        
        // plagiarismJudge 컬럼 업데이트
        result.updatePlagiarismJudge(saveResultRequestDto.isPlagiarize());
        resultRepository.save(result);
    }

    private void validateSubmissionData(Long assignmentId, Integer week, SaveResultRequestDto.StudentInfo studentInfo, Long studentId) {
        // 실제 제출물 조회
        Submission actualSubmission = submissionRepository.findByAssignmentIdAndWeekAndStudentId(assignmentId, week, studentId)
                .orElseThrow(() -> new StudentSubmissionNotFoundException());
        
        // 파일명 검증
        if (!actualSubmission.getFileName().equals(studentInfo.getFileName())) {
            throw new IllegalArgumentException();
        }
        
        // 학생명 검증
        if (!actualSubmission.getStudentName().equals(studentInfo.getName())) {
            throw new IllegalArgumentException();
        }
        
        // 제출시간 검증 (날짜 포맷 고려)
        String actualSubmissionTime = actualSubmission.getSubmissionDate().toString();
        if (!actualSubmissionTime.contains(studentInfo.getSubmittedTime().substring(0, 10))) { // 날짜 부분만 비교
            throw new IllegalArgumentException();
        }
    }

    @Transactional(readOnly = true)
    public ResultGraphDto resultGraph(UUID userUuid, Long assignmentId, Long week) {
    //1. 데이터베이스 조회 -> 과목,week에 해당하는 fromid, toid, accmulate 리턴
        List<FilteredPairsDto> rawDatas = resultRepository.findByUserAndWeek(userUuid, assignmentId, week);

    //2. 학생 데이터 조회 -> submission repository(dashboard참고)
        List<StudentResponseDto> students = submissionRepository.findStudentData(assignmentId, week);

    //3.그룹화 -> threshold기준값 넘는거 aboveThreshold, 넘지 않는거 belowThreshold
        //그룹화를 하면서 동시에 total, aboveThreshold, belowThreshold 숫자 세기
        List<FilteredPairsDto> aboveThreshold = rawDatas.stream()
                .filter(data -> data.similarity() >= SIMILARITY_THRESHOLD).toList();
        List<FilteredPairsDto> belowThreshold = rawDatas.stream()
                .filter(data -> data.similarity() < SIMILARITY_THRESHOLD).toList();

        FilterPairsGroupDto filterPairsGroupDto = new FilterPairsGroupDto(
                aboveThreshold,
                belowThreshold
        );

        FilterSummaryDto filterSummary = new FilterSummaryDto(
                rawDatas.size(),
                aboveThreshold.size(),
                belowThreshold.size(),
                SIMILARITY_THRESHOLD
        );

        ResultGraphDto resultGraphDto = new ResultGraphDto(
                students,
                filterSummary,
                filterPairsGroupDto
        );
        return resultGraphDto;
    }

    @Transactional(readOnly = true)
    public TopologyResponseDto getTopology(UUID userUuid, Long assignmentId, Integer week) {
        // 4. 특정 과제와 주차의 모든 제출물과 유사도 결과 조회
        List<Submission> submissions = submissionRepository.findTopologySubmissions(assignmentId, week);
        List<Result> results = resultRepository.findTopologyResults(userUuid, assignmentId, Long.valueOf(week));

        // 5. 각 학생의 노드 데이터 생성
        List<TopologyNodeDto> nodes = submissions.stream().map(submission -> {
            // 해당 학생과 관련된 유사도 결과들을 찾아서 관련 파일 정보 구성
            List<RelatedFileDto> relatedFiles = results.stream()
                    .filter(result -> result.getStudentFromId().equals(submission.getStudentId()) ||
                                    result.getStudentToId().equals(submission.getStudentId()))
                    .map(result -> {
                        // 해당 학생이 from인지 to인지에 따라 상대방 파일명을 찾음
                        Long partnerId = result.getStudentFromId().equals(submission.getStudentId()) 
                            ? result.getStudentToId() 
                            : result.getStudentFromId();
                        
                        String partnerFileName = submissions.stream()
                                .filter(s -> s.getStudentId().equals(partnerId))
                                .map(Submission::getFileName)
                                .findFirst()
                                .orElse("");
                        
                        return new RelatedFileDto(partnerFileName, result.getAccumulateResult());
                    })
                    .toList();

            return new TopologyNodeDto(
                    submission.getStudentId().toString(),
                    submission.getStudentName(),
                    submission.getFileName(),
                    submission.getSubmissionDate(),
                    relatedFiles
            );
        }).toList();

        // 6. 각 유사도 결과의 엣지 데이터 생성 
        List<TopologyEdgeDto> edges = results.stream().map(result -> {
            // from, to 학생의 제출시간 찾기
            LocalDateTime fromTime = submissions.stream()
                    .filter(s -> s.getStudentId().equals(result.getStudentFromId()))
                    .map(Submission::getSubmissionDate)
                    .findFirst().orElse(null);
            
            LocalDateTime toTime = submissions.stream()
                    .filter(s -> s.getStudentId().equals(result.getStudentToId()))
                    .map(Submission::getSubmissionDate)
                    .findFirst().orElse(null);

            // 파일명 조합 생성
            String fromFileName = submissions.stream()
                    .filter(s -> s.getStudentId().equals(result.getStudentFromId()))
                    .map(Submission::getFileName)
                    .findFirst().orElse("");
            
            String toFileName = submissions.stream()
                    .filter(s -> s.getStudentId().equals(result.getStudentToId()))
                    .map(Submission::getFileName)
                    .findFirst().orElse("");

            String comparedFiles = fromFileName + " - " + toFileName;

            // 히스토리 생성 (현재는 하나만 있지만 리스트 형태)
            List<HistoryDto> histories = List.of(new HistoryDto(fromTime, toTime));

            return new TopologyEdgeDto(
                    result.getStudentFromId() + "-" + result.getStudentToId(),
                    result.getStudentFromId().toString(),
                    result.getStudentToId().toString(),
                    result.getAccumulateResult(),
                    comparedFiles,
                    histories
            );
        }).toList();

        return new TopologyResponseDto(nodes, edges);
    }
}
