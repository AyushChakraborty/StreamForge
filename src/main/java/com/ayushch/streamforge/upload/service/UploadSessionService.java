package com.ayushch.streamforge.upload.service;

import com.ayushch.streamforge.upload.model.UploadSession;
import com.ayushch.streamforge.upload.model.dto.InitiateUploadRequest;
import com.ayushch.streamforge.upload.model.dto.InitiateUploadResponse;
import com.ayushch.streamforge.upload.repository.UploadSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadSessionService {

    private final UploadSessionRepository sessionRepository;

    @Transactional
    public InitiateUploadResponse initiateUpload(InitiateUploadRequest request) {
        //this method takes in the file metadata in the form of
        //InitiateUploadRequest DTO, and creates the UploadSession entity
        //based on it, and saves it to the psql db
        log.info("Initiating upload for file: {}", request.fileName());

        UploadSession session = UploadSession.builder()
                .filename(request.fileName())
                .contentType(request.contentType())
                .fileSize(request.fileSize())
                .status(UploadSession.UploadStatus.PENDING)
                .build();

        //save to psql db
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

}
