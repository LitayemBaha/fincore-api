package com.example.demo.service;

import com.example.demo.dto.account.AccountRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account create(Long userId, AccountRequest request) {
        User user = userRepository.getReferenceById(userId);
        Account account = new Account();
        account.setUser(user);
        account.setName(request.name());
        account.setType(request.type());
        account.setCurrency(request.currency().toUpperCase());
        account.setBalance(request.openingBalance() != null ? request.openingBalance() : BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Page<Account> list(Long userId, Pageable pageable) {
        return accountRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Account get(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    @Transactional
    public Account update(Long userId, Long accountId, AccountRequest request) {
        Account account = get(userId, accountId);
        account.setName(request.name());
        account.setType(request.type());
        account.setCurrency(request.currency().toUpperCase());
        return accountRepository.save(account);
    }

    @Transactional
    public void delete(Long userId, Long accountId) {
        Account account = get(userId, accountId);
        accountRepository.delete(account);
    }
}
