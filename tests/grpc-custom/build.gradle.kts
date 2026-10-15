val junitVersion = "6.1.3"

dependencies {
    // Exposes the generated gRPC stubs (:proto) and the request builders.
    testImplementation(project(":shared-test-utils"))
    // Transport for the gRPC channel - :proto only brings the stub/protobuf libraries.
    testImplementation("io.grpc:grpc-netty-shaded:1.83.1")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}
