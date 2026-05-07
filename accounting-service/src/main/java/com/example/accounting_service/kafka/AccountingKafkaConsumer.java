package com.example.accounting_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.accounting_service.event.StockUpdatedEvent;
import com.example.accounting_service.event.TransferEvent;
import com.example.accounting_service.service.TransactionService;

@Service
public class AccountingKafkaConsumer {

    private final TransactionService transactionService;

    public AccountingKafkaConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "stock.updated", groupId = "accounting-group")
    public void handleStockUpdated(StockUpdatedEvent event) {
        transactionService.recordFromStockEvent(event);
    }

    @KafkaListener(topics = "stock.transfer.approved", groupId = "accounting-group")
    public void handleTransferApproved(TransferEvent event) {
        transactionService.recordFromTransferEvent(event);
    }
}