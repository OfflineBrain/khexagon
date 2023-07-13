import java.net.URL

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.dokka") version "1.8.20"
    `maven-publish`
}

group = "io.github.offlinebrain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

val kotestVersion = "5.6.2"

dependencies {
    testImplementation(kotlin("test"))

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks.test {
    useJUnitPlatform()

    systemProperties(System.getProperties().mapKeys { it.key as String })
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(javadocJar)
            from(components["java"])
            pom {
                name.set("KHexagon")
                description.set("Hexagonal grid library for Kotlin")
                url.set("https://offlinebrain.github.io/khexagon/")

                scm {
                    connection.set("scm:git:git://github.com/OfflineBrain/khexagon.git")
                    developerConnection.set("scm:git:ssh://github.com:OfflineBrain/khexagon.git")
                    url.set("https://github.com/OfflineBrain/khexagon")
                }

                developers {
                    developer {
                        id.set("OfflineBrain")
                        name.set("Leonid Ivakhnenko")
                        email.set("leonidivakhnenko.dev@gmail.com")
                    }
                }
            }
        }
    }
}

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("html-docs")
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

tasks.dokkaHtml {
    dokkaSourceSets {
        named("main") {
            moduleName.set("KHexagon")
            skipEmptyPackages.set(true)

            includes.from("src/main/kotlin/package.md")

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/OfflineBrain/khexagon/tree/master/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
