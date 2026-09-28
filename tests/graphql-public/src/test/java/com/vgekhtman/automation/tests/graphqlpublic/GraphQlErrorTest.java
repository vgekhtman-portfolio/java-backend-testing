package com.vgekhtman.automation.tests.graphqlpublic;

import com.vgekhtman.automation.tests.graphqlpublic.client.GraphQlClient;
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
@Tag("public-api")
class GraphQlErrorTest {

    @Test
    void aQueryThatFailsDuringResolutionReturns200WithAnErrorsArray() {
        String query = "query { character(id: \"not-a-number\") { name } }";

        GraphQlClient.query(query).then()
                .statusCode(200)
                .body("errors", not(empty()))
                .body("errors[0].message", not(blankOrNullString()));
    }

    @Test
    void aQueryThatFailsSchemaValidationReturnsANonSuccessStatusInstead() {
        String query = "query { character(id: 1) { thisFieldDoesNotExist } }";

        GraphQlClient.query(query).then()
                .statusCode(400)
                .body("errors[0].message", not(blankOrNullString()));
    }
}
