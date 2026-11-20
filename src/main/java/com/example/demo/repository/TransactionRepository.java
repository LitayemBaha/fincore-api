package com.example.demo.repository;

import com.example.demo.model.Transaction;
import com.example.demo.model.TransactionType;
import com.example.demo.repository.projection.CategorySummary;
import com.example.demo.repository.projection.MonthlySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndAccountUserId(Long id, Long userId);

    /**
     * Filtered, paginated listing scoped to the current user. Any filter may be
     * null to be ignored.
     */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.user.id = :userId
              AND (:accountId IS NULL OR t.account.id = :accountId)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:type IS NULL OR t.type = :type)
              AND (:from IS NULL OR t.date >= :from)
              AND (:to IS NULL OR t.date <= :to)
            """)
    Page<Transaction> search(@Param("userId") Long userId,
                             @Param("accountId") Long accountId,
                             @Param("categoryId") Long categoryId,
                             @Param("type") TransactionType type,
                             @Param("from") LocalDate from,
                             @Param("to") LocalDate to,
                             Pageable pageable);

    /**
     * Total amount grouped by category for a user, optionally within a date range.
     */
    @Query("""
            SELECT c.id AS categoryId, c.name AS categoryName, t.type AS type, SUM(t.amount) AS total
            FROM Transaction t JOIN t.category c
            WHERE t.account.user.id = :userId
              AND (:from IS NULL OR t.date >= :from)
              AND (:to IS NULL OR t.date <= :to)
            GROUP BY c.id, c.name, t.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategorySummary> summarizeByCategory(@Param("userId") Long userId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    /**
     * Monthly income vs expense totals. Native query uses TO_CHAR which is
     * supported by both Oracle and H2 (Oracle compatibility mode).
     */
    @Query(value = """
            SELECT TO_CHAR(t.tx_date, 'YYYY-MM') AS period,
                   SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) AS income,
                   SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense
            FROM FIN_TRANSACTION t
            JOIN FIN_ACCOUNT a ON a.id = t.account_id
            WHERE a.user_id = :userId
            GROUP BY TO_CHAR(t.tx_date, 'YYYY-MM')
            ORDER BY period
            """, nativeQuery = true)
    List<MonthlySummary> summarizeByMonth(@Param("userId") Long userId);
}
