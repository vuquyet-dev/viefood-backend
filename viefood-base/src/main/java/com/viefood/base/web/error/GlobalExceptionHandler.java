package com.viefood.base.web.error;

import com.viefood.base.error.ErrorCode;
import com.viefood.base.error.ServiceException;
import com.viefood.base.web.trace.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Doi moi exception thanh ProblemDetail (RFC 7807) thong nhat toan he thong.
 * Viet mot lan o day, moi service import viefood-base-web deu tu dong co.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Loi nghiep vu chu dong throw tu service. */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ProblemDetail> handleServiceException(ServiceException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("ServiceException {}: {}", code.name(), ex.getMessage());

        ProblemDetail problem = build(code, ex.getMessage());
        if (!ex.getDetails().isEmpty()) {
            problem.setProperty("details", ex.getDetails());
        }
        return ResponseEntity.status(code.getHttpStatus()).body(problem);
    }

    /** Bean Validation that bai tren @Valid @RequestBody. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // merge: giu loi dau tien cua moi field cho gon
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ErrorCode code = ErrorCode.ERR_INVALID_DATA;
        ProblemDetail problem = build(code, code.getDefaultMessage());
        problem.setProperty("details", fieldErrors);
        return ResponseEntity.status(code.getHttpStatus()).body(problem);
    }

    /** JSON sai cu phap, thieu body, sai kieu du lieu. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        ErrorCode code = ErrorCode.ERR_FORMAT_REQUEST;
        log.warn("Request body khong doc duoc: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(code.getHttpStatus())
                .body(build(code, code.getDefaultMessage()));
    }

    /**
     * Exception cua chinh Spring MVC (404 khong co route, 405 sai method...).
     * Giu nguyen status cua no, chi bo sung correlationId cho dong nhat.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> handleSpringError(ErrorResponseException ex) {
        ProblemDetail problem = ex.getBody();
        problem.setProperty("correlationId", CorrelationIdFilter.currentCorrelationId());
        problem.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.status(ex.getStatusCode()).body(problem);
    }

    /** Luoi cuoi cung: khong bao gio de stack trace lot ra client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        ErrorCode code = ErrorCode.ERR_INTERNAL_ERROR;
        log.error("Loi khong mong doi", ex);
        return ResponseEntity.status(code.getHttpStatus())
                .body(build(code, code.getDefaultMessage()));
    }

    private ProblemDetail build(ErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(code.getHttpStatus()), detail);
        problem.setTitle(code.name());
        problem.setProperty("code", code.name());
        problem.setProperty("correlationId", CorrelationIdFilter.currentCorrelationId());
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
