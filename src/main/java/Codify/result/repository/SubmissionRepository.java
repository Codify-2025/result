package Codify.result.repository;

import Codify.result.domain.Submission;
import Codify.result.web.dto.StudentResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findBySubmissionId(Long submissionId);
    
    // 특정 과제와 주차에 대한 제출 존재 여부 확인
    boolean existsByAssignmentIdAndWeek(Long assignmentId, Integer week);

    @Query("SELECT DISTINCT s.studentId, s.studentName " +
            "FROM Submission s " +
            "WHERE s.assignmentId = :assignmentId AND s.week = :week")
    List<StudentResponseDto> findStudentData(Long assignmentId, Long week);
}
