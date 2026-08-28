package com.viefood.base.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Mot event dang cho gui sang RabbitMQ.
 *
 * Diem mau chot: ban ghi nay duoc INSERT trong CUNG transaction voi thay doi
 * nghiep vu, vao CUNG database. Nho vay hai thao tac hoac cung thanh cong hoac
 * cung that bai - dieu ma "ghi DB roi goi RabbitMQ" khong bao gio dam bao duoc.
 */
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    /** Trung voi EventMetadata.eventId. Ben nhan dung lam khoa chong trung. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    /** Loai thuc the sinh ra event: "contribution", "dish"... Chi de debug. */
    @Column(name = "aggregate_type", nullable = false, length = 60)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    /** JSON cua payload. Luu TEXT vi khong bao gio can query ben trong. */
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    /** null = chua gui duoc. Job relay quet dung cot nay. */
    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    /** JPA bat buoc phai co constructor rong. */
    protected OutboxEvent() {
    }

    public OutboxEvent(
            UUID id,
            String eventType,
            int schemaVersion,
            String aggregateType,
            UUID aggregateId,
            String payload,
            String correlationId,
            Instant occurredAt
    ) {
        this.id = id;
        this.eventType = eventType;
        this.schemaVersion = schemaVersion;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.correlationId = correlationId;
        this.occurredAt = occurredAt;
        this.attempts = 0;
    }

    public void markPublished(Instant at) {
        this.publishedAt = at;
        this.lastError = null;
    }

    public void markFailed(String error) {
        this.attempts++;
        // Cat bot cho vua cot, tranh loi khi stack trace qua dai
        this.lastError = error == null || error.length() <= 500 ? error : error.substring(0, 500);
    }

    public UUID getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }
}
