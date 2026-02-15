package com.ayushch.streamforge.upload.model.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
public record InitiateUploadRequest (
        @NotBlank(message = "File name is required")
        String fileName,

        @Positive(message = "File size must be positive")
        long fileSize,

        int totalChunks,

        String contentType
) {}
