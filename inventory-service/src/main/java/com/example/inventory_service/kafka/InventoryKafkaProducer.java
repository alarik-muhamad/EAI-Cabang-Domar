package com.example.inventory_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.inventory_service.event.LowStockEvent;
import com.example.inventory_service.event.StockUpdatedEvent;
import com.example.inventory_service.event.TransferEvent;

@Service
public class InventoryKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStockUpdated(StockUpdatedEvent event) {
        kafkaTemplate.send("stock.updated", String.valueOf(event.getBranchId()), event);
    }

    public void sendTransferRequested(TransferEvent event) {
        kafkaTemplate.send("stock.transfer.requested", event.getReferenceNumber(), event);
    }

    public void sendTransferApproved(TransferEvent event) {
        kafkaTemplate.send("stock.transfer.approved", event.getReferenceNumber(), event);
    }

    public void sendLowStockAlert(LowStockEvent event) {
        kafkaTemplate.send("low.stock.alert", String.valueOf(event.getBranchId()), event);
    }

    public void sendTransferCompensated(TransferEvent event) {
        kafkaTemplate.send("stock.transfer.compensated", event.getReferenceNumber(), event);
    }
}
