package Codify.result.web.dto.topology;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HistoryDto(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime submittedFrom,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime submittedTo
) {}
