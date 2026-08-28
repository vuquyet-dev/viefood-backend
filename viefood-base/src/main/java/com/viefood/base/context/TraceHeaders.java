package com.viefood.base.context;

public final class TraceHeaders {
    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String IDEMPOTENCY_KEY = "X-Idempotency-Key";
    public static final String SERVICE_VERSION = "X-Service-Version";

    private TraceHeaders()
    {

    }
}
