plugins {
    `java-library`
}

dependencies {
    // Exposed because RestAssuredLogging.config() returns Rest-Assured's own config type.
    api("io.rest-assured:rest-assured:6.0.1")
    implementation("org.slf4j:slf4j-api:2.0.17")
}
