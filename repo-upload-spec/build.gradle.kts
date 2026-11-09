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
    implementation(
        "com.google.api-client:google-api-client:2.9.2")
    implementation(
        "com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation(
        "com.google.apis:google-api-services-drive:v3-rev20261011-2.0.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}