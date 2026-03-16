package com.smartspend.service;

import com.smartspend.dto.TransactionResponse;
import com.smartspend.model.Transaction;
import com.smartspend.model.Wallet;
import com.smartspend.repository.TransactionRepository;
import com.smartspend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public List<TransactionResponse> getTransactionHistory(Long studentId) {
        Wallet wallet = walletRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for student: " + studentId));

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return toResponse(tx);
    }

    private TransactionResponse toResponse(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType().name())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}