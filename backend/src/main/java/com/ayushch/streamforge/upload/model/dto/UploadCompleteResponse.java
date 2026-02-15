package com.ayushch.streamforge.upload.model.dto;

import java.util.UUID;

public record UploadCompleteResponse (
        UUID uploadId,
        boolean success,
        String message
) {}
