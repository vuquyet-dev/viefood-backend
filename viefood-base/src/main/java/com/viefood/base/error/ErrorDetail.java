package com.viefood.base.error;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonPropertyOrder({"endpointName", "status", "correlationId", "timestamp", "details"})
public class ErrorDetail {
    private String endpointName;
    private String status;
    private String correlationId;
    private LocalDateTime timestamp;
    private Map<String, String> details;
}
