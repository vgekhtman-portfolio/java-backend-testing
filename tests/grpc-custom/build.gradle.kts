val junitVersion = "6.1.3"

dependencies {
    // Exposes the generated gRPC stubs (:proto) and the request builders.
    testImplementation(project(":shared-test-utils"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}
