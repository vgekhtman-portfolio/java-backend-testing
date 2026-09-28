# tests/graphql-public

GraphQL tests against a public practice API — plain HTTP + JSON, no GraphQL client library, no build or
local infrastructure required.

## Target

`https://rickandmortyapi.com/graphql`, confirmed reachable at build time. The base URL is overridable via
the `GRAPHQL_PUBLIC_BASE_URL` environment variable.

## Scenarios

- `CharacterQueryTest` — a basic query with scalar fields, and a query with nested/related fields
  (`episode { name }`).
- `CharactersFilterPaginationTest` — a query using a GraphQL variable together with a pagination/filter
  argument, asserting on the `info` pagination envelope as well as the `results` array.
- `GraphQlErrorTest` — GraphQL's HTTP-200-with-an-`errors`-array error model. See the class Javadoc for
  why the actual test query is a resolution failure (non-numeric `id`) rather than a nonexistent-field
  query — the latter was tried first and turned out to return HTTP 400 on this server (a schema
  *validation* failure, rejected before execution), which is a different, equally real behavior also
  covered by its own test here for contrast.

Every test is tagged `@Tag("public-api")`.

## Known limitations / assumptions

This target is a service we don't control. It has been stable historically, but assertions here avoid
hardcoding volatile aggregates (e.g. total character counts, which could shift if the dataset is ever
updated upstream) — those are asserted only for shape/positivity. The specific facts asserted directly
(character id `1` is "Rick Sanchez"; his first listed episode is "Pilot", the show's actual S01E01) are
stable identifiers/canon, not aggregates, and were confirmed against the live API before being relied on.
A failure here may reflect the dependency being down, rate-limiting, or an actual dataset change upstream
rather than a defect in the test code.
