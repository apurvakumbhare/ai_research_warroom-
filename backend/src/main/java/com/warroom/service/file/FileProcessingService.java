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

    /**
     * Processes an uploaded PDF, extracts text, and cleans it for AI consumption.
     * 
     * @param file the multipart PDF file
     * @return the normalized text content
     */
    public String processPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.error("File processing failed: file is missing");
            throw new WarRoomException("FILE_REQUIRED", "Please upload a valid PDF file");
        }

        log.info("Starting processing for file: {}", file.getOriginalFilename());

        // 1. Extract text
        String rawText = pdfExtractorService.extractText(file);

        // 2. Clean text
        String cleanedText = textCleanerService.clean(rawText);

        log.info("Successfully processed and cleaned text for: {}", file.getOriginalFilename());

        return cleanedText;
    }
}
