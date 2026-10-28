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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "rs.raf.sk"
            artifactId = "repouploader"
            version = "0.0.1"

            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}