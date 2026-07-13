package com.asp.transactionservice.config;

/*
 * Copyright (c) 2025-2026 Ayshi Shannidhya Panda. All rights reserved.
 *
 * Project: Neptune Bank — IEEE Research
 * Purpose: Custom Micrometer Metrics Configuration
 */

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures custom Micrometer metrics for performance measurement
 * in the IEEE research experiments.
 *
 * <p>Adds common tags to all metrics for filtering in Prometheus/Grafana,
 * and enables the {@link io.micrometer.core.annotation.Timed} annotation
 * for declarative method-level timing.</p>
 *
 * @author Ayshi Shannidhya Panda
 */
@Configuration
public class MetricsConfig {

    /**
     * Adds common tags to all metrics emitted by this service.
     * These tags enable filtering by service name and application
     * in Prometheus queries and Grafana dashboards.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(List.of(
                        Tag.of("application", "neptune-bank"),
                        Tag.of("service", "transaction-service")
                ));
    }

    /**
     * Enables the {@code @Timed} annotation on Spring-managed beans.
     * This allows declarative timing of individual methods without
     * manual timer code.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
