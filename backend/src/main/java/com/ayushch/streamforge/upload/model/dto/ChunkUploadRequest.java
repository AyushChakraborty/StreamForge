package com.ayushch.streamforge.upload.model.dto;

import java.io.InputStream;
import java.util.UUID;

public record ChunkUploadRequest (
        UUID uploadId,
        int chunkIndex,
        InputStream inputStream,
        long chunkSize
) {}
