package com.example.accounting_service.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.accounting_service.entity.FinancialReport;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, Long> {
    List<FinancialReport> findByBranchIdOrderByGeneratedAtDesc(Long branchId);
    List<FinancialReport> findByBranchIdAndReportType(Long branchId, FinancialReport.ReportType reportType);
    List<FinancialReport> findByPeriodStartGreaterThanEqualAndPeriodEndLessThanEqual(
            LocalDate start, LocalDate end);
}