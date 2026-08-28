package com.viefood.base.messaging.outbox;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.UUID;


public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Lay lo event chua gui, khoa lai de instance khac khong lay trung.
     *
     * PESSIMISTIC_WRITE  -> sinh ra "SELECT ... FOR UPDATE"
     * lock.timeout = -2  -> ma SKIP_LOCKED cua Hibernate, them "SKIP LOCKED"
     *
     * Ket qua: "SELECT ... FOR UPDATE SKIP LOCKED". Neu chay 2 instance service,
     * instance thu hai se BO QUA cac dong dang bi khoa thay vi ngoi cho, nen
     * khong bao gio co chuyen hai ben cung gui mot event.
     *
     * Thieu SKIP LOCKED thi moi message se bi gui 2 lan khi scale ngang.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select e from OutboxEvent e where e.publishedAt is null order by e.occurredAt asc")
    List<OutboxEvent> findUnpublished(Pageable pageable);

    /** Don rac: xoa event da gui truoc moc thoi gian nao do. */
    long deleteByPublishedAtNotNullAndPublishedAtBefore(java.time.Instant before);
}
