package com.nexus.identity.service;

import java.util.UUID;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID userId) {
        super("user not found: " + userId);
    }
}