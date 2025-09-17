package Codify.result.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Codeline")
public class Codeline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codelineId")
    private Long codelineId;
    
    @Column(name = "resultId")
    private Long resultId;
    
    @Column(name = "studentId")
    private Long studentId;
    
    @Column(name = "startLine")
    private Integer startLine;
    
    @Column(name = "endLine")
    private Integer endLine;
}
