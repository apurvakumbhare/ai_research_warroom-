package com.warroom.controller;

import com.warroom.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Finalizes a debate by generating a summarization report and saving it.
     */
    @PostMapping("/finalize/{debateId}")
    public ResponseEntity<String> finalizeReport(@PathVariable UUID debateId) {
        log.info("Received request to finalize report for debate: {}", debateId);
        String reportId = reportService.finalizeReport(debateId);
        return ResponseEntity.ok("Report created successfully with ID: " + reportId);
    }

    /**
     * Retrieves all reports for a specific project.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getReportsByProjectId(@PathVariable String projectId) {
        log.info("Fetching reports for project: {}", projectId);
        return ResponseEntity.ok(reportService.getReportsByProjectId(projectId));
    }
}
