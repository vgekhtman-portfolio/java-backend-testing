// Applied by every module whose tests run against the custom SUT stack (apply(from = ...) in its build file).
//
// The SUT is built inside Docker, so Gradle can't see it as an input of these modules' test tasks. Without
// the inputs below, a change that only touches the service would leave the tests reported as up to date
// and skipped. These are the files the SUT image and compose stack are built from.
tasks.withType<Test>().configureEach {
    val repo = rootProject.layout.projectDirectory

    inputs.files(repo.dir("sut").asFileTree.matching { exclude("build/**", ".gradle/**") })
        .withPropertyName("sutSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(repo.dir("proto").asFileTree.matching { exclude("build/**") })
        .withPropertyName("protoSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.files(
        repo.file("docker-compose.yml"),
        repo.file("docker-compose.test.yml"),
        repo.file("settings.gradle.kts"),
        repo.file("build.gradle.kts"),
        repo.file("gradle.properties"),
    )
        .withPropertyName("stackDefinition")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
