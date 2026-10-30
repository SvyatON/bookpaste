import java.util.Properties

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    kotlinController = true
    create(rootProject) {
        val manifests = file("versions/dependencies").listFiles()
            ?.filter { it.extension == "properties" }?.sortedBy { it.name }
            ?: error("Missing versions/dependencies directory")
        require(manifests.isNotEmpty()) { "No Minecraft targets configured" }
        for (manifest in manifests) {
            val properties = Properties().apply { manifest.inputStream().use { load(it) } }
            val minecraft = properties.getProperty("minecraft_version")
                ?: error("Missing minecraft_version in ${manifest.name}")
            require(minecraft == manifest.nameWithoutExtension) { "Version mismatch in ${manifest.name}" }
            val loaders = properties.getProperty("loaders")
                ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: error("Missing loaders in ${manifest.name}")
            require(loaders.isNotEmpty() && loaders.distinct().size == loaders.size) {
                "Empty or duplicate loaders in ${manifest.name}"
            }
            for (loader in loaders) {
                require(loader in setOf("fabric", "forge", "neoforge")) { "Unknown loader: $loader" }
                val script = "build.$loader.gradle.kts"
                require(file(script).isFile) { "$minecraft-$loader requires a working $script adapter" }
                version("$minecraft-$loader", minecraft).buildscript(script)
            }
        }
        vcsVersion = "1.21.11-fabric"
    }
}

rootProject.name = "bookpaste"
