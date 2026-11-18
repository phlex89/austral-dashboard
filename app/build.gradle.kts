import java.util.Properties

plugins {
    id("com.android.application")
}

fun leggiProperties(nome: String): Properties? =
    rootProject.file(nome).takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use { load(it) } }
    }

val versione = leggiProperties("version.properties")!!
val firma = leggiProperties("keystore.properties")

android {
    namespace = "app.australdashboard"
    compileSdk = 32

    defaultConfig {
        applicationId = "app.australdashboard"
        minSdk = 32
        targetSdk = 32
        versionCode = versione.getProperty("versionCode").toInt()
        versionName = versione.getProperty("versionName")
    }

    signingConfigs {
        if (firma != null) {
            create("release") {
                storeFile = file(firma.getProperty("storeFile"))
                storePassword = firma.getProperty("storePassword")
                keyAlias = firma.getProperty("keyAlias")
                keyPassword = firma.getProperty("keyPassword")
            }
        }
    }

    lint {
        disable += "ExpiredTargetSdkVersion"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20260814")
}
