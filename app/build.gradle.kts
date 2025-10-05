plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.onlyfan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.onlyfan"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.github.ismaeldivita:chip-navigation-bar:1.3.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Nếu bạn đang dùng version catalog cho viewpager2
    // Hãy chắc chắn trong libs.versions.toml đã khai báo. Nếu chưa, thay bằng:
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}