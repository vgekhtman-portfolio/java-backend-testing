package com.vgekhtman.automation.tests.graphqlpublic;

import com.vgekhtman.automation.tests.graphqlpublic.client.GraphQlClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Character id 1 is Rick Sanchez - a stable, well-known fact about this dataset (not something that
 * shifts upstream), so it's safe to assert on directly rather than treating it as volatile.
 */
@Epic("API Protocols")
@Feature("GraphQL")
@Story("Queries")
@Tag("public-api")
class CharacterQueryTest {

    @DisplayName("TC-GRAPHQL-001: a query returns the requested scalar fields")
    @Description("Queries a character by id for name, status and species.")
    @Test
    void queryingScalarFieldsReturnsThem() {
        String query = "query { character(id: 1) { name status species } }";

        GraphQlClient.query(query).then()
                .statusCode(200)
                .body("data.character.name", equalTo("Rick Sanchez"))
                .body("data.character.status", equalTo("Alive"))
                .body("data.character.species", equalTo("Human"));
    }

    @DisplayName("TC-GRAPHQL-002: a query returns nested related objects")
    @Description("Queries a character together with its list of episodes.")
    @Test
    void queryingNestedRelatedFieldsReturnsThem() {
        String query = "query { character(id: 1) { name episode { name } } }";

        GraphQlClient.query(query).then()
                .statusCode(200)
                .body("data.character.episode", not(empty()))
                // "Pilot" is the show's actual first episode (S01E01) - stable, not a volatile aggregate.
                .body("data.character.episode[0].name", equalTo("Pilot"));
    }
}
