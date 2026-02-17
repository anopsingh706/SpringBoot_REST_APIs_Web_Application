package com.BankingSystem.Account;

import org.springframework.boot.SpringApplication;

public class TestBankAccountApplication {

	public static void main(String[] args) {
		SpringApplication.from(BankAccountApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
