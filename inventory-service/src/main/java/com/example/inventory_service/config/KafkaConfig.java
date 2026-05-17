package com.example.inventory_service.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean public NewTopic stockUpdated() {
        return TopicBuilder.name("stock.updated").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic transferRequested() {
        return TopicBuilder.name("stock.transfer.requested").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic transferApproved() {
        return TopicBuilder.name("stock.transfer.approved").partitions(3).replicas(1).build();
    }
    @Bean public NewTopic transferCompensated() {
        // Topic baru untuk event kompensasi saga
        return TopicBuilder.name("stock.transfer.compensated").partitions(1).replicas(1).build();
    }
    @Bean public NewTopic lowStockAlert() {
        return TopicBuilder.name("low.stock.alert").partitions(1).replicas(1).build();
    }


    /** Consumer factory untuk group "inventory-group" (non-saga) */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        return buildConsumerFactory(
                "inventory-group",
                "com.example.inventory_service.event.TransferEvent");
    }

    /** Consumer factory untuk group "inventory-saga-group" (saga events) */
    @Bean("sagaConsumerFactory")
    public ConsumerFactory<String, Object> sagaConsumerFactory() {
        return buildConsumerFactory(
                "inventory-saga-group",
                "com.example.inventory_service.event.TransferEvent");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        return buildListenerFactory(consumerFactory());
    }

    @Bean("sagaKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> sagaKafkaListenerContainerFactory() {
        return buildListenerFactory(sagaConsumerFactory());
    }

    private ConsumerFactory<String, Object> buildConsumerFactory(String groupId, String defaultType) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        config.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        config.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, defaultType);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object> buildListenerFactory(
            ConsumerFactory<String, Object> factory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> cf =
                new ConcurrentKafkaListenerContainerFactory<>();
        cf.setConsumerFactory(factory);
        return cf;
    }
}
