package com.ronin.projects.dto;

public record ProjectFileUpdateResponse(
        String filePath,
        boolean dryRun,
        String oldContent,
        String newContent
) {
}
