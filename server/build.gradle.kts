plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.oo.srv"
version = "0.0.1"
val mainPrefix = group
java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation(project(":api"))
	testImplementation(kotlin("test"))
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val depLibsDir = "$projectDir/build/libs/lib"
val plain = "${rootProject.name}-plain-$version.jar"
val target = "${rootProject.name}-$version.jar"
val targetAbsPath = "$rootDir/build/libs/$target"

tasks.register<Copy>("copyDependencies") {
	from(configurations.runtimeClasspath).into(depLibsDir)
}
tasks.withType<Jar>{
	manifest {
		val cps = configurations.runtimeClasspath.get()
			.files.joinToString(" ") { "lib/${it.name}" }
		attributes["Class-Path"] = cps
		attributes["Main-Class"] = "$mainPrefix.MainKt"
	}
}
tasks {
	// Task to copy dependencies to "builds/libs/lib"
	val copyDependencies0 by registering(Copy::class) {
		from(configurations.runtimeClasspath)
		into(depLibsDir)
	}
	// Ensure dependencies are copied before building the JAR
	jar {
		dependsOn(copyDependencies0)
	}

}
tasks.build {
	dependsOn(tasks.jar)
	doLast {
		File(targetAbsPath).delete()
	}
}
