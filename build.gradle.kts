buildscript {
    val agp_version by extra("8.3.2")
}
plugins {
    id("com.android.application") version "8.8.1" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
//    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.10-1.0.30" apply false
//    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}
