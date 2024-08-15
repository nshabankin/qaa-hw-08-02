package ru.netology.docker.api.test;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import ru.netology.docker.sql.data.DemoDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.netology.docker.sql.data.DemoDataHelper.clearDatabase;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {

    private static final String base_uri = "http://localhost:9999/api";
    private static String token;  // Token will be stored here

    // Valid hardcoded AuthInfo from demo data
    static String hardcodedLogin = DemoDataHelper.getHardcodedAuthInfo().getHardcodedLogin();
    static String hardcodedPassword = DemoDataHelper.getHardcodedAuthInfo().getHardcodedPassword();

    // Get actual AuthInfo from demo data using hardcoded login
    static DemoDataHelper.AuthInfo user = DemoDataHelper.getAuthInfoFromDb(hardcodedLogin);

    // Get card IDs from demo data using user_id
    String card1Id = DemoDataHelper.getCardIdsFromDb(user.getId()).getCard1Id();
    String card2Id = DemoDataHelper.getCardIdsFromDb(user.getId()).getCard2Id();

    // Get card numbers from demo data using user_id
    String card1Number = DemoDataHelper.getCardNumbersFromDb(user.getId()).getCard1Number();
    String card2Number = DemoDataHelper.getCardNumbersFromDb(user.getId()).getCard2Number();

    // Variables to hold initial card balances
    static int initialCard1Balance;
    static int initialCard2Balance;

    // Variables to hold final card balances
    int finalCard1Balance;
    int finalCard2Balance;

    // Amount to transfer
    int amountToTransfer = 5000;

    @BeforeAll
    static void setUp() {
        // Set default parser to JSON
        RestAssured.defaultParser = Parser.JSON;

        // Ensure token is generated before each test if it's not already set
        generateToken();
    }

    // Method to clear and close the database after all tests
    @AfterAll
    public static void tearDown() {
        clearDatabase();
    }

    // Method to generate the token by logging in and verifying with auth code with assertions
    static void generateToken() {

        // Login
        Map<String, Object> loginRequestBody = new HashMap<>();
        loginRequestBody.put("login", hardcodedLogin);
        loginRequestBody.put("password", hardcodedPassword);

        Response loginResponse = RestAssured.given()
                .contentType("application/json")
                .body(loginRequestBody)
                .post(base_uri + "/auth");

        assertThat(loginResponse.statusCode(), equalTo(200));

        // Verify with auth code
        DemoDataHelper.AuthCode authCode = DemoDataHelper.getAuthCodeFromDb(user.getId());
        assertNotNull(authCode, "Auth code should not be null");

        Map<String, Object> verificationRequestBody = new HashMap<>();
        verificationRequestBody.put("login", hardcodedLogin);
        verificationRequestBody.put("code", authCode.getCode());

        Response verificationResponse = RestAssured.given()
                .contentType("application/json")
                .body(verificationRequestBody)
                .post(base_uri + "/auth/verification");

        // Extract and store the token
        token = verificationResponse.path("token");
        assertThat(verificationResponse.statusCode(), equalTo(200));
    }

    @Test
    @Order(1)
    void shouldGetCards() {

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send GET request to /cards endpoint to retrieve user's cards
        Response response = RestAssured.given()
                .contentType("application/json")
                .headers(headers)
                .get(base_uri + "/cards");

        // Assert that the status code is 200 (indicating successful retrieval of cards)
        assertThat(response.statusCode(), equalTo(200));

        // Extract card balances from the response
        List<Map<String, Object>> cards = response.jsonPath().getList("$");

        // Match card IDs to their balances
        for (Map<String, Object> card : cards) {
            String cardId = (String) card.get("id");
            int balance = (int) card.get("balance");

            if (cardId.equals(card1Id)) {
                initialCard1Balance = balance;
            } else if (cardId.equals(card2Id)) {
                initialCard2Balance = balance;
            }
        }
    }

    @Test
    @Order(2)
    void shouldTransferFromCard1ToCard2() {

        // Create the transfer request body with appropriate details
        Map<String, Object> transferRequestBody = new HashMap<>();
        transferRequestBody.put("from", card1Number);
        transferRequestBody.put("to", card2Number);
        transferRequestBody.put("amount", amountToTransfer);

        // Prepare headers with the token obtained from the previous steps
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Send POST request to /transfer endpoint to initiate the money transfer
        Response transferResponse = RestAssured.given()
                .contentType("application/json")
                .headers(headers)
                .body(transferRequestBody)
                .post(base_uri + "/transfer");

        // Assert that the status code is 200 (indicating successful transfer)
        assertThat(transferResponse.statusCode(), equalTo(200));

        // Get card balances after the transfer
        Response cardsResponse = RestAssured.given()
                .contentType("application/json")
                .headers(headers)
                .get(base_uri + "/cards");

        // Extract card balances from the response
        List<Map<String, Object>> updatedCards = cardsResponse.jsonPath().getList("$");

        // Match card IDs to their balances
        for (Map<String, Object> card : updatedCards) {
            String cardId = (String) card.get("id");
            int balance = (int) card.get("balance");

            if (cardId.equals(card1Id)) {
                finalCard1Balance = balance;
            } else if (cardId.equals(card2Id)) {
                finalCard2Balance = balance;
            }
        }

        // Assert the amounts of the cards after the transfer
        assertThat("Card 1 balance should decrease by the transferred amount",
                finalCard1Balance, equalTo(initialCard1Balance - amountToTransfer));
        assertThat("Card 2 balance should increase by the transferred amount",
                finalCard2Balance, equalTo(initialCard2Balance + amountToTransfer));
    }
}