package ru.netology.docker.api.helper;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import ru.netology.docker.sql.data.DemoDataHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApiHelper {

    private static final String baseUri = "http://localhost:9999/api";
    private static String token;  // Token will be stored here

    // Valid hardcoded AuthInfo from demo data
    static String hardcodedLogin = DemoDataHelper.getHardcodedAuthInfo().getHardcodedLogin();
    static String hardcodedPassword = DemoDataHelper.getHardcodedAuthInfo().getHardcodedPassword();

    // Get actual AuthInfo from demo data using hardcoded login
    static DemoDataHelper.AuthInfo user = DemoDataHelper.getAuthInfoFromDb(hardcodedLogin);

    // Get card IDs and numbers from demo data using user_id
    public static String card1Id = DemoDataHelper.getCardIdsFromDb(user.getId()).getCard1Id();
    public static String card2Id = DemoDataHelper.getCardIdsFromDb(user.getId()).getCard2Id();
    public static String card1Number = DemoDataHelper.getCardNumbersFromDb(user.getId()).getCard1Number();
    public static String card2Number = DemoDataHelper.getCardNumbersFromDb(user.getId()).getCard2Number();

    // Setup RestAssured default parser and generate token
    public static void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        generateToken();
    }

    // Method to generate the token by logging in and verifying with auth code
    private static void generateToken() {
        // Login request body
        Map<String, Object> loginRequestBody = new HashMap<>();
        loginRequestBody.put("login", hardcodedLogin);
        loginRequestBody.put("password", hardcodedPassword);

        // Login request
        given()
                .contentType("application/json")
                .body(loginRequestBody)
                .post(baseUri + "/auth")
                .then()
                .statusCode(200);  // Asserting within RestAssured

        // Get valid auth code by user_id
        DemoDataHelper.AuthCode authCode = DemoDataHelper.getAuthCodeFromDb(user.getId());
        assertNotNull(authCode, "Auth code should not be null");

        // Verification request body
        Map<String, Object> verificationRequestBody = new HashMap<>();
        verificationRequestBody.put("login", hardcodedLogin);
        verificationRequestBody.put("code", authCode.getCode());

        // Verification request and response
        Response verificationResponse = given()
                .contentType("application/json")
                .body(verificationRequestBody)
                .post(baseUri + "/auth/verification")
                .then()
                .statusCode(200)  // Asserting within RestAssured
                .extract()
                .response();

        // Extract and store the token
        token = verificationResponse.path("token");
    }

    // Method to retrieve user's cards
    public static List<Map<String, Object>> getCards() {

        // Request headers containing token
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Request and response to retrieve cards
        Response response = given()
                .contentType("application/json")
                .headers(headers)
                .get(baseUri + "/cards")
                .then()
                .statusCode(200)  // Asserting within RestAssured
                .extract()
                .response();

        // Retrieve a list with CardInfo
        return response.jsonPath().getList("$");
    }

    // Method to transfer money between cards
    public static void transferMoney(String fromCard, String toCard, int amount) {

        // Transfer request body
        Map<String, Object> transferRequestBody = new HashMap<>();
        transferRequestBody.put("from", fromCard);
        transferRequestBody.put("to", toCard);
        transferRequestBody.put("amount", amount);

        // Transfer request headers containing token
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        // Transfer request
        given()
                .contentType("application/json")
                .headers(headers)
                .body(transferRequestBody)
                .post(baseUri + "/transfer")
                .then()
                .statusCode(200);  // Asserting within RestAssured
    }
}

