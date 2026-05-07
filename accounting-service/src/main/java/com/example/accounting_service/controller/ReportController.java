package com.example.accounting_service.controller;

import com.example.accounting_service.dto.ReportRequest;
import com.example.accounting_service.entity.FinancialReport;
import com.example.accounting_service.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting/reports")
@Tag(name = "Reports", description = "Laporan keuangan per cabang")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Ambil semua laporan per cabang")
    public ResponseEntity<List<FinancialReport>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(reportService.getReportsByBranch(branchId));
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate laporan keuangan")
    public ResponseEntity<FinancialReport> generate(@RequestBody @Valid ReportRequest request) {
        return new ResponseEntity<>(reportService.generateReport(request), HttpStatus.CREATED);
    }
}