val restAssuredVersion = "6.0.1"
val junitVersion = "6.1.3"

dependencies {
    // CustomSut endpoints/credentials, the order request builders and the generated gRPC stubs (:proto).
    testImplementation(project(":shared-test-utils"))
    // The state-changing calls under verification are made over REST and gRPC...
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation(project(":test-support"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    // ...and checked with plain JDBC - no ORM, which would work against querying the database directly.
    testRuntimeOnly("org.postgresql:postgresql:42.7.13")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

// Tests run against the custom SUT stack: declare what feeds it as test inputs.
apply(from = rootProject.file("gradle/sut-backed.gradle.kts"))
