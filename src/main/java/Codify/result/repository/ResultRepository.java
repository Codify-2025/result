package Codify.result.repository;

import Codify.result.domain.Result;
import Codify.result.web.dto.FilteredPairsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResultRepository extends JpaRepository<Result, Long> {
    Optional<Result> findByStudentFromIdAndStudentToIdAndAssignmentId(
            Long studentFromId, Long studentToId, Long assignmentId);

    @Query("SELECT " +
            "r.studentFromId AS fromId, " +
            "r.studentToId AS toId, " +
            "r.accumulateResult AS similarity " +
            "FROM Assignment a " +
            "INNER JOIN Result r ON a.assignmentId = r.assignmentId " +
            "WHERE a.userUuid = :userUuid " +
            "AND a.week = :week " +
            "AND r.assignmentId = :assignmentId")
    List<FilteredPairsDto> findByUserAndWeek(@Param("userUuid") UUID userUuid,
                                             @Param("assignmentId") Long assignmentId,
                                             @Param("week") Long week);

    // 토폴로지를 위한 쿼리 - 특정 과제와 주차의 모든 유사도 결과 조회
    @Query("SELECT r FROM Result r " +
           "INNER JOIN Assignment a ON r.assignmentId = a.assignmentId " +
           "WHERE r.assignmentId = :assignmentId " +
           "AND a.week = :week " +
           "AND a.userUuid = :userUuid " +
           "ORDER BY r.studentFromId, r.studentToId")
    List<Result> findTopologyResults(@Param("userUuid") UUID userUuid,
                                     @Param("assignmentId") Long assignmentId,
                                     @Param("week") Long week);
}
