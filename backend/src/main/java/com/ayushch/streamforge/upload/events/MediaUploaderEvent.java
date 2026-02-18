package com.ayushch.streamforge.upload.events;

import java.util.UUID;

public record MediaUploaderEvent(
    UUID fileId
) {}
