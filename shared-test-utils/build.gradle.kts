plugins {
    `java-library`
}

val junitVersion = "6.1.3"
val testcontainersVersion = "1.21.4"

dependencies {
    // Leaks into every module that extends CustomSutExtension or uses the gRPC request builders.
    api(project(":proto"))
    api("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    api("org.testcontainers:testcontainers:$testcontainersVersion")
    api("org.wiremock:wiremock:3.13.2")

    implementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("io.grpc:grpc-netty-shaded:1.83.1")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
}
