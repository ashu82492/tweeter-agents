package com.nexus.core.pagination;

import java.util.List;

public record Page<T>(List<T> items, String nextCursor) {
    public Page {
        items = List.copyOf(items);
    }
}