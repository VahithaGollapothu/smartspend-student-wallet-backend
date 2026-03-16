package com.smartspend.controller;

import com.smartspend.dto.TransactionResponse;
import com.smartspend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{studentId}/history")
    public ResponseEntity<?> getHistory(@PathVariable Long studentId) {
        try {
            List<TransactionResponse> transactions = transactionService.getTransactionHistory(studentId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/detail/{transactionId}")
    public ResponseEntity<?> getById(@PathVariable Long transactionId) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}