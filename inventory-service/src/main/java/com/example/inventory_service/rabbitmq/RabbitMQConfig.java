package com.example.inventory_service.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSFER_APPROVE_QUEUE = "cmd.transfer.approve";
    public static final String TRANSFER_DONE_QUEUE    = "notif.transfer.done";
    public static final String RESTOCK_EXCHANGE       = "notif.restock";

    @Bean
    public Queue transferApproveQueue() {
        return new Queue(TRANSFER_APPROVE_QUEUE, true);
    }

    @Bean
    public Queue transferDoneQueue() {
        return new Queue(TRANSFER_DONE_QUEUE, true);
    }

    @Bean
    public TopicExchange restockExchange() {
        return new TopicExchange(RESTOCK_EXCHANGE);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}