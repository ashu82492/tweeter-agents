package com.think9.core.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IdempotencyKeyTest {

    @Test
    void createsKey_whenValueIsWithinAllowedBounds() {
        IdempotencyKey key = new IdempotencyKey("tweet-request-1");

        assertEquals("tweet-request-1", key.value());
    }

    @Test
    void rejectsBlankKey() {
        assertThrows(IllegalArgumentException.class, () -> new IdempotencyKey("  "));
    }
}