// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    //Add the KSP plugin to the project
    alias(libs.plugins.ksp) apply false
}
