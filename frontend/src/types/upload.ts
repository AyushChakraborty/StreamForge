export interface InitiateUploadRequest {
    fileName: string;
    fileSize: number;
    totalChunks: number;
    contentType: string;
};

export interface InitiateUploadResponse {
    uploadId: string;
    key: string;
};

export interface ChunkUploadResponse {
    uploadResponse: string;
    chunkIndex: number;
    success: boolean;
    message: string;
};

export interface UploadCompleteRequest {
    uploadId: string;
};

export interface UploadCompleteResponse {
        uploadId: string;
        success: boolean,
        message: string;
};
