import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))}
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}


application {
    mainClass.set("com.optimystical.ops.api.ServerKt")
}

group = "com.optimystical.ops"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // **JVM‑specific** core & engine
    implementation("io.ktor:ktor-server-core-jvm:2.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.0")

    // Ktor client (no -jvm suffix needed)
    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-client-cio:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")

    // Koin
    implementation("io.insert-koin:koin-core:3.4.0")
    implementation("io.insert-koin:koin-ktor:3.4.0")

    // Exposed ORM & HikariCP for PostgreSQL
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Databases & messaging
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.rabbitmq:amqp-client:5.18.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // Logging backend for SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.11")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}