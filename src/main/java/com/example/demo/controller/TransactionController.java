package com.example.demo.controller;

import com.example.demo.dto.common.PageResponse;
import com.example.demo.dto.transaction.TransactionRequest;
import com.example.demo.dto.transaction.TransactionResponse;
import com.example.demo.model.TransactionType;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Record and query financial transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@AuthenticationPrincipal UserPrincipal user,
                                                      @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transactionService.create(user.getId(), request)));
    }

    @GetMapping
    @Operation(summary = "List transactions with optional filters (account, category, type, date range)")
    public PageResponse<TransactionResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(
                transactionService.search(user.getId(), accountId, categoryId, type, from, to, pageable),
                TransactionResponse::from);
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return TransactionResponse.from(transactionService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id,
                                      @Valid @RequestBody TransactionRequest request) {
        return TransactionResponse.from(transactionService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        transactionService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
