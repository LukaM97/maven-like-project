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

}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}