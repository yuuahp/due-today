plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "dev.yuua"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("org.mnode.ical4j:ical4j:4.2.3")
    implementation("com.akuleshov7:ktoml-core:0.7.1")
    implementation("com.akuleshov7:ktoml-file:0.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}