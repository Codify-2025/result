package Codify.result.service;

import Codify.result.domain.Codeline;
import Codify.result.domain.Result;
import Codify.result.exception.*;
import Codify.result.repository.CodelineRepository;
import Codify.result.repository.ResultRepository;
import Codify.result.repository.SubmissionRepository;
import Codify.result.repository.UserRepository;
import Codify.result.web.dto.CompareResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultService {

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

        // Result 테이블에서 결과 조회
        Result result = resultRepository.findByStudentFromIdAndStudentToIdAndAssignmentId(
                studentFromId, studentToId, assignmentId
        ).orElseThrow(() -> new ComparisonResultNotFoundException());

        // 각 학생별 표절된 코드 라인 정보 조회
        List<Codeline> student1Lines = codelineRepository.findByResultIdAndStudentId(
                result.getId(), studentFromId
        );
        List<Codeline> student2Lines = codelineRepository.findByResultIdAndStudentId(
                result.getId(), studentToId
        );

        // 제출 정보 조회
        SubmissionInfoService.SubmissionInfo submission1 = submissionInfoService.getSubmissionInfo(result.getSubmissionFromId());
        SubmissionInfoService.SubmissionInfo submission2 = submissionInfoService.getSubmissionInfo(result.getSubmissionToId());

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
}
