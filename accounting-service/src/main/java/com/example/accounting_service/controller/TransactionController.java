package com.example.accounting_service.controller;

import com.example.accounting_service.dto.TransactionRequest;
import com.example.accounting_service.entity.Transaction;
import com.example.accounting_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounting/transactions")
@Tag(name = "Transactions", description = "Manajemen transaksi keuangan")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "Ambil semua transaksi")
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Ambil transaksi per cabang")
    public ResponseEntity<List<Transaction>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(transactionService.getByBranch(branchId));
    }

    @GetMapping("/branch/{branchId}/period")
    @Operation(summary = "Ambil transaksi per cabang dan periode")
    public ResponseEntity<List<Transaction>> getByBranchAndPeriod(
            @PathVariable Long branchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(transactionService.getByBranchAndPeriod(branchId, start, end));
    }

    @PostMapping
    @Operation(summary = "Buat transaksi manual")
    public ResponseEntity<Transaction> create(@RequestBody @Valid TransactionRequest request) {
        return new ResponseEntity<>(transactionService.createTransaction(request), HttpStatus.CREATED);
    }
}