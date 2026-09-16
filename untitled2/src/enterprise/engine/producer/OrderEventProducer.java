package com.enterprise.engine.producer;

import com.enterprise.engine.domain.event.OrderCreatedEvent;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Producer Service to asynchronously publish Order events to Apache Kafka.
 * Engineered to leverage Java 21 Virtual Threads for non-blocking I/O execution.
 */
@Service
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.order-created:orders.v1}")
    private String topicName;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, OrderCreatedEvent>> publishOrderEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent with ID: {} to topic: {}", event.eventId(), topicName);

        // Publish message asynchronously using customerId as the partition key to guarantee order per customer
        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(topicName, event.customerId(), event);

        // Register non-blocking callbacks for telemetry, monitoring, and error handling
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent event [{}] to partition [{}] with offset [{}]",
                        event.eventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event [{}] due to error: {}", event.eventId(), ex.getMessage(), ex);
            }
        });

        return future;
    }
}