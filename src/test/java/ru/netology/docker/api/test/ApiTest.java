package ru.netology.docker.api.test;

import org.junit.jupiter.api.*;
import ru.netology.docker.api.helper.ApiHelper;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static ru.netology.docker.sql.data.DemoDataHelper.clearDatabase;

public class ApiTest {

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
        // Set up the API helper (includes generating token)
        ApiHelper.setUp();
    }

    // Method to clear and close the database after all tests
    @AfterAll
    public static void tearDown() {
        clearDatabase();
    }

    @Test
    void shouldTransferFromCard1ToCard2() {

        // Retrieve cards using ApiHelper
        List<Map<String, Object>> cards = ApiHelper.getCards();

        // Match card IDs to their balances
        for (Map<String, Object> card : cards) {
            String cardId = (String) card.get("id");
            int balance = (int) card.get("balance");

            if (cardId.equals(ApiHelper.card1Id)) {
                initialCard1Balance = balance;
            } else if (cardId.equals(ApiHelper.card2Id)) {
                initialCard2Balance = balance;
            }
        }

        // Transfer money from card 1 to card 2 using ApiHelper
        ApiHelper.transferMoney(ApiHelper.card1Number, ApiHelper.card2Number, amountToTransfer);

        // Retrieve updated card balances
        List<Map<String, Object>> updatedCards = ApiHelper.getCards();

        // Match card IDs to their balances
        for (Map<String, Object> card : updatedCards) {
            String cardId = (String) card.get("id");
            int balance = (int) card.get("balance");

            if (cardId.equals(ApiHelper.card1Id)) {
                finalCard1Balance = balance;
            } else if (cardId.equals(ApiHelper.card2Id)) {
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
