package com.pfm.type;

import static com.pfm.helpers.TestAccountTypeProvider.accountTypeInvestment;
import static com.pfm.helpers.TestUsersProvider.userZdzislaw;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.pfm.account.type.AccountType;
import com.pfm.account.type.AccountTypeController;
import com.pfm.account.type.AccountTypeService;
import com.pfm.auth.UserProvider;
import com.pfm.helpers.IntegrationTestsBase;
import com.pfm.history.HistoryEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

class AccountTypeControllerTransactionalTest extends IntegrationTestsBase {

  @SpyBean
  private HistoryEntryService historyEntryService;

  @MockBean
  private UserProvider userProvider;

  @SpyBean
  private AccountTypeService accountTypeService;

  @Autowired
  private AccountTypeController accountTypeController;

  @Override
  @BeforeEach
  public void before() {
    super.before();
    userId = userService.registerUser(userZdzislaw()).getId();
    when(userProvider.getCurrentUserId()).thenReturn(userId);
  }

  @Test
  void shouldRollbackTransactionWhenAccountTypeAddFailed() {
    // given
    AccountType accountType = accountTypeInvestment();
    doThrow(IllegalStateException.class).when(historyEntryService).addHistoryEntryOnAdd(any(Object.class), any(Long.class));

    // when
    assertThrows(IllegalStateException.class, () -> {
      accountTypeController.addAccountType(convertAccountTypeToAccountTypeRequest(accountType));
    });

    // then
    assertThat(accountTypeService.getAccountTypes(userId), hasSize(0));
  }

  @Test
  void shouldRollbackTransactionWhenAccountTypeDeleteFailed() {
    // given
    AccountType accountType = accountTypeInvestment();
    final Long accountTypeId = accountTypeService.saveAccountType(userId, accountType).getId();

    doThrow(IllegalStateException.class).when(historyEntryService).addHistoryEntryOnDelete(any(Object.class), any(Long.class));
    doThrow(IllegalStateException.class).when(accountTypeService).deleteAccountType(accountTypeId);

    // when
    assertThrows(IllegalStateException.class, () -> accountTypeController.deleteAccountType(accountTypeId));

    // then
    assertThat(historyEntryService.getHistoryEntries(userId), hasSize(0));
  }

  @Test
  void shouldRollbackTransactionWhenAccountTypeUpdateFailed() {
    // given
    AccountType accountType = accountTypeInvestment();
    final Long accountTypeId = accountTypeService.saveAccountType(userId, accountType).getId();

    AccountType updatedAccountType = accountTypeInvestment();
    updatedAccountType.setName("updatedAccountTypeName");

    doThrow(IllegalStateException.class).when(accountTypeService).updateAccountType(any(Long.class), any(Long.class), any(AccountType.class));

    // when
    try {
      accountTypeController.updateAccountType(accountTypeId, convertAccountTypeToAccountTypeRequest(updatedAccountType));
      fail();
    } catch (IllegalStateException ex) {
      assertNotNull(ex);
    }

    // then
    assertThat(historyEntryService.getHistoryEntries(userId), hasSize(0));

  }
}
