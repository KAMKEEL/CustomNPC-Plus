pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net")
		maven("https://maven.quiltmc.org/repository/release")
		maven("https://maven.kikugie.dev/snapshots")
	}
}


plugins {
	id("dev.kikugie.stonecutter") version "0.7.11"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"
	shared {
		versions("1.21.8", "1.21.10")
	}
	create(rootProject)
}

rootProject.name = "Nodex"
