import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.application)
}

group = "dev.yuua"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.bundles.testing)

    implementation(libs.bundles.logging)
    implementation(libs.bundles.calendar)
    implementation(libs.bundles.ktoml)
    implementation(libs.bundles.kotlinx)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.database)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("dev.yuua.due_today.MainKt")
}

tasks.named("shadowJar", ShadowJar::class) {
    archiveBaseName.set("due-today")
    archiveClassifier.set("all")
    mergeServiceFiles()
}

tasks.test {
    useJUnitPlatform()
}