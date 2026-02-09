package com.ayushch.streamforge.upload.service;

import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class StorageServiceTest {

//    @Autowired
//    private StorageService storageService;
//
//    @Autowired
//    private MinioClient minioClient;
//
//
//    @Test
//    void testSampleFileUpload() {
//        String filename = "testfile.txt";
//        String content = "hello minio!";
//
//        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
//
//        storageService.uploadFile(filename, stream, content.length(), "text/plain");
//
//        boolean exists = checkFileExistsInMinio("streamforge-media", filename);
//        assertTrue(exists, "File should exist in MinIO");
//    }
//
//    private boolean checkFileExistsInMinio(String bucket, String objectName) {
//        try {
//            //makes a HEAD request, basically a req for the metadata of the object
//            minioClient.statObject(StatObjectArgs.builder()
//                    .bucket(bucket)
//                    .object(objectName)
//                    .build());
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
