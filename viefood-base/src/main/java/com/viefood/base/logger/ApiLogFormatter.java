package com.viefood.base.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ApiLogFormatter {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");

    private ApiLogFormatter() {
    }

    public static String format(
            String method,
            String uri,
            int status,
            long duration,
            String requestBody,
            String responseBody
    ) {

        StringBuilder log =
                new StringBuilder();

        log.append("\n");

        log.append("[")
                .append(
                        LocalDateTime.now()
                                .format(TIME_FORMATTER)
                )
                .append("] ");

        log.append("[")
                .append(method)
                .append(" ")
                .append(uri)
                .append("] ");

        log.append("[")
                .append(status)
                .append("] ");

        log.append("[")
                .append(duration)
                .append("ms]");

        log.append("\n");

        log.append("request:");

        log.append("\n");

        if (requestBody == null ||
                requestBody.isBlank()) {

            log.append("{}");

        } else {

            log.append(requestBody);
        }

        log.append("\n\n");

        log.append("response:");

        log.append("\n");

        if (responseBody == null ||
                responseBody.isBlank()) {

            log.append("{}");

        } else {

            log.append(responseBody);
        }

        return log.toString();
    }
}
