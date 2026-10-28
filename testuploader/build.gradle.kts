plugins {
    kotlin("jvm")
}

group = "rs.raf.sk"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("rs.raf.sk:repouploader:0.0.1")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}