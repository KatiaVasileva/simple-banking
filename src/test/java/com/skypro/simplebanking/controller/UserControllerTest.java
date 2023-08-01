package com.skypro.simplebanking.controller;

import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.skypro.simplebanking.utility.Utilities.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void givenNoUsersInDatabase_whenCreateUser_thenUserIsSavedInDatabase() throws Exception {
        getAdminAuthentication(true);
        JSONObject jsonObject = createUserWithDefaultAccounts();

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").isNotEmpty())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.accounts").isNotEmpty())
                .andExpect(jsonPath("$.accounts.length()").value(3))
                .andExpect(jsonPath("$.accounts[0].id").value(1))
                .andExpect(jsonPath("$.accounts[1].id").value(2))
                .andExpect(jsonPath("$.accounts[2].id").value(3))
                .andExpect(jsonPath("$.accounts[0].currency").value("USD"))
                .andExpect(jsonPath("$.accounts[1].currency").value("EUR"))
                .andExpect(jsonPath("$.accounts[2].currency").value("RUB"))
                .andExpect(jsonPath("$.accounts[0].amount").value(1))
                .andExpect(jsonPath("$.accounts[1].amount").value(1))
                .andExpect(jsonPath("$.accounts[2].amount").value(1));
    }

    @Test
    public void givenNoUsersInDatabase_whenCreateUserByUser_thenForbidden() throws Exception {
        getAdminAuthentication(false);
        JSONObject jsonObject = createUserWithDefaultAccounts();

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenListOfUsers_whenCreateUserWithExistingName_thenBadRequest() throws Exception {
        getAdminAuthentication(true);
        JSONObject jsonObject = createUserWithDefaultAccounts();

        mockMvc.perform(post("/user/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()))
                .andExpect(status().isOk());

        JSONObject newJsonObject = createUserWithDefaultAccounts();

        mockMvc.perform(post("/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newJsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenListOfUsers_whenGetAllUsers_thenGetListOfUsers() throws Exception {
        createUserOneWithDefaultAccounts();
        createUserTwoWithDefaultAccounts();
        getAdminAuthentication(false);

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].username").value("user"))
                .andExpect(jsonPath("$[1].username").value("user_2"))
                .andExpect(jsonPath("$[0].accounts[0].accountId").value(1))
                .andExpect(jsonPath("$[0].accounts[1].accountId").value(2))
                .andExpect(jsonPath("$[0].accounts[2].accountId").value(3))
                .andExpect(jsonPath("$[1].accounts[0].accountId").value(4))
                .andExpect(jsonPath("$[1].accounts[1].accountId").value(5))
                .andExpect(jsonPath("$[1].accounts[2].accountId").value(6))
                .andExpect(jsonPath("$[0].accounts[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].accounts[1].currency").value("EUR"))
                .andExpect(jsonPath("$[0].accounts[2].currency").value("RUB"))
                .andExpect(jsonPath("$[1].accounts[0].currency").value("USD"))
                .andExpect(jsonPath("$[1].accounts[1].currency").value("EUR"))
                .andExpect(jsonPath("$[1].accounts[2].currency").value("RUB"));
    }

    @Test
    public void givenListOfUsers_whenGetAllUsersByAdmin_thenForbidden() throws Exception {
        createUserOneWithDefaultAccounts();
        getAdminAuthentication(true);

        mockMvc.perform(get("/user/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void givenUserInDatabase_whenGetUser_thenReceiveUserInfo() throws Exception {
        createUserOneWithDefaultAccounts();
        getAdminAuthentication(false);

        mockMvc.perform(get("/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.accounts.length()").value(3))
                .andExpect(jsonPath("$.accounts[0].amount").value(10_000L))
                .andExpect(jsonPath("$.accounts[0].currency").value("USD"))
                .andExpect(jsonPath("$.accounts[1].amount").value(10_000L))
                .andExpect(jsonPath("$.accounts[1].currency").value("EUR"))
                .andExpect(jsonPath("$.accounts[2].amount").value(10_000L))
                .andExpect(jsonPath("$.accounts[2].currency").value("RUB"));
    }

    @Test
    public void givenUserInDatabase_whenGetUserByAdmin_thenForbidden() throws Exception {
        createUserOneWithDefaultAccounts();
        getAdminAuthentication(true);

        mockMvc.perform(get("/user/me"))
                .andExpect(status().isForbidden());
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
