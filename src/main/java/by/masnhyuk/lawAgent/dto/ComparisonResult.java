package by.masnhyuk.lawAgent.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ComparisonResult(
        List<DiffBlock> diffBlocks,
        boolean hasChanges,
        LocalDateTime firstVersionDate,
        LocalDateTime secondVersionDate,
        UUID firstVersionId,
        UUID secondVersionId,
        String firstVersionHtml,
        String secondVersionHtml
) {}