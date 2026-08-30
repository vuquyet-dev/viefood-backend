package com.viefood.base.web.error;

import com.viefood.base.error.ErrorCode;
import com.viefood.base.error.ErrorDetail;
import com.viefood.base.error.ServiceException;
import com.viefood.base.web.trace.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Doi moi exception thanh ErrorDetail thong nhat toan he thong.
 * Viet mot lan o day, moi service import viefood-base-web deu tu dong co.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Loi nghiep vu chu dong throw tu service. */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorDetail> handleServiceException(
            ServiceException ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ex.getErrorCode();
        log.warn("ServiceException {}: {}", code.name(), ex.getMessage());

        ErrorDetail errorDetail = build(code, request, ex.getDetails());
        return ResponseEntity.status(code.getHttpStatus()).body(errorDetail);
    }

    /** Bean Validation that bai tren @Valid @RequestBody. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ErrorCode.ERR_INVALID_DATA;
        FieldError fieldError = ex.getBindingResult().getFieldErrors()
                .stream()
                .findFirst()
                .orElse(null);

        Map<String, String> details = fieldError == null
                ? details(null, code.getDefaultMessage())
                : details(fieldError.getField(), fieldError.getDefaultMessage());

        return ResponseEntity.status(code.getHttpStatus())
                .body(build(code, request, details));
    }

    /** JSON sai cu phap, thieu body, sai kieu du lieu. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetail> handleUnreadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ErrorCode.ERR_FORMAT_REQUEST;
        log.warn("Request body khong doc duoc: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(code.getHttpStatus())
                .body(build(
                        code,
                        request,
                        details("requestBody", code.getDefaultMessage())
                ));
    }

    /**
     * Exception cua chinh Spring MVC (404 khong co route, 405 sai method...).
     * Giu nguyen status cua no, chi bo sung correlationId cho dong nhat.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorDetail> handleSpringError(
            ErrorResponseException ex,
            HttpServletRequest request
    ) {
        int httpStatus = ex.getStatusCode().value();
        HttpStatus resolvedStatus = HttpStatus.resolve(httpStatus);
        String status = resolvedStatus == null
                ? String.valueOf(httpStatus)
                : resolvedStatus.name();
        String description = ex.getBody().getDetail();
        if (description == null || description.isBlank()) {
            description = ex.getMessage();
        }

        ErrorDetail errorDetail = build(
                request,
                status,
                details(null, description)
        );
        return ResponseEntity.status(ex.getStatusCode()).body(errorDetail);
    }

    /** Luoi cuoi cung: khong bao gio de stack trace lot ra client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ErrorCode.ERR_INTERNAL_ERROR;
        log.error("Loi khong mong doi", ex);
        return ResponseEntity.status(code.getHttpStatus())
                .body(build(
                        code,
                        request,
                        details(null, code.getDefaultMessage())
                ));
    }

    private ErrorDetail build(
            ErrorCode code,
            HttpServletRequest request,
            Map<String, String> details
    ) {
        return build(request, code.name(), details);
    }

    private ErrorDetail build(
            HttpServletRequest request,
            String status,
            Map<String, String> details
    ) {
        return ErrorDetail.builder()
                .endpointName(request.getRequestURI())
                .status(status)
                .correlationId(CorrelationIdFilter.currentCorrelationId())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }

    private Map<String, String> details(String field, String description) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("field", field);
        details.put("description", description);
        return Collections.unmodifiableMap(details);
    }
}
