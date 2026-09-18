plugins {
    id("com.android.application")
}

val signingStorePath = System.getenv("FINTEST_KEYSTORE_PATH")
val signingStorePassword = System.getenv("FINTEST_KEYSTORE_PASSWORD")
val signingKeyAlias = System.getenv("FINTEST_KEY_ALIAS")
val signingKeyPassword = System.getenv("FINTEST_KEY_PASSWORD")

android {
    namespace = "com.danielslima.testeappinicial"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.danielslima.testeappinicial"
        minSdk = 26
        targetSdk = 36
        versionCode = 19
        versionName = "0.15.1"
    }

    signingConfigs {
        create("release") {
            if (!signingStorePath.isNullOrBlank()) {
                storeFile = file(signingStorePath)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
