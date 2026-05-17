package com.example.inventory_service.saga;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory/saga")
@Tag(name = "Transfer Saga", description = "Monitoring status saga transfer stok")
public class TransferSagaController {

    private final TransferSagaStateRepository sagaRepo;
    private final TransferSagaOrchestrator orchestrator;

    public TransferSagaController(TransferSagaStateRepository sagaRepo,
                                   TransferSagaOrchestrator orchestrator) {
        this.sagaRepo = sagaRepo;
        this.orchestrator = orchestrator;
    }

    @GetMapping("/{referenceNumber}")
    @Operation(summary = "Cek status saga berdasarkan reference number")
    public ResponseEntity<TransferSagaState> getStatus(@PathVariable String referenceNumber) {
        return sagaRepo.findByReferenceNumber(referenceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Ambil semua saga dengan status tertentu")
    public ResponseEntity<List<TransferSagaState>> getByStatus(
            @PathVariable TransferSagaState.SagaStatus status) {
        return ResponseEntity.ok(sagaRepo.findByStatus(status));
    }

    /**
     * Manual retry untuk saga yang stuck di COMPENSATING.
     * Berguna saat infrastructure sementara down.
     */
    @PostMapping("/{referenceNumber}/retry-compensation")
    @Operation(summary = "Manual retry kompensasi saga yang gagal")
    public ResponseEntity<Map<String, String>> retryCompensation(
            @PathVariable String referenceNumber) {
        return sagaRepo.findByReferenceNumber(referenceNumber)
                .map(saga -> {
                    if (saga.getStatus() != TransferSagaState.SagaStatus.COMPENSATING) {
                        return ResponseEntity.badRequest()
                                .<Map<String, String>>body(Map.of(
                                        "message", "Saga tidak dalam status COMPENSATING"));
                    }
                    orchestrator.triggerCompensation(saga);
                    return ResponseEntity.ok(Map.of("message", "Retry kompensasi dipicu"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
