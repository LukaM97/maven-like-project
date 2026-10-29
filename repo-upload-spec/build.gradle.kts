plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

group = "rs.raf.sk"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Source: https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.14.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}