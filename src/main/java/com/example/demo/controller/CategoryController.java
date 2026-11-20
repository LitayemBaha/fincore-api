package com.example.demo.controller;

import com.example.demo.dto.category.CategoryRequest;
import com.example.demo.dto.category.CategoryResponse;
import com.example.demo.dto.common.PageResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Categories", description = "Manage transaction categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal UserPrincipal user,
                                                   @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.from(categoryService.create(user.getId(), request)));
    }

    @GetMapping
    public PageResponse<CategoryResponse> list(@AuthenticationPrincipal UserPrincipal user,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(categoryService.list(user.getId(), pageable), CategoryResponse::from);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return CategoryResponse.from(categoryService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id,
                                   @Valid @RequestBody CategoryRequest request) {
        return CategoryResponse.from(categoryService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        categoryService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
