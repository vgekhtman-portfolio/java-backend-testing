# tests/websocket-public

WebSocket tests against a public echo server, using the JDK's built-in `java.net.http.WebSocket` — no
WebSocket client dependency, no build or local infrastructure required.

## Target

`wss://echo.websocket.org/`, confirmed reachable and working at build time. The endpoint is overridable
via the `WEBSOCKET_PUBLIC_URI` environment variable. This particular server sends a one-time greeting
message immediately on connect (e.g. `"Request served by <id>"`); every test that goes on to exercise echo
behavior drains that message first.

## Scenarios

- `ConnectionLifecycleTest` — open a connection, confirm it's live, close it, and assert the close event
  reports the expected status code.
- `EchoOrderingTest` — a single message echoed back exactly, and several messages sent in sequence echoed
  back in the same order.
- `ReceiveTimeoutTest` — waiting for a message that was never sent times out deliberately rather than
  hanging.

Every test is tagged `@Tag("public-api")`.

## Known limitation: echo-only scope

An echo server only proves protocol mechanics — connect, send, receive, close. It has no concept of
topics, subscriptions, or server-initiated push messages unrelated to something the client sent. This
module doesn't attempt to simulate pub/sub against it. This is a deliberate, understood scope boundary,
not a gap.
