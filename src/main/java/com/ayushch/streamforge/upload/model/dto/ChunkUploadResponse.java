package com.ayushch.streamforge.upload.model.dto;

import java.util.UUID;

public record ChunkUploadResponse (
        UUID uploadId,
        int chunkIndex,
        boolean success,
        String message
) {}
