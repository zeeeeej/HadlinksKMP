import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    repositories {
        maven {
            name = "HadlinksKMP"
            url = uri("https://github.com/zeeeeej/HadlinksKMP")
            // username and password (a personal Github access token) should be specified as
            // `githubPackagesUsername` and `githubPackagesPassword` Gradle properties or alternatively
            // as `ORG_GRADLE_PROJECT_githubPackagesUsername` and `ORG_GRADLE_PROJECT_githubPackagesPassword`
            // environment variables
            credentials(PasswordCredentials::class)
        }
    }
}

publishing {
    publications {
        matching {
            return@matching it.name in listOf("iosArm64", "iosX64", "kotlinMultiplatform")
        }.all {
            tasks.withType<AbstractPublishToMaven>()
                .matching { it.publication == this@all }
                .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/SergeiMikhailovskii/kmp-app-review")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_API_KEY")
            }
        }
    }
}
