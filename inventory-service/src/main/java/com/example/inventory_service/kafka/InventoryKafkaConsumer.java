package com.example.inventory_service.kafka;

import com.example.inventory_service.event.TransferEvent;
import com.example.inventory_service.service.TransferService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryKafkaConsumer {

    private final TransferService transferService;

    public InventoryKafkaConsumer(TransferService transferService) {
        this.transferService = transferService;
    }

    @KafkaListener(topics = "stock.transfer.approved", groupId = "inventory-group")
    public void handleTransferApproved(TransferEvent event) {
        transferService.executeApprovedTransfer(event.getReferenceNumber());
    }
}