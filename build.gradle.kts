plugins {
    id("java")
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "com.zahed75"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(kotlin("test"))
    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = "251.*"
        }
    }
}
