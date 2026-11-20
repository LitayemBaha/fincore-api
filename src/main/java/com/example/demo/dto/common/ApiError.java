package com.example.demo.dto.common;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error payload returned by the global exception handler.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
