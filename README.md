## how to use?

### build.gradle.kts
```kotlin
repositories {
    maven{
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation ("com.github.volta2030:bumblebee:1.0.0")
}

```

### build.gradle

```groovy
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation "com.github.volta2030:bumblebee:1.0.0"
}
```