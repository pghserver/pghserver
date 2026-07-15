plugins {
    java
}

allprojects {
    group = "com.pghserver"
    version = "7"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}