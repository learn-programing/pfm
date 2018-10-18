package com.pfm.transaction;

import com.pfm.account.Account;
import com.pfm.account.AccountService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class TransactionService {

  private TransactionRepository transactionRepository;
  private AccountService accountService;

  public Optional<Transaction> getTransactionByIdAndUserId(long id, long userId) {
    return transactionRepository.findByIdAndUserId(id, userId);
  }

  public List<Transaction> getTransactions(long userId) {
    return StreamSupport.stream(transactionRepository.findByUserId(userId).spliterator(), false)
        .sorted(Comparator.comparing(Transaction::getId))
        .collect(Collectors.toList());
  }

  public Transaction addTransaction(long userId, Transaction transaction) {
    for (AccountPriceEntry entry : transaction.getAccountPriceEntries()) {
      addAmountToAccount(entry.getAccountId(), userId, entry.getPrice());
    }
    // TODO - did you enabled transactions? account state should be not changed when transaction save is failing!!
    return transactionRepository.save(transaction);
  }

  public void updateTransaction(long id, long userId, Transaction transaction) {
    Transaction transactionToUpdate = getTransactionFromDatabase(id, userId);

    // subtract old value
    for (AccountPriceEntry entry : transactionToUpdate.getAccountPriceEntries()) {
      subtractAmountFromAccount(entry.getAccountId(), userId, entry.getPrice());
    }

    // add new value
    for (AccountPriceEntry entry : transaction.getAccountPriceEntries()) {
      addAmountToAccount(entry.getAccountId(), userId, entry.getPrice());
    }

    transactionToUpdate.setDescription(transaction.getDescription());
    transactionToUpdate.setCategoryId(transaction.getCategoryId());
    transactionToUpdate.setAccountPriceEntries(transaction.getAccountPriceEntries());
    transactionToUpdate.setDate(transaction.getDate());

    transactionRepository.save(transactionToUpdate);

    // TODO - did you enabled transactions? account state should be not changed when transaction save is failing!!

  }

  public void deleteTransaction(long id, long userId) {
    Transaction transactionToDelete = getTransactionFromDatabase(id, userId);
    transactionRepository.deleteById(id);

    // TODO - did you enabled transactions? account state should be not changed when transaction save is failing!!
    for (AccountPriceEntry entry : transactionToDelete.getAccountPriceEntries()) {
      subtractAmountFromAccount(entry.getAccountId(), userId, entry.getPrice());
    }
  }

  private Transaction getTransactionFromDatabase(long id, long userId) {
    Optional<Transaction> transactionFromDb = getTransactionByIdAndUserId(id, userId);

    if (!transactionFromDb.isPresent()) {
      throw new IllegalStateException("Transaction with id: " + id + " does not exist in database");
    }

    return transactionFromDb.get();
  }

  private void subtractAmountFromAccount(long accountId, long userId, BigDecimal amountToAdd) {
    updateAccountBalance(accountId, userId, amountToAdd, BigDecimal::subtract);
  }

  private void addAmountToAccount(long accountId, long userId, BigDecimal amountToSubtract) {
    updateAccountBalance(accountId, userId, amountToSubtract, BigDecimal::add);
  }

  private void updateAccountBalance(long accountId, long userId, BigDecimal amount, BiFunction<BigDecimal, BigDecimal, BigDecimal> operation) {
    Optional<Account> account = accountService.getAccountByIdAndUserId(accountId, userId);

    if (!account.isPresent()) {
      throw new IllegalStateException("Account with id: " + accountId + " does not exist in database");
    }

    Account accountToUpdate = account.get();
    // TODO maybe you can write query which updates only balance? that's common operation so does not make sense to update other values
    // I can
    accountToUpdate.setBalance(operation.apply(accountToUpdate.getBalance(), amount));

    accountService.updateAccount(accountToUpdate.getId(), userId, accountToUpdate);
  }

}
