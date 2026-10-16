import io.qameta.allure.gradle.adapter.AllureAdapterExtension
import io.qameta.allure.gradle.base.AllureExtension
import org.gradle.api.plugins.ExtensionAware

plugins {
    java
    id("io.qameta.allure-adapter") version "4.3.0" apply false
    id("io.qameta.allure-report") version "4.3.0"
}

allprojects {
    group = "com.vgekhtman.automation"
    version = "0.1.0"
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    // `-PincludeTags=custom-sut` (or `public-api`, comma-separated for several) runs only tests carrying those
    // JUnit tags across every module; modules without any matching test are then not an error.
    val includedTags = providers.gradleProperty("includeTags").map { it.split(",") }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            if (includedTags.isPresent) includeTags(*includedTags.get().toTypedArray())
        }
        if (includedTags.isPresent) failOnNoDiscoveredTests = false
        // Tests exercise live services (the public APIs, the Docker stack); a result restored from the build
        // cache would report a run that never happened.
        outputs.cacheIf { false }
        // So every module's tests resolve "docker-compose.yml" the same way, regardless of module depth.
        workingDir = rootProject.projectDir
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

// ---- Conventions for every module that contains tests (shared-test-utils and everything under tests/) ----
// Allure result collection, logging, and the root-level task that merges the demonstrated tests' results.
//
// shared-test-utils holds the harness self-tests: they run first when everything runs together, but are
// reported on their own (`:shared-test-utils:allureServe`) so the main report contains only the demonstrated tests.

val allureLibraryVersion = "3.0.0"
extra["allureVersion"] = allureLibraryVersion

val frameworkModule = project(":shared-test-utils")
val demoModules = subprojects.filter { it.path.startsWith(":tests:") }
val testModules = listOf(frameworkModule) + demoModules

configure(testModules) {
    apply(plugin = "io.qameta.allure-adapter")

    val allure = extensions.getByType<AllureExtension>()
    (allure as ExtensionAware).extensions.getByType<AllureAdapterExtension>().apply {
        allureJavaVersion.set(allureLibraryVersion)
        // @Step/@Attachment aren't used (steps and attachments go through the Allure runtime API), so the
        // AspectJ agent and the Javadoc-to-@Description processor the plugin would otherwise add are off.
        aspectjWeaver.set(false)
        autoconfigureJavadocDescriptions.set(false)
    }

    dependencies {
        // One BOM keeps every Allure artifact (adapter, rest-assured filter, ...) on the same version.
        add("testImplementation", platform("io.qameta.allure:allure-bom:$allureLibraryVersion"))
        add("testImplementation", "org.slf4j:slf4j-api:2.0.17")
        add("testRuntimeOnly", "ch.qos.logback:logback-classic:1.5.18")
    }

    // Results from an earlier run would otherwise pile up in the directory and end up in the report.
    tasks.withType<Test>().configureEach {
        doFirst { delete(layout.buildDirectory.dir("allure-results")) }
    }

    // One logback-test.xml for every module, so levels and format are the same everywhere.
    extensions.getByType<SourceSetContainer>().named("test") {
        resources.srcDir(rootProject.file("gradle/test-resources"))
    }
}

// The single directory holding every demonstrated module's results (what CI uploads and `allure serve` can open).
// Modules still write to their own build/allure-results so Gradle sees non-overlapping outputs.
tasks.register<Sync>("allureResults") {
    group = "reporting"
    description = "Merges the demonstrated test modules' Allure raw results into build/allure-results."
    into(layout.buildDirectory.dir("allure-results"))
    // Every module writes its own executor.json; the first one wins, result files are uniquely named.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    demoModules.forEach { module ->
        from(module.layout.buildDirectory.dir("allure-results"))
        mustRunAfter(module.tasks.withType<Test>())
    }
}

// Harness self-tests go first when everything runs together. Ordering only, not a dependency: a demonstrated
// module that doesn't need Docker (rest-public, ...) must stay runnable on its own.
demoModules.forEach { module ->
    module.tasks.withType<Test>().configureEach { mustRunAfter(frameworkModule.tasks.withType<Test>()) }
}

// The root `test` lifecycle (what `./gradlew test` selects alongside every module's `test`) waits for all test modules,
// then the merge runs once, after the last of them. If a module's tests fail Gradle never starts this task, so CI runs
// `./gradlew allureResults` explicitly after the test step.
tasks.named("test") {
    testModules.forEach { module -> dependsOn(module.tasks.withType<Test>()) }
    finalizedBy("allureResults")
}

// `./gradlew allureServe` opens one combined report across the demonstrated test modules.
dependencies {
    demoModules.forEach { module -> add("allureReport", project(module.path)) }
}

// The harness self-tests get their own report: `./gradlew :shared-test-utils:allureServe`.
configure(listOf(frameworkModule)) {
    apply(plugin = "io.qameta.allure-report")
    dependencies { add("allureReport", project(path)) }
    extensions.getByType<AllureExtension>().version.set("2.34.1")
    tasks.named("allureReport") {
        doFirst { delete(layout.buildDirectory.dir("reports/allure-report")) }
    }
}

// Allure 2's report generator is Java-based (no Node needed) and reads the same result files as Allure 3's.
allure {
    version.set("2.34.1")
}

// The generator refuses to write into an existing report directory, so clear it before each run.
tasks.named("allureReport") {
    doFirst { delete(layout.buildDirectory.dir("reports/allure-report")) }
}
