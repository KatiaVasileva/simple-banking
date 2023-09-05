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
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void prepareTestDataWithUserAuthentication() {
        getAdminAuthentication(false);
        createUserOneWithDefaultAccounts();
        createUserTwoWithDefaultAccounts();
    }

    @Test
    public void givenUserAccounts_whenTransfer_thenTransferFromOneAccountToAnother() throws Exception {
        JSONObject jsonObject = createTransferRequest();

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/account/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(5_000))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    public void givenUserAccounts_whenTransferByAnotherUser_thenNotFound() throws Exception {
        getUserTwoAuthentication();
        JSONObject jsonObject = createTransferRequest();
        whenTransfer_thenNotFound(jsonObject);
    }

    @Test
    public void givenUserAccounts_whenTransferByAdmin_thenForbidden() throws Exception {
        getAdminAuthentication(true);
        JSONObject jsonObject = createTransferRequest();

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenUserAccounts_whenTransferWrongCurrency_thenBadRequest() throws Exception {
        JSONObject jsonObject = createTransferRequest();
        jsonObject.put("toUserId", 2);
        jsonObject.put("toAccountId", 5);

        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenUserAccounts_whenTransferFromOrToWrongAccount_thenNotFound() throws Exception {
        JSONObject jsonObject1 = createTransferRequest();
        jsonObject1.put("fromAccountId", 13);
        whenTransfer_thenNotFound(jsonObject1);

        JSONObject jsonObject2 = createTransferRequest();
        jsonObject2.put("toAccountId", 13);
        whenTransfer_thenNotFound(jsonObject2);
    }

    public void whenTransfer_thenNotFound(JSONObject jsonObject) throws Exception {
        mockMvc.perform(post("/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isNotFound());
    }

    //Test Data Preparation
    public void createUserOneWithDefaultAccounts() {
        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);
        createDefaultAccounts(user);
    }

    public void createUserTwoWithDefaultAccounts() {
        User user = new User();
        user.setUsername("user_2");
        user.setPassword(passwordEncoder.encode("password_2"));
        userRepository.save(user);
        createDefaultAccounts(user);
    }

    public void createDefaultAccounts(User user) {
        Account account1 = new Account();
        account1.setAccountCurrency(AccountCurrency.USD);
        account1.setAmount(10_000L);
        account1.setUser(user);
        accountRepository.save(account1);
        Account account2 = new Account();
        account2.setAccountCurrency(AccountCurrency.EUR);
        account2.setAmount(10_000L);
        account2.setUser(user);
        accountRepository.save(account2);
        Account account3 = new Account();
        account3.setAccountCurrency(AccountCurrency.RUB);
        account3.setAmount(10_000L);
        account3.setUser(user);
        accountRepository.save(account3);
    }
}
