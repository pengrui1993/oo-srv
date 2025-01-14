plugins {
    kotlin("jvm")
}

group = "com.oo.srv"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.gavaghan:geodesy:1.1.3")
}

tasks.test {
    useJUnitPlatform()
}