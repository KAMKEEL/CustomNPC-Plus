import org.gradle.api.tasks.bundling.Jar
import kotlin.String

plugins {
	id("fabric-loom")
	java
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.javafx)
}

val mcVersion = property("mcVersion")!!.toString()

val javafx = libs.versions.javafxapp.get()
// http://insecure.repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.8/
val javafxClassifiers = listOf(
	"win",
	"mac",
	"mac-aarch64",
	"linux"
)

fun javafxDep(module: String, classifier: String) =
	"org.openjfx:javafx-$module:$javafx:$classifier"

// Naming examples
// Version Name: 0.0.1-fabric+mc1.20.5-1.21.8
// Jar Name: Nodex-0.0.1-fabric+mc1.20.5-1.21.8.jar
version = "${property("mod.version")}-fabric+mc${property("mod.mc_targets").toString().replace(" ", "-")}"
group = project.findProperty("maven_group") as String

base.archivesName = property("mod.id") as String

repositories {
	mavenCentral()
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	mavenLocal()
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")

	mappings(loom.officialMojangMappings())

	modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

	modImplementation(libs.fabric.kotlin)
	modRuntimeOnly(libs.devauth)

	// Style and icon packs
	implementation(libs.atlantaFX)
	implementation(libs.ikonliJavaFX)
	implementation(libs.ikonliCore)
	implementation(libs.material2)
	implementation(libs.feather)

	include(libs.atlantaFX)
	include(libs.ikonliJavaFX)
	include(libs.ikonliCore)
	include(libs.material2)
	include(libs.feather)

	// RichTextFX and it's dependencies
	implementation(libs.richTextFX)
	implementation(libs.flowless)
	implementation(libs.reactFX)
	implementation(libs.undofx)
	implementation(libs.wellbehavedfx)

	include(libs.richTextFX)
	include(libs.flowless)
	include(libs.reactFX)
	include(libs.undofx)
	include(libs.wellbehavedfx)

	implementation(libs.jacksonCore)
	implementation(libs.directoryWatcher)
	implementation(libs.jsvg)
	include(libs.jsvg)
	include(libs.directoryWatcher)
	include(libs.jacksonCore)

	// JavaFX
	for (classifier in javafxClassifiers) {
		implementation(javafxDep("base", classifier))
		implementation(javafxDep("graphics", classifier))
		implementation(javafxDep("web", classifier))
		implementation(javafxDep("media", classifier))
		implementation(javafxDep("controls", classifier))

		include(javafxDep("base", classifier))
		include(javafxDep("graphics", classifier))
		include(javafxDep("web", classifier))
		include(javafxDep("media", classifier))
		include(javafxDep("controls", classifier))
	}
}

// Taken from Stonecutter template
loom {
	runConfigs.all {
		ideConfigGenerated(true)
		vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
		runDir = "../../run" // Shares the run directory between versions
	}
}

tasks.named<ProcessResources>("processResources") {
	inputs.property("id", project.findProperty("mod.id"))
	inputs.property("name", project.findProperty("mod.name"))
	inputs.property("version", project.findProperty("mod.version"))
	inputs.property("minecraft", project.findProperty("mod.mc_dep"))

	val props = mapOf(
		"id" to project.property("mod.id"),
		"name" to project.property("mod.name"),
		"version" to project.property("mod.version"),
		"minecraft" to project.property("mod.mc_dep")
	)

	filesMatching("fabric.mod.json") { expand(props) }
}

tasks.withType<JavaCompile> {
	options.release.set(21)
}

tasks.named<Jar>("jar") {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_${project.findProperty("archives_base_name")}" }
	}
	from(rootProject.file("docs")) {
		into("docs")
		exclude("README_Pictures")
	}
	exclude("module-info.class")
	exclude("**/module-info.class")
	exclude("META-INF/MANIFEST.MF")
	exclude("META-INF/*.SF")
	exclude("META-INF/*.DSA")
	exclude("META-INF/*.RSA")

	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("remapJar") {
	doLast {
		copy {
			from(outputs.files)
			into(rootProject.file("build/libs"))
		}
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.findProperty("archives_base_name") as String
			from(components["java"])
		}
	}
	repositories {

	}
}