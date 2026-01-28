package com.ayushch.streamforge.upload.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "upload_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession {
    @Id   //primary key
    private UUID id;
}
