plugins {
    java
}

allprojects {
    group = "com.pghserver"
    version = "1"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}