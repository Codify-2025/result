package Codify.result.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "Assignment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignmentId")
    private Long assignmentId;

    @Column(name = "assignmentName")
    private String assignmentName;

    @Column(name = "startDate")
    private LocalDateTime startDate;

    @Column(name = "endDate")
    private LocalDateTime endDate;

    @Column(name = "week")
    private Long week;

    @Column(name = "userUuid", columnDefinition = "BINARY(16)")
    private UUID userUuid;

    @Column(name = "subjectId")
    private Long subjectId;

}