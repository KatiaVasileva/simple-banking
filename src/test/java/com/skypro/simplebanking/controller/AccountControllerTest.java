package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.skypro.simplebanking.utility.Utilities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void prepareTestDataWithAuthentication() {
        getAdminAuthentication(false);
        createUserWithAccount();
    }

    @Test
    public void givenUserAccountInDatabase_whenGetUserAccountIsCalled_thenAccountIsFound() throws Exception {

        mockMvc.perform(get("/account/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(10_000))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    public void givenUserAccountInDatabase_whenGetUserAccountIsCalledByAnotherUser_thenNotFound() throws Exception {
        getUserTwoAuthentication();

        mockMvc.perform(get("/account/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUserAccountInDatabase_whenGetUserAccountIsCalledByAdmin_thenForbidden() throws Exception {
        getAdminAuthentication(true);

        mockMvc.perform(get("/account/{id}", 1))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenNoUserAccount_whenGetUserAccountIsCalled_thenAccountIsNotFound() throws Exception{

        mockMvc.perform(get("/account/{id}", 2))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUserAccountInDatabase_whenDepositToAccount_thenBalanceIsChanged() throws Exception {
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/deposit/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(15_000))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    public void givenUserAccountInDatabase_whenDepositToAccountByAnotherUser_thenNotFound() throws Exception {
        getUserTwoAuthentication();
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/deposit/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUserAccountInDatabase_whenDepositToAccountByAdmin_thenForbidden() throws Exception {
        getAdminAuthentication(true);
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/deposit/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenUserAccountInDatabase_whenDepositWrongAmountToAccount_thenBadRequest() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", -5_000L);

        mockMvc.perform(post("/account/deposit/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Amount should be more than 0"));
    }

    @Test
    public void givenNoUserAccountInDatabase_whenDepositToAccount_thenNotFound() throws Exception {
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/deposit/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUserAccountInDatabase_whenWithdrawFromAccount_thenBalanceIsChanged() throws Exception {
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/withdraw/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(5_000))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    public void givenUserAccountInDatabase_whenWithdrawFromAccountByAnotherUser_thenNotFound() throws Exception {
        getUserTwoAuthentication();
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/withdraw/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void givenUserAccountInDatabase_whenWithdrawFromAccountByAdmin_thenForbidden() throws Exception {
        getAdminAuthentication(true);
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/withdraw/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenUserAccountInDatabase_whenWithdrawWrongAmountFromAccount_thenBadRequest() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", -5_000L);

        mockMvc.perform(post("/account/withdraw/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Amount should be more than 0"));
    }

    @Test
    public void givenUserAccountInDatabase_whenWithdrawBiggerAmountFromAccount_thenBadRequest() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 15_000L);

        mockMvc.perform(post("/account/withdraw/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Cannot withdraw 15000 USD"));
    }

    @Test
    public void givenNoUserAccountInDatabase_whenWithdrawFromAccount_thenNotFound() throws Exception {
        JSONObject jsonObject = createBalanceChangeRequest();

        mockMvc.perform(post("/account/withdraw/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    //Test Data Preparation
    public void createUserWithAccount() {
        User user = new User();
        user.setUsername("test_username_1");
        user.setPassword(passwordEncoder.encode("test_password_1"));
        userRepository.save(user);
        Account account = new Account();
        account.setAccountCurrency(AccountCurrency.USD);
        account.setAmount(10_000L);
        account.setUser(user);
        accountRepository.save(account);
    }
}
