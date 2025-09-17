package Codify.result.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "Submission")
public class Submission {
    @Id
    @Column(name = "submissionId")
    private Long submissionId;
    
    @Column(name = "assignmentId")
    private Long assignmentId;
    
    @Column(name = "fileName")
    private String fileName;
    
    @Column(name = "week")
    private Integer week;
    
    @Column(name = "submissionDate")
    private LocalDateTime submissionDate;
    
    @Column(name = "studentId")
    private Long studentId;
    
    @Column(name = "studentName")
    private String studentName;
    
    @Column(name = "s3Key")
    private String s3Key;
}
