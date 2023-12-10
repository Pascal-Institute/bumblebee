plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven{
        url = uri("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("com.github.volta2030:delta:1.1.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.snacklab"
            artifactId = "bumblebee"

            version = "1.1.0"

            from(components["java"])
        }
    }
}