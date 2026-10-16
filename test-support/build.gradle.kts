plugins {
    `java-library`
}

dependencies {
    // Exposed because RestAssuredLogging.config() and AllureHttpFilter are Rest-Assured types.
    api("io.rest-assured:rest-assured:6.0.1")
    implementation(platform("io.qameta.allure:allure-bom:${rootProject.extra["allureVersion"]}"))
    implementation("io.qameta.allure:allure-java-commons")
    // Pretty-prints JSON request bodies and serializes object bodies for the Allure attachment.
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
}
