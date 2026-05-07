package com.example.accounting_service.dto;

import java.time.LocalDate;

import com.example.accounting_service.entity.FinancialReport;

import jakarta.validation.constraints.NotNull;

public class ReportRequest {

    @NotNull(message = "Branch ID wajib diisi")
    private Long branchId;

    @NotNull(message = "Tipe laporan wajib diisi")
    private FinancialReport.ReportType reportType;

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate periodStart;

    @NotNull(message = "Tanggal selesai wajib diisi")
    private LocalDate periodEnd;

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public FinancialReport.ReportType getReportType() { return reportType; }
    public void setReportType(FinancialReport.ReportType reportType) { this.reportType = reportType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
}