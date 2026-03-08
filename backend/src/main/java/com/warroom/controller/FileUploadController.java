package com.warroom.controller;

import com.warroom.service.file.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for handling file uploads and initial text extraction.
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileProcessingService fileProcessingService;

    /**
     * Uploads a PDF and returns the cleaned, extracted text.
     * 
     * @param file the multipart PDF file
     * @return the processed and cleaned text content
     */
    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request for: {}", file.getOriginalFilename());
        String processedText = fileProcessingService.processPdf(file);
        return ResponseEntity.ok(processedText);
    }
}
