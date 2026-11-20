package com.example.demo.service;

import com.example.demo.dto.category.CategoryRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Category create(Long userId, CategoryRequest request) {
        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.name())) {
            throw new DuplicateResourceException("Category already exists: " + request.name());
        }
        User user = userRepository.getReferenceById(userId);
        Category category = new Category();
        category.setUser(user);
        category.setName(request.name());
        category.setType(request.type());
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Page<Category> list(Long userId, Pageable pageable) {
        return categoryRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Category get(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    @Transactional
    public Category update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = get(userId, categoryId);
        category.setName(request.name());
        category.setType(request.type());
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = get(userId, categoryId);
        categoryRepository.delete(category);
    }
}
