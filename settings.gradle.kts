rootProject.name = "java-backend-testing"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "proto",
    "sut",
    "shared-test-utils",
    "tests:rest-public",
    "tests:graphql-public",
    "tests:websocket-public",
    "tests:rest-custom",
    "tests:grpc-custom",
    "tests:db-verification",
)
