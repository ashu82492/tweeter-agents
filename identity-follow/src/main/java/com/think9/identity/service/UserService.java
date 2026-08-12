package com.think9.identity.service;

import com.think9.identity.domain.User;
import java.util.UUID;

public interface UserService {
    User create(String username, String password, String displayName);

    User get(UUID userId);
}