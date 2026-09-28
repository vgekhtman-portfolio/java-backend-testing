# Build stage: compile the SUT into a runnable distribution using the project's Gradle version.
# The whole repo is copied (not just proto/ and sut/) because Gradle's settings.gradle.kts declares
# every module in the build - including test-only ones - and needs each one's directory to exist to
# configure the build at all, even though this image only ever runs the :sut:installDist task.
FROM gradle:9.7.1-jdk21 AS build
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon :sut:installDist

# Runtime stage: JRE only, no build tooling.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/sut/build/install/sut /app
EXPOSE 8080 9090
ENTRYPOINT ["/app/bin/sut"]
