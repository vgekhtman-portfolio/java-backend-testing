val restAssuredVersion = "6.0.1"
val junitVersion = "6.1.3"

dependencies {
    testImplementation(project(":shared-test-utils"))
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.qameta.allure:allure-rest-assured")
    testImplementation(project(":test-logging"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}

// Tests run against the custom SUT stack: declare what feeds it as test inputs.
apply(from = rootProject.file("gradle/sut-backed.gradle.kts"))
