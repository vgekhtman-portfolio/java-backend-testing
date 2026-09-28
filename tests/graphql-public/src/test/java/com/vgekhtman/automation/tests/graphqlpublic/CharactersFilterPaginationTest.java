package com.vgekhtman.automation.tests.graphqlpublic;

import com.vgekhtman.automation.tests.graphqlpublic.client.GraphQlClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

/**
 * A query using GraphQL variables and a pagination/filter argument together - and asserting on the
 * pagination envelope (info.count/info.pages), not just the results array. Counts aren't hardcoded since
 * the live dataset's totals could shift upstream; only their shape/positivity is asserted.
 */
@Tag("public-api")
class CharactersFilterPaginationTest {

    @Test
    void queryingWithVariablesAppliesPaginationAndFilter() {
        String query = "query($page: Int, $status: String) { "
                + "characters(page: $page, filter: {status: $status}) { "
                + "info { count pages } "
                + "results { name status } "
                + "} }";

        GraphQlClient.query(query, Map.of("page", 2, "status", "Alive")).then()
                .statusCode(200)
                .body("data.characters.info.count", greaterThan(0))
                .body("data.characters.info.pages", greaterThan(0))
                .body("data.characters.results", not(empty()))
                .body("data.characters.results.status", everyItem(equalTo("Alive")));
    }
}
