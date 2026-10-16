# java-backend-testing

[![CI](https://github.com/vgekhtman-portfolio/java-backend-testing/actions/workflows/ci.yml/badge.svg)](https://github.com/vgekhtman-portfolio/java-backend-testing/actions/workflows/ci.yml)

Backend API test automation in Java across REST, GraphQL, gRPC, WebSocket, contract testing (Pact), database
verification and service virtualization (WireMock). 50 tests, run locally with one command and in GitHub Actions
with a combined Allure report.

## Running it

Requires Docker with Compose v2 (2.24+) and a JDK 17+ to launch Gradle (it provisions the Java 21 toolchain).
Use `gradlew.bat` on Windows.

```bash
./gradlew test                                    # everything: harness self-tests first, then the rest
./gradlew :shared-test-utils:test                 # only the test-harness self-tests
./gradlew test -PincludeTags=custom-sut -x :shared-test-utils:test   # against the bundled service (needs Docker)
./gradlew test -PincludeTags=public-api -x :shared-test-utils:test   # against public APIs (needs internet only)
./gradlew :tests:rest-custom:test                 # a single module
./gradlew allureServe                             # open the combined Allure report (after a test run)
```

The bundled service (orders-service, Postgres, WireMock) is started and stopped by the tests themselves through
Testcontainers, so there is no manual `docker compose` step. Set `TEST_LOG_LEVEL=DEBUG` for more console detail
(gRPC messages, SQL); the Allure report carries the full request and response evidence either way.

## Why more than one system under test

No single public system exposes REST, GraphQL, gRPC and WebSocket together, and two of the skills shown here cannot
be demonstrated against someone else's service at all:

- **Pact** needs a provider whose state you control (and can verify for real), and **SQL verification** needs a
  database you can query. Both need a service you own.
- **Failure-path testing** needs control over a dependency. WireMock stands in for the payment provider, so a
  failing or hanging dependency can be produced on demand, which is impossible against a real provider.

So the project is hybrid. REST, GraphQL and WebSocket are tested against public practice APIs, which shows how to
test an uncontrolled, shared target: tests create their own fixtures, assert only on stable facts, and accept
that the target can be slow or down. gRPC, contract, database and failure-path tests run against a deliberately
small custom service, `orders-service` (a REST orders resource and a gRPC inventory service on Postgres, calling
a payment dependency over HTTP). It has no auth, no retries and a fixed unit price: just enough behaviour to
test.

The tags follow that split. `custom-sut` tests are fully under the project's control and block CI;
`public-api` tests depend on third parties and are reported without blocking.

## What it demonstrates

| Module | Target | Demonstrates |
|---|---|---|
| [`tests/rest-public`](tests/rest-public) | restful-booker | Rest-Assured: CRUD flow, auth token extracted once and reused, field/type response checks, negative cases (missing id, no token) |
| [`tests/graphql-public`](tests/graphql-public) | Rick and Morty GraphQL API | Plain HTTP + JSON GraphQL: scalar and nested fields, variables, pagination envelope, the 200-with-`errors` model versus a 400 validation failure |
| [`tests/websocket-public`](tests/websocket-public) | public echo server | JDK `WebSocket` client: connection lifecycle and close code, message ordering, deliberate timeout when nothing arrives |
| [`tests/rest-custom`](tests/rest-custom) | orders-service | Validation (`400`), retrieval (`404`), and WireMock overriding the payment dependency so a failure or timeout yields a `PAYMENT_FAILED` order |
| [`tests/grpc-custom`](tests/grpc-custom) | orders-service | Generated blocking stubs, explicit `Status.Code` assertions (`NOT_FOUND`, `INVALID_ARGUMENT`), business outcome (`success=false`) versus protocol error |
| [`tests/db-verification`](tests/db-verification) | orders-service Postgres | JDBC assertions on persisted state: the row an API call created, the inventory change a gRPC call made, and the absence of a row after a rejected request |
| [`tests/contract`](tests/contract) | orders-service | Pact consumer test generating a pact, then verification against the running service with database-backed provider states |
| [`shared-test-utils`](shared-test-utils) | — | Test harness: Testcontainers Compose stack shared per module, WireMock control, request builders. Has its own self-tests (tag `framework`), run first and reported apart from the demonstrated tests |

Every scenario, with its test ID, is listed in [`docs/SCENARIOS.md`](docs/SCENARIOS.md).

## CI and reports

The [workflow](.github/workflows/ci.yml) runs on pushes and pull requests to `main`/`develop` in stages:

1. **Framework tests** (blocking) run first; every other job waits for them.
2. **Custom-SUT tests** (blocking) and **public-API tests** (visible when failing, never blocking) run next.
3. A single **Allure report** of the demonstrated tests is generated from both. Pushes publish it to GitHub Pages:
   `main` at [the site root](https://vgekhtman-portfolio.github.io/java-backend-testing/), `develop` under
   [`/develop/`](https://vgekhtman-portfolio.github.io/java-backend-testing/develop/), each with its own history
   trends. Pull requests keep the report as a downloadable `allure-report` artifact. Each run's summary page links to its
   report, and the publish job shows a link to the published one.

A manual run (Actions -> CI -> Run workflow) takes a `scope`: `all` (default), `custom-sut` or `public-api`; the
framework tests always run first. Only a full manual run on `main` or `develop` publishes the report.

Locally, `./gradlew allureServe` opens the same combined report; `./gradlew allureReport` writes it to
`build/reports/allure-report`. Tests are grouped by Epic/Feature/Story. Every REST, GraphQL, gRPC and SQL call is a
step in the test body (`POST /orders -> 201 Created`) that expands to its request and response, shown inline; the
WebSocket message log is attached the same way.

## Layout

```
proto/               inventory.proto and the generated gRPC stubs
sut/                 the custom service (orders REST API, inventory gRPC service, Flyway migrations, Dockerfile)
docker-compose.yml   service + Postgres + WireMock (default payment stub in sut/wiremock/mappings)
shared-test-utils/   test harness and its self-tests
test-support/        Rest-Assured logging via Logback and inline Allure evidence (requests, responses)
tests/               one module per protocol or technique (table above)
docs/                the scenario list
```

## Notes and limitations

- **Public targets are not under our control.** They can be down, rate-limited or change. Overrides:
  `REST_PUBLIC_BASE_URL`, `GRAPHQL_PUBLIC_BASE_URL`, `WEBSOCKET_PUBLIC_URI`. restful-booker was chosen because it
  is a purpose-built practice API with full CRUD and token auth.
- **WebSocket scope is echo-only.** An echo server proves protocol mechanics (connect, send, receive, close), not
  topics or server push. This is a deliberate boundary, not a gap.
- **Pact covers REST only**, with pacts generated and verified locally (no Pact Broker). Renaming a response field
  in the service makes the provider verification fail for the interactions that contain it.
- **No containerized test runner.** Tests need Docker (for the bundled service) and a JDK on the host; Testcontainers
  uses the host's own `docker compose`.
- **Each module starts its own stack.** Modules run in parallel, so the Compose stack is shared by the classes of
  one module, not across modules; a test-only override (`docker-compose.test.yml`) drops the fixed host ports so the
  stacks never collide.

## Tech stack

Java 21 · Gradle 9 (Kotlin DSL) · JUnit 6 · AssertJ · Rest-Assured 6 · grpc-java 1.83 / Protobuf ·
Pact-JVM 4.7 · Testcontainers 1.21 · WireMock 3.13 · JDBC / PostgreSQL 17 · Allure ·
SLF4J + Logback · Docker Compose · GitHub Actions. The service itself: Undertow, grpc-java, HikariCP, Flyway.
