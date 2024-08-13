package ru.netology.docker.api.test;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.netology.docker.api.requests.RequestBuilder;
import ru.netology.docker.sql.data.DemoDataHelper;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.netology.docker.sql.data.DemoDataHelper.clearDatabase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    private static String token;  // Token will be stored here

    // Valid hardcoded AuthInfo from demo data
    String validLogin = DemoDataHelper.getValidAuthInfo().getValidLogin();
    String validPassword = DemoDataHelper.getValidAuthInfo().getValidPassword();
    //String token = DemoDataHelper.getValidAuthInfo().getToken();
    String card1Number = DemoDataHelper.getValidAuthInfo().getCard1Number();
    String card2Number = DemoDataHelper.getValidAuthInfo().getCard2Number();
    String amountInKopecks = DemoDataHelper.getValidAuthInfo().getAmountInKopecks();

    // Get actual AuthInfo from demo data using hardcoded login
    DemoDataHelper.AuthInfo user = DemoDataHelper.getAuthInfoFromDb(validLogin);

    @BeforeEach
    void setUp() {
        // Set default parser to JSON
        RestAssured.defaultParser = Parser.JSON;

        // Ensure token is generated before each test if it's not already set
        if (token == null) {
            generateToken();
        }
    }

    // Method to clear and close the database after all tests
    @AfterAll
    public static void tearDown() {
        clearDatabase();
    }

    // Method to generate the token by logging in and verifying auth code
    void generateToken() {
        // Login
        Map<String, String> loginRequestBody = new HashMap<>();
        loginRequestBody.put("login", validLogin);
        loginRequestBody.put("password", validPassword);
        Response loginResponse = RequestBuilder.sendPostRequest("/auth", loginRequestBody, null);
        assertThat(loginResponse.statusCode(), equalTo(200));

        // Verify with auth code
        DemoDataHelper.AuthCode authCode = DemoDataHelper.getAuthCodeFromDb(user.getId());
        assertNotNull(authCode, "Auth code should not be null");

        Map<String, String> verificationRequestBody = new HashMap<>();
        verificationRequestBody.put("login", validLogin);
        verificationRequestBody.put("code", authCode.getCode());
        Response verificationResponse = RequestBuilder.sendPostRequest("/auth/verification", verificationRequestBody, null);

        // Extract and store the token
        token = verificationResponse.path("token");
        assertThat(verificationResponse.statusCode(), equalTo(200));
    }

    @Test
    @Order(1)
    void shouldLogin() {

        // Create the login request body using valid login and password
        Map<String, String> loginRequestBody = new HashMap<>();
        loginRequestBody.put("login", validLogin);
        loginRequestBody.put("password", validPassword);

        // Send POST request to /auth endpoint to authenticate the user
        Response response = RequestBuilder.sendPostRequest("/auth", loginRequestBody, null);

        // Assert that the status is 200 (indicating successful login)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    @Order(2)
    void shouldVerifyWithAuthCode() {

        // Retrieve authentication code for the user from the database
        DemoDataHelper.AuthCode authCode = DemoDataHelper.getAuthCodeFromDb(user.getId());
        assertNotNull(authCode, "Auth code should not be null");

        // Create the verification request body using valid login and retrieved verification code
        Map<String, String> verificationRequestBody = new HashMap<>();
        verificationRequestBody.put("login", validLogin);
        verificationRequestBody.put("code", authCode.getCode());

        // Send POST request to /auth/verification endpoint to verify the code
        Response response = RequestBuilder.sendPostRequest("/auth/verification", verificationRequestBody, null);

        // Assert that the status code is 200 (indicating successful verification)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    @Order(3)
    void shouldGetCards() {

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send GET request to /cards endpoint to retrieve user's cards
        Response response = RequestBuilder.sendGetRequest("/cards", headers);

        // Assert that the status code is 200 (indicating successful retrieval of cards)
        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    @Order(4)
    void shouldTransferFromCard1ToCard2() {

        // Create the transfer request body with appropriate details
        Map<String, String> transferRequestBody = new HashMap<>();
        transferRequestBody.put("from", card1Number);
        transferRequestBody.put("to", card2Number);
        transferRequestBody.put("amount", amountInKopecks);

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send POST request to /transfer endpoint to initiate the money transfer
        Response response = RequestBuilder.sendPostRequest("/transfer", transferRequestBody, headers);

        // Assert that the status code is 200 (indicating successful transfer)
        assertThat(response.statusCode(), equalTo(200));
    }
}