# tests/rest-public

REST-Assured tests against a public REST practice API — no build or local infrastructure required.

## Target

`https://restful-booker.herokuapp.com`, confirmed reachable at build time. The base URL is overridable via
the `REST_PUBLIC_BASE_URL` environment variable.

## Scenarios

- `BookingLifecycleTest` — full CRUD flow (`POST /booking` → `GET` → `POST /auth` → `PUT` → `DELETE` →
  `GET` confirms gone), and the auth-token-extract-then-reuse pattern used across two separate
  authenticated calls.
- `BookingShapeTest` — response shape validation via explicit field/type assertions (restful-booker has no
  published schema to validate against - see the class Javadoc for how the expected types were derived),
  for both the create-booking response envelope and the plain booking shape returned by `GET`.
- `BookingNegativeTest` — a booking id that never existed, one this suite deleted itself, and `PUT`/`DELETE`
  without a valid token.

Every test is tagged `@Tag("public-api")`.

## Known limitation

This target is a service we don't control, running on Heroku's free tier, which has its own known
reliability caveats (cold starts, occasional downtime, no uptime guarantee). Every test here creates its
own booking as a fixture and only ever asserts against data it created itself — nothing assumes booking id
`1` (or any other id) exists globally, since that data is shared with every other user of this public demo
API and can't be relied on to be stable. A failure here may reflect the dependency being down or
rate-limiting rather than a defect in the test code — see phase 12's CI design, which treats this module's
failures as non-blocking for exactly this reason.
