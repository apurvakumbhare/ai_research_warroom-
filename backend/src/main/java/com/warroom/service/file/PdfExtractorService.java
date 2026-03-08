package com.warroom.service.file;

import com.warroom.exception.WarRoomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service for extracting raw text from PDF documents.
 * Utilizes Apache PDFBox for robust document parsing.
 */
@Slf4j
@Service
public class PdfExtractorService {

    /**
     * Extracts text from an uploaded PDF file.
     * 
     * @param file the multipart file from the controller
     * @return the raw extracted text
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new WarRoomException("FILE_EMPTY", "Cannot extract text from an empty file");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new WarRoomException("INVALID_FILE_TYPE", "Only PDF files are supported");
        }

        log.info("Starting text extraction from PDF: {}", file.getOriginalFilename());

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new WarRoomException("PDF_ENCRYPTED", "Encrypted PDFs are not supported");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            log.info("Successfully extracted text from PDF: {} (Length: {})",
                    file.getOriginalFilename(), text.length());

            return text;

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage());
            throw new WarRoomException("PDF_EXTRACTION_FAILURE", "Could not read the PDF content");
        }
    }
}
