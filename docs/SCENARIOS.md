# Test scenarios

Every demonstrated test, by module: its ID, the scenario, and what it checks. Test IDs and scenario titles are the `@DisplayName`s
in the code, as they appear in the Allure report; "What it checks" is each test's `@Description`. The harness self-tests in
`shared-test-utils` (`TC-INFRA-*`) test the test infrastructure rather than a system under test and are not listed.

Tests against a public API are tagged `public-api`, tests against the custom service `custom-sut`.

## REST: restful-booker (public API)

Module: [`tests/rest-public`](../tests/rest-public)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-RESTPUB-001 | a booking can be created, read, updated and deleted | Full CRUD against a booking the test creates itself, including the auth-token extract-and-reuse pattern. | `BookingLifecycleTest` |
| TC-RESTPUB-002 | one auth token authorizes several write calls | Reuses a single token across two independent update and delete calls. | `BookingLifecycleTest` |
| TC-RESTPUB-003 | the create-booking response has the expected shape | Field and type assertions on the create response envelope. | `BookingShapeTest` |
| TC-RESTPUB-004 | the get-booking response has the expected shape | Field and type assertions on the plain booking response. | `BookingShapeTest` |
| TC-RESTPUB-005 | a booking id that never existed returns 404 | GET for an id that was never created. | `BookingNegativeTest` |
| TC-RESTPUB-006 | a booking deleted by the test returns 404 | Deletes a booking the test created, then reads it back. | `BookingNegativeTest` |
| TC-RESTPUB-007 | updating without a valid token is forbidden | PUT with an invalid token returns 403. | `BookingNegativeTest` |
| TC-RESTPUB-008 | deleting without a valid token is forbidden | DELETE with an invalid token returns 403. | `BookingNegativeTest` |

## GraphQL: Rick and Morty API (public API)

Module: [`tests/graphql-public`](../tests/graphql-public)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-GRAPHQL-001 | a query returns the requested scalar fields | Queries a character by id for name, status and species. | `CharacterQueryTest` |
| TC-GRAPHQL-002 | a query returns nested related objects | Queries a character together with its list of episodes. | `CharacterQueryTest` |
| TC-GRAPHQL-003 | variables drive pagination and filtering, including the info envelope | Uses GraphQL variables for page and status and asserts on the pagination envelope as well as the results. | `CharactersFilterPaginationTest` |
| TC-GRAPHQL-004 | a resolution failure returns HTTP 200 with an errors array | A schema-valid query that fails during resolution still returns 200, with the failure in the errors array. | `GraphQlErrorTest` |
| TC-GRAPHQL-005 | a schema validation failure returns HTTP 400 | A query that fails schema validation is rejected before execution with a 4xx status. | `GraphQlErrorTest` |

## WebSocket: public echo server (public API)

Module: [`tests/websocket-public`](../tests/websocket-public)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-WEBSOCKET-001 | a connection opens and closes with the expected code | Opens a connection, confirms it is live, closes it and asserts the close status code. | `ConnectionLifecycleTest` |
| TC-WEBSOCKET-002 | a sent message is echoed back exactly | Sends one text message and asserts the identical message returns. | `EchoOrderingTest` |
| TC-WEBSOCKET-003 | several messages are echoed back in the order they were sent | Sends four messages in sequence and asserts they return in order. | `EchoOrderingTest` |
| TC-WEBSOCKET-004 | waiting for a message that never comes times out instead of hanging | Handles the nothing-happened case deliberately, not just the happy path. | `ReceiveTimeoutTest` |

## REST: orders-service (custom service)

