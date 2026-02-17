package com.BankingSystem.Account.service;

import com.BankingSystem.Account.dto.WalletOperationRequest;
import com.BankingSystem.Account.dto.WalletResponse;
import com.BankingSystem.Account.entity.Wallet;
import com.BankingSystem.Account.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class WalletConcurrencyTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("wallet_test_db")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private WalletRepository walletRepository;
    
    private UUID testWalletId;
    
    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        testWalletId = UUID.randomUUID();
        
        // Create wallet with balance
        Wallet wallet = new Wallet(testWalletId);
        wallet.setBalance(new BigDecimal("10000.00"));
        walletRepository.save(wallet);
    }
    
    @Test
    void testConcurrentDeposits_100Threads() throws InterruptedException, ExecutionException {
        int threadCount = 100;
        BigDecimal depositAmount = new BigDecimal("10.00");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        List<Future<WalletResponse>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Submit 100 concurrent deposit operations
        for (int i = 0; i < threadCount; i++) {
            Future<WalletResponse> future = executorService.submit(() -> {
                try {
                    WalletOperationRequest request = new WalletOperationRequest(
                        testWalletId,
                        WalletOperationRequest.OperationType.DEPOSIT,
                        depositAmount
                    );
                    WalletResponse response = walletService.processOperation(request);
                    successCount.incrementAndGet();
                    return response;
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    throw e;
                }
            });
            futures.add(future);
        }
        
        // Wait for all operations to complete
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        
        // Verify all operations succeeded
        assertEquals(threadCount, successCount.get(), "All operations should succeed");
        assertEquals(0, errorCount.get(), "No errors should occur");
        
        // Verify final balance
        WalletResponse finalBalance = walletService.getWalletBalance(testWalletId);
        BigDecimal expectedBalance = new BigDecimal("10000.00")
            .add(depositAmount.multiply(new BigDecimal(threadCount)));
        assertEquals(expectedBalance, finalBalance.getBalance());
    }
    
    @Test
    void testConcurrentWithdrawals_50Threads() throws InterruptedException {
        int threadCount = 50;
        BigDecimal withdrawAmount = new BigDecimal("10.00");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        List<Future<WalletResponse>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Submit 50 concurrent withdrawal operations
        for (int i = 0; i < threadCount; i++) {
            Future<WalletResponse> future = executorService.submit(() -> {
                try {
                    WalletOperationRequest request = new WalletOperationRequest(
                        testWalletId,
                        WalletOperationRequest.OperationType.WITHDRAW,
                        withdrawAmount
                    );
                    WalletResponse response = walletService.processOperation(request);
                    successCount.incrementAndGet();
                    return response;
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    throw e;
                }
            });
            futures.add(future);
        }
        
        // Wait for all operations to complete
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        
        // Verify all operations succeeded
        assertEquals(threadCount, successCount.get(), "All operations should succeed");
        assertEquals(0, errorCount.get(), "No errors should occur");
        
        // Verify final balance
        WalletResponse finalBalance = walletService.getWalletBalance(testWalletId);
        BigDecimal expectedBalance = new BigDecimal("10000.00")
            .subtract(withdrawAmount.multiply(new BigDecimal(threadCount)));
        assertEquals(expectedBalance, finalBalance.getBalance());
    }
    
    @Test
    void testMixedConcurrentOperations_200Threads() throws InterruptedException {
        int threadCount = 200;
        BigDecimal operationAmount = new BigDecimal("5.00");
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Submit 100 deposits and 100 withdrawals concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    WalletOperationRequest.OperationType operationType = 
                        (index % 2 == 0) ? 
                        WalletOperationRequest.OperationType.DEPOSIT : 
                        WalletOperationRequest.OperationType.WITHDRAW;
                    
                    WalletOperationRequest request = new WalletOperationRequest(
                        testWalletId,
                        operationType,
                        operationAmount
                    );
                    walletService.processOperation(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error in thread " + index + ": " + e.getMessage());
                }
            });
        }
        
        // Wait for all operations to complete
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        
        // Verify all operations succeeded (no 50X errors)
        assertEquals(threadCount, successCount.get(), 
            "All operations should succeed without 50X errors");
        assertEquals(0, errorCount.get(), "No errors should occur");
        
        // Verify final balance (100 deposits - 100 withdrawals = 0 net change)
        WalletResponse finalBalance = walletService.getWalletBalance(testWalletId);
        assertEquals(new BigDecimal("10000.00"), finalBalance.getBalance());
    }
    
    @Test
    void testHighConcurrency_500Operations() throws InterruptedException {
        int operationCount = 500;
        BigDecimal depositAmount = new BigDecimal("1.00");
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        
        CountDownLatch latch = new CountDownLatch(operationCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        // Submit 500 operations
        for (int i = 0; i < operationCount; i++) {
            executorService.submit(() -> {
                try {
                    WalletOperationRequest request = new WalletOperationRequest(
                        testWalletId,
                        WalletOperationRequest.OperationType.DEPOSIT,
                        depositAmount
                    );
                    walletService.processOperation(request);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all operations
        latch.await(60, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        executorService.shutdown();
        
        // Verify all succeeded
        assertEquals(operationCount, successCount.get());
        
        // Verify final balance
        WalletResponse finalBalance = walletService.getWalletBalance(testWalletId);
        BigDecimal expectedBalance = new BigDecimal("10000.00")
            .add(depositAmount.multiply(new BigDecimal(operationCount)));
        assertEquals(expectedBalance, finalBalance.getBalance());
        
        System.out.println("Processed " + operationCount + " operations in " + 
            duration + "ms (" + (operationCount * 1000.0 / duration) + " ops/sec)");
    }
}