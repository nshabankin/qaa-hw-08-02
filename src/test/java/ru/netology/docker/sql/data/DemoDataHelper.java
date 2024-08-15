package ru.netology.docker.sql.data;

import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import ru.netology.docker.sql.util.SQLQueries;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DemoDataHelper {

    private DemoDataHelper() {
    }

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/app";
    private static final String DB_USER = "app";
    private static final String DB_PASS = "pass";

    // SQL Queries
    private static final String SELECT_USER_BY_LOGIN = SQLQueries.getQuery("select_user_by_login");
    private static final String SELECT_LATEST_AUTH_CODE_BY_USER_ID = SQLQueries.getQuery("select_latest_auth_code_by_user_id");
    private static final String SELECT_CARD_INFO_BY_USER_ID = SQLQueries.getQuery("select_card_info_by_user_id");


    private static final String CLEAR_CARD_TRANSACTIONS = SQLQueries.getQuery("clear_card_transactions");
    private static final String CLEAR_CARDS = SQLQueries.getQuery("clear_cards");
    private static final String CLEAR_AUTH_CODES = SQLQueries.getQuery("clear_auth_codes");
    private static final String CLEAR_USERS = SQLQueries.getQuery("clear_users");

    // Utility method to establish a database connection
    @SneakyThrows
    public static Connection getConnection() {
        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASS
        );
    }

    // Method to clear all tables in the database
    @SneakyThrows
    public static void clearDatabase() {
        try (Connection conn = getConnection()) {
            QueryRunner runner = new QueryRunner();
            runner.update(conn, CLEAR_CARD_TRANSACTIONS);
            runner.update(conn, CLEAR_CARDS);
            runner.update(conn, CLEAR_AUTH_CODES);
            runner.update(conn, CLEAR_USERS);
        }
    }

    // Hardcoded valid user AuthInfo class
    @Value
    public static class hardcodedAuthInfo {
        String hardcodedLogin;
        String hardcodedPassword;
    }

    // Method to retrieve the valid AuthInfo from demo data
    public static hardcodedAuthInfo getHardcodedAuthInfo() {
        // Hardcoded valid user credentials
        return new hardcodedAuthInfo(
                "vasya",
                "qwerty123"
        );
    }

    // AuthInfo class to represent user information
    @Value
    public static class AuthInfo {
        String id;
        String login;
        String password;
        String status;
    }

    /**
     * Fetches AuthInfo for an existing user with the specified login.
     *
     * @param login the login of the user
     * @return AuthInfo object containing user details
     */
    @SneakyThrows
    public static AuthInfo getAuthInfoFromDb(String login) {

        // Establish a connection to the database
        try (Connection connection = getConnection()) {

            // Create a QueryRunner
            QueryRunner runner = new QueryRunner();

            // Use MapHandler to get a map of column names to values
            Map<String, Object> result = runner.query(connection, SELECT_USER_BY_LOGIN, new MapHandler(), login);
            if (result != null) {
                return new AuthInfo(
                        (String) result.get("id"),
                        (String) result.get("login"),
                        (String) result.get("password"),
                        (String) result.get("status")
                );
            }
            return null;
        }
    }

    // AuthCode class to represent authentication code
    @Value
    public static class AuthCode {
        String id;
        String user_id;
        String code;
        java.sql.Timestamp created;
    }

    /**
     * Fetches the latest AuthCode for the specified user.
     *
     * @param userId the ID of the user
     * @return AuthCode object containing the latest auth code for the user
     */
    @SneakyThrows
    public static AuthCode getAuthCodeFromDb(String userId) {

        // Establish a connection to the database
        try (Connection connection = getConnection()) {

            // Create a QueryRunner
            QueryRunner runner = new QueryRunner();

            // Use MapHandler to get a map of column names to values
            Map<String, Object> result = runner.query(connection, SELECT_LATEST_AUTH_CODE_BY_USER_ID, new MapHandler(), userId);
            if (result != null) {
                return new AuthCode(
                        (String) result.get("id"),
                        (String) result.get("user_id"),
                        (String) result.get("code"),
                        (java.sql.Timestamp) result.get("created")
                );
            }
            return null;
        }
    }

    // CardIdsByUserId class that represents card IDs belonging to a user ID
    @Value
    public static class CardIdsByUserId {
        String card1Id;
        String card2Id;
    }

    /**
     * Fetches the card IDs for a user by their user_id.
     *
     * @param userId the ID of the user
     * @return CardIdsByUserId object containing card IDs for the user
     */
    @SneakyThrows
    public static CardIdsByUserId getCardIdsFromDb(String userId) {

        // Establish a connection to the database
        try (Connection connection = getConnection()) {

            // Create a QueryRunner
            QueryRunner runner = new QueryRunner();

            // Fetch the card numbers for the specified user_id
            List<Map<String, Object>> result = runner.query(connection, SELECT_CARD_INFO_BY_USER_ID, new MapListHandler(), userId);

            // Stream through the result and map to card numbers
            List<String> cardIds = result.stream()
                    .map(row -> (String) row.get("id"))
                    .collect(Collectors.toList());

            // Retrieve the first two cards or return empty strings if not present
            String card1 = cardIds.stream().findFirst().orElse("");
            String card2 = cardIds.stream().skip(1).findFirst().orElse("");

            return new CardIdsByUserId(card1, card2);
        }
    }

    // CardNumbersByUserId class that represents card numbers belonging to a user ID
    @Value
    public static class CardNumbersByUserId {
        String card1Number;
        String card2Number;
    }

    /**
     * Fetches the card numbers for a user by their user_id.
     *
     * @param userId the ID of the user
     * @return CardNumbersByUserId object containing card numbers for the user
     */
    @SneakyThrows
    public static CardNumbersByUserId getCardNumbersFromDb(String userId) {

        // Establish a connection to the database
        try (Connection connection = getConnection()) {

            // Create a QueryRunner
            QueryRunner runner = new QueryRunner();

            // Fetch the card numbers for the specified user_id
            List<Map<String, Object>> result = runner.query(connection, SELECT_CARD_INFO_BY_USER_ID, new MapListHandler(), userId);

            // Stream through the result and map to card numbers
            List<String> cardNumbers = result.stream()
                    .map(row -> (String) row.get("number"))
                    .collect(Collectors.toList());

            // Retrieve the first two cards or return empty strings if not present
            String card1Number = cardNumbers.stream().findFirst().orElse("");
            String card2Number = cardNumbers.stream().skip(1).findFirst().orElse("");

            return new CardNumbersByUserId(card1Number, card2Number);
        }
    }
}