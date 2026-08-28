package com.viefood.base.web.trace;

import com.viefood.base.context.TraceHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Doc correlation ID tu request (hoac sinh moi neu chua co), day vao MDC de
 * moi dong log deu mang ID nay, roi tra lai qua response header.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";

    private final String serviceVersion;

    public CorrelationIdFilter(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Gateway thuong da sinh san; neu goi truc tiep service thi tu sinh.
        String correlationId = request.getHeader(TraceHeaders.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Request ID rieng cho chang HTTP nay, luon sinh moi.
        String requestId = UUID.randomUUID().toString();

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_REQUEST_ID, requestId);

        response.setHeader(TraceHeaders.CORRELATION_ID, correlationId);
        response.setHeader(TraceHeaders.REQUEST_ID, requestId);
        if (serviceVersion != null) {
            response.setHeader(TraceHeaders.SERVICE_VERSION, serviceVersion);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // BAT BUOC: thread duoc tai su dung tu pool, khong xoa se ro ri
            // correlation ID cua request truoc sang request sau.
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    /** Tien ich cho GlobalExceptionHandler lay lai correlation ID hien tai. */
    public static String currentCorrelationId() {
        return MDC.get(MDC_CORRELATION_ID);
    }
}
