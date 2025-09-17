package Codify.result.web.controller;

import Codify.result.service.ResultService;
import Codify.result.web.dto.CompareResponseDto;
import Codify.result.web.dto.PlagiarismJudgeResponseDto;
import Codify.result.web.dto.SaveResultRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/result")
@Slf4j
public class ResultController {

    private final ResultService resultService;

    @Operation(
            operationId = "compareStudentSubmissions",
            summary = "두 학생의 제출물 비교 결과 조회",
            description = """
            두 학생의 제출물을 비교한 결과를 반환합니다.
            - studentFromId, studentToId: 비교할 학생 ID
            - week: 주차 정보
            - S3에서 실제 코드 파일과 함께 표절 의심라인 반환
            """
    )
    @GetMapping("/assignments/{assignmentId}/compare")
    public ResponseEntity<CompareResponseDto> compareStudents(
            @RequestHeader("USER-UUID") String userUuidHeader,
            @PathVariable Long assignmentId,
            @RequestParam("studentFromId") Long studentFromId,
            @RequestParam("studentToId") Long studentToId,
            @RequestParam("week") Integer week
    ) {
        final UUID userUuid = UUID.fromString(userUuidHeader);
        
        CompareResponseDto compareResult = resultService.getCompareResult(userUuid, assignmentId, studentFromId, studentToId, week);
        return ResponseEntity.ok(compareResult);
    }

    @Operation(
            operationId = "judgePlagiarism",
            summary = "표절 판단 결과 조회",
            description = """
            두 학생의 제출물에 대한 표절 판단 결과를 반환합니다.
            - studentFromId, studentToId: 비교할 학생 ID
            - week: 주차 정보
            - assignmentId: 과제 ID
            """
    )
    @GetMapping("/assignments/{assignmentId}/judge")
    public ResponseEntity<PlagiarismJudgeResponseDto> judgePlagiarism(
            @RequestHeader("USER-UUID") String userUuidHeader,
            @PathVariable Long assignmentId,
            @RequestParam("studentFromId") Long studentFromId,
            @RequestParam("studentToId") Long studentToId,
            @RequestParam("week") Integer week
    ) {
        final UUID userUuid = UUID.fromString(userUuidHeader);
        
        PlagiarismJudgeResponseDto judgeResult = resultService.getPlagiarismJudgeResult(userUuid, assignmentId, studentFromId, studentToId, week);
        return ResponseEntity.ok(judgeResult);
    }

    @Operation(
            operationId = "saveResult",
            summary = "표절 결과 저장",
            description = """
            표절 검사 결과를 저장합니다.
            - assignmentId: 과제 ID (Path Variable)
            - week: 주차 정보 (Request Parameter)
            - USER-UUID: 사용자 UUID (Request Header)
            - plagiarize: 표절 여부 (true: 표절, false: 표절 아님)
            - student1, student2: 비교 대상 학생들의 정보
            """
    )
    @PostMapping("/assignments/{assignmentId}/save")
    public ResponseEntity<Void> saveResult(
            @RequestHeader("USER-UUID") String userUuidHeader,
            @PathVariable Long assignmentId,
            @RequestParam("week") Integer week,
            @RequestBody SaveResultRequestDto saveResultRequestDto
    ) {
        final UUID userUuid = UUID.fromString(userUuidHeader);
        
        resultService.saveResult(userUuid, assignmentId, week, saveResultRequestDto);
        return ResponseEntity.ok().build();
    }
}
