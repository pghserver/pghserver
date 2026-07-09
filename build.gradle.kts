plugins {
    java
}

allprojects {
    group = "com.pghserver"
    version = "4"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}