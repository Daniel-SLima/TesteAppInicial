plugins {
    id("com.android.application")
}

android {
    namespace = "com.danielslima.testeappinicial"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.danielslima.testeappinicial"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "0.8.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
