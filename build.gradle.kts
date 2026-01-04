import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Properties

import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

description = "JACOB (Java-COM bridge)"

plugins {
    id("com.vanniktech.maven.publish") version "0.35.0"
    `module-lib`
}

group = "io.github.osobolev"
version = "1.21"

tasks.jar {
    manifest.attributes["Automatic-Module-Name"] = "${project.group}.${project.name}"
}

val jacobProperties = tasks.register("jacobProperties") {
    val outputDir = layout.buildDirectory.dir("generated/resources")
    outputs.dir(outputDir)
    val version = project.version
    doLast {
        val props = Properties()
        props.setProperty("version", "${version}")
        props.setProperty("build.date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss", Locale.ROOT)))
        val dir = outputDir.get().asFile.resolve("META-INF")
        dir.mkdirs()
        dir.resolve("JacobVersion.properties").bufferedWriter().use {
            props.store(it, null)
        }
    }
}
sourceSets {
    main {
        resources.srcDir(jacobProperties)
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("${project.group}", "${project.name}", "${project.version}")
    configure(JavaLibrary(
        javadocJar = JavadocJar.Javadoc(),
        sourcesJar = true
    ))
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            for (arch in listOf("x86", "x64")) {
                artifact(file("dll/jacob-${project.version}-${arch}.dll")) {
                    classifier = arch
                    extension = "dll"
                }
            }
        }
    }
}

mavenPublishing.pom {
    name.set("jacob")
    description.set("JACOB (Java-COM bridge)")
    url.set("https://github.com/osobolev/jacob-project")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    developers {
        developer {
            name.set("Oleg Sobolev")
            organizationUrl.set("https://github.com/osobolev")
        }
    }
    scm {
        connection.set("scm:git:https://github.com/osobolev/jacob-project.git")
        developerConnection.set("scm:git:https://github.com/osobolev/jacob-project.git")
        url.set("https://github.com/osobolev/jacob-project")
    }
}
