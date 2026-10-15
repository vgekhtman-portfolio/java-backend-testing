#!/usr/bin/env bash
# Runs the whole test suite: ./run-tests.sh [extra Gradle arguments]
#
# Needs Docker (the custom service under test is started automatically via Testcontainers) and a JDK 17+
# to launch Gradle; the project itself builds with a Java 21 toolchain that Gradle provisions. Nothing else.
set -euo pipefail
cd "$(dirname "$0")"

fail() {
    echo "run-tests.sh: $1" >&2
    exit 1
}

command -v java >/dev/null 2>&1 || fail "no JDK found on PATH (need JDK 17 or newer to run Gradle)."
java_major=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n 1)
[ "${java_major:-0}" -ge 17 ] || fail "JDK ${java_major:-unknown} found, need 17 or newer to run Gradle."

command -v docker >/dev/null 2>&1 || fail "docker not found on PATH - it is needed to run the service under test."
docker info >/dev/null 2>&1 || fail "the Docker daemon isn't reachable - start Docker and retry."

# docker-compose.test.yml uses the !reset tag, which needs Compose 2.24 or newer.
compose_version=$(docker compose version --short 2>/dev/null) || fail "'docker compose' (Compose v2) is required."
[ "$(printf '%s\n2.24.0\n' "${compose_version#v}" | sort -V | head -n 1)" = "2.24.0" ] \
    || fail "Docker Compose ${compose_version} is too old; 2.24 or newer is required."

exec ./gradlew test "$@"
