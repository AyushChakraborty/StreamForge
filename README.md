# StreamForge

## Overview

StreamForge is a media upload and processing application. It handles large file uploads via chunking and uses an event driven architecture to perform asynchronous media processing tasks, such as thumbnail generation.

## Architecture & Tech Stack

- Frontend: Next.js, React
- Backend: Java, Spring Boot
- Object Storage: MinIO
- Database: PostgreSQL
- Event Streaming: Apache Kafka, Zookeeper
- Image Processing: Thumbnailator

## Prerequisites

- Java 17 or higher
- Node.js and npm
- Docker and Docker Compose
- Maven

## Getting Started

### 1. Infrastructure

Start the required infrastructure services (PostgreSQL, MinIO, Kafka, Zookeeper) using Docker Compose.

```bash
cd backend
docker compose up -d
```

### 2. Backend

Start the Spring Boot server.

```bash
cd backend
./mvnw clean compile spring-boot:run
```

### 3. Frontend

Start the Next.js development server.

```bash
cd frontend
npm install
npm run dev
```

The frontend will be accessible at http://localhost:3000.

(This project is still in progress, and I will be deploying it when its at a satisfactory level according to me)

### Core features (till now)

- Chunked file uploads for large media files.

- Automatic file assembly and validation upon upload completion.

- Asynchronous thumbnail generation for images via Kafka event streaming.

- Unique object key storage to prevent file collisions in MinIO.
