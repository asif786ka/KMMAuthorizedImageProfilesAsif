plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization) // Added for Kotlinx Serialization support
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Your multiplatform dependencies here
                implementation(libs.ktor.core)
                implementation(libs.ktor.json)
                implementation(libs.ktor.serialization)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kermit)
                implementation(libs.accompanist.permissions) // Assuming you use Kermit for logging
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.android)
            }
        }
        // Ensure iosMain source set is correctly referenced
        val iosMain by creating {
            dependencies {
                implementation(libs.ktor.client.ios)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test")) // Kotlin Test framework

                // Ktor Mock Engine for testing HTTP client
                implementation("io.ktor:ktor-client-mock:2.0.0")

                // Coroutine Test Library for testing coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

                // Kermit logging library (if you're using Kermit for logging)
                implementation("co.touchlab:kermit:0.1.9")
            }
        }

    }
}

android {
    namespace = "com.asif.kmmauthorizedimageprofiles"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
