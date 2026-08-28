package com.viefood.base.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viefood.base.logger.ApiLoggingFilter;
import com.viefood.base.web.error.GlobalExceptionHandler;
import com.viefood.base.web.trace.CorrelationIdFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Duoc kich hoat qua file:
 *   META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * Service chi can khai dependency viefood-base-web la co, khong can @Import
 * hay @ComponentScan gi them.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ViefoodWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler viefoodGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(name = "viefoodCorrelationIdFilter")
    public FilterRegistrationBean<Filter> viefoodCorrelationIdFilter(
            org.springframework.beans.factory.ObjectProvider<BuildProperties> buildProperties
    ) {
        // BuildProperties chi ton tai khi service bat goal build-info cua
        // spring-boot-maven-plugin. Khong co thi bo qua header version.
        String version = buildProperties.getIfAvailable() != null
                ? buildProperties.getIfAvailable().getVersion()
                : null;

        FilterRegistrationBean<Filter> registration =
                new FilterRegistrationBean<>(new CorrelationIdFilter(version));
        registration.addUrlPatterns("/*");
        // Chay som nhat co the: MDC phai co san truoc moi filter/log khac.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "viefoodApiLoggingFilter")
    public FilterRegistrationBean<ApiLoggingFilter> viefoodApiLoggingFilter(
            ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<ApiLoggingFilter> registration =
                new FilterRegistrationBean<>(new ApiLoggingFilter(objectMapper));

        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

        return registration;
    }
}
