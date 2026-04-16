package com.ayushch.streamforge.upload.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ayushch.streamforge.upload.model.UploadSession;
import com.ayushch.streamforge.upload.repository.UploadSessionRepository;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import net.coobird.thumbnailator.Thumbnails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaEventConsumer {
    
    private final MinioClient minioClient;
    private final UploadSessionRepository sessionRepository;

    @Value("${minio.bucket.name}")
    private String BUCKET_NAME;
    
    @KafkaListener(topics = "media-uploads", groupId = "streamforge-group")
    public void consume(MediaUploaderEvent event) {
        log.info("[kafka-topic]: Worker received file processing request for ID: {}", event.fileId());
        
        if (!event.contentType().startsWith("image/")) {
            log.info("Skipping non-image file: {}", event.contentType());
            // Video processing logic will go here later
            return;
        }

        try (InputStream originalStream = minioClient.getObject(
            GetObjectArgs.builder()
            .bucket(BUCKET_NAME)
            .object(event.storageKey())
            .build())) {
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(originalStream)
                .size(320, 180)      //16:9 ratio for thumbnail
                .outputFormat("jpg")
                .toOutputStream(os);

            byte[] thumbnailBytes = os.toByteArray();
            String thumbnailKey = "thumbnails/" + event.fileId() + ".jpg";

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .object(thumbnailKey)
                    .stream(new ByteArrayInputStream(thumbnailBytes), (long) thumbnailBytes.length, -1L)
                    .contentType("image/jpeg")
                    .build()
            );
            log.info("Thumbnail successfully uploaded to MinIO at key: {}", thumbnailKey);

            UploadSession session = sessionRepository.findById(event.fileId())
                .orElseThrow(() -> new RuntimeException("Session not found"));
            session.setThumbnailPath(thumbnailKey);
            sessionRepository.save(session);


        } catch (Exception e) {
            log.error("Failed to process thumbnail for fileId: {}", event.fileId(), e);
        }
    }
}
