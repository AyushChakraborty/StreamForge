package com.ayushch.streamforge.upload.model.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UploadCompleteRequest (
        @NotNull
        UUID uploadId
) {}
