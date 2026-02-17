package com.BankingSystem.Account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationRequest {
    
    @NotNull(message = "Wallet ID is required")
    @JsonProperty("valletId")  // Typo in the spec, but we follow it
    private UUID valletId;
    
    @NotNull(message = "Operation type is required")
    @JsonProperty("operationType")
    private OperationType operationType;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;
    
    public enum OperationType {
        DEPOSIT,
        WITHDRAW
    }
}