Module: [`tests/rest-custom`](../tests/rest-custom)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-RESTCUSTOM-001 | a valid order is CONFIRMED against the default payment stub | The happy path: POST /orders with valid input and the shipped payment stub in place. | `OrderCreationTest` |
| TC-RESTCUSTOM-002 | a blank customer id is rejected with 400 | Validation: customerId must not be blank. | `OrderCreationTest` |
| TC-RESTCUSTOM-003 | a blank product id is rejected with 400 | Validation: productId must not be blank. | `OrderCreationTest` |
| TC-RESTCUSTOM-004 | a zero quantity is rejected with 400 | Validation: quantity must be positive. | `OrderCreationTest` |
| TC-RESTCUSTOM-005 | a negative quantity is rejected with 400 | Validation: quantity must be positive. | `OrderCreationTest` |
| TC-RESTCUSTOM-006 | an existing order is returned by id | Creates an order, then reads it back through GET /orders/{id}. | `OrderRetrievalTest` |
| TC-RESTCUSTOM-007 | an unknown order id returns 404 | GET /orders/{id} for a random UUID returns the documented not-found response. | `OrderRetrievalTest` |
| TC-RESTCUSTOM-008 | a failing payment dependency results in a PAYMENT_FAILED order | WireMock makes the payment call fail on demand, a path that can't be provoked on a real payment provider; the API still answers 201. | `PaymentFailurePathTest` |
| TC-RESTCUSTOM-009 | a payment dependency that times out results in a PAYMENT_FAILED order | WireMock delays the payment response past the service's timeout. | `PaymentFailurePathTest` |

## gRPC: orders-service inventory (custom service)

Module: [`tests/grpc-custom`](../tests/grpc-custom)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-GRPC-001 | CheckStock reports the stock of a seeded product | CheckStock for prod-abc returns its seeded quantity. | `StockCheckTest` |
| TC-GRPC-002 | CheckStock for an unknown product fails with NOT_FOUND | Asserts the gRPC status code itself, not just that the call failed. | `StockCheckTest` |
| TC-GRPC-003 | ReserveStock decrements stock and refuses to go below zero | Insufficient stock is a business outcome in the response, not a gRPC error, and stock never goes negative. | `StockReservationTest` |
| TC-GRPC-004 | ReserveStock with zero quantity fails with INVALID_ARGUMENT | Asserts the INVALID_ARGUMENT status code for a zero quantity. | `StockReservationTest` |
| TC-GRPC-005 | ReserveStock with a negative quantity fails with INVALID_ARGUMENT | Asserts the INVALID_ARGUMENT status code for a negative quantity. | `StockReservationTest` |

## Database verification: orders-service Postgres (custom service)

Module: [`tests/db-verification`](../tests/db-verification)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-DB-001 | a created order is persisted with the fields the API returned | Queries the orders table directly and compares the row with the API response instead of trusting the response alone. | `OrderPersistenceTest` |
| TC-DB-002 | reserving stock decrements the inventory row | Measures the inventory row before and after a ReserveStock call and checks the response's claim against the database. | `InventoryPersistenceTest` |
| TC-DB-003 | an order with a blank customer id is rejected and leaves no row | Proves the absence of a side effect, with a control order showing the count query can see rows. | `RejectedOrderPersistenceTest` |
| TC-DB-004 | an order with a non-positive quantity is rejected and leaves no row | Same absence check for the quantity validation rule. | `RejectedOrderPersistenceTest` |

## Contract testing (Pact): orders-service (custom service)

Module: [`tests/contract`](../tests/contract)

| ID | Scenario | What it checks | Test class |
|---|---|---|---|
| TC-PACT-001 | consumer: creating an order matches the declared contract | The consumer client sends the declared POST /orders request and parses every field of the declared response. | `OrdersConsumerPactTest` |
| TC-PACT-002 | consumer: reading an existing order matches the declared contract | The consumer client sends the declared GET /orders/{id} request and parses the declared response exactly. | `OrdersConsumerPactTest` |
| TC-PACT-003 | consumer: a missing order is treated as absent | The consumer client maps the declared 404 response to an empty result. | `OrdersConsumerPactTest` |
| TC-PACT-004 | provider: a request to create an order | The real orders-service answers the pact interaction "a request to create an order" as the consumer declared it; provider states are set up by writing to Postgres directly. | `OrdersProviderVerificationTest` |
| TC-PACT-005 | provider: a request for an existing order | The real orders-service answers the pact interaction "a request for an existing order" as the consumer declared it; provider states are set up by writing to Postgres directly. | `OrdersProviderVerificationTest` |
| TC-PACT-006 | provider: a request for an order that does not exist | The real orders-service answers the pact interaction "a request for an order that does not exist" as the consumer declared it; provider states are set up by writing to Postgres directly. | `OrdersProviderVerificationTest` |
