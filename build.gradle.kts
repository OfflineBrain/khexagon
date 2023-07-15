import java.net.URL

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("org.jetbrains.dokka") version "1.8.20"
    id("convention.publication")

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
}

val ciVersion = System.getenv("GITHUB_REF") ?: "local"

group = "io.github.offlinebrain"
version = when {
//    ciVersion.startsWith("refs/tags/v") -> ciVersion.removePrefix("refs/tags/v")
//    ciVersion.startsWith("refs/tags/") -> ciVersion.removePrefix("refs/tags/")
//    ciVersion == "local" -> "0.0.0-LOCAL"
//    else -> (System.getenv("GITHUB_REF_NAME")?.removeSuffix("-SNAPSHOT") ?: "local") + "-SNAPSHOT"
    else -> "0.1.2"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val kotestVersion = "5.6.2"

kotlin {
    jvmToolchain(17)
    jvm {
        compilations {
            all {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
            val main by getting {}
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()

            systemProperties(System.getProperties().mapKeys { it.key as String })
        }
    }
    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {

        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.property)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.framework.datatest)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(kotlin("reflect"))
            }
        }
        val jsMain by getting
        val jsTest by getting
        val nativeMain by getting
        val nativeTest by getting
    }
}

tasks.dokkaHtml {
    dokkaSourceSets {
        named("commonMain") {
            moduleName.set("KHexagon")
            skipEmptyPackages.set(true)

            includes.from("src/commonMain/kotlin/package.md")

            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/OfflineBrain/khexagon/tree/master/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

//val javadocJar by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//    from(tasks.dokkaHtml)
//}

//afterEvaluate {
//    publishing {
//        repositories {
//            maven {
//                name = "Sonatype"
//                url = if (project.version.toString().endsWith("SNAPSHOT")) {
//                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//                } else {
//                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//                }
//
//                credentials {
//                    username = System.getenv("OSSRH_USERNAME")
//                    password = System.getenv("OSSRH_TOKEN")
//                }
//            }
//        }
//
//        publications.withType<MavenPublication> {
//
//            version = project.version.toString()
//            groupId = project.group.toString()
//            artifactId = "khexagon"
//
//            // We need to add the javadocJar to every publication
//            // because otherwise maven is complaining.
//            // It is not sufficient to only have it in the "root" folder.
//            artifact(javadocJar)
//
//            // pom information needs to be specified per publication
//            // because otherwise maven will complain again that
//            // information like license, developer or url are missing.
//            pom {
//                name.set("KHexagon")
//                description.set("Hexagonal grid library for Kotlin")
//                url.set("https://offlinebrain.github.io/khexagon/")
//
//                scm {
//                    connection.set("scm:git:git://github.com/OfflineBrain/khexagon.git")
//                    developerConnection.set("scm:git:ssh://github.com:OfflineBrain/khexagon.git")
//                    url.set("https://github.com/OfflineBrain/khexagon")
//                }
//
//                licenses {
//                    license {
//                        name.set("MIT License")
//                        url.set("https://opensource.org/licenses/MIT")
//                    }
//                }
//
//                developers {
//                    developer {
//                        id.set("OfflineBrain")
//                        name.set("Leonid Ivakhnenko")
//                        email.set("leonidivakhnenko.dev@gmail.com")
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//signing {
//    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
//    sign(publishing.publications)
//}

tasks.named("publishJsPublicationToSonatypeRepository") {
    dependsOn("signJvmPublication")
}