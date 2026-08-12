package com.think9.app.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, SecurityException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> invalidRequest(RuntimeException exception) { return Map.of("code", "INVALID_REQUEST", "message", exception.getMessage()); }
}