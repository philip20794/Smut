plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.philip20794.smut"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.philip20794.smut"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-dev"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.webkit:webkit:1.17.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}


// F-03: Der Android-Build erstellt die Offline-Assets selbst. Kein vorgebautes
// dist/ und kein externer Server werden für die APK benötigt.
val editorDirectory = rootProject.file("editor")
val editorAssetsRoot = layout.buildDirectory.dir("generated/smutEditorAssets")
val editorNpmCi = tasks.register<Exec>("editorNpmCi") {
    workingDir = editorDirectory
    commandLine("npm", "ci", "--ignore-scripts", "--no-audit", "--no-fund")
    inputs.files(editorDirectory.resolve("package.json"), editorDirectory.resolve("package-lock.json"))
    outputs.dir(editorDirectory.resolve("node_modules"))
}
val editorBuild = tasks.register<Exec>("editorBuild") {
    dependsOn(editorNpmCi)
    workingDir = editorDirectory
    commandLine("npm", "run", "build")
    inputs.dir(editorDirectory.resolve("src"))
    inputs.dir(editorDirectory.resolve("scripts"))
    inputs.file(editorDirectory.resolve("tsconfig.json"))
    outputs.dir(editorDirectory.resolve("dist"))
}
val stageEditorAssets = tasks.register<Sync>("stageEditorAssets") {
    dependsOn(editorBuild)
    from(editorDirectory.resolve("dist"))
    into(editorAssetsRoot.map { it.dir("editor") })
}
android.sourceSets.getByName("main").assets.srcDir(editorAssetsRoot)
tasks.named("preBuild").configure { dependsOn(stageEditorAssets) }
tasks.configureEach {
    if (name.startsWith("merge") && name.endsWith("Assets")) {
        dependsOn(stageEditorAssets)
    }
}
