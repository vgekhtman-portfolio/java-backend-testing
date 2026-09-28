plugins {
    java
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

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // So every module's tests resolve "docker-compose.yml" the same way, regardless of module depth.
        workingDir = rootProject.projectDir
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
