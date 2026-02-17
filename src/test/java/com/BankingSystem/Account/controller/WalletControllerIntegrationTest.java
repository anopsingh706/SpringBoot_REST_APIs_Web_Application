package com.BankingSystem.Account.controller;

import com.BankingSystem.Account.entity.Wallet;
import com.BankingSystem.Account.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class WalletControllerIntegrationTest {

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
        registry.add("spring.liquibase.enabled", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WalletRepository walletRepository;

    private UUID testWalletId;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        testWalletId = UUID.randomUUID();
        Wallet wallet = new Wallet(testWalletId);
        wallet.setBalance(new BigDecimal("1000.00"));
        walletRepository.save(wallet);
    }

    private String json(UUID id, String type, String amount) {
        return String.format(
            "{\"valletId\":\"%s\",\"operationType\":\"%s\",\"amount\":%s}",
            id, type, amount
        );
    }

    @Test
    void testDeposit_Success() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(testWalletId, "DEPOSIT", "500.00")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
            .andExpect(jsonPath("$.balance", is(1500.00)));
    }

    @Test
    void testWithdraw_Success() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(testWalletId, "WITHDRAW", "300.00")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
            .andExpect(jsonPath("$.balance", is(700.00)));
    }

    @Test
    void testWithdraw_InsufficientFunds() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(testWalletId, "WITHDRAW", "9999.00")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testOperation_WalletNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(UUID.randomUUID(), "DEPOSIT", "100.00")))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetBalance_Success() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}", testWalletId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.walletId", is(testWalletId.toString())))
            .andExpect(jsonPath("$.balance", is(1000.00)));
    }

    @Test
    void testGetBalance_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}", UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ bad json }"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingAmount() throws Exception {
        String body = String.format(
            "{\"valletId\":\"%s\",\"operationType\":\"DEPOSIT\"}", testWalletId
        );
        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidUUID() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/{walletId}", "not-a-uuid"))
            .andExpect(status().isBadRequest());
    }
}