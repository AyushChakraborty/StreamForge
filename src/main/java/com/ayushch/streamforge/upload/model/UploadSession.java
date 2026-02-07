package com.ayushch.streamforge.upload.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "upload_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession {
    @Id   //primary key
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String contentType;

    private long fileSize;

    private int totalChunks;

    public enum UploadStatus {
        PENDING, COMPLETED, FAILED
    }
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UploadStatus status = UploadStatus.PENDING;

    //defining the 1 to many relationship with ChunkMetadata
    //mappedBy uses the field name(in object world) and not attribute name(in the db world)
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChunkMetadata> chunks = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
