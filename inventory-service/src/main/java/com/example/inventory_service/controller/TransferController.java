package com.example.inventory_service.controller;

import com.example.inventory_service.dto.TransferRequestDto;
import com.example.inventory_service.entity.TransferRequest;
import com.example.inventory_service.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/transfer")
@Tag(name = "Transfer", description = "Transfer stok antar cabang")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/pending")
    @Operation(summary = "Ambil semua transfer yang menunggu approval")
    public ResponseEntity<List<TransferRequest>> getPending() {
        return ResponseEntity.ok(transferService.getPendingTransfers());
    }

    @PostMapping("/request")
    @Operation(summary = "Request transfer stok ke cabang lain")
    public ResponseEntity<TransferRequest> request(@RequestBody @Valid TransferRequestDto dto) {
        return new ResponseEntity<>(transferService.requestTransfer(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/approve/{referenceNumber}")
    @Operation(summary = "Approve transfer (khusus PUSAT)")
    public ResponseEntity<TransferRequest> approve(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(transferService.approveTransfer(referenceNumber));
    }

    @PatchMapping("/reject/{referenceNumber}")
    @Operation(summary = "Reject transfer (khusus PUSAT)")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String referenceNumber) {
        transferService.rejectTransfer(referenceNumber);
        return ResponseEntity.ok(Map.of("message", "Transfer ditolak"));
    }
}