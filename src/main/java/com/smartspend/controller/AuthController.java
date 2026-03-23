package com.smartspend.controller;

import com.smartspend.model.Transaction;
import com.smartspend.service.TransactionService;
import com.smartspend.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {

    private final TransactionService transactionService;

    @GetMapping("/{studentId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Long studentId) {
        try {
            List<TransactionResponse> transactions = transactionService.getTransactionHistory(studentId);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo  = now.minus(7,  ChronoUnit.DAYS);
            LocalDateTime monthAgo = now.minus(30, ChronoUnit.DAYS);

            // This week spending
            BigDecimal weekSpent = transactions.stream()
                .filter(t -> t.getType().equals("DEBIT"))
                .filter(t -> t.getCreatedAt().isAfter(weekAgo))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // This month spending
            BigDecimal monthSpent = transactions.stream()
                .filter(t -> t.getType().equals("DEBIT"))
                .filter(t -> t.getCreatedAt().isAfter(monthAgo))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total added
            BigDecimal totalAdded = transactions.stream()
                .filter(t -> t.getType().equals("CREDIT"))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Category breakdown
            Map<String, BigDecimal> byCategory = transactions.stream()
                .filter(t -> t.getType().equals("DEBIT"))
                .collect(Collectors.groupingBy(
                    TransactionResponse::getDescription,
                    Collectors.reducing(BigDecimal.ZERO, TransactionResponse::getAmount, BigDecimal::add)
                ));

            // Days elapsed this month
            int dayOfMonth = now.getDayOfMonth();
            int daysInMonth = now.getMonth().length(now.toLocalDate().isLeapYear());

            return ResponseEntity.ok(Map.of(
                "weekSpent",    weekSpent,
                "monthSpent",   monthSpent,
                "totalAdded",   totalAdded,
                "byCategory",   byCategory,
                "dayOfMonth",   dayOfMonth,
                "daysInMonth",  daysInMonth,
                "transactions", transactions
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}