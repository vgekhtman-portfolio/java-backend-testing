package com.vgekhtman.automation.tests.graphqlpublic;

import com.vgekhtman.automation.tests.graphqlpublic.client.GraphQlClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Character id 1 is Rick Sanchez - a stable, well-known fact about this dataset (not something that
 * shifts upstream), so it's safe to assert on directly rather than treating it as volatile.
 */
@Tag("public-api")
class CharacterQueryTest {

    @Test
    void queryingScalarFieldsReturnsThem() {
        String query = "query { character(id: 1) { name status species } }";

        GraphQlClient.query(query).then()
                .statusCode(200)
                .body("data.character.name", equalTo("Rick Sanchez"))
                .body("data.character.status", equalTo("Alive"))
                .body("data.character.species", equalTo("Human"));
    }

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
