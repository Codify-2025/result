package Codify.result.service;

import Codify.result.domain.Submission;
import Codify.result.exception.StudentSubmissionNotFoundException;
import Codify.result.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionInfoService {

    private final SubmissionRepository submissionRepository;

    @Value("${codify.timezone.source:UTC}")
    private String sourceTimezone;

    @Value("${codify.timezone.target:Asia/Seoul}")
    private String targetTimezone;

    @Value("${codify.timezone.format:yyyy-MM-dd HH:mm}")
    private String dateFormat;

    @Transactional(readOnly = true)
    public SubmissionInfo getSubmissionInfo(Long submissionId) {
        // 제출 정보 조회
        Submission submission = submissionRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new StudentSubmissionNotFoundException());
        
        // UTC → KST 변환
        ZonedDateTime targetTime = submission.getSubmissionDate()
                .atZone(ZoneId.of(sourceTimezone))
                .withZoneSameInstant(ZoneId.of(targetTimezone));

        String formattedTime = targetTime.format(DateTimeFormatter.ofPattern(dateFormat));
        
        // 어떤 제출물을 조회했는지 추적, 문제 발생 시 호출 이력 확인
        log.info("Retrieved submission info for submissionId: {}, fileName: {}",
                submissionId, submission.getFileName());
        
        return new SubmissionInfo(
                submission.getFileName(), 
                submission.getStudentName(), 
                formattedTime,
                submission.getS3Key()
        );
    }

    // 제출 정보를 나타내는 클래스
    public static class SubmissionInfo {
        private final String fileName;
        private final String studentName;
        private final String submissionTime;
        private final String s3Key;

        public SubmissionInfo(String fileName, String studentName, String submissionTime, String s3Key) {
            this.fileName = fileName;
            this.studentName = studentName;
            this.submissionTime = submissionTime;
            this.s3Key = s3Key;
        }

        public String getFileName() { return fileName; }
        public String getStudentName() { return studentName; }
        public String getSubmissionTime() { return submissionTime; }
        public String getS3Key() { return s3Key; }
    }
}
