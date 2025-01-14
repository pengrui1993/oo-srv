import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

//tasks.getByName<Jar>("jar") {
//    enabled = false
//}
group = "com.oo.srv"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.springframework.session:spring-session-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(project(":core"))
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("cn.hutool:hutool-all:5.8.25")
    implementation("commons-codec:commons-codec:1.17.0")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.tika:tika:3.0.0")
    implementation("org.apache.tika:tika-core:3.0.0")

}

tasks.test {
    useJUnitPlatform()
}