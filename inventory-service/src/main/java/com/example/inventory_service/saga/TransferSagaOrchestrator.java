package com.example.inventory_service.saga;

import com.example.inventory_service.dto.StockRequest;
import com.example.inventory_service.dto.TransferRequestDto;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.entity.TransferRequest;
import com.example.inventory_service.event.TransferEvent;
import com.example.inventory_service.kafka.InventoryKafkaProducer;
import com.example.inventory_service.rabbitmq.RabbitMQProducer;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockRepository;
import com.example.inventory_service.repository.TransferRequestRepository;
import com.example.inventory_service.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Choreography-based Saga Orchestrator untuk transfer stok antar cabang.
 *
 * Happy path:
 *   STARTED → STOCK_RESERVED → TRANSFER_APPROVED → STOCK_DELIVERED → COMPLETED
 *
 * Compensating path (jika gagal setelah STOCK_RESERVED):
 *   COMPENSATING → [rollback stok cabang asal] → COMPENSATED
 */
@Service
public class TransferSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(TransferSagaOrchestrator.class);
    private static final int MAX_RETRY = 3;

    private final TransferSagaStateRepository sagaRepo;
    private final TransferRequestRepository transferRepo;
    private final StockRepository stockRepo;
    private final ProductRepository productRepo;
    private final StockService stockService;
    private final InventoryKafkaProducer kafkaProducer;
    private final RabbitMQProducer rabbitMQProducer;

    public TransferSagaOrchestrator(TransferSagaStateRepository sagaRepo,
                                     TransferRequestRepository transferRepo,
                                     StockRepository stockRepo,
                                     ProductRepository productRepo,
                                     StockService stockService,
                                     InventoryKafkaProducer kafkaProducer,
                                     RabbitMQProducer rabbitMQProducer) {
        this.sagaRepo = sagaRepo;
        this.transferRepo = transferRepo;
        this.stockRepo = stockRepo;
        this.productRepo = productRepo;
        this.stockService = stockService;
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    // ─── Step 1: Mulai saga dan reserve stok ─────────────────────────────────

    @Transactional
    public TransferRequest startSaga(TransferRequestDto dto) {
        var product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        var fromStock = stockRepo
                .findByProductIdAndBranchId(dto.getProductId(), dto.getFromBranchId())
                .orElseThrow(() -> new RuntimeException("Stok tidak ditemukan di cabang asal"));

        if (fromStock.getQuantity() < dto.getQuantity())
            throw new RuntimeException("Stok cabang asal tidak cukup: tersedia " + fromStock.getQuantity());

        String refNumber = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Buat transfer request
        TransferRequest transfer = new TransferRequest();
        transfer.setReferenceNumber(refNumber);
        transfer.setProduct(product);
        transfer.setFromBranchId(dto.getFromBranchId());
        transfer.setToBranchId(dto.getToBranchId());
        transfer.setQuantity(dto.getQuantity());
        transfer.setStatus(TransferRequest.TransferStatus.PENDING);
        transfer.setNotes(dto.getNotes());
        transfer.setCreatedAt(LocalDateTime.now());
        TransferRequest saved = transferRepo.save(transfer);

        // Buat saga state
        TransferSagaState saga = TransferSagaState.create(
                refNumber, product.getId(),
                dto.getFromBranchId(), dto.getToBranchId(), dto.getQuantity());

        // Reserve stok cabang asal (kurangi dulu sebelum approval)
        try {
            StockRequest outReq = buildStockRequest(
                    product.getId(), dto.getFromBranchId(), dto.getQuantity(),
                    StockMovement.MovementType.TRANSFER_OUT, refNumber,
                    "Saga step 1: reserve stok untuk transfer");
            stockService.adjustStock(outReq);
            saga.advance(TransferSagaState.SagaStatus.STOCK_RESERVED);
            log.info("[SAGA] {} → STOCK_RESERVED", refNumber);
        } catch (Exception e) {
            saga.fail("Gagal reserve stok: " + e.getMessage());
            sagaRepo.save(saga);
            transferRepo.delete(saved);
            throw new RuntimeException("Saga gagal di step reserve stok: " + e.getMessage());
        }

        sagaRepo.save(saga);

        // Publish event ke Kafka agar PUSAT bisa approve
        kafkaProducer.sendTransferRequested(new TransferEvent(
                refNumber, product.getId(),
                dto.getFromBranchId(), dto.getToBranchId(),
                dto.getQuantity(), "PENDING"));

        return saved;
    }

    // ─── Step 2: Approval oleh PUSAT ─────────────────────────────────────────

    @Transactional
    public void onTransferApproved(String referenceNumber) {
        TransferSagaState saga = findActiveSaga(referenceNumber);

        if (saga.getStatus() != TransferSagaState.SagaStatus.STOCK_RESERVED) {
            log.warn("[SAGA] {} approval diabaikan, status={}", referenceNumber, saga.getStatus());
            return;
        }

        TransferRequest transfer = findTransfer(referenceNumber);
        transfer.setStatus(TransferRequest.TransferStatus.APPROVED);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepo.save(transfer);

        saga.advance(TransferSagaState.SagaStatus.TRANSFER_APPROVED);
        sagaRepo.save(saga);
        log.info("[SAGA] {} → TRANSFER_APPROVED", referenceNumber);

        // Publish ke Kafka untuk trigger delivery stok ke cabang tujuan
        kafkaProducer.sendTransferApproved(new TransferEvent(
                referenceNumber, saga.getProductId(),
                saga.getFromBranchId(), saga.getToBranchId(),
                saga.getQuantity(), "APPROVED"));
    }

    // ─── Step 3: Delivery stok ke cabang tujuan ──────────────────────────────

    @Transactional
    public void onDeliverStock(String referenceNumber) {
        TransferSagaState saga = findActiveSaga(referenceNumber);

        if (saga.getStatus() != TransferSagaState.SagaStatus.TRANSFER_APPROVED) {
            log.warn("[SAGA] {} delivery diabaikan, status={}", referenceNumber, saga.getStatus());
            return;
        }

        try {
            StockRequest inReq = buildStockRequest(
                    saga.getProductId(), saga.getToBranchId(), saga.getQuantity(),
                    StockMovement.MovementType.TRANSFER_IN, referenceNumber,
                    "Saga step 3: deliver stok ke cabang tujuan");
            stockService.adjustStock(inReq);

            TransferRequest transfer = findTransfer(referenceNumber);
            transfer.setStatus(TransferRequest.TransferStatus.COMPLETED);
            transfer.setUpdatedAt(LocalDateTime.now());
            transferRepo.save(transfer);

            saga.advance(TransferSagaState.SagaStatus.STOCK_DELIVERED);
            sagaRepo.save(saga);
            log.info("[SAGA] {} → STOCK_DELIVERED", referenceNumber);

            completeSaga(saga, referenceNumber);

        } catch (Exception e) {
            log.error("[SAGA] {} gagal di delivery: {}", referenceNumber, e.getMessage());
            saga.fail("Gagal deliver stok: " + e.getMessage());
            sagaRepo.save(saga);
            triggerCompensation(saga);
        }
    }

    // ─── Step 4: Finalisasi saga ──────────────────────────────────────────────

    @Transactional
    public void completeSaga(TransferSagaState saga, String referenceNumber) {
        saga.advance(TransferSagaState.SagaStatus.COMPLETED);
        sagaRepo.save(saga);
        log.info("[SAGA] {} → COMPLETED", referenceNumber);

        // Notif ke accounting via RabbitMQ
        TransferEvent doneEvent = new TransferEvent(
                referenceNumber, saga.getProductId(),
                saga.getFromBranchId(), saga.getToBranchId(),
                saga.getQuantity(), "COMPLETED");
        rabbitMQProducer.sendTransferDoneNotif(doneEvent);
    }

    // ─── Compensating Transactions ────────────────────────────────────────────

    /**
     * Dijalankan jika saga gagal setelah STOCK_RESERVED.
     * Rollback: kembalikan stok ke cabang asal.
     */
    @Transactional
    public void triggerCompensation(TransferSagaState saga) {
        String refNumber = saga.getReferenceNumber();
        log.warn("[SAGA] {} mulai kompensasi dari status={}", refNumber, saga.getStatus());

        saga.setStatus(TransferSagaState.SagaStatus.COMPENSATING);
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepo.save(saga);

        try {
            // Kembalikan stok ke cabang asal
            StockRequest rollbackReq = buildStockRequest(
                    saga.getProductId(), saga.getFromBranchId(), saga.getQuantity(),
                    StockMovement.MovementType.TRANSFER_IN,
                    refNumber + "-ROLLBACK",
                    "Saga kompensasi: rollback stok ke cabang asal");
            stockService.adjustStock(rollbackReq);

            // Update status transfer menjadi REJECTED
            transferRepo.findByReferenceNumber(refNumber).ifPresent(t -> {
                t.setStatus(TransferRequest.TransferStatus.REJECTED);
                t.setUpdatedAt(LocalDateTime.now());
                transferRepo.save(t);
            });

            saga.advance(TransferSagaState.SagaStatus.COMPENSATED);
            sagaRepo.save(saga);
            log.info("[SAGA] {} → COMPENSATED (rollback berhasil)", refNumber);

            // Publish failure event
            kafkaProducer.sendTransferRequested(new TransferEvent(
                    refNumber, saga.getProductId(),
                    saga.getFromBranchId(), saga.getToBranchId(),
                    saga.getQuantity(), "COMPENSATED"));

        } catch (Exception e) {
            log.error("[SAGA] {} kompensasi gagal: {}", refNumber, e.getMessage());
            saga.incrementRetry();
            saga.setFailureReason("Kompensasi gagal: " + e.getMessage());
            sagaRepo.save(saga);
            // Akan di-retry oleh scheduler
        }
    }

    // ─── Rejection (PUSAT menolak) ────────────────────────────────────────────

    @Transactional
    public void onTransferRejected(String referenceNumber) {
        TransferSagaState saga = findActiveSaga(referenceNumber);

        if (saga.getStatus() != TransferSagaState.SagaStatus.STOCK_RESERVED) {
            return;
        }

        log.info("[SAGA] {} ditolak PUSAT, mulai kompensasi", referenceNumber);
        saga.fail("Transfer ditolak oleh PUSAT");
        sagaRepo.save(saga);
        triggerCompensation(saga);
    }

    // ─── Retry scheduler untuk saga yang gagal kompensasi ────────────────────

    @Scheduled(fixedDelay = 60_000) // setiap 60 detik
    public void retryFailedCompensations() {
        List<TransferSagaState> stuck = sagaRepo.findByStatusAndRetryCountLessThan(
                TransferSagaState.SagaStatus.COMPENSATING, MAX_RETRY);

        for (TransferSagaState saga : stuck) {
            log.info("[SAGA] Retry kompensasi {} (attempt {})",
                    saga.getReferenceNumber(), saga.getRetryCount() + 1);
            triggerCompensation(saga);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TransferSagaState findActiveSaga(String referenceNumber) {
        return sagaRepo.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Saga tidak ditemukan untuk referensi: " + referenceNumber));
    }

    private TransferRequest findTransfer(String referenceNumber) {
        return transferRepo.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Transfer tidak ditemukan: " + referenceNumber));
    }

    private StockRequest buildStockRequest(Long productId, Long branchId, Integer quantity,
                                            StockMovement.MovementType type,
                                            String refNumber, String notes) {
        StockRequest req = new StockRequest();
        req.setProductId(productId);
        req.setBranchId(branchId);
        req.setQuantity(quantity);
        req.setMovementType(type);
        req.setReferenceNumber(refNumber);
        req.setNotes(notes);
        return req;
    }
}
