plugins {
    kotlin("jvm") version "1.9.25"
    //spock...
    groovy
    id("com.github.ben-manes.versions") version "0.21.0"
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
    //spock...
    testImplementation("org.spockframework:spock-core:2.4-M4-groovy-4.0")
    testRuntimeOnly("net.bytebuddy:byte-buddy:1.15.8")  //enables mocking of classes (in addition to interfaces)
    testRuntimeOnly("org.objenesis:objenesis:3.4")    //enables mocking of classes without default constructor (together with ByteBuddy or CGLIB)

}

tasks.test {
    useJUnitPlatform()
}