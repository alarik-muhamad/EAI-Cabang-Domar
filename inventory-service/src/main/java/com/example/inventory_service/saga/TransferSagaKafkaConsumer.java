package com.example.inventory_service.saga;

import com.example.inventory_service.event.TransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer khusus untuk event saga transfer.
 * Memisahkan routing event dari logika bisnis.
 */
@Service
public class TransferSagaKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferSagaKafkaConsumer.class);

    private final TransferSagaOrchestrator orchestrator;

    public TransferSagaKafkaConsumer(TransferSagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Event ini diproduksi oleh inventory service sendiri setelah PUSAT approve
     * via REST API. Orchestrator kemudian melakukan delivery stok ke cabang tujuan.
     */
    @KafkaListener(topics = "stock.transfer.approved", groupId = "inventory-saga-group")
    public void onTransferApproved(TransferEvent event) {
        log.info("[SAGA CONSUMER] Received transfer.approved: {}", event.getReferenceNumber());
        try {
            orchestrator.onDeliverStock(event.getReferenceNumber());
        } catch (Exception e) {
            log.error("[SAGA CONSUMER] Gagal proses delivery untuk {}: {}",
                    event.getReferenceNumber(), e.getMessage());
        }
    }

    /**
     * Event kompensasi: saga sudah di-rollback, bisa dipakai untuk notifikasi
     * atau audit trail.
     */
    @KafkaListener(topics = "stock.transfer.compensated", groupId = "inventory-saga-group")
    public void onTransferCompensated(TransferEvent event) {
        log.info("[SAGA CONSUMER] Transfer {} telah dikompensasi (rollback selesai)",
                event.getReferenceNumber());
    }
}
