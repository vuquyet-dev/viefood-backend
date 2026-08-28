package com.viefood.base.event;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
        UUID eventId,
        String eventType,
        int schemaVersion,
        String producer,
        Instant occurredAt,
        String correlationId
) {
}
