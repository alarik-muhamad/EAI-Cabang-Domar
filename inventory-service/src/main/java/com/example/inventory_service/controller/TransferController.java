package com.example.inventory_service.controller;

import com.example.inventory_service.dto.TransferRequestDto;
import com.example.inventory_service.entity.TransferRequest;
import com.example.inventory_service.saga.TransferSagaOrchestrator;
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
@Tag(name = "Transfer", description = "Transfer stok antar cabang (Saga)")
public class TransferController {

    private final TransferService transferService;
    private final TransferSagaOrchestrator sagaOrchestrator;

    public TransferController(TransferService transferService,
                              TransferSagaOrchestrator sagaOrchestrator) {
        this.transferService = transferService;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @GetMapping("/pending")
    @Operation(summary = "Ambil semua transfer yang menunggu approval")
    public ResponseEntity<List<TransferRequest>> getPending() {
        return ResponseEntity.ok(transferService.getPendingTransfers());
    }


    @PostMapping("/request")
    @Operation(summary = "Request transfer stok (Saga: reserve stok + publish event)")
    public ResponseEntity<TransferRequest> request(@RequestBody @Valid TransferRequestDto dto) {
        return new ResponseEntity<>(sagaOrchestrator.startSaga(dto), HttpStatus.CREATED);
    }


    @PatchMapping("/approve/{referenceNumber}")
    @Operation(summary = "Approve transfer (khusus PUSAT) — melanjutkan saga")
    public ResponseEntity<Map<String, String>> approve(@PathVariable String referenceNumber) {
        sagaOrchestrator.onTransferApproved(referenceNumber);
        return ResponseEntity.ok(Map.of("message", "Transfer disetujui, saga melanjutkan delivery"));
    }


    @PatchMapping("/reject/{referenceNumber}")
    @Operation(summary = "Reject transfer (khusus PUSAT) — trigger kompensasi saga")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String referenceNumber) {
        sagaOrchestrator.onTransferRejected(referenceNumber);
        return ResponseEntity.ok(Map.of("message", "Transfer ditolak, saga mengembalikan stok"));
    }
}
