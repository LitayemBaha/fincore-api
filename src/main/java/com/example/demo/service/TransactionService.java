package com.example.demo.service;

import com.example.demo.dto.transaction.TransactionRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Account;
import com.example.demo.model.Category;
import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Transaction create(Long userId, TransactionRequest request) {
        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId()));

        Category category = resolveCategory(userId, request.categoryId(), request.type());

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setCategory(category);
        tx.setType(request.type());
        tx.setAmount(request.amount());
        tx.setDescription(request.description());
        tx.setDate(request.date());

        applyToBalance(account, request.type(), request.amount());
        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Page<Transaction> search(Long userId, Long accountId, Long categoryId,
                                    TransactionType type, LocalDate from, LocalDate to, Pageable pageable) {
        return transactionRepository.search(userId, accountId, categoryId, type, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public Transaction get(Long userId, Long transactionId) {
        return transactionRepository.findByIdAndAccountUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
    }

    @Transactional
    public Transaction update(Long userId, Long transactionId, TransactionRequest request) {
        Transaction tx = get(userId, transactionId);

        // Reverse the previous effect on the original account.
        reverseFromBalance(tx.getAccount(), tx.getType(), tx.getAmount());

        Account account = accountRepository.findByIdAndUserId(request.accountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountId()));
        Category category = resolveCategory(userId, request.categoryId(), request.type());

        tx.setAccount(account);
        tx.setCategory(category);
        tx.setType(request.type());
        tx.setAmount(request.amount());
        tx.setDescription(request.description());
        tx.setDate(request.date());

        applyToBalance(account, request.type(), request.amount());
        return transactionRepository.save(tx);
    }

    @Transactional
    public void delete(Long userId, Long transactionId) {
        Transaction tx = get(userId, transactionId);
        reverseFromBalance(tx.getAccount(), tx.getType(), tx.getAmount());
        transactionRepository.delete(tx);
    }

    private Category resolveCategory(Long userId, Long categoryId, TransactionType type) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        if (category.getType() != type) {
            throw new BadRequestException("Category type " + category.getType()
                    + " does not match transaction type " + type);
        }
        return category;
    }

    private void applyToBalance(Account account, TransactionType type, BigDecimal amount) {
        BigDecimal delta = type == TransactionType.INCOME ? amount : amount.negate();
        account.setBalance(account.getBalance().add(delta));
    }

    private void reverseFromBalance(Account account, TransactionType type, BigDecimal amount) {
        BigDecimal delta = type == TransactionType.INCOME ? amount.negate() : amount;
        account.setBalance(account.getBalance().add(delta));
    }
}
