plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11-fabric"

stonecutter parameters {
    constants {
        match(current.project.substringAfterLast('-'), "fabric", "forge", "neoforge")
    }
}

val nodes = stonecutter.tree.nodes.map { it.project.name }

tasks.register("buildAll") {
    group = "build"
    description = "Builds every enabled Minecraft and loader target"
    dependsOn(nodes.map { ":$it:build" })
}

tasks.register("buildAndCollect") {
    group = "build"
    dependsOn("buildAll")
}

tasks.register("listTargets") {
    group = "help"
    doLast {
        nodes.forEach { println(it) }
    }
}
