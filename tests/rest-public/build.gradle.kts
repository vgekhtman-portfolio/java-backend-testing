val restAssuredVersion = "6.0.1"
val junitVersion = "6.1.3"

dependencies {
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.qameta.allure:allure-rest-assured")
    // Rest-Assured needs an object mapper on the classpath to (de)serialize the Booking/BookingDates
    // records used as request/response bodies.
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}
