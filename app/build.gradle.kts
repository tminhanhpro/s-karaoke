import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Phien ban: 1.<appMinor>. Workflow GitHub tu tang appMinor sau moi lan build (1.1, 1.2, 1.3...).
val appMinor = (project.findProperty("appMinor") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "vn.savify.karaoke"
    compileSdk = 35

    defaultConfig {
        applicationId = "vn.savify.karaoke"
        minSdk = 24
        targetSdk = 34
        versionCode = 100 + appMinor
        versionName = "1.$appMinor"
        resValue("string", "app_name", "Savify Karaoke v1.$appMinor")
    }

    buildFeatures {
        resValues = true
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("karaoke.jks")
            storePassword = "karaoke123"
            keyAlias = "karaoke"
            keyPassword = "karaoke123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=androidx.media3.common.util.UnstableApi"
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES", "META-INF/LICENSE*", "META-INF/NOTICE*",
                "META-INF/INDEX.LIST", "META-INF/*.kotlin_module", "META-INF/AL2.0", "META-INF/LGPL2.1"
            )
        }
    }
}

// NewPipeExtractor goi URLEncoder.encode(String, Charset) (chi co tu Android 13) -> NoSuchMethodError tren box cu.
// CompatFactory (trong buildSrc) thay cac loi goi do bang ban tuong thuong trong Compat.kt luc build.
androidComponents {
    onVariants { variant ->
        variant.instrumentation.transformClassesWith(CompatFactory::class.java, InstrumentationScope.ALL) {}
        variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COPY_FRAMES)
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.5")
}
