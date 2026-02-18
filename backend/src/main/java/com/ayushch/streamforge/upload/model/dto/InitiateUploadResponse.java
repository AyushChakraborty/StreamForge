package com.ayushch.streamforge.upload.model.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record InitiateUploadResponse (
        @NotNull
        UUID uploadId,
        String key     //unique object key in MinIO
) {}
