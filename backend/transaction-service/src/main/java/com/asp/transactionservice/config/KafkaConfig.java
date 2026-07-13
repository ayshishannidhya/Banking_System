package com.asp.transactionservice.config;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Kafka Producer/Consumer Configuration for Transaction Service
 */

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the Transaction Service.
 *
 * <p>This configuration is only active when the {@code kafka} Spring Profile
 * is enabled. It configures:</p>
 * <ul>
 *   <li>Kafka producer factory with configurable ack level and batch size</li>
 *   <li>Kafka consumer factory with configurable group and concurrency</li>
 *   <li>{@link ReplyingKafkaTemplate} for request-reply pattern</li>
 * </ul>
 *
 * <p>Producer acknowledgment level is an independent variable in the
 * IEEE experiment (levels: 0, 1, all).</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Configuration
@Profile("kafka")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.producer.acks:1}")
    private String producerAcks;

    @Value("${kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${kafka.producer.linger-ms:5}")
    private int lingerMs;

    @Value("${kafka.consumer.group-id:neptune-transaction-group}")
    private String consumerGroupId;

    @Value("${kafka.consumer.concurrency:3}")
    private int consumerConcurrency;

    @Value("${kafka.topic.account.validation.reply:account.validation.reply}")
    private String validationReplyTopic;

    @Value("${kafka.topic.account.debit.reply:account.debit.reply}")
    private String debitReplyTopic;

    @Value("${kafka.topic.account.credit.reply:account.credit.reply}")
    private String creditReplyTopic;

    // ===========================
    // PRODUCER CONFIGURATION
    // ===========================

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ===========================
    // CONSUMER CONFIGURATION
    // ===========================

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerConcurrency);
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    // ===========================
    // REPLYING KAFKA TEMPLATE
    // ===========================

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentMessageListenerContainer<String, String> repliesContainer) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        // Listen on all reply topics
        ConcurrentMessageListenerContainer<String, String> container =
                containerFactory.createContainer(
                        validationReplyTopic,
                        debitReplyTopic,
                        creditReplyTopic);
        container.getContainerProperties().setGroupId(consumerGroupId + "-replies");
        container.setAutoStartup(true);
        return container;
    }
}
