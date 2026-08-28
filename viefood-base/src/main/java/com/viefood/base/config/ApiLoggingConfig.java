package com.viefood.base.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viefood.base.logger.ApiLoggingFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiLoggingConfig {

    @Bean
    @ConditionalOnMissingBean(name = "viefoodApiLoggingFilter")
    public FilterRegistrationBean<Filter> viefoodApiLoggingFilter(
            ObjectMapper objectMapper
    ) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(
                new ApiLoggingFilter(objectMapper)
        );

        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);

        return registration;
    }
}
