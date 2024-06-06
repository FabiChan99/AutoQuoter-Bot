import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    java
    kotlin("plugin.serialization") version "1.6.10"
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application.mainClass.set("me.fabichan.autoquoter.Main")
group = "me.fabichan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("net.dv8tion:JDA:5.0.0-beta.24")
    implementation("io.github.freya022:BotCommands:3.0.0-alpha.15")
    implementation("org.flywaydb:flyway-core:10.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.3.8")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.11.0")
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()

    archiveFileName.set("AutoQuoter.jar")

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.release.set(17)
}

kotlin {
    jvmToolchain(17)
}