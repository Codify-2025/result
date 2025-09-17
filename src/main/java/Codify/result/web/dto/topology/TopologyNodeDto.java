package Codify.result.web.dto.topology;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record TopologyNodeDto(
        String id,
        String label,
        String fileName,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime submittedAt,
        List<RelatedFileDto> relatedFiles
) {}
