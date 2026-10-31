import java.util.Properties

plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.141"
}

val target = Properties().apply {
    rootProject.file("versions/dependencies/${sc.current.version}.properties").inputStream().use { load(it) }
}
fun targetProperty(key: String): String = target.getProperty(key)?.takeIf { it.isNotBlank() }
    ?: error("Missing $key for ${sc.current.project}")
require(sc.current.project.endsWith("-neoforge"))
require(targetProperty("minecraft_version") == sc.current.version)
val javaRelease = targetProperty("java_version").toInt()
version = "${property("mod_version")}+${targetProperty("version_suffix")}"
group = property("maven_group").toString()
base.archivesName.set("${property("archives_base_name")}-neo")
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaRelease))
    withSourcesJar()
}
sourceSets.main {
    java.setSrcDirs(listOf(rootProject.file("src/neoforge/java")))
    java.source(objects.sourceDirectorySet("sharedJava", "Shared BookPaste code").apply {
        srcDir(rootProject.file("src/main/java"))
        include(
            "**/AtomicConfigFile.java", "**/BookPasteConfig.java", "**/BookPasteConfigLayout.java",
            "**/BookPasteScreenHandler.java", "**/PasteFormatMode.java", "**/ScholarPageAccess.java",
            "**/ScribblePageAccess.java", "**/ServerBookCapabilities.java"
        )
    })
    resources.setSrcDirs(listOf(rootProject.file("src/main/resources")))
    resources.exclude("fabric.mod.json")
}
neoForge {
    version = providers.gradleProperty("neoforge_version").orElse(targetProperty("neoforge_version")).get()
    runs {
        create("client") { client(); gameDirectory.set(rootProject.file(".tools/neoforge/run-client")) }
        create("server") { server(); programArguments.add("--nogui"); gameDirectory.set(rootProject.file(".tools/neoforge/run-server")) }
    }
    mods { create("bookpaste") { sourceSet(sourceSets.main.get()) } }
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaRelease)
}
tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "neoforge_min_version" to targetProperty("neoforge_min_version"),
        "minecraft_dependency" to targetProperty("minecraft_dependency"),
        "mixin_java" to "JAVA_$javaRelease"
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "bookpaste.mixins.json")) { expand(props) }
}
tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE"))
    from(rootProject.file("NOTICE"))
    duplicatesStrategy = DuplicatesStrategy.FAIL
}
val collectMod = tasks.register<Copy>("collectMod") {
    from(tasks.jar.flatMap { it.archiveFile })
    from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }) { into("sources") }
    into(rootProject.layout.buildDirectory.dir("neoforge"))
}
tasks.build { dependsOn(collectMod) }
