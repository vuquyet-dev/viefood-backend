package com.viefood.base.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public class ApiLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger =
            LoggerFactory.getLogger("API_LOG");

    private static final int CACHE_LIMIT = 1024 * 1024;
    private static final int LOG_BODY_LIMIT = 16 * 1024;

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "passwordhash", "accesstoken", "refreshtoken",
            "authorization", "secret", "clientsecret"
    );

    private final ObjectMapper objectMapper;

    public ApiLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request, CACHE_LIMIT);

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {

            filterChain.doFilter(
                    requestWrapper,
                    responseWrapper
            );

        } finally {

            long duration =
                    System.currentTimeMillis() - startTime;

            String requestBody = safeJsonForLog(
                    request.getContentType(),
                    getRequestBody(requestWrapper)
            );

            String responseBody = safeJsonForLog(
                    responseWrapper.getContentType(),
                    getResponseBody(responseWrapper)
            );

            String method =
                    request.getMethod();

            String uri =
                    request.getRequestURI();

            int status =
                    responseWrapper.getStatus();

            String log = ApiLogFormatter.format(
                    method,
                    uri,
                    status,
                    duration,
                    requestBody,
                    responseBody
            );

            logger.info(log);

            /*
             * Rất quan trọng:
             * phải copy response body về response thật.
             */
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getRequestBody(
            ContentCachingRequestWrapper request
    ) {

        byte[] content =
                request.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return new String(
                content,
                StandardCharsets.UTF_8
        );
    }

    private String getResponseBody(
            ContentCachingResponseWrapper response
    ) {

        byte[] content =
                response.getContentAsByteArray();

        if (content.length == 0) {
            return "";
        }

        return new String(
                content,
                StandardCharsets.UTF_8
        );
    }

    private String safeJsonForLog(String contentType, String body) {
        if (body == null || body.isBlank()) {
            return "{}";
        }

        if (body.length() > LOG_BODY_LIMIT) {
            return "[body omitted: larger than 16 KB]";
        }

        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("json")) {
            return "[body omitted: non-JSON content]";
        }

        try {
            JsonNode root = objectMapper.readTree(body).deepCopy();
            maskSensitiveFields(root);
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(root);
        } catch (IOException exception) {
            return "[body omitted: invalid JSON]";
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            objectNode.fields().forEachRemaining(entry -> {
                if (SENSITIVE_FIELDS.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                    objectNode.put(entry.getKey(), "***");
                } else {
                    maskSensitiveFields(entry.getValue());
                }
            });
            return;
        }

        if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::maskSensitiveFields);
        }
    }

    /**
     * Không log các request static resource.
     */
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String uri =
                request.getRequestURI();

        return uri.startsWith("/favicon")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs");
    }
}
