package com.viefood.base.error;

import java.util.Map;

public class ServiceException extends RuntimeException{
    private final ErrorCode errorCode;
    private final Map<String, String> details;


    public ServiceException(ErrorCode errorCode)
    {
        this(errorCode, errorCode.getDefaultMessage(), Map.of());
    }

    public ServiceException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public ServiceException(
            ErrorCode errorCode,
            String message,
            Map<String, String> details
    ) {
        super(message);
        this.errorCode = errorCode;
        this.details = Map.copyOf(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
