package com.example.accounting_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.accounting_service.dto.TransactionRequest;
import com.example.accounting_service.entity.Transaction;
import com.example.accounting_service.event.StockUpdatedEvent;
import com.example.accounting_service.event.TransferEvent;
import com.example.accounting_service.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getByBranch(Long branchId) {
        return transactionRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
    }

    public List<Transaction> getByBranchAndPeriod(Long branchId,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
        return transactionRepository.findByBranchIdAndPeriod(branchId, start, end);
    }

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setReferenceNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tx.setType(request.getType());
        tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx.setBranchId(request.getBranchId());
        tx.setRelatedBranchId(request.getRelatedBranchId());
        tx.setProductId(request.getProductId());
        tx.setProductName(request.getProductName());
        tx.setQuantity(request.getQuantity());
        tx.setUnitPrice(request.getUnitPrice());
        tx.setTotalAmount(request.getQuantity() * request.getUnitPrice());
        tx.setNotes(request.getNotes());
        tx.setCreatedAt(LocalDateTime.now());
        return transactionRepository.save(tx);
    }

    @Transactional
    public void recordFromStockEvent(StockUpdatedEvent event) {
        if (transactionRepository.findByReferenceNumber(
                event.getReferenceNumber()).isPresent()) return;

        Transaction.TransactionType type = switch (event.getMovementType()) {
            case "STOCK_IN" -> Transaction.TransactionType.STOCK_IN;
            case "STOCK_OUT" -> Transaction.TransactionType.STOCK_OUT;
            case "ADJUSTMENT" -> Transaction.TransactionType.ADJUSTMENT;
            default -> null;
        };
        if (type == null) return;

        Transaction tx = new Transaction();
        tx.setReferenceNumber(event.getReferenceNumber() != null
                ? event.getReferenceNumber()
                : "AUTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tx.setType(type);
        tx.setStatus(Transaction.TransactionStatus.COMPLETED);
        tx.setBranchId(event.getBranchId());
        tx.setProductId(event.getProductId());
        tx.setProductName("Product-" + event.getProductId());
        tx.setQuantity(event.getNewQuantity());
        tx.setUnitPrice(0.0);
        tx.setTotalAmount(0.0);
        tx.setNotes("Auto-recorded dari Kafka event");
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    @Transactional
    public void recordFromTransferEvent(TransferEvent event) {
        if (transactionRepository.findByReferenceNumber(
                event.getReferenceNumber()).isPresent()) return;

        Transaction tx = new Transaction();
        tx.setReferenceNumber(event.getReferenceNumber());
        tx.setType(Transaction.TransactionType.TRANSFER);
        tx.setStatus(Transaction.TransactionStatus.PENDING);
        tx.setBranchId(event.getFromBranchId());
        tx.setRelatedBranchId(event.getToBranchId());
        tx.setProductId(event.getProductId());
        tx.setProductName("Product-" + event.getProductId());
        tx.setQuantity(event.getQuantity());
        tx.setUnitPrice(0.0);
        tx.setTotalAmount(0.0);
        tx.setNotes("Transfer dari cabang " + event.getFromBranchId()
                + " ke cabang " + event.getToBranchId());
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }

    @Transactional
    public void completeTransferTransaction(String referenceNumber) {
        transactionRepository.findByReferenceNumber(referenceNumber).ifPresent(tx -> {
            tx.setStatus(Transaction.TransactionStatus.COMPLETED);
            tx.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(tx);
        });
    }
}