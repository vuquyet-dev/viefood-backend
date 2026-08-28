package com.viefood.base.event;

public record EventEnvelope<T>(
        EventMetadata metadata,
        T payload
) {
}
