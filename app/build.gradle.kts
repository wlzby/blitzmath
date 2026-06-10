plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation("androidx.core:core-splashscreen:1.0.1")
                implementation("androidx.fragment:fragment-ktx:1.8.6")
                implementation("androidx.fragment:fragment:1.8.6")
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(project.dependencies.platform(libs.androidx.compose.bom))
                implementation(libs.androidx.ui)
                implementation(libs.androidx.ui.graphics)
                implementation(libs.androidx.ui.tooling.preview)
                implementation(libs.androidx.material3)
                implementation(libs.androidx.animation)
                implementation(libs.androidx.material.icons)
                implementation(libs.androidx.material.icons.extended)
                implementation("com.google.android.gms:play-services-ads:23.3.0")
                implementation("com.google.android.play:app-update:2.1.0")
                implementation("com.google.android.play:review:2.0.1")
                implementation("com.google.android.ump:user-messaging-platform:2.2.0")
                implementation("com.google.ads.mediation:unity:4.12.3.0")
                implementation("com.unity3d.ads:unity-ads:4.12.3")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                implementation("androidx.navigation:navigation-compose:2.7.7")
                implementation(libs.androidx.work.runtime)
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.messaging)
                implementation(libs.firebase.analytics)
                implementation(libs.firebase.crashlytics)
                implementation("com.google.android.play:review-ktx:2.0.1")
                implementation(libs.huawei.hms.base)
                implementation(libs.huawei.hms.analytics)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
        }
        val iosMain by creating {
            dependencies {
            }
        }
    }
}

android {
    namespace = "com.mawelly.blitzmath"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mawelly.blitzmath"
        minSdk = 24
        targetSdk = 35
        versionCode = 19
        versionName = "1.3.6"

        resourceConfigurations += listOf("en", "tr", "es", "de", "fr", "it", "pt", "hi", "zh", "ru")
    }
    
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].java.srcDirs("src/androidMain/kotlin")

    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        noCompress("png")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    signingConfigs {
        create("release") {
            storeFile = project.file("blitzmath-keystore.jks")
            storePassword = "Galata8955"
            keyAlias = "blitzmath"
            keyPassword = "Galata8955"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
}

apply(plugin = "com.huawei.agconnect")
