package com.smartspend.service;

import com.smartspend.model.Transaction;
import com.smartspend.model.Wallet;
import com.smartspend.repository.TransactionRepository;
import com.smartspend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public Map<String, Object> getBalance(Long studentId) {
        Wallet wallet = getWallet(studentId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("balance", wallet.getBalance());
        resp.put("walletId", wallet.getId());
        resp.put("spendLimit", wallet.getSpendLimit());  // ← include limit in response
        return resp;
    }

    @Transactional
    public Map<String, Object> addMoney(Long studentId, BigDecimal amount, String description) {
        Wallet wallet = getWallet(studentId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = transactionRepository.save(Transaction.builder()
                .amount(amount)
                .type(Transaction.TransactionType.CREDIT)
                .description(description)
                .wallet(wallet)
                .build());

        Map<String, Object> resp = new HashMap<>();
        resp.put("newBalance", wallet.getBalance());
        resp.put("message", "Money added successfully");
        resp.put("transactionId", tx.getId());
        return resp;
    }

    @Transactional
    public Map<String, Object> spendMoney(Long studentId, BigDecimal amount, String description) {
        Wallet wallet = getWallet(studentId);

        if (wallet.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("Insufficient balance");

        // ── Check spend limit ─────────────────────────────────────────────
        if (wallet.getSpendLimit() != null) {
            BigDecimal monthlySpent = getMonthlySpent(wallet);
            BigDecimal newTotal = monthlySpent.add(amount);
            if (newTotal.compareTo(wallet.getSpendLimit()) > 0) {
                throw new RuntimeException(
                    "Spend limit exceeded! Monthly limit: ₹" + wallet.getSpendLimit() +
                    ", Already spent: ₹" + monthlySpent +
                    ", Trying to spend: ₹" + amount
                );
            }
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction tx = transactionRepository.save(Transaction.builder()
                .amount(amount)
                .type(Transaction.TransactionType.DEBIT)
                .description(description)
                .wallet(wallet)
                .build());

        Map<String, Object> resp = new HashMap<>();
        resp.put("newBalance", wallet.getBalance());
        resp.put("message", "Payment successful");
        resp.put("transactionId", tx.getId());
        return resp;
    }

    // ── Set spend limit ───────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> setSpendLimit(Long studentId, BigDecimal limit) {
        Wallet wallet = getWallet(studentId);
        wallet.setSpendLimit(limit);
        walletRepository.save(wallet);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Spend limit set to ₹" + limit);
        resp.put("spendLimit", limit);
        return resp;
    }

    // ── Remove spend limit ────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> removeSpendLimit(Long studentId) {
        Wallet wallet = getWallet(studentId);
        wallet.setSpendLimit(null);
        walletRepository.save(wallet);
        return Map.of("message", "Spend limit removed");
    }

    // ── Calculate this month's total spending ─────────────────────────────
    private BigDecimal getMonthlySpent(Wallet wallet) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.DEBIT)
                .filter(t -> t.getCreatedAt().isAfter(startOfMonth))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Wallet getWallet(Long studentId) {
        return walletRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}