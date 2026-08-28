package com.viefood.base.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cau hinh trong application.yml duoi tien to viefood.messaging.* */
@ConfigurationProperties(prefix = "viefood.messaging")
public class ViefoodMessagingProperties {

    /** Topic exchange chung cho toan he thong. */
    private String exchange = "viefood.events";

    /** Ten service phat event, di vao EventMetadata.producer. */
    private String producer = "unknown";

    private final Outbox outbox = new Outbox();

    public static class Outbox {
        /** Moi vong quet lay toi da bao nhieu event. */
        private int batchSize = 100;

        /** Giu event da gui bao nhieu ngay truoc khi don. */
        private int retentionDays = 7;

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public Outbox getOutbox() {
        return outbox;
    }
}
