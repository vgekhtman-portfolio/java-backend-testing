# Build stage: compile the SUT into a runnable distribution using the project's Gradle version.
FROM gradle:9.7.1-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts gradle.properties ./
COPY --chown=gradle:gradle proto proto
COPY --chown=gradle:gradle sut sut
RUN gradle --no-daemon :sut:installDist

# Runtime stage: JRE only, no build tooling.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/sut/build/install/sut /app
EXPOSE 8080 9090
ENTRYPOINT ["/app/bin/sut"]
