package com.viefood.base.messaging;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * BAY QUAN TRONG NHAT CUA PHAN MESSAGING - va ly do class nay ton tai rieng.
 *
 * VAN DE
 * OutboxEvent va ProcessedEvent nam o package com.viefood.base.messaging,
 * KHONG nam duoi package cua service (vd com.viefood.identity). Mac dinh Spring
 * Boot chi quet entity/repository trong package cua class @SpringBootApplication
 * tro xuong, nen se khong bao gio thay chung.
 *
 * CACH SAI THUONG THAY
 * Dat @EntityScan / @EnableJpaRepositories trong thu vien. Lam vay khien
 * JpaRepositoriesAutoConfiguration cua Spring Boot tu tat (no co
 * @ConditionalOnMissingBean), va tu do CHI package cua thu vien duoc quet -
 * repository cua chinh service bien mat. Loi rat kho doan ra.
 *
 * CACH DUNG
 * Dang ky them package vao AutoConfigurationPackages. Spring Boot lay danh sach
 * do de quet entity va repository, nen ca package cua base lan cua service deu
 * duoc quet, khong ai de len ai.
 *
 * VI SAO PHAI TACH RA CLASS RIENG
 * Viec dang ky package phai xay ra TRUOC khi JpaRepositoriesAutoConfiguration
 * doc danh sach package (nen o day dung before = ...).
 * Nhung cac @Bean dung repository lai phai duoc tao SAU khi repository ton tai
 * (nen ViefoodMessagingAutoConfiguration dung after = ...).
 * Hai yeu cau nguoc chieu nhau nen khong the nam chung mot class.
 */
@AutoConfiguration(before = {
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
})
@ConditionalOnClass(EntityManager.class)
@ConditionalOnProperty(prefix = "viefood.messaging", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@Import(ViefoodMessagingPackagesAutoConfiguration.MessagingPackageRegistrar.class)
public class ViefoodMessagingPackagesAutoConfiguration {

    static class MessagingPackageRegistrar implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            AutoConfigurationPackages.register(registry, "com.viefood.base.messaging");
        }
    }
}
