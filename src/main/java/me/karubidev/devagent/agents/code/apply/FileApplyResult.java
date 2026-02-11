package me.karubidev.devagent.agents.code.apply;

import java.util.List;

public record FileApplyResult(
    boolean dryRun,
    int parsedFiles,
    int writtenFiles,
    int skippedFiles,
    List<FileApplyItem> files
) {
}
