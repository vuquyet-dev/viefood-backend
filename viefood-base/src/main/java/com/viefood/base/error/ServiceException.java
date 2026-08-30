package com.viefood.base.error;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ServiceException extends RuntimeException {
    private final ErrorCode errorCode;
    private final Map<String, String> details;

    public ServiceException(ErrorCode errorCode, String field) {
        this(errorCode, field, errorCode.getDefaultMessage());
    }

    public ServiceException(
            ErrorCode errorCode,
            String field,
            String description
    ) {
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");

        Map<String, String> detailValues = new LinkedHashMap<>();
        detailValues.put("field", Objects.requireNonNull(field, "field must not be null"));
        detailValues.put("description", description);
        this.details = Collections.unmodifiableMap(detailValues);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
