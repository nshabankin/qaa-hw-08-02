package ru.netology.docker.sql.data;

import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import ru.netology.docker.sql.util.SQLQueries;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

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

    // Valid user AuthInfo class
    @Value
    public static class hardcodedAuthInfo {
        String validLogin;
        String validPassword;
        String invalidPassword;
        //String token;
        String card1Number;
        String card2Number;
        String amountInKopecks;
    }

    // Method to retrieve the valid AuthInfo
    public static hardcodedAuthInfo getValidAuthInfo() {
        // Hardcoded valid user credentials
        return new hardcodedAuthInfo(
                "petya",
                "123qwerty",
                "wrongpassword",
                //"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJsb2dpbiI6InBldHlhIn0.TotWpKiZWvp_H67GvgakR-wdIfBVpLb5eqbb246_CKo",
                "5559 0000 0000 0002",
                "5559 0000 0000 0008",
                "5000"
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
}