package com.enterprise.engine.consumer;

import com.enterprise.engine.domain.event.OrderCreatedEvent;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer component executing within a dedicated Consumer Group.
 * Processes incoming JSON event payloads and manages offset commits safely.
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(
            topics = "${app.kafka.topics.order-created:orders.v1}",
            groupId = "${spring.kafka.consumer.group-id:order-processing-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void processOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            Acknowledgment ack) {

        log.info("Received event ID [{}] for Order ID [{}]", event.eventId(), event.orderId());

        try {
            // Execute business logic (e.g., store state, update inventory, trigger downstream workflows)
            executeIdempotentProcessing(event);

            // Manually commit Kafka offset to acknowledge successful message processing
            ack.acknowledge();
            log.info("Successfully processed and committed offset for Event ID [{}]", event.eventId());

        } catch (Exception ex) {
            log.error("Failed processing Event ID [{}] - delegating to container retry/DLQ", event.eventId(), ex);
            // Exception triggers configured Spring Kafka error handler
            throw ex;
        }
    }

    private void executeIdempotentProcessing(OrderCreatedEvent event) {
        // Business state execution logic
        log.info("Processing business state change for Customer [{}] with Amount [{}]",
                event.customerId(), event.amount());
    }
}