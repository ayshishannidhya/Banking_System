package com.asp.auditservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Audit Service for Neptune Bank.
 *
 * <p>Consumes transaction events from Kafka and persists an immutable
 * audit trail for regulatory compliance and forensic analysis.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@SpringBootApplication
public class AuditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
