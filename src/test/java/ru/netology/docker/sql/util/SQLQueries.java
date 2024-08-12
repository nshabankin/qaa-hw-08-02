package ru.netology.docker.sql.util;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class SQLQueries {
    private static final Map<String, String> sqlQueries = new HashMap<>();

    // Load SQL queries from file
    static {
        loadSqlQueries();
    }

    private SQLQueries() {
    }

    @SneakyThrows
    private static void loadSqlQueries() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/test/resources/requests.sql"))) {
            String line;
            StringBuilder query = new StringBuilder();
            String key = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("--")) { // Identify the query key by comments in the file
                    if (key != null) {
                        sqlQueries.put(key, query.toString().trim());
                        query.setLength(0); // Reset the query builder for the next query
                    }
                    key = line.substring(2).trim(); // Extract the key from the comment line
                } else {
                    query.append(line).append(" "); // Append query lines to the query builder
                }
            }
            if (key != null) {
                sqlQueries.put(key, query.toString().trim()); // Add the last query to the map
            }
        }
    }

    // Retrieve the SQL query by key
    public static String getQuery(String key) {
        return sqlQueries.get(key);
    }
}
