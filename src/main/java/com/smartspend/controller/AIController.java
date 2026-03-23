package com.smartspend.controller;

import com.smartspend.dto.TransactionResponse;
import com.smartspend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            LocalDateTime now      = LocalDateTime.now();
            LocalDateTime weekAgo  = now.minus(7,  ChronoUnit.DAYS);
            LocalDateTime monthAgo = now.minus(30, ChronoUnit.DAYS);

            BigDecimal weekSpent = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .filter(t -> t.getCreatedAt().isAfter(weekAgo))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthSpent = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .filter(t -> t.getCreatedAt().isAfter(monthAgo))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalAdded = transactions.stream()
                .filter(t -> "CREDIT".equals(t.getType()))
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, BigDecimal> byCategory = new HashMap<>();
            transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .forEach(t -> byCategory.merge(
                    t.getDescription(),
                    t.getAmount(),
                    BigDecimal::add
                ));

            int dayOfMonth  = now.getDayOfMonth();
            int daysInMonth = now.getMonth().length(now.toLocalDate().isLeapYear());

            Map<String, Object> result = new HashMap<>();
            result.put("weekSpent",    weekSpent);
            result.put("monthSpent",   monthSpent);
            result.put("totalAdded",   totalAdded);
            result.put("byCategory",   byCategory);
            result.put("dayOfMonth",   dayOfMonth);
            result.put("daysInMonth",  daysInMonth);
            result.put("transactions", transactions);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}