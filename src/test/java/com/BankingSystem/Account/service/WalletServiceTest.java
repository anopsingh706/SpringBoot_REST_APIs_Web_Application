package com.BankingSystem.Account.service;

import com.BankingSystem.Account.dto.WalletOperationRequest;
import com.BankingSystem.Account.dto.WalletResponse;
import com.BankingSystem.Account.entity.Wallet;
import com.BankingSystem.Account.exception.InsufficientFundsException;
import com.BankingSystem.Account.exception.WalletNotFoundException;
import com.BankingSystem.Account.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    
    @Mock
    private WalletRepository walletRepository;
    
    @InjectMocks
    private WalletService walletService;
    
    private UUID testWalletId;
    private Wallet testWallet;
    
    @BeforeEach
    void setUp() {
        testWalletId = UUID.randomUUID();
        testWallet = new Wallet(testWalletId);
        testWallet.setBalance(new BigDecimal("1000.00"));
    }
    
    @Test
    void testProcessOperation_Deposit_Success() {
        // Given
        WalletOperationRequest request = new WalletOperationRequest(
            testWalletId,
            WalletOperationRequest.OperationType.DEPOSIT,
            new BigDecimal("500.00")
        );
        
        when(walletRepository.findByIdWithLock(testWalletId))
            .thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        WalletResponse response = walletService.processOperation(request);
        
        // Then
        assertNotNull(response);
        assertEquals(testWalletId, response.getWalletId());
        assertEquals(new BigDecimal("1500.00"), response.getBalance());
        verify(walletRepository).findByIdWithLock(testWalletId);
        verify(walletRepository).save(testWallet);
    }
    
    @Test
    void testProcessOperation_Withdraw_Success() {
        // Given
        WalletOperationRequest request = new WalletOperationRequest(
            testWalletId,
            WalletOperationRequest.OperationType.WITHDRAW,
            new BigDecimal("300.00")
        );
        
        when(walletRepository.findByIdWithLock(testWalletId))
            .thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        WalletResponse response = walletService.processOperation(request);
        
        // Then
        assertNotNull(response);
        assertEquals(testWalletId, response.getWalletId());
        assertEquals(new BigDecimal("700.00"), response.getBalance());
        verify(walletRepository).findByIdWithLock(testWalletId);
        verify(walletRepository).save(testWallet);
    }
    
    @Test
    void testProcessOperation_Withdraw_InsufficientFunds() {
        // Given
        WalletOperationRequest request = new WalletOperationRequest(
            testWalletId,
            WalletOperationRequest.OperationType.WITHDRAW,
            new BigDecimal("2000.00")
        );
        
        when(walletRepository.findByIdWithLock(testWalletId))
            .thenReturn(Optional.of(testWallet));
        
        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            walletService.processOperation(request);
        });
        
        verify(walletRepository).findByIdWithLock(testWalletId);
        verify(walletRepository, never()).save(any());
    }
    
    @Test
    void testProcessOperation_WalletNotFound() {
        // Given
        WalletOperationRequest request = new WalletOperationRequest(
            testWalletId,
            WalletOperationRequest.OperationType.DEPOSIT,
            new BigDecimal("100.00")
        );
        
        when(walletRepository.findByIdWithLock(testWalletId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(WalletNotFoundException.class, () -> {
            walletService.processOperation(request);
        });
        
        verify(walletRepository).findByIdWithLock(testWalletId);
        verify(walletRepository, never()).save(any());
    }
    
    @Test
    void testGetWalletBalance_Success() {
        // Given
        when(walletRepository.findById(testWalletId))
            .thenReturn(Optional.of(testWallet));
        
        // When
        WalletResponse response = walletService.getWalletBalance(testWalletId);
        
        // Then
        assertNotNull(response);
        assertEquals(testWalletId, response.getWalletId());
        assertEquals(new BigDecimal("1000.00"), response.getBalance());
        verify(walletRepository).findById(testWalletId);
    }
    
    @Test
    void testGetWalletBalance_NotFound() {
        // Given
        when(walletRepository.findById(testWalletId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(WalletNotFoundException.class, () -> {
            walletService.getWalletBalance(testWalletId);
        });
        
        verify(walletRepository).findById(testWalletId);
    }
    
    @Test
    void testCreateWallet_Success() {
        // Given
        when(walletRepository.existsById(testWalletId))
            .thenReturn(false);
        when(walletRepository.save(any(Wallet.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        WalletResponse response = walletService.createWallet(testWalletId);
        
        // Then
        assertNotNull(response);
        assertEquals(testWalletId, response.getWalletId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletRepository).existsById(testWalletId);
        verify(walletRepository).save(any(Wallet.class));
    }
    
    @Test
    void testCreateWallet_AlreadyExists() {
        // Given
        when(walletRepository.existsById(testWalletId))
            .thenReturn(true);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.createWallet(testWalletId);
        });
        
        verify(walletRepository).existsById(testWalletId);
        verify(walletRepository, never()).save(any());
    }
}