package com.asp.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Notification Service for Neptune Bank.
 *
 * <p>Consumes transaction events from Kafka/RabbitMQ and dispatches
 * email/SMS notifications to customers.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
