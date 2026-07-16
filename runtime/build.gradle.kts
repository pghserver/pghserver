plugins {
    java
    id("com.gradleup.shadow") version "9.5.1"
}

val mainClass = "$group.runtime.Main"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
}

tasks.processResources {
    filesMatching("release.pgh") {
        expand("version" to project.version.toString())
    }
}

tasks.jar {
    archiveBaseName.set("pghserver")
    archiveVersion.set(project.name)
    archiveClassifier.set("raw")
    manifest.attributes["Main-Class"] = mainClass
}

tasks.shadowJar {
    archiveBaseName.set(tasks.jar.get().archiveBaseName.get())
    archiveVersion.set(tasks.jar.get().archiveVersion.get())
    archiveClassifier.set("")
}

tasks.withType<JavaCompile>().configureEach {
    options.isDebug = false
}