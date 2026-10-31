import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.util.Properties

buildscript {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:${property("loom_version")}")
    }
}

plugins {
    id("dev.kikugie.stonecutter")
}

val minecraftVersion = sc.current.version
val loader = sc.current.project.substringAfterLast('-')
require(loader == "fabric") { "Fabric adapter cannot build $loader" }
val manifest = rootProject.file("versions/dependencies/$minecraftVersion.properties")
val target = Properties().apply { manifest.inputStream().use { load(it) } }
fun targetProperty(key: String): String = target.getProperty(key)?.takeIf { it.isNotBlank() }
    ?: error("Missing $key in ${manifest.name}")

val javaRelease = targetProperty("java_version").toInt()
val unobfuscated = target.getProperty("unobfuscated", "false").toBooleanStrict()
val supportsServerBooks = target.getProperty("supports_server", "false").toBooleanStrict()
val fabricApiId = if (sc.eval(minecraftVersion, "<1.18.2")) "fabric" else "fabric-api"
val loaderVersion = target.getProperty("fabric_loader_version", property("fabric_loader_version").toString())
val modVersion = property("mod_version").toString()
val modId = property("archives_base_name").toString()
val suffix = targetProperty("version_suffix")
require(targetProperty("minecraft_version") == minecraftVersion)
require(targetProperty("java_dependency") == ">=$javaRelease")
require(javaRelease in setOf(8, 16, 17, 21, 25))

apply(plugin = if (unobfuscated) "net.fabricmc.fabric-loom" else "net.fabricmc.fabric-loom-remap")

version = "$modVersion+$suffix"
group = property("maven_group").toString()
configure<BasePluginExtension> {
    archivesName.set("$modId-$loader")
}
configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com") {
        content { includeGroup("com.terraformersmc") }
    }
}

dependencies {
    add("minecraft", "com.mojang:minecraft:$minecraftVersion")
    if (!unobfuscated) add("mappings", "net.fabricmc:yarn:${targetProperty("yarn_mappings")}:v2")
    val implementation = if (unobfuscated) "implementation" else "modImplementation"
    val compileOnly = if (unobfuscated) "compileOnly" else "modCompileOnly"
    add(implementation, "net.fabricmc:fabric-loader:$loaderVersion")
    add(implementation, "net.fabricmc.fabric-api:fabric-api:${targetProperty("fabric_api_version")}")
    add(compileOnly, "com.terraformersmc:modmenu:${targetProperty("modmenu_version")}") {
        isTransitive = false
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaRelease)
}

val resourceProperties = mapOf(
    "version" to version.toString(),
    "minecraft_dependency" to targetProperty("minecraft_dependency"),
    "java_dependency" to targetProperty("java_dependency"),
    "fabric_loader_dependency" to ">=$loaderVersion",
    "mixin_java" to "JAVA_$javaRelease"
)

tasks.named<ProcessResources>("processResources") {
    exclude("META-INF/neoforge.mods.toml")
    inputs.properties(resourceProperties)
    inputs.property("requires_keybinding_api", !unobfuscated)
    inputs.property("key_mapping_dependency", if (supportsServerBooks) fabricApiId
            else if (!unobfuscated) "fabric-key-binding-api-v1" else "fabric-key-mapping-api-v1")
    inputs.property("supports_server", supportsServerBooks)
    filesMatching(listOf("fabric.mod.json", "bookpaste.mixins.json")) {
        expand(resourceProperties)
    }
    filesMatching("fabric.mod.json") {
        filter { line: String ->
            when {
                supportsServerBooks && line.contains("\"environment\":") -> "  \"environment\": \"*\","
                supportsServerBooks && line.contains("\"fabric-key-binding-api-v1\"") -> "    \"$fabricApiId\": \"*\","
                unobfuscated && line.contains("\"fabric-key-binding-api-v1\"") ->
                    if (supportsServerBooks) "    \"fabric-api\": \"*\","
                    else "    \"fabric-key-mapping-api-v1\": \"*\","
                sc.eval(minecraftVersion, "<1.16.5") && line.contains("\"fabric-networking-api-v1\"") -> "    \"fabric-networking-v0\": \"*\","
                !supportsServerBooks && line.contains("\"main\":") -> "    \"main\": [],"
                else -> line
            }
        }
    }
    filesMatching("bookpaste.mixins.json") {
        filter { line: String ->
            if ((sc.eval(minecraftVersion, "<1.20.5") && line.contains("UnlimitedWritableBookMixin"))
                || (sc.eval(minecraftVersion, "<1.17") && line.contains("UnlimitedBookPacketMixin"))) null
            else if (!supportsServerBooks && listOf("UnlimitedWritableBookMixin", "UnlimitedBookPacketMixin",
                    "ServerBookLimitMixin", "UnlimitedBookEditScreenMixin", "ClientBookLimitMixin").any { line.contains(it) }) null else line
        }
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE"))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}

val modJar = tasks.named<AbstractArchiveTask>(if (unobfuscated) "jar" else "remapJar")
val sourceJar = tasks.named<AbstractArchiveTask>(if (unobfuscated) "sourcesJar" else "remapSourcesJar")

val collectMod = tasks.register<Copy>("collectMod") {
    group = "distribution"
    from(modJar.flatMap { it.archiveFile })
    from(sourceJar.flatMap { it.archiveFile }) {
        into("sources")
    }
    into(rootProject.layout.buildDirectory.dir(loader))
}

tasks.named("build") { dependsOn(collectMod) }
tasks.register("buildAndCollect") {
    group = "build"
    dependsOn(tasks.named("build"))
}
