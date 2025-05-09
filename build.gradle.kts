// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("androidx.navigation.safeargs") version "2.8.9" apply false

}
buildscript {
    dependencies {
        classpath(libs.google.services)


    }

    repositories {
        google() // ← bu kesin olacak!
        mavenCentral() // ← bu da lazım
    }
}