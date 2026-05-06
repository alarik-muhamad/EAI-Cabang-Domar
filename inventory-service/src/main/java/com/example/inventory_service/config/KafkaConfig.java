package com.example.inventory_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean public NewTopic stockUpdated() {
        return TopicBuilder.name("stock.updated").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic transferRequested() {
        return TopicBuilder.name("stock.transfer.requested").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic transferApproved() {
        return TopicBuilder.name("stock.transfer.approved").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic lowStockAlert() {
        return TopicBuilder.name("low.stock.alert").partitions(1).replicas(1).build();
    }
}