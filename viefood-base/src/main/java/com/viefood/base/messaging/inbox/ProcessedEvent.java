package com.viefood.base.messaging.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * Dau vet mot event DA duoc xu ly. Nua sau cua co che chong trung.
 *
 * Vi sao can: outbox chi dam bao "it nhat mot lan" (at-least-once). Neu job
 * relay gui xong nhung chet truoc khi kip UPDATE published_at, vong sau se gui
 * lai. Ban than RabbitMQ cung redeliver khi consumer nack hoac timeout.
 *
 * Ket hop outbox (khong mat) + inbox (khong trung) = xu ly dung mot lan.
 */
@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_processed_events_event_consumer",
                columnNames = {"event_id", "consumer"})
)
public class ProcessedEvent {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Chinh la EventMetadata.eventId cua ben gui. */
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    /**
     * Ten consumer da xu ly. Can cot nay vi mot service co the co NHIEU
     * consumer cung nghe mot event; neu chi khoa theo event_id thi consumer
     * thu hai se bi chan oan.
     */
    @Column(name = "consumer", nullable = false, length = 120)
    private String consumer;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID eventId, String consumer, Instant processedAt) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.consumer = consumer;
        this.processedAt = processedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getConsumer() {
        return consumer;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
