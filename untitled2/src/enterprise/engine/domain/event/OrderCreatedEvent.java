package com.enterprise.engine.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable Domain Event representing an Order Creation state change.
 * Utilizes Java 21 Record features for zero boilerplate and total thread-safety.
 */
public record OrderCreatedEvent(
        @JsonProperty("eventId")
        UUID eventId,

        @JsonProperty("orderId")
        String orderId,

        @JsonProperty("customerId")
        String customerId,

        @JsonProperty("amount")
        Double amount,

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("timestamp")
        Instant timestamp
) {
    // Factory method for creating atomic events with default metadata
    public static OrderCreatedEvent create(String orderId, String customerId, Double amount) {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                orderId,
                customerId,
                amount,
                "ORDER_CREATED",
                Instant.now()
        );
    }
}