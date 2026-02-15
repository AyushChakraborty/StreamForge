package com.ayushch.streamforge.upload.repository;

import com.ayushch.streamforge.upload.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, UUID> {
}
