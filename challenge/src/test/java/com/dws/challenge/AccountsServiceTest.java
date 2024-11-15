package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountsServiceTest {

	@MockBean
	private AccountsRepository accountsRepository;

	@MockBean
	private NotificationService notificationService;

	private AccountsService accountsService;

	@BeforeEach
	void setUp() {
		// Initialize mocks for each test
		notificationService = mock(NotificationService.class);
		accountsRepository = mock(AccountsRepository.class);

		// Manually inject mocks into AccountsService
		accountsService = new AccountsService(accountsRepository, notificationService);
	}

	@Test
	void createAccount_success() {
		Account account = new Account("Id-1", BigDecimal.valueOf(1000));
		accountsService.createAccount(account);
		verify(accountsRepository, times(1)).createAccount(account);
	}

	@Test
	void getAccount_success() {
		Account account = new Account("Id-1", BigDecimal.valueOf(1000));
		when(accountsRepository.getAccount("Id-1")).thenReturn(account);

		Account retrievedAccount = accountsService.getAccount("Id-1");
		assertThat(retrievedAccount).isEqualTo(account);
		verify(accountsRepository, times(1)).getAccount("Id-1");
	}

	@Test
	void transferMoney_success() {

		Account accountFrom = new Account("Id-1", BigDecimal.valueOf(1000));
		Account accountTo = new Account("Id-2", BigDecimal.valueOf(500));

		when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("Id-2")).thenReturn(accountTo);

		accountsService.transferMoney("Id-1", "Id-2", BigDecimal.valueOf(200));

		assertThat(accountFrom.getBalance()).isEqualByComparingTo("800");
		assertThat(accountTo.getBalance()).isEqualByComparingTo("700");

		verify(notificationService, times(1)).notifyAboutTransfer(accountFrom, "Transferred 200 to account Id-2");
		verify(notificationService, times(1)).notifyAboutTransfer(accountTo, "Received 200 from account Id-1");
	}

	@Test
	void transferMoney_insufficientBalance() {
		Account accountFrom = new Account("Id-1", BigDecimal.valueOf(100));
		Account accountTo = new Account("Id-2", BigDecimal.valueOf(500));

		when(accountsRepository.getAccount("Id-1")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("Id-2")).thenReturn(accountTo);

		assertThrows(IllegalArgumentException.class,
				() -> accountsService.transferMoney("Id-1", "Id-2", BigDecimal.valueOf(200)));

		assertThat(accountFrom.getBalance()).isEqualByComparingTo("100");
		assertThat(accountTo.getBalance()).isEqualByComparingTo("500");

		verify(notificationService, never()).notifyAboutTransfer(any(), any());
	}

	@Test
	void withdraw_success() {
		Account account = new Account("Id-1", BigDecimal.valueOf(1000));

		accountsService.withdraw(account, BigDecimal.valueOf(300));

		assertThat(account.getBalance()).isEqualByComparingTo("700");
	}

	@Test
	void withdraw_insufficientBalance() {
		Account account = new Account("Id-1", BigDecimal.valueOf(100));

		assertThrows(IllegalArgumentException.class, () -> accountsService.withdraw(account, BigDecimal.valueOf(200)));

		assertThat(account.getBalance()).isEqualByComparingTo("100");
	}

	@Test
	void deposit_success() {
		Account account = new Account("Id-2", BigDecimal.valueOf(500));

		accountsService.deposit(account, BigDecimal.valueOf(200));

		assertThat(account.getBalance()).isEqualByComparingTo("700");
	}
}