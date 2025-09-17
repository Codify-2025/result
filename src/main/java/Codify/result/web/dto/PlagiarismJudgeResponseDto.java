package Codify.result.web.dto;

import lombok.Builder;

@Builder
public record PlagiarismJudgeResponseDto(
        Integer similarity,
        StudentInfoDto student1,
        StudentInfoDto student2
) {
    @Builder
    public static record StudentInfoDto(
            String id,
            String name,
            String submittedTime
    ) {}
}
