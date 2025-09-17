package Codify.result.web.dto;

import java.util.List;

public record FilterPairsGroupDto(List<FilteredPairsDto> aboveThreshold, List<FilteredPairsDto> belowThreshold) {
}
