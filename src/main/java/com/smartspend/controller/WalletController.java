package com.smartspend.controller;

import com.smartspend.dto.AddMoneyRequest;
import com.smartspend.dto.SpendLimitRequest;
import com.smartspend.dto.SpendRequest;
import com.smartspend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{studentId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(walletService.getBalance(studentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{studentId}/add")
    public ResponseEntity<?> addMoney(@PathVariable Long studentId,
                                      @RequestBody AddMoneyRequest request) {
        try {
            return ResponseEntity.ok(walletService.addMoney(studentId, request.getAmount(), request.getDescription()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{studentId}/spend")
    public ResponseEntity<?> spendMoney(@PathVariable Long studentId,
                                        @RequestBody SpendRequest request) {
        try {
            return ResponseEntity.ok(walletService.spendMoney(studentId, request.getAmount(), request.getDescription()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{studentId}/set-limit")
    public ResponseEntity<?> setLimit(@PathVariable Long studentId,
                                      @RequestBody SpendLimitRequest request) {
        try {
            BigDecimal limit = BigDecimal.valueOf(request.getSpendLimit());
            return ResponseEntity.ok(walletService.setSpendLimit(studentId, limit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{studentId}/remove-limit")
    public ResponseEntity<?> removeLimit(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(walletService.removeSpendLimit(studentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}