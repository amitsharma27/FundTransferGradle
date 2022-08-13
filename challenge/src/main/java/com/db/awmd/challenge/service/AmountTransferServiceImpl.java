package com.db.awmd.challenge.service;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.db.awmd.Ichallenge.service.AmountTransferService;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AmountTransferResponse;
import com.db.awmd.challenge.exception.AccountBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AmountTransferServiceImpl implements AmountTransferService {

	@Autowired
	AccountsRepository accountsRepository;

	@Autowired
	NotificationService notificationService;

	@Override
	// @Transactional(isolation = REPEATABLE_READ) //will use this annotation for getting updated amount
	public AmountTransferResponse fundTransfer(Long fromAccount, Long toAccount, BigDecimal amount) {
		log.info("Sending fund transfer ");
		try {
			if (fromAccount < toAccount) {
				synchronized (fromAccount) {
					synchronized (toAccount) {
						withdraw(amount, fromAccount);
						deposit(amount, toAccount);
					}
				}
			} else {
				synchronized (toAccount) {
					synchronized (fromAccount) {
						withdraw(amount, fromAccount);
						deposit(amount, toAccount);
					}
				}
			}

		} catch (Exception e) {
			log.error("Exception during Fund Transfer", e);
			throw new AccountBalanceException("Can not Transfer fund ,SomeThing Went Wrong");

		}
		AmountTransferResponse amountTransferResponse = new AmountTransferResponse();
		amountTransferResponse.setMessage("Success");
		amountTransferResponse.setStatusCode(200);
		getEmailNotification(toAccount, amount);// fire and forget ,service can work async
		return amountTransferResponse;
	}

	private void validateIsNegativeAmount(BigDecimal amount) {
		if (BigDecimal.ZERO.compareTo(amount) < 0)
			throw new AccountBalanceException("Incorrect Amount Entered");
	}

	public synchronized void withdraw(BigDecimal amount, Long fromAccountoRequest) {
		validateIsNegativeAmount(amount);
		Account fromAccount = accountsRepository.getAccount(fromAccountoRequest);
		if (BigDecimal.ZERO.compareTo(fromAccount.getBalance()) <= 0)
			throw new AccountBalanceException("insufficient Balance");
		fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
		accountsRepository.updateBalance(fromAccount.getAccountId(), fromAccount.getBalance());

	}

	public synchronized void deposit(BigDecimal amount, @NotNull Long toAccountNoRequest) {
		validateIsNegativeAmount(amount);
		Account toAccount = accountsRepository.getAccount(toAccountNoRequest);
		toAccount.setBalance(toAccount.getBalance().add(amount));
		accountsRepository.updateBalance(toAccount.getAccountId(), toAccount.getBalance());
	}

	@Async("asyncExecutor")
	public void getEmailNotification(@NotNull Long accountNO, @NotNull BigDecimal amount) {
		log.info("Sent Email Notification");

		notificationService.notifyAboutTransferAsync(accountNO, amount);

	}
}
