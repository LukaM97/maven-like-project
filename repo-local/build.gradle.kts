plugins {
    kotlin("jvm")
}

group = "rs.raf.sk"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":repo-spec"))
    implementation(project(":repo-upload-spec"))
    implementation(project(":repo-download-spec"))

    // Source: https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.14.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}