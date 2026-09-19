package com.chari.chariapp.operations.application;

import java.util.List;

public record OperationsPage<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public OperationsPage {
        content = List.copyOf(content);
    }
}
