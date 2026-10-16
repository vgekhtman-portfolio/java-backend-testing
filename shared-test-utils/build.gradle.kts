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

    // Transport for InventoryClient's channel; consumers get it on their runtime classpath transitively.
    implementation("io.grpc:grpc-netty-shaded:1.83.1")
    implementation("org.slf4j:slf4j-api:2.0.17")
    // InventoryClient attaches each call to the Allure report.
    implementation(platform("io.qameta.allure:allure-bom:${rootProject.extra["allureVersion"]}"))
    implementation("io.qameta.allure:allure-java-commons")
    implementation(project(":test-support"))

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
}

// Tests run against the custom SUT stack: declare what feeds it as test inputs.
apply(from = rootProject.file("gradle/sut-backed.gradle.kts"))
