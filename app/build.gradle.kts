import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Optional release signing. Copy keystore.properties.example → keystore.properties (gitignored).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.zenlink.hydra"
    compileSdk = 35
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.zenlink.hydra"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-alpha"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Online endpoints. Empty string = offline-only build (in-memory relay stub).
        buildConfigField("String", "RELAY_BASE_URL", "\"\"")
        // Mock billing is the default until Play Console products exist.
        buildConfigField("boolean", "USE_MOCK_BILLING", "true")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://zenlink.tech/hydra/privacy\"")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:device"))
    implementation(project(":core:database"))
    implementation(project(":core:llama"))
    implementation(project(":core:download"))
    implementation(project(":core:billing"))
    implementation(project(":core:sharing"))
    implementation(project(":core:monitoring"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:models"))
    implementation(project(":feature:settings"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.ktx)
    // RoomDatabase is a supertype of HydraDatabase, so :app needs it on its compile classpath.
    implementation(libs.androidx.room.runtime)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
