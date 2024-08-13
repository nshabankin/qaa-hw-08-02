package ru.netology.docker.api.requests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RequestBuilder {

    private static final String BASE_URL = "http://localhost:9999/api";

    public static Response sendPostRequest(String endpoint, Map<String, String> body, Map<String, String> headers) {
        // Build the request specification
        RequestSpecification request = RestAssured.given().contentType("application/json").body(body);

        // Add headers if they are not null
        if (headers != null) {
            request.headers(headers);
        }

        // Send the POST request to the specified endpoint
        return request.post(BASE_URL + endpoint);
    }

    public static Response sendGetRequest(String endpoint, Map<String, String> headers) {
        // Build the request specification
        RequestSpecification request = RestAssured.given().contentType("application/json");

        // Add headers if they are not null
        if (headers != null) {
            request.headers(headers);
        }

        // Send the GET request to the specified endpoint
        return request.get(BASE_URL + endpoint);
    }
}
