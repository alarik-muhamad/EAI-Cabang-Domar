package com.example.inventory_service.rabbitmq;

import com.example.inventory_service.event.LowStockEvent;
import com.example.inventory_service.event.TransferEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTransferDoneNotif(TransferEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TRANSFER_DONE_QUEUE, event);
    }

    public void sendRestockNotif(LowStockEvent event) {
        String routingKey = "notif.restock." + event.getBranchId();
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.RESTOCK_EXCHANGE, routingKey, event);
    }
}