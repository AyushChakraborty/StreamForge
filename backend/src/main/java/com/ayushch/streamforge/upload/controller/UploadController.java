package com.ayushch.streamforge.upload.controller;

import com.ayushch.streamforge.upload.model.dto.*;
import com.ayushch.streamforge.upload.service.UploadSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadSessionService uploadSessionService;

    @PostMapping(path = "/initiate")
    public ResponseEntity<InitiateUploadResponse> initiateUpload(
            @Valid @RequestBody InitiateUploadRequest request
            ) {
        InitiateUploadResponse response = uploadSessionService.initiateUpload(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChunkUploadResponse> uploadChunk(
            @RequestParam("uploadId") UUID uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("file") MultipartFile file
            ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ChunkUploadResponse(uploadId, chunkIndex, false, "File chunk is empty"));
        }

        ChunkUploadRequest request = new ChunkUploadRequest(
                uploadId,
                chunkIndex,
                file.getInputStream(),
                file.getSize()
        );

        ChunkUploadResponse response = uploadSessionService.uploadChunk(request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping(path = "/complete")
    public ResponseEntity<UploadCompleteResponse> completeUpload(
            @Valid @RequestBody UploadCompleteRequest request
    ) {
        UploadCompleteResponse response = uploadSessionService.checkCompletion(request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
