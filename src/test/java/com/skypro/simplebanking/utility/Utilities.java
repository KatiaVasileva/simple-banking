package com.skypro.simplebanking.utility;

import com.skypro.simplebanking.dto.BankingUserDetails;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Utilities {

    public static void getAdminAuthentication(boolean isAdmin) {
        BankingUserDetails bankingUserDetails;
        if (isAdmin) {
            bankingUserDetails = new BankingUserDetails(-1L, "admin", "****", true);
        } else {
            bankingUserDetails = new BankingUserDetails(1L, "user", "password", false);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(bankingUserDetails, null, bankingUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void getUserTwoAuthentication() {
        BankingUserDetails bankingUserDetails = new BankingUserDetails(2L, "user_2", "password_2", false);
        Authentication authentication = new UsernamePasswordAuthenticationToken(bankingUserDetails, null, bankingUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static JSONObject createUserWithDefaultAccounts() throws JSONException{
        JSONObject account1 = new JSONObject();
        account1.put("id", "1");
        account1.put("amount", "1");
        account1.put("currency", "USD");

        JSONObject account2 = new JSONObject();
        account2.put("id", "2");
        account2.put("amount", "1");
        account2.put("currency", "EUR");

        JSONObject account3 = new JSONObject();
        account3.put("id", "3");
        account3.put("amount", "1");
        account3.put("currency", "RUB");

        JSONArray accounts = new JSONArray();
        accounts.put(account1);
        accounts.put(account2);
        accounts.put(account3);

        JSONObject user = new JSONObject();
        user.put("id", "1");
        user.put("username", "username");
        user.put("password", "password");
        user.put("accounts", accounts);

        return user;
    }

    public static JSONObject createBalanceChangeRequest() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", 5_000L);
        return jsonObject;
    }

    public static JSONObject createTransferRequest() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fromAccountId", 1);
        jsonObject.put("toUserId", 2);
        jsonObject.put("toAccountId", 4);
        jsonObject.put("amount", 5_000L);
        return jsonObject;
    }
}
