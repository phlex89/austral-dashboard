plugins {
    id("com.android.application")
}

android {
    namespace = "app.australdashboard.provamusica"
    compileSdk = 32

    defaultConfig {
        applicationId = "app.australdashboard.provamusica"
        minSdk = 32
        targetSdk = 32
        versionCode = 1
        versionName = "1"
    }

    lint {
        disable += "ExpiredTargetSdkVersion"
    }
}
