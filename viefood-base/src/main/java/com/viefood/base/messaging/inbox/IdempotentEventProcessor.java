package com.viefood.base.messaging.inbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Boc logic "chi xu ly mot lan" cho consumer.
 *
 * Cach dung trong service:
 *
 *   @RabbitListener(queues = "catalog.contribution-approved")
 *   public void onApproved(EventEnvelope&lt;ContributionApprovedPayload&gt; event) {
 *       processor.processOnce(event.metadata().eventId(), "catalog.dish-creator",
 *           () -> catalogService.createDishFrom(event.payload()));
 *   }
 */
public class IdempotentEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(IdempotentEventProcessor.class);

    private final ProcessedEventRepository repository;

    public IdempotentEventProcessor(ProcessedEventRepository repository) {
        this.repository = repository;
    }

    /**
     * Chay action neu event nay chua tung duoc consumer nay xu ly.
     *
     * @return true neu vua xu ly, false neu da xu ly tu truoc va bi bo qua
     *
     * REQUIRED: action va ban ghi ProcessedEvent nam CUNG mot transaction.
     * Neu action loi giua chung thi ProcessedEvent cung bi rollback, nen lan
     * redeliver sau van chay lai duoc. Neu tach transaction thi se co truong
     * hop danh dau "da xu ly" trong khi cong viec that bai - mat du lieu.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean processOnce(UUID eventId, String consumer, Runnable action) {
        if (repository.existsByEventIdAndConsumer(eventId, consumer)) {
            log.debug("Bo qua event {} - consumer {} da xu ly roi", eventId, consumer);
            return false;
        }

        action.run();

        try {
            repository.saveAndFlush(new ProcessedEvent(eventId, consumer, Instant.now()));
        } catch (DataIntegrityViolationException ex) {
            // Hai message giong nhau ve cung luc: ca hai deu qua duoc existsBy...
            // Unique constraint o database la choi chan cuoi cung. Ben thua
            // cuoc phai rollback de khong xu ly hai lan.
            log.warn("Event {} bi xu ly dong thoi boi consumer {}, rollback ban thua",
                    eventId, consumer);
            throw ex;
        }
        return true;
    }
}
