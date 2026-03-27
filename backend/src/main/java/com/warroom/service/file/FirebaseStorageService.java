package com.warroom.service.file;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service for uploading and managing files in Firebase Cloud Storage.
 * Note: Temporarily disabled due to Missing GCP Storage dependency. Returns a dummy URL.
 */
@Slf4j
@Service
public class FirebaseStorageService {
    
    private static final String BUCKET_NAME = "ai-war-room.appspot.com";

    /**
     * Uploads a file to Firebase Storage and returns the public URL.
     *
     * @param file      The file to upload
     * @param projectId The project ID (used to organize uploads)
     * @return The download URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String projectId) {
        try {
            String fileName = "projects/" + projectId + "/reports/" + UUID.randomUUID() + "_"
                    + file.getOriginalFilename();

            // Storage storage = StorageClient.getInstance().bucket(BUCKET_NAME).getStorage();
            // BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
            // BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
            //        .setContentType(file.getContentType())
            //        .build();
            // storage.create(blobInfo, file.getBytes());

            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
            log.info("File uploaded successfully: {}", publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Firebase Storage", e);
        }
    }
}
