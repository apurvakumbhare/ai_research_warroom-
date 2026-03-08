package com.warroom.service.file;

import com.warroom.exception.WarRoomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Orchestrator service for file processing flows.
 * Coordinates PDF extraction and content cleaning.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final PdfExtractorService pdfExtractorService;
    private final TextCleanerService textCleanerService;
    private final FirebaseStorageService firebaseStorageService;
    private final com.google.cloud.firestore.Firestore firestore;

    /**
     * Processes an uploaded PDF, extracts text, cleans it, uploads to Storage, and
     * saves metadata.
     */
    public String processPdf(MultipartFile file, String projectId, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            log.error("File processing failed: file is missing");
            throw new WarRoomException("FILE_REQUIRED", "Please upload a valid PDF file");
        }

        log.info("Starting processing for file: {}", file.getOriginalFilename());

        // 1. Extract text
        String rawText = pdfExtractorService.extractText(file);

        // 2. Clean text
        String cleanedText = textCleanerService.clean(rawText);

        // 3. Upload to Firebase Storage
        String storageUrl = firebaseStorageService.uploadFile(file, projectId);

        // 4. Save metadata to Firestore if projectId is provided
        if (projectId != null) {
            java.util.Map<String, Object> uploadData = new java.util.HashMap<>();
            uploadData.put("fileName", file.getOriginalFilename());
            uploadData.put("fileType", file.getContentType());
            uploadData.put("fileSize", file.getSize());
            uploadData.put("storageUrl", storageUrl);
            uploadData.put("extractedText", cleanedText);
            uploadData.put("uploadedAt", com.google.cloud.Timestamp.now());
            uploadData.put("uploadedBy", uploadedBy);

            firestore.collection("projects").document(projectId).collection("uploads").add(uploadData);
            log.info("Saved upload metadata to Firestore for project: {}", projectId);
        }

        log.info("Successfully processed and stored file: {}", file.getOriginalFilename());
        return cleanedText;
    }
}
