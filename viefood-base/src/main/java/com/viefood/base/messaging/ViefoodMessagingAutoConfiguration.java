package com.viefood.base.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viefood.base.messaging.inbox.IdempotentEventProcessor;
import com.viefood.base.messaging.inbox.ProcessedEventRepository;
import com.viefood.base.messaging.outbox.OutboxEventPublisher;
import com.viefood.base.messaging.outbox.OutboxEventRepository;
import com.viefood.base.messaging.outbox.OutboxRelay;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Kich hoat outbox/inbox cho service nao co ca JPA lan RabbitMQ tren classpath.
 */
/*
 * after = ... la BAT BUOC, khong phai cho dep.
 *
 * Bean repository cua Spring Data duoc dang ky RAT MUON. Neu auto-config naySS
 * chay truoc JpaRepositoriesAutoConfiguration thi luc do OutboxEventRepository
 * chua ton tai, va @ConditionalOnBean(OutboxEventRepository.class) se danh gia
 * la FALSE -> bean bi bo qua im lang, service khoi dong binh thuong nhung
 * khong co outbox.
 *
 * Vi ly do do o duoi cung KHONG dung @ConditionalOnBean cho repository:
 * @ConditionalOnClass da du de biet co JPA hay khong, va repository chac chan
 * ton tai nho MessagingPackageRegistrar.
 */
@AutoConfiguration(after = {
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class,
        RabbitAutoConfiguration.class
})
@ConditionalOnClass({EntityManager.class, RabbitTemplate.class})
@ConditionalOnProperty(prefix = "viefood.messaging", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ViefoodMessagingProperties.class)
//@EnableJpaRepositories(basePackages = {
//        "com.viefood.base.messaging.outbox",
//        "com.viefood.base.messaging.inbox"
//})
//@EntityScan(basePackages = {
//        "com.viefood.base.messaging.outbox",
//        "com.viefood.base.messaging.inbox"
//})
@EnableScheduling
public class ViefoodMessagingAutoConfiguration {

    @Bean
    @ConditionalOnBean(OutboxEventRepository.class)
    @ConditionalOnMissingBean
    public OutboxEventPublisher outboxEventPublisher(
            OutboxEventRepository repository,
            ObjectMapper objectMapper
    ) {
        return new OutboxEventPublisher(repository, objectMapper);
    }

    @Bean
    @ConditionalOnBean(OutboxEventRepository.class)
    @ConditionalOnMissingBean
    public OutboxRelay outboxRelay(
            OutboxEventRepository repository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            ViefoodMessagingProperties properties
    ) {
        return new OutboxRelay(repository, rabbitTemplate, objectMapper, properties);
    }

    @Bean
    @ConditionalOnBean(ProcessedEventRepository.class)
    @ConditionalOnMissingBean
    public IdempotentEventProcessor idempotentEventProcessor(ProcessedEventRepository repository) {
        return new IdempotentEventProcessor(repository);
    }
}
