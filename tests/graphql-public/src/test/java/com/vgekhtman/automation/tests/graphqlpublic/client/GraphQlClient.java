package com.vgekhtman.automation.tests.graphqlpublic.client;

import com.vgekhtman.automation.testsupport.AllureEvidence;
import com.vgekhtman.automation.testsupport.AllureHttpFilter;
import com.vgekhtman.automation.testsupport.RestAssuredLogging;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/** Posts raw GraphQL query strings to the target endpoint - no GraphQL client library, just HTTP + JSON. */
public final class GraphQlClient {

    static {
        RestAssured.baseURI = env("GRAPHQL_PUBLIC_BASE_URL", "https://rickandmortyapi.com/graphql");
        // Every request/response pair is attached to the Allure report.
        RestAssured.filters(new AllureHttpFilter());
        // .log() output goes through Logback instead of stdout.
        RestAssured.config = RestAssuredLogging.config();
    }

    private GraphQlClient() {
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static Response query(String query) {
        return query(query, Map.of());
    }

    public static Response query(String query, Map<String, Object> variables) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        body.put("variables", variables);

        // The raw query is unreadable inside the JSON request body (escaped newlines), so it is attached as is.
        AllureEvidence.text("GraphQL query", query);
        if (!variables.isEmpty()) {
            AllureEvidence.text("GraphQL variables", variables.toString());
        }

        return given()
                .log().method()
                .log().uri()
                .contentType(JSON)
                .body(body)
                .when()
                .post()
                .then()
                .log().status()
                .extract()
                .response();
    }
}
