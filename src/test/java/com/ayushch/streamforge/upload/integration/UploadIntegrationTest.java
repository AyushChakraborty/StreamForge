package com.ayushch.streamforge.upload.integration;

import com.ayushch.streamforge.upload.model.UploadSession;
import com.ayushch.streamforge.upload.model.dto.ChunkUploadRequest;
import com.ayushch.streamforge.upload.model.dto.InitiateUploadRequest;
import com.ayushch.streamforge.upload.model.dto.UploadCompleteRequest;
import com.ayushch.streamforge.upload.model.dto.UploadCompleteResponse;
import com.ayushch.streamforge.upload.repository.UploadSessionRepository;
import com.ayushch.streamforge.upload.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadSessionRepository uploadSessionRepository;

    @MockitoBean
    private StorageService storageService;

    @BeforeEach
    void setup() {
        //ensure fresh db state before each test
        uploadSessionRepository.deleteAll();
    }

    @Test
    void testHappyPathFlow() throws Exception {
        //setup storage layer mocks
        when(storageService.uploadChunk(any(), anyInt(), any(), anyLong(), any()))
                .thenReturn("etag-12345");  //dummy etag

        when(storageService.assembleChunks(any(), anyInt(), any(), any()))
                .thenReturn(true);

        //initiate the upload
        final int ONE_MB = 1024 * 1024;
        final int CHUNK_SIZE = 5 * ONE_MB;
        long file_size = 12 * CHUNK_SIZE;

        int total_chunks = (int)Math.ceil((double)file_size/CHUNK_SIZE);

        InitiateUploadRequest initRequest = new InitiateUploadRequest(
                "test-video.mp4",
                file_size,
                total_chunks,
                "video/mp4"
        );

        String initResponse = mockMvc.perform(post("/api/v1/upload/initiate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadId").exists())
                .andReturn().getResponse().getContentAsString();

        String uploadIdStr = objectMapper.readTree(initResponse).get("uploadId").asString();
        UUID uploadId = UUID.fromString(uploadIdStr);

        //upload the chunks
        for (int i=0; i<total_chunks; i++) {
            MockMultipartFile chunkFile = new MockMultipartFile(
                    "file",
                    "chunk"+i,
                    "application/octet-stream",
                    new byte[CHUNK_SIZE]
            );

            mockMvc.perform(multipart("/api/v1/upload/chunk")
                    .file(chunkFile)
                    .param("uploadId", uploadIdStr)
                    .param("chunkIndex", String.valueOf(i)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        //check for upload complete
        UploadCompleteRequest completeRequest = new UploadCompleteRequest(uploadId);

        mockMvc.perform(post("/api/v1/upload/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        //final chk in the db
        UploadSession finalSession = uploadSessionRepository.findById(uploadId).orElseThrow();
        assertEquals(UploadSession.UploadStatus.COMPLETED, finalSession.getStatus());
    }
}
