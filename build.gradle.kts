// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false // Plugin de Hilt
    alias(libs.plugins.ksp) apply false // Plugin KSP para Hilt
    id("com.google.gms.google-services") version "4.4.2" apply false
}