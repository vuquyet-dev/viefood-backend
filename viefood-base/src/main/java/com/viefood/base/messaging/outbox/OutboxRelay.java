package com.viefood.base.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.viefood.base.context.TraceHeaders;
import com.viefood.base.event.EventMetadata;
import com.viefood.base.messaging.ViefoodMessagingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Job chay nen: quet outbox_events, gui sang RabbitMQ, danh dau da gui.
 *
 * Day la nua sau cua outbox pattern. No tach roi khoi nghiep vu: neu RabbitMQ
 * chet, job that bai nhung du lieu nghiep vu van dung, vong quet sau gui lai.
 */
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ViefoodMessagingProperties properties;

    public OutboxRelay(
            OutboxEventRepository repository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            ViefoodMessagingProperties properties
    ) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${viefood.messaging.outbox.poll-interval-ms:1000}")
    @Transactional
    public void relay() {
        List<OutboxEvent> batch = repository.findUnpublished(
                PageRequest.of(0, properties.getOutbox().getBatchSize()));

        if (batch.isEmpty()) {
            return;
        }

        for (OutboxEvent event : batch) {
            try {
                send(event);
                event.markPublished(Instant.now());
            } catch (Exception ex) {
                // Khong nem ra ngoai: mot event hong khong duoc chan ca lo.
                // published_at van null nen vong sau se thu lai.
                event.markFailed(ex.getMessage());
                log.error("Gui outbox event {} ({}) that bai, lan thu {}",
                        event.getId(), event.getEventType(), event.getAttempts(), ex);
            }
        }
    }

    private void send(OutboxEvent event) throws Exception {
        EventMetadata metadata = new EventMetadata(
                event.getId(),
                event.getEventType(),
                event.getSchemaVersion(),
                properties.getProducer(),
                event.getOccurredAt(),
                event.getCorrelationId());

        // Dung payload da luu dang JSON, khong serialize lai lan nua
        // (neu khong se bi boc chuoi trong chuoi).
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.set("metadata", objectMapper.valueToTree(metadata));
        envelope.set("payload", objectMapper.readTree(event.getPayload()));

        String json = objectMapper.writeValueAsString(envelope);

        // Routing key chinh la eventType, vi du "contribution.approved.v1".
        rabbitTemplate.convertAndSend(properties.getExchange(), event.getEventType(), json, message -> {
            message.getMessageProperties().setContentType("application/json");
            // messageId = eventId: RabbitMQ va consumer deu dung duoc de chong trung
            message.getMessageProperties().setMessageId(event.getId().toString());
            if (event.getCorrelationId() != null) {
                message.getMessageProperties()
                        .setHeader(TraceHeaders.CORRELATION_ID, event.getCorrelationId());
            }
            return message;
        });
    }

    /**
     * Don rac dinh ky. Khong co buoc nay thi outbox_events phinh vo han -
     * moi event la mot dong ton tai vinh vien.
     */
    @Scheduled(cron = "${viefood.messaging.outbox.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanup() {
        Instant threshold = Instant.now()
                .minus(java.time.Duration.ofDays(properties.getOutbox().getRetentionDays()));
        long deleted = repository.deleteByPublishedAtNotNullAndPublishedAtBefore(threshold);
        if (deleted > 0) {
            log.info("Da don {} outbox event da gui truoc {}", deleted, threshold);
        }
    }
}
