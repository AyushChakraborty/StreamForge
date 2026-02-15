package com.ayushch.streamforge.upload.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "chunk_metadata", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_id", "chunk_index"})
}) //ensures that no duplicate chunks are stored if sent over the network
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private UploadSession session;

    @Column(nullable = false)
    private Integer chunkIndex;

    private Long size;

    private String etag;   //MinIO ETag, the ETag uniquely identifies the data content of an object
}
