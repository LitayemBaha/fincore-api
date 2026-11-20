package com.example.demo.dto.category;

import com.example.demo.model.Category;
import com.example.demo.model.TransactionType;

public record CategoryResponse(
        Long id,
        String name,
        TransactionType type
) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType());
    }
}
