import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

//            implementation(libs.ktor.client.core)

//            implementation(libs.ktor.client.cio)
//            implementation(libs.ktor.client.core)

//            api(libs.kotlinx.datetime)
//            api(libs.kotlinx.coroutines.core)
//            api(libs.ktor.client.core)
//            api(libs.ktor.client.cio)
//            api(libs.ktor.client.content.negotiation)
//            api(libs.ktor.serialization.kotlinx.json)
//            api(libs.ktor.serialization.kotlinx.protobuf)
//            implementation(libs.ktor.client.logging)
//            implementation(libs.ktor.client.core.wasm)
//            implementation(libs.ktor.client.core)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources) // SEE https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
            implementation(compose.components.uiToolingPreview)
//            implementation(projects.yunextCommon)
//            implementation(projects.yunextContext)
            implementation(libs.bundles.yunext)
            implementation(projects.yunextBle)
            implementation(libs.bundles.navigation)
        }

        iosMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.cio)
            api(libs.ktor.client.darwin)
        }

        androidMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.okhttp)
            api(libs.ktor.client.cio)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.core.ktx)
            implementation(libs.bundles.androidx.lifecycle)
        }

        val wasmJsMain by getting {
            dependencies {
                api(libs.ktor.client.core.wasm)
            }
        }
        
        jvmMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.okhttp)
            api(libs.ktor.client.cio)
        }
    }
}

android {
    namespace = "com.yunext.kotlin.kmp.sample.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
