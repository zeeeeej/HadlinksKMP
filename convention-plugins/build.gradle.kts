import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    `kotlin-dsl`
    alias(libs.plugins.maven.publish)

}

dependencies {
    implementation(libs.nexus.publish)
}