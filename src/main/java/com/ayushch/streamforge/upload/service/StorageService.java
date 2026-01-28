package com.ayushch.streamforge.upload.service;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
public class StorageService {
    private final MinioClient minioClient;

    //get the properties from application.properties
    @Value("${minio.bucket.name}")
    private String mediaBucket;

    @Value("${minio.bucket.chunk}")
    private String chunkBucket;

    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void uploadFile(String objectName, InputStream inputStream, long fileSize, String contentType) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(mediaBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(mediaBucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder().bucket(mediaBucket)
                    .object(objectName).stream(
                            inputStream, fileSize, -1
                    ).contentType(contentType).build());

        }catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    public void uploadChunk(String uploadId, String chunkIndex, InputStream inputStream, long chunkSize, String contentType) {
        try {
            String objectName = uploadId + "/chunk-" + chunkIndex;
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(chunkBucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(chunkBucket).build());
            }
            minioClient.putObject(PutObjectArgs.builder().bucket(chunkBucket)
                    .object(objectName).stream(
                            inputStream, chunkSize, -1
                    ).contentType(contentType).build());
        }catch (Exception e) {
            throw new RuntimeException("Failed to upload chunk: " + e.getMessage());
        }
    }

    public void assembleChunks(String uploadId, int totalChunks, String fileName, String contentType) throws IOException {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("upload-", "-" + uploadId);

            try (OutputStream out = Files.newOutputStream(tempFile, StandardOpenOption.APPEND)) {
                for (int i=0; i<totalChunks; i++) {
                    InputStream chunkStream = getChunk(uploadId, i);
                    chunkStream.transferTo(out);
                    chunkStream.close();
                }
            }

            try (InputStream fileStream = Files.newInputStream(tempFile, StandardOpenOption.READ)) {
                long fileSize = Files.size(tempFile);

                minioClient.putObject(PutObjectArgs.builder()
                                .bucket(mediaBucket)
                                .object(fileName)
                                .stream(fileStream, fileSize, -1)
                                .contentType(contentType)
                        .build());
            }

            deleteChunks(uploadId, totalChunks);

        }catch (Exception e) {
            throw new RuntimeException("Failed to assemble chunks: " + e.getMessage());
        }finally {
            if (tempFile != null) {
                Files.deleteIfExists(tempFile);
            }
        }
    }

    private InputStream getChunk(String uploadId, int chunkIndex) throws Exception{
        String objectName = uploadId + "/chunk-" + chunkIndex;

        return minioClient.getObject(
                GetObjectArgs.builder().bucket(chunkBucket).object(objectName).build()
        );
    }

    public void deleteChunks(String uploadId, int totalChunks) {
        try {
            for (int i=0; i<totalChunks; i++) {
                String objectName = uploadId + "/chunk-" + i;

                minioClient.removeObject(RemoveObjectArgs.builder()
                                .bucket(chunkBucket)
                                .object(objectName)
                                .build());
            }
        }catch (Exception e) {
            log.error("Failed to delete chunks for upload: {}", uploadId, e);
        }
    }

}
