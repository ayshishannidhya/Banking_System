package com.asp.notificationservice.listener;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Kafka Notification Event Listener
 */

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens for notification events from Kafka and dispatches
 * email/SMS notifications.
 *
 * <p>This is a fire-and-forget consumer — no reply is expected.
 * It demonstrates the event-driven notification pattern used
 * in the IEEE paper's architecture.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Component
@Profile("kafka")
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final Counter notificationsProcessed;
    private final Counter notificationsFailed;

    public NotificationEventListener(MeterRegistry meterRegistry) {
        this.notificationsProcessed = Counter.builder("neptune.notifications.processed")
                .description("Total notifications processed")
                .register(meterRegistry);

        this.notificationsFailed = Counter.builder("neptune.notifications.failed")
                .description("Total notification failures")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = "${kafka.topic.notification.events:notification.events}",
            groupId = "neptune-notification-group"
    )
    public void handleNotificationEvent(ConsumerRecord<String, String> record) {
        try {
            String eventPayload = record.value();
            log.info("Processing notification event: key={}, partition={}, offset={}",
                    record.key(), record.partition(), record.offset());

            // TODO: Implement actual email/SMS dispatch
            // For benchmarking, this simulates processing time
            Thread.sleep(5); // Simulate I/O (email send)

            notificationsProcessed.increment();
            log.debug("Notification dispatched successfully for event: {}", record.key());

        } catch (Exception e) {
            notificationsFailed.increment();
            log.error("Failed to process notification event: {}", e.getMessage());
        }
    }
}
