package com.example.inventory_service.controller;

import com.example.inventory_service.dto.StockRequest;
import com.example.inventory_service.entity.Stock;
import com.example.inventory_service.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Manajemen stok per cabang")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/branch/{branchId}")
    @Operation(summary = "Ambil semua stok di cabang")
    public ResponseEntity<List<Stock>> getByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(stockService.getStockByBranch(branchId));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Ambil semua stok yang hampir habis")
    public ResponseEntity<List<Stock>> getLowStocks() {
        return ResponseEntity.ok(stockService.getLowStocks());
    }

    @GetMapping("/low-stock/branch/{branchId}")
    @Operation(summary = "Ambil stok hampir habis per cabang")
    public ResponseEntity<List<Stock>> getLowStocksByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(stockService.getLowStocksByBranch(branchId));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Adjust stok masuk/keluar")
    public ResponseEntity<Stock> adjust(@RequestBody @Valid StockRequest request) {
        return ResponseEntity.ok(stockService.adjustStock(request));
    }
}