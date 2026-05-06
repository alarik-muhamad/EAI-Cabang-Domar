package com.example.inventory_service.rabbitmq;

import com.example.inventory_service.event.TransferEvent;
import com.example.inventory_service.service.TransferService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private final TransferService transferService;

    public RabbitMQConsumer(TransferService transferService) {
        this.transferService = transferService;
    }

    @RabbitListener(queues = RabbitMQConfig.TRANSFER_APPROVE_QUEUE)
    public void handleTransferApproval(TransferEvent event) {
        if ("APPROVED".equals(event.getStatus())) {
            transferService.executeApprovedTransfer(event.getReferenceNumber());
        } else if ("REJECTED".equals(event.getStatus())) {
            transferService.rejectTransfer(event.getReferenceNumber());
        }
    }
}