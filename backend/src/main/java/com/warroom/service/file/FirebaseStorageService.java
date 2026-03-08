package com.warroom.service.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.warroom.exception.WarRoomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    private final StorageClient storageClient;

    /**
     * Uploads a file to Firebase Storage.
     * 
     * @param file      the multipart file to upload
     * @param projectId optional project ID to organize files
     * @return the public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String projectId) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            fileName = UUID.randomUUID().toString();
        }

        String path = (projectId != null ? "projects/" + projectId + "/" : "uploads/") + UUID.randomUUID() + "_"
                + fileName;

        try {
            Bucket bucket = storageClient.bucket();
            Blob blob = bucket.create(path, file.getBytes(), file.getContentType());
            log.info("Uploaded file to Firebase Storage: {}", path);

            // To get a download URL, we can construct the Firebase storage URL format
            // format:
            // https://firebasestorage.googleapis.com/v0/b/{bucket_name}/o/{path}?alt=media
            String encodedPath = java.net.URLEncoder.encode(path, "UTF-8");
            return "https://firebasestorage.googleapis.com/v0/b/" + bucket.getName() + "/o/" + encodedPath
                    + "?alt=media";
        } catch (IOException e) {
            log.error("Failed to upload file to Firebase Storage", e);
            throw new WarRoomException("UPLOAD_FAILED", "Failed to upload file to storage: " + e.getMessage());
        }
    }
}
