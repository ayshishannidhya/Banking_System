package com.asp.transactionservice.strategy;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Kafka Communication Strategy (Event-Driven)
 */

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Kafka-based event-driven implementation of the communication strategy.
 *
 * <p>This implementation uses Spring Kafka's {@link ReplyingKafkaTemplate}
 * for request-reply semantics over Kafka topics. Each operation publishes
 * a request to a dedicated topic and awaits a correlated reply on a
 * reply topic.</p>
 *
 * <p>For fire-and-forget operations (audit, notification), the standard
 * {@code KafkaTemplate} is used instead.</p>
 *
 * <p>This is the event-driven paradigm under evaluation in the IEEE paper.</p>
 *
 * <p>Activated by: {@code --spring.profiles.active=kafka}</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("kafka")
public class KafkaCommunicationStrategy implements CommunicationStrategy {

    private static final Logger log = LoggerFactory.getLogger(KafkaCommunicationStrategy.class);

    private final ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;

    @Value("${kafka.topic.account.validation:account.validation}")
    private String validationTopic;

    @Value("${kafka.topic.account.validation.reply:account.validation.reply}")
    private String validationReplyTopic;

    @Value("${kafka.topic.account.debit:account.debit}")
    private String debitTopic;

    @Value("${kafka.topic.account.debit.reply:account.debit.reply}")
    private String debitReplyTopic;

    @Value("${kafka.topic.account.credit:account.credit}")
    private String creditTopic;

    @Value("${kafka.topic.account.credit.reply:account.credit.reply}")
    private String creditReplyTopic;

    @Value("${kafka.reply.timeout.ms:5000}")
    private long replyTimeoutMs;

    private final Timer validationTimer;
    private final Timer debitTimer;
    private final Timer creditTimer;
    private final Counter kafkaErrorCounter;

    public KafkaCommunicationStrategy(
            ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate,
            MeterRegistry meterRegistry) {

        this.replyingKafkaTemplate = replyingKafkaTemplate;

        this.validationTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "Kafka")
                .tag("operation", "validation")
                .description("Account validation latency via Kafka")
                .register(meterRegistry);

        this.debitTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "Kafka")
                .tag("operation", "debit")
                .description("Account debit latency via Kafka")
                .register(meterRegistry);

        this.creditTimer = Timer.builder("neptune.communication.latency")
                .tag("strategy", "Kafka")
                .tag("operation", "credit")
                .description("Account credit latency via Kafka")
                .register(meterRegistry);

        this.kafkaErrorCounter = Counter.builder("neptune.communication.errors")
                .tag("strategy", "Kafka")
                .description("Kafka communication error count")
                .register(meterRegistry);

        log.info("Kafka Communication Strategy activated — event-driven mode");
    }

    @Override
    public boolean validateAccount(String accountNumber) {
        return validationTimer.record(() ->
                sendAndReceive(validationTopic, validationReplyTopic, accountNumber, accountNumber));
    }

    @Override
    public boolean debitAccount(String accountNumber, BigDecimal amount) {
        String payload = accountNumber + ":" + amount.toPlainString();
        return debitTimer.record(() ->
                sendAndReceive(debitTopic, debitReplyTopic, accountNumber, payload));
    }

    @Override
    public boolean creditAccount(String accountNumber, BigDecimal amount) {
        String payload = accountNumber + ":" + amount.toPlainString();
        return creditTimer.record(() ->
                sendAndReceive(creditTopic, creditReplyTopic, accountNumber, payload));
    }

    /**
     * Sends a request to a Kafka topic and waits for a correlated reply.
     *
     * <p>Uses Kafka headers for correlation. The reply topic is specified
     * via {@link KafkaHeaders#REPLY_TOPIC}. The correlation ID is automatically
     * managed by {@link ReplyingKafkaTemplate}.</p>
     *
     * @param requestTopic the topic to send the request to
     * @param replyTopic   the topic to listen for the reply on
     * @param key          the Kafka message key (used for partition routing)
     * @param payload      the request payload
     * @return {@code true} if the reply indicates success
     */
    private boolean sendAndReceive(String requestTopic, String replyTopic, String key, String payload) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, key, payload);
            record.headers().add(new RecordHeader(
                    KafkaHeaders.REPLY_TOPIC,
                    replyTopic.getBytes(StandardCharsets.UTF_8)));

            RequestReplyFuture<String, String, String> future =
                    replyingKafkaTemplate.sendAndReceive(record, Duration.ofMillis(replyTimeoutMs));

            ConsumerRecord<String, String> reply = future.get(replyTimeoutMs, TimeUnit.MILLISECONDS);
            return "true".equalsIgnoreCase(reply.value());

        } catch (Exception e) {
            kafkaErrorCounter.increment();
            log.error("Kafka request-reply failed on topic {}: {}", requestTopic, e.getMessage());
            return false;
        }
    }
}
