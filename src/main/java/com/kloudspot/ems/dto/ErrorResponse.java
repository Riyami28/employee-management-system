package com.kloudspot.ems.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private final LocalDateTime timestamp;

    @Schema(example = "400")
    private final int status;

    @Schema(example = "Bad Request")
    private final String error;

    @Schema(example = "Validation failed")
    private final String message;

    @Schema(example = "/api/employees")
    private final String path;

    @Schema(example = "{\"email\": \"Please enter a valid email address\"}")
    private final Map<String, String> fieldErrors;

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
