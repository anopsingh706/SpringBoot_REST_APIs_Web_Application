package com.BankingSystem.Account.service;

import com.BankingSystem.Account.dto.WalletOperationRequest;
import com.BankingSystem.Account.dto.WalletResponse;
import com.BankingSystem.Account.entity.Wallet;
import com.BankingSystem.Account.exception.InsufficientFundsException;
import com.BankingSystem.Account.exception.WalletNotFoundException;
import com.BankingSystem.Account.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    /**
     * Process wallet operation with pessimistic locking for high concurrency
     * Retries on optimistic locking failures
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(
        retryFor = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public WalletResponse processOperation(WalletOperationRequest request) {
        UUID walletId = request.getValletId();
        
        // Use pessimistic locking to handle high concurrency (1000 RPS)
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));
        
        try {
            switch (request.getOperationType()) {
                case DEPOSIT:
                    wallet.deposit(request.getAmount());
                    log.info("Deposited {} to wallet {}", request.getAmount(), walletId);
                    break;
                case WITHDRAW:
                    wallet.withdraw(request.getAmount());
                    log.info("Withdrawn {} from wallet {}", request.getAmount(), walletId);
                    break;
            }
            
            Wallet savedWallet = walletRepository.save(wallet);
            return new WalletResponse(savedWallet.getId(), savedWallet.getBalance());
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Insufficient funds")) {
                throw new InsufficientFundsException(e.getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Get wallet balance by ID
     */
    @Transactional(readOnly = true)
    public WalletResponse getWalletBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException(walletId));
        
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }
    
    /**
     * Create a new wallet with zero balance
     */
    @Transactional
    public WalletResponse createWallet(UUID walletId) {
        if (walletRepository.existsById(walletId)) {
            throw new IllegalArgumentException("Wallet already exists with ID: " + walletId);
        }
        
        Wallet wallet = new Wallet(walletId);
        Wallet savedWallet = walletRepository.save(wallet);
        
        log.info("Created new wallet with ID: {}", walletId);
        return new WalletResponse(savedWallet.getId(), savedWallet.getBalance());
    }
}