import org.gradle.kotlin.dsl.signing
import java.util.*

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_TOKEN")
}


val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            url = if (project.version.toString().endsWith("SNAPSHOT")) {
                uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            } else {
                uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            }

            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {

        version = project.version.toString()
        groupId = project.group.toString()
        artifactId = "khexagon"

        // We need to add the javadocJar to every publication
        // because otherwise maven is complaining.
        // It is not sufficient to only have it in the "root" folder.
        artifact(javadocJar)

        // pom information needs to be specified per publication
        // because otherwise maven will complain again that
        // information like license, developer or url are missing.
        pom {
            name.set("KHexagon")
            description.set("Hexagonal grid library for Kotlin")
            url.set("https://offlinebrain.github.io/khexagon/")

            scm {
                connection.set("scm:git:git://github.com/OfflineBrain/khexagon.git")
                developerConnection.set("scm:git:ssh://github.com:OfflineBrain/khexagon.git")
                url.set("https://github.com/OfflineBrain/khexagon")
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
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

// Signing artifacts. Signing.* extra properties values will be used
signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    sign(publishing.publications)
}