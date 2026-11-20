package com.example.demo.controller;

import com.example.demo.dto.account.AccountRequest;
import com.example.demo.dto.account.AccountResponse;
import com.example.demo.dto.common.PageResponse;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.AccountService;
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
@RequestMapping("/api/accounts")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accounts", description = "Manage financial accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@AuthenticationPrincipal UserPrincipal user,
                                                  @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountResponse.from(accountService.create(user.getId(), request)));
    }

    @GetMapping
    public PageResponse<AccountResponse> list(@AuthenticationPrincipal UserPrincipal user,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(accountService.list(user.getId(), pageable), AccountResponse::from);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        return AccountResponse.from(accountService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public AccountResponse update(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id,
                                  @Valid @RequestBody AccountRequest request) {
        return AccountResponse.from(accountService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable Long id) {
        accountService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
