package com.example.accounting_service.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "financial_reports")
public class FinancialReport {

    public enum ReportType {
        DAILY, MONTHLY, YEARLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long branchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false)
    private Double totalStockInValue;

    @Column(nullable = false)
    private Double totalStockOutValue;

    @Column(nullable = false)
    private Double totalTransferValue;

    @Column(nullable = false)
    private Double netValue;

    @Column(nullable = false)
    private Integer totalTransactions;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    public FinancialReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Double getTotalStockInValue() { return totalStockInValue; }
    public void setTotalStockInValue(Double totalStockInValue) { this.totalStockInValue = totalStockInValue; }
    public Double getTotalStockOutValue() { return totalStockOutValue; }
    public void setTotalStockOutValue(Double totalStockOutValue) { this.totalStockOutValue = totalStockOutValue; }
    public Double getTotalTransferValue() { return totalTransferValue; }
    public void setTotalTransferValue(Double totalTransferValue) { this.totalTransferValue = totalTransferValue; }
    public Double getNetValue() { return netValue; }
    public void setNetValue(Double netValue) { this.netValue = netValue; }
    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}