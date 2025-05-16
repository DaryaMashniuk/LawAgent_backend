package by.masnhyuk.lawAgent.dto;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

public record DiffBlock(
        DiffMatchPatch.Operation operation,
        String text,
        Integer oldIndex,
        Integer newIndex
) {}