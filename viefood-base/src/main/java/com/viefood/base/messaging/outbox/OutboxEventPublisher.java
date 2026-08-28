package com.viefood.base.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.viefood.base.error.ErrorCode;
import com.viefood.base.error.ServiceException;
import com.viefood.base.web.trace.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * API cho service goi khi muon phat event.
 *
 * Luu y: no KHONG dung toi RabbitMQ. No chi INSERT mot dong vao outbox_events.
 * Viec gui that do OutboxRelay lam sau, o transaction khac.
 */
public class OutboxEventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * propagation = MANDATORY la co y: neu goi ham nay ma KHONG co transaction
     * dang mo san, Spring nem loi ngay lap tuc.
     *
     * Vi sao khat khe vay: toan bo gia tri cua outbox nam o cho event duoc ghi
     * CUNG transaction voi thay doi nghiep vu. Goi no ngoai transaction thi
     * pattern mat tac dung ma khong ai nhan ra. Bat loi luc chay con hon de
     * du lieu lech ngoai production.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(
            String eventType,
            int schemaVersion,
            String aggregateType,
            UUID aggregateId,
            Object payload
    ) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new ServiceException(
                    ErrorCode.ERR_INTERNAL_ERROR,
                    "Khong serialize duoc payload cua event " + eventType);
        }

        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                eventType,
                schemaVersion,
                aggregateType,
                aggregateId,
                json,
                CorrelationIdFilter.currentCorrelationId(),
                Instant.now());

        repository.save(event);
        return event.getId();
    }

    /** Ban rut gon cho truong hop schema version 1. */
    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publish(String eventType, String aggregateType, UUID aggregateId, Object payload) {
        return publish(eventType, 1, aggregateType, aggregateId, payload);
    }
}
