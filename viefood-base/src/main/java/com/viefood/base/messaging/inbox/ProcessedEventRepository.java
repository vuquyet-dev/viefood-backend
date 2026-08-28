package com.viefood.base.messaging.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    boolean existsByEventIdAndConsumer(UUID eventId, String consumer);

    long deleteByProcessedAtBefore(Instant before);
}
