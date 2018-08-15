package com.pfm;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.pfm.helpers.TestHelper;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AccountsScreenTest extends TestBase {

  // TODO take this value from System properties / gradle properties - it should be possible to provide this value from outside
  private static final String FRONTEND_URL
      = "http://personal-finance-manager.s3-website.us-east-2.amazonaws.com/accounts";

  private AccountsScreen accountsScreen;

  @BeforeClass
  void getElements() {
    webDriver.get(FRONTEND_URL);
    accountsScreen = new AccountsScreen(webDriver);
  }

  @Test
  public void shouldRemoveAllAccountsBeforeTest() throws InterruptedException {
    List<WebElement> optionsButtonList = accountsScreen.optionsButton();
    //when
    for (WebElement anOptionsButtonList : optionsButtonList) {
      anOptionsButtonList.click();
      accountsScreen.deleteButton();
      Thread.sleep(500);
    }
    optionsButtonList = accountsScreen.optionsButton();
    assertThat(optionsButtonList.size(), is(0));
  }

  @Test(dependsOnMethods = {"shouldRemoveAllAccountsBeforeTest"})
  public void shouldAddAccount() throws InterruptedException {
    //given
    Random random = new Random();
    long randomNumber = random.nextInt(1000);
    String[] expectedListOfDescription =
        {"bzwbk number: " + randomNumber, "mbank number: " + randomNumber,
            "alior number: " + randomNumber, "pko number: " + randomNumber,
            "ing number: " + randomNumber};
    BigDecimal[] expectedListOfBalance =
        {BigDecimal.valueOf(100.25).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(500.48).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(50.00).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(666.78).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(2.82).add(BigDecimal.valueOf(randomNumber))};
    List<String> resultListOfDescription;
    List<BigDecimal> resultListOfBalance;

    //when
    for (int i = 0; i < expectedListOfDescription.length; i++) {
      accountsScreen.addAccountButton();
      accountsScreen.addDescription(expectedListOfDescription[i]);
      accountsScreen.addBalance(expectedListOfBalance[i]);
      accountsScreen.saveOptionButton();
      Thread.sleep(500);
    }
    resultListOfDescription = accountsScreen.getDescription();
    resultListOfBalance = accountsScreen.getBalance();

    //then
    for (int i = 0; i < expectedListOfBalance.length; i++) {
      assertThat(resultListOfDescription.contains(expectedListOfDescription[i]),
          is(true));
      assertThat(resultListOfBalance.contains(expectedListOfBalance[i]), is(true));
    }
  }

  @Test(dependsOnMethods = {"shouldAddAccount"})
  public void shouldSortBalanceAscending() {
    //given
    List<BigDecimal> balanceAscending = accountsScreen.getBalance();
    balanceAscending.sort(BigDecimal::compareTo);
    List<BigDecimal> resultBalanceAscending;

    //when
    accountsScreen.balanceAscendingButton();
    resultBalanceAscending = accountsScreen.getBalance();

    //then
    assertThat(resultBalanceAscending, is(equalTo(balanceAscending)));
  }

  @Test(dependsOnMethods = {"shouldSortBalanceAscending"})
  public void shouldSortBalanceDescending() {
    //given
    List<BigDecimal> balanceDescending = accountsScreen.getBalance();
    balanceDescending.sort(Collections.reverseOrder());
    List<BigDecimal> resultBalanceDescending;

    //when
    accountsScreen.balanceDescendingButton();
    resultBalanceDescending = accountsScreen.getBalance();

    //then
    assertThat(resultBalanceDescending, is(equalTo(balanceDescending)));
  }


  @Test(dependsOnMethods = {"shouldSortBalanceDescending"})
  public void shouldSortDescriptionAscending() {
    //given
    List<String> descriptionAscending = accountsScreen.getDescription();
    descriptionAscending.sort(Collections.reverseOrder());
    List<String> resultDescriptionAscending;

    //when
    accountsScreen.descriptionAscendingButton();
    resultDescriptionAscending = accountsScreen.getDescription();

    //then
    assertThat(resultDescriptionAscending, is(equalTo(descriptionAscending)));
  }

  @Test(dependsOnMethods = {"shouldSortDescriptionAscending"})
  public void shouldSortDescriptionDescending() {
    //given
    List<String> descriptionDescending = accountsScreen.getDescription();
    descriptionDescending.sort(String::compareToIgnoreCase);
    List<String> resultDescriptionDescending;

    //when
    accountsScreen.descriptionDescendingButton();
    resultDescriptionDescending = accountsScreen.getDescription();

    //then
    assertThat(resultDescriptionDescending, is(equalTo(descriptionDescending)));
  }

  @Test(dependsOnMethods = {"shouldSortDescriptionDescending"})
  public void shouldDeleteAccount() throws InterruptedException {
    //given
    List<WebElement> optionsButtonList = accountsScreen.optionsButton();
    List<String> resultListOfDescription;
    List<BigDecimal> resultListOfBalance;
    List<String> deletedDescription = new ArrayList<>();
    List<BigDecimal> deletedBalance = new ArrayList<>();

    //when
    for (int i = 0; i < 2; i++) {
      deletedDescription.add(accountsScreen.getDescription().get(i));
      deletedBalance.add(accountsScreen.getBalance().get(i));
      optionsButtonList.get(i).click();
      accountsScreen.deleteButton();
      Thread.sleep(500);
    }
    resultListOfDescription = accountsScreen.getDescription();
    resultListOfBalance = accountsScreen.getBalance();

    //then
    for (int i = 0; i < deletedBalance.size(); i++) {
      assertThat(resultListOfDescription.contains(deletedDescription.get(i)), is(false));
      assertThat(resultListOfBalance.contains(deletedBalance.get(i)), is(false));
    }
  }

  @Test(dependsOnMethods = {"shouldDeleteAccount"})
  public void shouldUpdateAccount() throws InterruptedException {
    //given
    Random random = new Random();
    long randomNumber = random.nextInt(1000);
    List<WebElement> optionsButtonList = accountsScreen.optionsButton();
    String[] descriptionsList = {"aSantander number: " + randomNumber,
        "bMilenium number: " + randomNumber,
        "cPekao number: " + randomNumber};
    BigDecimal[] balanceList =
        {BigDecimal.valueOf(77.77).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(12.12).add(BigDecimal.valueOf(randomNumber)),
            BigDecimal.valueOf(0.45).add(BigDecimal.valueOf(randomNumber))};
    List<String> resultListOfDescription;
    List<BigDecimal> resultListOfBalance;

    //when
    for (int i = 0; i < 3; i++) {
      optionsButtonList.get(i).click();
      accountsScreen.editButton();
      Thread.sleep(500);
      accountsScreen.addDescription(descriptionsList[i]);
      accountsScreen.addBalance(balanceList[i]);
      accountsScreen.saveOptionButton();
      Thread.sleep(500);
    }

    webDriver.navigate().refresh();
    Thread.sleep(1000);
    resultListOfDescription = accountsScreen.getDescription();
    resultListOfBalance = accountsScreen.getBalance();
    System.out.println(resultListOfDescription);

    //then
    for (int i = 0; i < descriptionsList.length; i++) {
      assertThat(resultListOfDescription.contains(descriptionsList[i]), is(true));
      assertThat(resultListOfBalance.contains(balanceList[i]), is(true));
    }
  }

  @Test(dependsOnMethods = {"shouldUpdateAccount"})
  public void shouldRefreshPage() throws IOException, InterruptedException {
    //given
    BigDecimal sampleBalance = BigDecimal.valueOf(320.00);
    String sampleDescription = "ideaBank";
    List<String> resultListOfDescription;
    List<BigDecimal> resultListOfBalance;

    //when
    TestHelper.addSampleAccount();
    resultListOfDescription = accountsScreen.getDescription();
    resultListOfBalance = accountsScreen.getBalance();
    assertThat(resultListOfBalance.contains(sampleBalance), is(false));
    assertThat(resultListOfDescription.contains(sampleDescription), is(false));

    Thread.sleep(5000);
    accountsScreen.refreshButton();
    Thread.sleep(5000);

    resultListOfDescription = accountsScreen.getDescription();
    resultListOfBalance = accountsScreen.getBalance();

    //then
    assertThat(resultListOfBalance.contains(sampleBalance), is(true));
    assertThat(resultListOfDescription.contains(sampleDescription), is(true));
  }
}