package com.ayushch.streamforge.upload.model.dto;

import java.util.UUID;

public record InitiateUploadResponse (
        UUID uploadId,
        String key     //unique object key in MinIO
) {}
