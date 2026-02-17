package com.BankingSystem.Account.controller;

import com.BankingSystem.Account.dto.WalletOperationRequest;
import com.BankingSystem.Account.dto.WalletResponse;
import com.BankingSystem.Account.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    
    private final WalletService walletService;
    
    /**
     * Process wallet operation (DEPOSIT or WITHDRAW)
     * POST /api/v1/wallet
     */
    @PostMapping("/wallet")
    public ResponseEntity<WalletResponse> processOperation(
            @Valid @RequestBody WalletOperationRequest request) {
        
        log.info("Processing {} operation for wallet {}", 
            request.getOperationType(), request.getValletId());
        
        WalletResponse response = walletService.processOperation(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get wallet balance
     * GET /api/v1/wallets/{WALLET_UUID}
     */
    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getWalletBalance(
            @PathVariable UUID walletId) {
        
        log.info("Getting balance for wallet {}", walletId);
        
        WalletResponse response = walletService.getWalletBalance(walletId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create a new wallet (helper endpoint for testing)
     * POST /api/v1/wallets
     */
    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody UUID walletId) {
        log.info("Creating new wallet with ID {}", walletId);
        
        WalletResponse response = walletService.createWallet(walletId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}