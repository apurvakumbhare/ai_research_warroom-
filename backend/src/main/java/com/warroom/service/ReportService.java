package com.warroom.service;

import com.google.cloud.firestore.Firestore;
import com.warroom.entity.AgentOutput;
import com.warroom.entity.ChatSession;
import com.warroom.entity.Project;
import com.warroom.exception.WarRoomException;
import com.warroom.repository.AgentOutputRepository;
import com.warroom.repository.ChatSessionRepository;
import com.warroom.repository.ProjectRepository;
import com.warroom.service.agent.OpenAIClient;
import com.warroom.service.file.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ChatSessionRepository chatSessionRepository;
    private final ProjectRepository projectRepository;
    private final AgentOutputRepository agentOutputRepository;
    private final OpenAIClient openAIClient;
    private final FirebaseStorageService firebaseStorageService;
    private final Firestore firestore;

    /**
     * Finalizes the chat session by generating a professional summary,
     * converting it to a PDF, uploading to Firebase, and recording it in Firestore.
     */
    public String finalizeReport(UUID chatSessionId) {
        log.info("Finalizing report for chat session: {}", chatSessionId);

        ChatSession session = chatSessionRepository.findById(chatSessionId.toString()).block();
        if (session == null) {
            throw new WarRoomException("NOT_FOUND", "Chat session not found");
        }

        Project project = projectRepository.findById(session.getProjectId())
                .orElseThrow(() -> new WarRoomException("NOT_FOUND", "Project not found"));

        List<AgentOutput> outputs = agentOutputRepository.findByChatSessionIdOrderByGeneratedAtAsc(session.getId());
        String debateHistory = outputs.stream()
                .map(o -> o.getAgentName() + ": " + o.getOutput())
                .collect(Collectors.joining("\n\n"));

        // 1. Synthesize final summary via LLM
        String prompt = "You are a Chief AI Summarizer. Given the following debate history, provide a concise but highly professional executive summary (1-2 paragraphs) and a 1-100 confidence score format (just the number at the end). \n\nDebate History:\n"
                + debateHistory;
        String llmResponse = openAIClient.call(prompt);

        // Simple extraction for the sake of standard implementation
        String summary = llmResponse.replaceAll("(\\d{1,3})$", "").trim();
        String confidenceScoreStr = llmResponse.replaceAll(".*[^0-9](\\d{1,3})$", "$1").trim();
        double confidence = 85.0;
        try {
            confidence = Double.parseDouble(confidenceScoreStr);
        } catch (Exception ignored) {
        }

        // 2. Generate PDF
        byte[] pdfBytes = generatePdf("Executive Summary: " + project.getProjectName(), summary);

        // 3. Upload PDF to Storage
        MultipartFile pdfFile = new MultipartFile() {
            @Override
            public String getName() {
                return "report.pdf";
            }

            @Override
            public String getOriginalFilename() {
                return "report.pdf";
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public boolean isEmpty() {
                return pdfBytes.length == 0;
            }

            @Override
            public long getSize() {
                return pdfBytes.length;
            }

            @Override
            public byte[] getBytes() {
                return pdfBytes;
            }

            @Override
            public java.io.InputStream getInputStream() {
                return new java.io.ByteArrayInputStream(pdfBytes);
            }

            @Override
            public void transferTo(java.io.File dest) throws java.io.IOException, IllegalStateException {
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                    fos.write(pdfBytes);
                }
            }
        };
        String fileUrl = firebaseStorageService.uploadFile(pdfFile, project.getId());

        // 4. Save to Firestore
        String reportId = UUID.randomUUID().toString();
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("projectId", project.getId());
        reportData.put("chatSessionId", session.getId());
        reportData.put("title", "Final Report - " + project.getProjectName());
        reportData.put("summary", summary);
        reportData.put("confidenceScore", confidence);
        reportData.put("fileUrl", fileUrl);
        reportData.put("status", "COMPLETED");
        reportData.put("createdAt", com.google.cloud.Timestamp.now());
        reportData.put("ownerId", "AI_SYSTEM");

        firestore.collection("reports").document(reportId).set(reportData);

        log.info("Successfully generated and saved report: {}", reportId);
        return reportId;
    }

    /**
     * Retrieves reports for a given project ID from Firestore.
     */
    public java.util.List<Map<String, Object>> getReportsByProjectId(String projectId) {
        try {
            com.google.api.core.ApiFuture<com.google.cloud.firestore.QuerySnapshot> future = firestore
                    .collection("reports")
                    .whereEqualTo("projectId", projectId)
                    .get();
            List<com.google.cloud.firestore.QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents.stream().map(doc -> {
                Map<String, Object> data = doc.getData();
                data.put("id", doc.getId());
                return data;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch reports for project {}", projectId, e);
            throw new WarRoomException("FETCH_FAILED", "Failed to retrieve reports");
        }
    }

    private byte[] generatePdf(String title, String content) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 650);
                contentStream.setLeading(14.5f);

                // Simple wrap
                String[] words = content.split(" ");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    if (line.length() + word.length() > 80) {
                        contentStream.showText(line.toString());
                        contentStream.newLine();
                        line = new StringBuilder();
                    }
                    line.append(word).append(" ");
                }
                if (line.length() > 0) {
                    contentStream.showText(line.toString());
                }
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error creating PDF: ", e);
            throw new WarRoomException("PDF_GENERATION_FAILED", "Could not generate PDF report");
        }
    }
}
