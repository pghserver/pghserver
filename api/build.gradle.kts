plugins {
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:26.1.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("PghServer API")
                description.set("Official library for interfacing with the PghServer runtime")
                url.set("https://github.com/boyninja1555/pghserver")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/boyninja1555/pghserver/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("boyninja1555")
                        name.set("Floor Mann")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/boyninja1555/pghserver.git")
                    developerConnection.set("scm:git:ssh://git@github.com/boyninja1555/pghserver.git")
                    url.set("https://github.com/boyninja1555/pghserver")
                }
            }

            groupId = project.group.toString()
            artifactId = "pghserver-api"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "localRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}