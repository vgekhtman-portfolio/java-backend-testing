package com.vgekhtman.automation.tests.graphqlpublic;

import com.vgekhtman.automation.tests.graphqlpublic.client.GraphQlClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

/**
 * GraphQL's error model on this server distinguishes two failure kinds: a query that fails schema
 * *validation* (e.g. a field that doesn't exist) is rejected with HTTP 400 before execution starts, while
 * a query that is schema-valid but fails during *resolution* (e.g. an argument value the upstream REST
 * API can't handle) returns HTTP 200 with a populated {@code errors} array - the well-known GraphQL gotcha
 * of "200 doesn't mean success". Both are demonstrated below.
 */
@Epic("API Protocols")
@Feature("GraphQL")
@Story("Error model")
@Tag("public-api")
class GraphQlErrorTest {

    @DisplayName("TC-GRAPHQL-004: a resolution failure returns HTTP 200 with an errors array")
    @Description("A schema-valid query that fails during resolution still returns 200, with the failure in the errors array.")
    @Test
    void aQueryThatFailsDuringResolutionReturns200WithAnErrorsArray() {
        String query = "query { character(id: \"not-a-number\") { name } }";

        GraphQlClient.query(query).then()
                .statusCode(200)
                .body("errors", not(empty()))
                .body("errors[0].message", not(blankOrNullString()));
    }

    @DisplayName("TC-GRAPHQL-005: a schema validation failure returns HTTP 400")
    @Description("A query that fails schema validation is rejected before execution with a 4xx status.")
    @Test
    void aQueryThatFailsSchemaValidationReturnsANonSuccessStatusInstead() {
        String query = "query { character(id: 1) { thisFieldDoesNotExist } }";

        GraphQlClient.query(query).then()
                .statusCode(400)
                .body("errors[0].message", not(blankOrNullString()));
    }
}
