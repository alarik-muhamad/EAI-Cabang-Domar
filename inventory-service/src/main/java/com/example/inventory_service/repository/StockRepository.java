package com.example.inventory_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.inventory_service.entity.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductIdAndBranchId(Long productId, Long branchId);
    List<Stock> findByBranchId(Long branchId);

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.minimumStock")
    List<Stock> findLowStocks();

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.minimumStock AND s.branchId = :branchId")
    List<Stock> findLowStocksByBranch(Long branchId);
}