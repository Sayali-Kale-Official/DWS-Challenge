package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	private final AccountsRepository accountsRepository;
	private final NotificationService notificationService; // Inject NotificationService

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public void transferMoney(String fromAccountId, String toAccountId, BigDecimal transferAmount) {
		if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Enter a Valid Positive Transfer Amount");
		}

		Account accountFrom = accountsRepository.getAccount(fromAccountId);
		Account accountTo = accountsRepository.getAccount(toAccountId);
	
		withdraw(accountFrom,transferAmount);
		deposit(accountTo, transferAmount);
		
		// Create the transfer description for the notification
		String transferDescriptionFrom = String.format("Transferred %s to account %s", transferAmount, toAccountId);
		notificationService.notifyAboutTransfer(accountFrom, transferDescriptionFrom);

		String transferDescriptionTo = String.format("Received %s from account %s", transferAmount, fromAccountId);
		notificationService.notifyAboutTransfer(accountTo, transferDescriptionTo);
	}

	// Withdraw method to decrease the balance
	public synchronized void withdraw(Account accountFrom, BigDecimal withdrawAmount) {
		BigDecimal balance = accountFrom.getBalance();
		if (balance.compareTo(withdrawAmount) < 0) {
			throw new IllegalArgumentException("Insufficient balance");
		}
		balance = balance.subtract(withdrawAmount);
		accountFrom.setBalance(balance);
	}

	// Deposit method to increase the balance
	public synchronized void deposit(Account accountTo, BigDecimal depositAmount) {
		BigDecimal balance = accountTo.getBalance();
		balance = balance.add(depositAmount);
		accountTo.setBalance(balance);
	}

}
