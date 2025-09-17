package Codify.result.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CompareResponseDto(
        StudentCompareDto student1,
        StudentCompareDto student2
) {
    @Builder
    public static record StudentCompareDto(
            String id,
            String name,
            String fileName,
            String submissionTime,
            List<String> code,
            List<Integer> lines
    ) {}
}