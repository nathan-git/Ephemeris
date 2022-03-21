plugins {
    id("com.android.library")
    kotlin("android")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = 32

    defaultConfig {
        targetSdk = 32
        minSdk = 23
    }

    compileOptions.isCoreLibraryDesugaringEnabled = true

    buildFeatures.viewBinding = true

    testOptions {
        devices {
            create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api31") {
                device = "Pixel 2"
                apiLevel = 31
                systemImageSource = "aosp"
            }
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    api(project(":core"))

    api(libs.kotlinx.coroutines.core)

    api(libs.androidx.recyclerview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewpager)
}

detekt {
    config = files("${rootDir.absolutePath}/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}
