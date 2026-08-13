package com.nexus.identity.service;

import com.nexus.identity.domain.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    User create(String username, String password, String displayName);

    User get(UUID userId);

    List<User> list(int limit, UUID excludedUserId);
}