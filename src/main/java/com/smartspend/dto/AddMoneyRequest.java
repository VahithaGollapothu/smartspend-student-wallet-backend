package com.smartspend.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddMoneyRequest {
    private Double amount;
    private String description = "Wallet top-up";

    public BigDecimal getAmount() {
        return amount != null ? BigDecimal.valueOf(amount) : null;
    }
}