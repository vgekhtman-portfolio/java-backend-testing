val pactVersion = "4.7.5"
val junitVersion = "6.1.3"

dependencies {
    // CustomSut endpoints/credentials: the provider is verified against the running orders-service.
    testImplementation(project(":shared-test-utils"))
    testImplementation("au.com.dius.pact.consumer:junit5:$pactVersion")
    testImplementation("au.com.dius.pact.provider:junit5:$pactVersion")
    // The hypothetical consumer client is built on Rest-Assured, which needs an object mapper for its
    // JSON request/response bodies.
    testImplementation("io.rest-assured:rest-assured:6.0.1")
    testImplementation(project(":test-support"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    // Provider states insert rows straight into Postgres.
    testRuntimeOnly("org.postgresql:postgresql:42.7.13")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

tasks.test {
    // The consumer tests write the pact file here and the provider verification reads it back, so it is a
    // declared output and cleared before each run so a stale pact from an earlier run can never be verified.
    val pactDir = layout.buildDirectory.dir("pacts")
    outputs.dir(pactDir)
    doFirst { delete(pactDir) }
}

// Tests run against the custom SUT stack: declare what feeds it as test inputs.
apply(from = rootProject.file("gradle/sut-backed.gradle.kts"))
