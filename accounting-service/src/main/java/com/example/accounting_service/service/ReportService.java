package com.example.accounting_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.accounting_service.dto.ReportRequest;
import com.example.accounting_service.entity.FinancialReport;
import com.example.accounting_service.entity.Transaction;
import com.example.accounting_service.repository.FinancialReportRepository;
import com.example.accounting_service.repository.TransactionRepository;

@Service
public class ReportService {

    private final FinancialReportRepository reportRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(FinancialReportRepository reportRepository,
                         TransactionRepository transactionRepository) {
        this.reportRepository = reportRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<FinancialReport> getReportsByBranch(Long branchId) {
        return reportRepository.findByBranchIdOrderByGeneratedAtDesc(branchId);
    }

    @Transactional
    public FinancialReport generateReport(ReportRequest request) {
        LocalDateTime start = request.getPeriodStart().atStartOfDay();
        LocalDateTime end = request.getPeriodEnd().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository
                .findByBranchIdAndPeriod(request.getBranchId(), start, end);

        double stockInValue = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.STOCK_IN)
                .mapToDouble(Transaction::getTotalAmount).sum();

        double stockOutValue = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.STOCK_OUT)
                .mapToDouble(Transaction::getTotalAmount).sum();

        double transferValue = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.TRANSFER)
                .mapToDouble(Transaction::getTotalAmount).sum();

        FinancialReport report = new FinancialReport();
        report.setBranchId(request.getBranchId());
        report.setReportType(request.getReportType());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setTotalStockInValue(stockInValue);
        report.setTotalStockOutValue(stockOutValue);
        report.setTotalTransferValue(transferValue);
        report.setNetValue(stockInValue - stockOutValue);
        report.setTotalTransactions(transactions.size());
        report.setGeneratedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }
}