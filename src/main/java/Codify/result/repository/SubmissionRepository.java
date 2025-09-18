package Codify.result.repository;

import Codify.result.domain.Submission;
import Codify.result.web.dto.StudentResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findBySubmissionId(Long submissionId);
    
    // 특정 과제와 주차에 대한 제출 존재 여부 확인
    boolean existsByAssignmentIdAndWeek(Long assignmentId, Integer week);
    
    // 학생의 특정 과제, 주차 제출물 조회
    Optional<Submission> findByAssignmentIdAndWeekAndStudentId(Long assignmentId, Integer week, Long studentId);

    @Query("SELECT DISTINCT s.studentId, s.studentName " +
            "FROM Submission s " +
            "WHERE s.assignmentId = :assignmentId AND s.week = :week")
    List<StudentResponseDto> findStudentData(Long assignmentId, Long week);
    
    // 토폴로지를 위한 특정 과제와 주차의 모든 학생 제출물 조회
    @Query("SELECT s FROM Submission s " +
           "WHERE s.assignmentId = :assignmentId " +
           "AND s.week = :week " +
           "ORDER BY s.studentId")
    List<Submission> findTopologySubmissions(@Param("assignmentId") Long assignmentId, 
                                           @Param("week") Integer week);
}
