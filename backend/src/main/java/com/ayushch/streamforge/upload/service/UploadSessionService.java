package com.ayushch.streamforge.upload.service;

import com.ayushch.streamforge.upload.events.MediaEventProducer;
import com.ayushch.streamforge.upload.events.MediaUploaderEvent;
import com.ayushch.streamforge.upload.model.ChunkMetadata;
import com.ayushch.streamforge.upload.model.UploadSession;
import com.ayushch.streamforge.upload.model.dto.*;
import com.ayushch.streamforge.upload.repository.ChunkMetadataRepository;
import com.ayushch.streamforge.upload.repository.UploadSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository sessionRepository;
    private final ChunkMetadataRepository chunkMetadataRepository;
    private final StorageService storageService;
    private final MediaEventProducer mediaEventProducer;

    @Transactional
    public InitiateUploadResponse initiateUpload(InitiateUploadRequest request) {
        log.info("Initiating upload for file: {}", request.fileName());

        //preparing the record to be inserted
        UploadSession session = UploadSession.builder()
                .filename(request.fileName())
                .contentType(request.contentType())
                .fileSize(request.fileSize())
                .totalChunks(request.totalChunks())
                .status(UploadSession.UploadStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        log.info("created record, time to insert it now");
        //insert row into existing 'upload_sessions' table
        UploadSession savedSession = sessionRepository.save(session);

        log.info("Upload session created with ID: {}", session.getId());

        //return Id to the client, so that they can use it to start sending the chunks
        //via the POST /upload/chunk with uploadID, chunkIndex and byte[] binary
        //representing that chunk

        return new InitiateUploadResponse(
                savedSession.getId(),
                savedSession.getId().toString() + "/" + request.fileName()
        );
    }

    @Transactional
    public ChunkUploadResponse uploadChunk(ChunkUploadRequest request) {
        log.info("Initiating chunk upload for uploadId: {} with chunkIndex: {}", request.uploadId(), request.chunkIndex());

        UploadSession session = sessionRepository.findById(request.uploadId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String etag = storageService.uploadChunk(
                request.uploadId(),
                request.chunkIndex(),
                request.inputStream(),
                request.chunkSize(),
                session.getContentType()
        );

        if (etag.isEmpty()) {
            return new ChunkUploadResponse(
                    request.uploadId(),
                    request.chunkIndex(),
                    false,
                    "Re-upload the chunk"
            );
        }

        //add record of this chunk
        ChunkMetadata metadata = ChunkMetadata.builder()
                .session(session)
                .chunkIndex(request.chunkIndex())
                .size(request.chunkSize())
                .etag(etag)
                .build();

        session.getChunks().add(metadata);
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return new ChunkUploadResponse(
                request.uploadId(),
                request.chunkIndex(),
                true,
                "Chunk uploaded successfully"
        );
    }

    private boolean areAllChunksPresent(UploadSession session) {
        //extract all unique indices in a set (unique constraint already present in chunk_metadata
        //but just to be sure)
        if (session.getChunks() == null || session.getChunks().isEmpty()) {
            return false;
        }
        Set<Integer> uploadedIndices = session.getChunks().stream()
                .map(ChunkMetadata::getChunkIndex)
                .collect(Collectors.toSet());

        if (uploadedIndices.size() != session.getTotalChunks()) {
            return false;
        }

        for (int i = 0; i < session.getTotalChunks(); i++) {
            if (!uploadedIndices.contains(i)) {
                return false;
            }
        }
        return true;
    }

    public UploadCompleteResponse checkCompletion(UploadCompleteRequest request) {
        UploadSession session = sessionRepository.findById(request.uploadId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
        log.info("the session is present, starting checks for assembly now!");
        //validate if all the chunks are present
        if (!areAllChunksPresent(session)) {
            log.info("not all chunks are present");
            return new UploadCompleteResponse(
                    session.getId(),
                    false,
                    "Upload incomplete: Missing chunks"
            );
        }

        try {
            boolean assembled = storageService.assembleChunks(
                    session.getId(),
                    session.getTotalChunks(),
                    session.getFilename(),
                    session.getContentType()
            );

            if (!assembled) {
                log.info("assembly for sure failed");
                return new UploadCompleteResponse(
                        session.getId(),
                        false,
                        "File assembly failed. Please retry completion"
                );
            }

            session.setStatus(UploadSession.UploadStatus.COMPLETED);
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(session);

            //once saved to psql db, push the uploadId to media-uploads topic to be
            //pulled by the processor to make and save its thumbnail to the object store

            //start of producer logic for thumbnail generation
            String minioObjectKey = session.getId().toString() + "/" + session.getFilename();

            MediaUploaderEvent event = new MediaUploaderEvent(
                session.getId(),
                minioObjectKey,
                session.getContentType()
            );
            mediaEventProducer.sendMediaUploaderEvent(event);

            return new UploadCompleteResponse(
                    session.getId(),
                    true,
                    "Upload completed and file assembled successfully"
            );
            //end of producer logic here

        }catch (Exception e) {
            log.info("Failed to assemble file for session {}", session.getId(), e);
            return new UploadCompleteResponse(
                    session.getId(),
                    false,
                    "Upload incomplete"
            );
        }
    }
}
