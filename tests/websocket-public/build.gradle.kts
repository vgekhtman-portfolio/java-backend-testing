val junitVersion = "6.1.3"

dependencies {
    // java.net.http.WebSocket is built into the JDK - no WebSocket client dependency needed.
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}
