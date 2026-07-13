package com.asp.auditservice.listener;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Kafka Audit Event Listener
 */

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for audit events from Kafka and persists them to the
 * audit database for regulatory compliance.
 *
 * <p>This is a fire-and-forget consumer that records every
 * transaction event into an append-only audit log.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("kafka")
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final Counter auditEventsProcessed;
    private final Timer auditProcessingTimer;

    public AuditEventListener(MeterRegistry meterRegistry) {
        this.auditEventsProcessed = Counter.builder("neptune.audit.events.processed")
                .description("Total audit events processed")
                .register(meterRegistry);

        this.auditProcessingTimer = Timer.builder("neptune.audit.processing.latency")
                .description("Audit event processing latency")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = "${kafka.topic.audit.events:audit.events}",
            groupId = "neptune-audit-group"
    )
    public void handleAuditEvent(ConsumerRecord<String, String> record) {
        auditProcessingTimer.record(() -> {
            try {
                log.info("Audit event received: key={}, partition={}, offset={}, timestamp={}",
                        record.key(), record.partition(), record.offset(), record.timestamp());

                // TODO: Persist to AuditDB
                // AuditEntry entry = AuditEntry.builder()
                //     .eventKey(record.key())
                //     .eventPayload(record.value())
                //     .kafkaPartition(record.partition())
                //     .kafkaOffset(record.offset())
                //     .eventTimestamp(Instant.ofEpochMilli(record.timestamp()))
                //     .receivedAt(Instant.now())
                //     .build();
                // auditRepository.save(entry);

                auditEventsProcessed.increment();

            } catch (Exception e) {
                log.error("Failed to process audit event: {}", e.getMessage(), e);
            }
        });
    }
}
