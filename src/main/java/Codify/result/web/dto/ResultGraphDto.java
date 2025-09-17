package Codify.result.web.dto;

import java.util.List;

public record ResultGraphDto(List<StudentResponseDto> nodes, FilterSummaryDto filterSummary, FilterPairsGroupDto filterPairs) {
}
