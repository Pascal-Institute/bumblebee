plugins {
    kotlin("jvm") version "2.0.0"
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
    implementation ("com.github.Pascal-Institute:komat:1.9.2")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.pascal.institute"//Navigate beyond computing oceans
            artifactId = "bumblebee"

            version = "1.2.2"

            from(components["java"])
        }
    }
}