package com.smartspend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SpendRequest {
    private Double amount;
    private String description;

    public BigDecimal getAmount() {
        return amount != null ? BigDecimal.valueOf(amount) : null;
    }
}