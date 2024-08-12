package ru.netology.docker.api.test;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.docker.api.request.RequestHelper;
import ru.netology.docker.sql.data.DemoDataHelper;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiTest {

    private String token;
    private DemoDataHelper.ValidAuthInfo validAuthInfo;
    private DemoDataHelper.AuthCode authCode;

    @BeforeEach
    void setUp() {
        // Set default parser to JSON
        RestAssured.defaultParser = Parser.JSON;

        // Retrieve valid authentication info (login and password)
        validAuthInfo = DemoDataHelper.getValidAuthInfo();

        // Retrieve authentication code for the user from the database
        DemoDataHelper.AuthInfo authInfoFromDb = DemoDataHelper.getAuthInfoFromDb(validAuthInfo.getValidLogin());
        authCode = DemoDataHelper.getAuthCodeFromDb(authInfoFromDb.getId());
        assertNotNull(authCode, "Auth code should not be null");

        // Hardcode token
        token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6InBldHlhIn0.TotWpKiZWvp_H67GvgakR-wdIfBVpLb5eqbb246_CKo";
    }

    @Test
    void testLogin() {

        // Create the login request body using valid login and password
        Map<String, String> loginRequestBody = new HashMap<>();
        loginRequestBody.put("login", validAuthInfo.getValidLogin());
        loginRequestBody.put("password", validAuthInfo.getValidPassword());

        // Send POST request to /auth endpoint to authenticate the user
        Response response = RequestHelper.sendPostRequest("/auth", loginRequestBody, null);

        // Assert that the status is 200 (indicating successful login)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    void testVerification() {

        // Create the verification request body using valid login and retrieved verification code
        Map<String, String> verificationRequestBody = new HashMap<>();
        verificationRequestBody.put("login", validAuthInfo.getValidLogin());
        verificationRequestBody.put("code", authCode.getCode());

        // Send POST request to /auth/verification endpoint to verify the code
        Response response = RequestHelper.sendPostRequest("/auth/verification", verificationRequestBody, null);

        // Extract the new token from the response
        token = response.path("token");

        // Assert that the status code is 200 (indicating successful verification)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    void testGetCards() {

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send GET request to /cards endpoint to retrieve user's cards
        Response response = RequestHelper.sendGetRequest("/cards", headers);

        // Assert that the status code is 200 (indicating successful retrieval of cards)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    void testTransfer() {

        // Create the transfer request body with appropriate details
        Map<String, String> transferRequestBody = new HashMap<>();
        transferRequestBody.put("from", "5559 0000 0000 0002");
        transferRequestBody.put("to", "5559 0000 0000 0008");
        transferRequestBody.put("amount", "5000");

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send POST request to /transfer endpoint to initiate the money transfer
        Response response = RequestHelper.sendPostRequest("/transfer", transferRequestBody, headers);

        // Assert that the status code is 200 (indicating successful transfer)
        assertThat(response.statusCode(), equalTo(200));
    }
}