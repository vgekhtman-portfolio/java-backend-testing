plugins {
    java
    application
}

val grpcVersion = "1.83.1"
val jacksonVersion = "2.22.2"

dependencies {
    implementation(project(":proto"))

    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")

    implementation("io.undertow:undertow-core:2.4.3.Final")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.postgresql:postgresql:42.7.13")
    implementation("org.flywaydb:flyway-core:13.4.0")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:13.4.0")

    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")
}

application {
    mainClass = "com.vgekhtman.automation.sut.Main"
}
