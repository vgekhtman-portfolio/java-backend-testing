val restAssuredVersion = "6.0.1"
val junitVersion = "6.1.3"

dependencies {
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    // Rest-Assured needs an object mapper on the classpath to serialize the Map request bodies used here.
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.22.2")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitVersion")
    testImplementation("org.assertj:assertj-core:3.27.7")
}
