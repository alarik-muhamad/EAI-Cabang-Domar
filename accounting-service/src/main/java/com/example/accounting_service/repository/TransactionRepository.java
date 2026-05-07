package com.example.accounting_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.accounting_service.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    List<Transaction> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<Transaction> findByBranchIdAndTypeOrderByCreatedAtDesc(Long branchId, Transaction.TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.branchId = :branchId " +
           "AND t.createdAt BETWEEN :start AND :end ORDER BY t.createdAt DESC")
    List<Transaction> findByBranchIdAndPeriod(Long branchId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByPeriod(LocalDateTime start, LocalDateTime end);
}