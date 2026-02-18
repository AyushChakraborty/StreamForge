'use client'

import { useState, ChangeEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import axios from "axios";
import { 
    FileIcon,
    CloudArrowUpIcon,
    CheckCircleIcon
} from "@phosphor-icons/react";
import { InitiateUploadRequest, InitiateUploadResponse, ChunkUploadResponse, UploadCompleteRequest, UploadCompleteResponse } from "@/types/upload";
import { stat } from "fs";
import { SpinnerGapIcon } from "@phosphor-icons/react/dist/ssr";

const CHUNK_SIZE = 5 * 1024 * 1024;   //5mb
const API_BASE = "http://localhost:8080/api/v1/upload"

export default function UploadWidget() {
    const [file, setFile] = useState<File | null>(null);
    const [status, setStatus] = useState('IDLE');
    const [progress, setProgress] = useState(0);
    const [uploadId, setUploadId] = useState<string>("");

    const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setFile(e.target.files[0]);
            setStatus('IDLE');
            setProgress(0);
            setUploadId("");
        }
    };

    const uploadFile = async () => {
        if (!file) return;
        setStatus('UPLOADING');
        setProgress(0);

        try {
            //initiate the upload first
            const total_chunks = Math.ceil(file.size / CHUNK_SIZE);
            console.log(`number of chunks: ${total_chunks}`);
            const initPayload: InitiateUploadRequest = {
                fileName: file.name,
                fileSize: file.size,
                totalChunks: total_chunks,
                contentType: file.type,
            };

            const {data: initResponse} = await axios.post<InitiateUploadResponse>(
                `${API_BASE}/initiate`,
                initPayload
            );      
            
            //since there is no UUID type in ts, its handled as a string here
            const currentUploadId = initResponse.uploadId;
            setUploadId(currentUploadId);
            console.log(`upload id is set: ${currentUploadId}`);
            
            //for now using a simple blocking loop, later plan is for a sliding windowed version
            for (let i = 0; i < total_chunks; i++) {
                const start = i * CHUNK_SIZE;
                const end = Math.min(start + CHUNK_SIZE, file.size);
                const chunk = file.slice(start, end); 

                const formData = new FormData();
                formData.append('uploadId', initResponse.uploadId);
                formData.append('chunkIndex', i.toString());
                formData.append('file', chunk);
                
                console.log(`ok got till here, chunk is made, its size is: ${formData}`)
                
                const {data : chunkUploadResponse} = await axios.post<ChunkUploadResponse>(
                    `${API_BASE}/chunk`,
                    formData
                );

                if (!chunkUploadResponse) {
                    throw new Error(`failed to upload chunk {i}, try again`);
                }
                
                setProgress(Math.round((i+1)/total_chunks)*100);
            }

            //since upload is completed, hit the /complete server endpoint to stitch
            //the chunks
            const completePayload: UploadCompleteRequest = {
                uploadId: currentUploadId,
            };

            const {data : completeResponse} = await axios.post<UploadCompleteResponse>(
                `${API_BASE}/complete`,
                completePayload
            );

            setStatus('COMPLETED');
        }catch (e) {
            console.log(`$error: ${e}`);
            setStatus('ERROR');
        }
    };

    return (
        <div className="w-full max-w-lg bg-[#F5F5F5] border-2 border-retro-dark shadow-[8px_8px_0px_0px_rgba(26,26,26,1)]">
            {/*upload widget header*/}
            <div className="bg-retro-dark text-retro-bg p-4 flex justify-between items-center">
                <span className="text-sm uppercase tracking-widest">Operation: Upload</span>
                <div className="flex gap-2">
                    <div className="w-3 h-3 rounded-full bg-red-500 border border-white/20"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-500 border border-white/20"></div>
                    <div className="w-3 h-3 rounded-full bg-green-500 border border-white/20"></div>
                </div>
            </div>

            {/*upload widget body*/}
            <div className="p-6 relative">
                <div className="absolute top-0 left-0 w-full h-1 border b-2 border-dashed border-retro-dark"></div>

                {/*file drop area*/}
                <div className="mb-6">
                    <label className={`
                        flex flex-col items-center justify-center h-48
                        border-2 border-dashed border-retro-dark/40
                        hover:bg-retro-dark/5 transition-all cursor-pointer
                        ${file ? 'bg-retro-green/10 border-retro-green': ''}
                    `}>
                        <div className="flex flex-col items-center justify-center pt-5 pb-6">
                            {file ? (
                                <>
                                    <FileIcon size={48} weight="duotone" className="text-retro-dark mb-2" />
                                    <p className="mb-2 text-sm text-retro-dark font-bold uppercase">{file.name}</p>
                                    <p className="text-xs text-retro-dark/60">
                                        {(file.size / (1024*1024)).toFixed(2)} MB
                                    </p>
                                </>
                            ) : (
                                <>
                                    <CloudArrowUpIcon size={48} className="text-retro-dark/60 mb-3" />
                                    <p className="mb-2 text-sm text-retro-dark">
                                        <span className="font-bold underland">CLICK TO SELECT</span>
                                    </p>
                                    <p className="text-xs text-retro-dark/50">ANY FORMAT!</p>
                                </>
                            )}
                        </div>
                        <input type="file" className="hidden" onChange={handleFileChange} />
                    </label>
                </div>

                {/*action button*/}
                <button
                    onClick={uploadFile}
                    disabled={!file || status === 'UPLOADING'}
                    className={`
                        w-full py-3 px-4 border-2 border-retro-dark text-sm uppercase font-bold tracking-wider
                        transaction-all active:translate-y-1 active:shadow-none
                        flex items-center justify-center gap-2
                        ${!file || status === 'UPLOADING'
                            ? 'bg-gray-200 text-gray-400 cursor-not-allowed border-gray-400'
                            : 'bg-retro-green text-retro-dark hover:bg-retro-green/80 shadow-[4px_4px_0px_0px_rgba(26,26,26,1)]'
                        }
                    `}
                >
                    {status === 'UPLOADING' ? (
                        <>
                            <SpinnerGapIcon size={20} className="animate-spin/" />
                            Processing...
                        </>
                    ) : (
                        'Initiate Transfer'
                    )}
                </button>

                {/*status area*/}
                {(status === 'UPLOADING' || status === 'COMPLETED') && (
                    <div className="mt-6 font-mono text-xs border-t-2 border-dashed border-retro-dark/20 pt-4">
                        <div className="flex justify-between mb-1">
                            <span>STATUS:</span>
                            <span className={status === 'COMPLETED' ? 'text-green-600' : 'text-blue-600'}>
                                {status}
                            </span>
                        </div>

                        {/*progress bar container*/}
                        <div className="w-full h-4 border border-retro-dark p-0.5 mt-2">
                            <div
                                className="h-full bg-retro-dark transition-all duration-300"
                                style={{width : `${progress}%`}}
                            ></div>
                        </div>
                        <div className="text-right mt-1">{progress}%</div>

                        {status === 'COMPLETED' && (
                            <div className="mt-4 p-2 bg-retro-green/20 border border-retro-green text-retro-dark break-all">
                                <div className="flex items-center gap-2 mb-1 font-bold">
                                    <CheckCircleIcon size={16} weight="fill" />
                                    UPLOAD COMPLETE!
                                </div>
                                ID: {uploadId}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
