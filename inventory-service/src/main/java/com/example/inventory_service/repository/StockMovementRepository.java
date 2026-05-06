package com.example.inventory_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inventory_service.entity.StockMovement;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    List<StockMovement> findByProductIdAndBranchIdOrderByCreatedAtDesc(Long productId, Long branchId);
    List<StockMovement> findByReferenceNumber(String referenceNumber);
}