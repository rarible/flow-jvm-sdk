// configuration variables

val javaTargetVersion = "1.8"
val defaultGroupId = "com.nftco"
val defaultVersion = "0.7.1.2"

// other variables

fun getProp(name: String, defaultValue: String? = null): String? {
    return project.findProperty("flow.$name")?.toString()?.trim()?.ifBlank { null }
        ?: project.findProperty(name)?.toString()?.trim()?.ifBlank { null }
        ?: defaultValue
}

group = getProp("groupId", defaultGroupId)!!
version = when {
    getProp("version") !in setOf("unspecified", null) -> { getProp("version")!! }
    getProp("snapshotDate") != null -> { "${defaultVersion.replace("-SNAPSHOT", "")}.${getProp("snapshotDate")!!}-SNAPSHOT" }
    else -> { defaultVersion }
}

plugins {
//    id("org.jetbrains.dokka") version "1.6.10"
    kotlin("jvm") version "1.5.31"
    idea
    jacoco
//    signing
    `java-library`
    `java-test-fixtures`
    `maven-publish`
//    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
//    id("org.jmailen.kotlinter") version "3.4.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
//    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.20")

    api("org.onflow:flow:0.21")

    api("com.github.TrustedDataFramework:java-rlp:1.1.20")

    api("org.bouncycastle:bcpkix-jdk15on:1.70")

    api(platform("com.fasterxml.jackson:jackson-bom:2.12.2"))
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")

    testApi("org.junit.jupiter:junit-jupiter:5.8.2")
    testApi("org.assertj:assertj-core:3.23.1")

    testFixturesImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks {

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showCauses = true
        }
        finalizedBy("jacocoTestReport")
    }

    compileKotlin {
        sourceCompatibility = javaTargetVersion
        targetCompatibility = javaTargetVersion

        kotlinOptions {
            jvmTarget = javaTargetVersion
            apiVersion = "1.5"
            languageVersion = "1.5"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        }
    }

    compileTestKotlin {
        sourceCompatibility = javaTargetVersion
        targetCompatibility = javaTargetVersion

        kotlinOptions {
            jvmTarget = javaTargetVersion
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    jacocoTestReport {
        dependsOn(test)
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    jacoco {
        toolVersion = "0.8.7"
    }

/*
    kotlinter {
        ignoreFailures = false
        indentSize = 4
        reporters = arrayOf("checkstyle", "plain", "html")
        experimentalRules = false

        // be sure to update .editorconfig in the root as well
        disabledRules = arrayOf(
            "filename",
            "no-wildcard-imports",
            "import-ordering",
            "chain-wrapping"
        )
    }
*/

/*
    val documentationJar by creating(Jar::class) {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.get().outputs)
    }
*/

    val sourcesJar by creating(Jar::class) {
        dependsOn(classes)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource + sourceSets["testFixtures"].allSource)
    }

    artifacts {
//        add("archives", documentationJar)
        add("archives", sourcesJar)
    }

//    nexusPublishing {
//        repositories {
//            sonatype {
//
//                nexusUrl.set(uri("https://repo.rarible.org/repository/maven-public/"))
//                System.getenv("GRADLE_NEXUS_USER")?.let(username::set) ?: println("Not found `GRADLE_NEXUS_USER`")
//                System.getenv("GRADLE_NEXUS_PASS")?.let(password::set) ?: println("Not found `GRADLE_NEXUS_PASS`")

/*
                if (getProp("sonatype.nexusUrl") != null) {
                    nexusUrl.set(uri(getProp("sonatype.nexusUrl")!!))
                }
                if (getProp("sonatype.snapshotRepositoryUrl") != null) {
                    snapshotRepositoryUrl.set(uri(getProp("sonatype.snapshotRepositoryUrl")!!))
                }
                if (getProp("sonatype.username") != null) {
                    username.set(getProp("sonatype.username")!!)
                }
                if (getProp("sonatype.password") != null) {
                    password.set(getProp("sonatype.password")!!)
                }
*/
/*            }
        }
    }*/

    publishing {
        repositories {
            maven {
                val releaseUrl = uri("http://nexus-ext.rarible.int/repository/maven-releases/")
                val snapshotUrl = uri("http://nexus-ext.rarible.int/repository/maven-snapshots/")
                url = uri(if (version.toString().contains("SNAPSHOT")) snapshotUrl else releaseUrl)
                isAllowInsecureProtocol = true
                credentials.username = System.getenv("GRADLE_NEXUS_USER")
                credentials.password = System.getenv("GRADLE_NEXUS_PASS")
            }
            mavenLocal()
        }
        publications {
            create<MavenPublication>("mavenJava") {
                from(project.components["java"])
//                artifact(documentationJar)
                artifact(sourcesJar)

                pom {
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    name.set(project.name)
                    url.set("https://onflow.org")
                    description.set("The Flow Blockchain JVM SDK")
                    scm {
                        url.set("https://github.com/onflow/flow")
                        connection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
                        developerConnection.set("scm:git:git@github.com/onflow/flow-jvm-sdk.git")
                    }
                    developers {
                        developer {
                            name.set("Flow Developers")
                            url.set("https://onflow.org")
                        }
                    }
                }
            }
        }
    }

/*
    signing {
        if (getProp("signing.key") != null) {
            useInMemoryPgpKeys(getProp("signing.key"), getProp("signing.password"))
        } else {
            useGpgCmd()
        }
        sign(publishing.publications)
    }
*/
}
