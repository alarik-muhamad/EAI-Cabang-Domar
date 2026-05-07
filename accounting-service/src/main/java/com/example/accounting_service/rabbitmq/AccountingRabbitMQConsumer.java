package com.example.accounting_service.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.example.accounting_service.event.TransferEvent;
import com.example.accounting_service.service.TransactionService;

@Service
public class AccountingRabbitMQConsumer {

    private final TransactionService transactionService;

    public AccountingRabbitMQConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = AccountingRabbitMQConfig.TRANSFER_DONE_QUEUE)
    public void handleTransferDone(TransferEvent event) {
        transactionService.completeTransferTransaction(event.getReferenceNumber());
    }
}