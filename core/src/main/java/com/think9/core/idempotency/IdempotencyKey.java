package com.think9.core.idempotency;

import java.util.Objects;

public record IdempotencyKey(String value) {
    public IdempotencyKey {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank() || value.length() > 128) {
            throw new IllegalArgumentException("idempotency key must contain between 1 and 128 characters");
        }
    }
}