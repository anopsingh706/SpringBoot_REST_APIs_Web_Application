package com.BankingSystem.Account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    
    @JsonProperty("walletId")
    private UUID walletId;
    
    @JsonProperty("balance")
    private BigDecimal balance;
}