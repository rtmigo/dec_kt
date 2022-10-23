plugins {
    kotlin("jvm") version "1.6.20"
    id("java-library")
    jacoco
    java
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.20"
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

group = "io.github.rtmigo"
version = "0.1.3" // -SNAPSHOT

publishing {
    publications {
        create<MavenPublication>("dec") {
            from(components["java"])
            pom {
                val github = "https://github.com/rtmigo/dec_kt"

                name.set("dec")
                description.set("64-bit decimal floating-point number. " +
                                    "Kotlin wrapper for Java BigDecimal.")
                url.set(github)

                organization {
                    this.name.set("Revercode")
                    this.url.set("https://revercode.com")
                }

                developers {
                    developer {
                        name.set("Artsiom iG")
                        email.set("ortemeo@gmail.com")
                    }
                }
                scm {
                    url.set(github)
                    connection.set(github.replace("https:", "scm:git:"))
                }
                licenses {
                    license {
                        name.set("ISC License")
                        url.set("$github/blob/HEAD/LICENSE")
                    }
                }
            }
        }
    }
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.5.1")
}



tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating JaCoCo report
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // JaCoCo report is always generated after tests run
}

tasks.register("updateReadmeVersion") {
    doFirst {
        // найдем что-то вроде "io.github.rtmigo:dec:0.0.1"
        // и поменяем на актуальную версию
        val readmeFile = project.rootDir.resolve("README.md")
        val prefixToFind = "io.github.rtmigo:dec:"
        val regex = """(?<=${Regex.escape(prefixToFind)})[0-9\.+]+""".toRegex()
        val oldText = readmeFile.readText()
        val newText = regex.replace(oldText, project.version.toString())
        if (newText!=oldText)
            readmeFile.writeText(newText)
    }
}

tasks.build {
    dependsOn("updateReadmeVersion")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(true)
    }
}