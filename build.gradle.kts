import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Properties

description = "JACOB (Java-COM bridge)"

plugins {
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
    doLast {
        val props = Properties()
        props.setProperty("version", "${project.version}")
        props.setProperty("build.date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss", Locale.ROOT)))
        val dir = outputDir.get().asFile.resolve("META-INF")
        mkdir(dir)
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

val pub = (publishing.publications["mavenJava"] as MavenPublication)

for (arch in listOf("x86", "x64")) {
    pub.artifact(artifacts.add("archives", file("dll/jacob-${project.version}-${arch}.dll")) {
        classifier = arch
        type = "dll"
    })
}
pub.pom {
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
