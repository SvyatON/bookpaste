# BookPaste

Paste long texts into Minecraft books and signs with automatic page splitting and formatting.

**Version 2.2 | Fabric | By SvyatON**

## Downloads

- [Modrinth](https://modrinth.com/mod/bookpaste)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/bookpaste)

Download the JAR matching your Minecraft version. Compiled releases are distributed through Modrinth and CurseForge; this repository contains the source code.

## Features

- Paste clipboard text into writable books, automatically splitting it across pages.
- Paste text into signs with word wrapping across lines.
- Choose smart text wrapping or keep whole paragraphs together.
- Keep unfinished text in the clipboard when a book or sign runs out of space.
- Switch formatting modes with a configurable hotkey. The default is **F8**.
- Receive temporary notifications above the hotbar without adding messages to chat.
- Set a custom book page limit for singleplayer or a dedicated server, including limits below 100 pages.
- View the server's page settings in the configuration menu. Server values are read-only and do not overwrite personal settings.
- Use adapters for Scribble and Scholar. Compatibility depends on the Minecraft and optional-mod versions installed.
- Includes English, Russian, Ukrainian, German, Spanish, French, Brazilian Portuguese, Japanese and Simplified Chinese translations.

## Installation and use

1. Install Fabric Loader and Fabric API for your Minecraft version.
2. Put the matching BookPaste JAR in your `mods` folder.
3. Install Mod Menu if you want to open BookPaste's settings from the mods list.
4. Open a writable book or a sign and press **Ctrl+V** to paste.

For books above 100 pages, install BookPaste and Fabric API on the server and participating clients. Each Minecraft build uses the same JAR for its client and dedicated server. Without confirmed server support, the client keeps the standard 100-page limit.

Forge and NeoForge ports are planned. The builds in this repository currently target Fabric.

## Supported Minecraft versions

One JAR covers each range below. For example, Minecraft **1.16.4** uses the **1.16.2-1.16.5** JAR.

JAR names follow `bookpaste-fabric-2.2+<Minecraft range>.jar`.

| Minecraft range | Build target | Java for Minecraft |
|---|---|---|
| 1.16-1.16.1 | `1.16.1-fabric` | 8+ |
| 1.16.2-1.16.5 | `1.16.5-fabric` | 8+ |
| 1.17-1.17.1 | `1.17.1-fabric` | 16+ |
| 1.18-1.18.1 | `1.18.1-fabric` | 17+ |
| 1.18.2 | `1.18.2-fabric` | 17+ |
| 1.19-1.19.2 | `1.19.2-fabric` | 17+ |
| 1.19.3 | `1.19.3-fabric` | 17+ |
| 1.19.4 | `1.19.4-fabric` | 17+ |
| 1.20-1.20.1 | `1.20.1-fabric` | 17+ |
| 1.20.2-1.20.4 | `1.20.4-fabric` | 17+ |
| 1.20.5-1.20.6 | `1.20.6-fabric` | 21+ |
| 1.21-1.21.4 | `1.21.4-fabric` | 21+ |
| 1.21.5-1.21.8 | `1.21.8-fabric` | 21+ |
| 1.21.9-1.21.10 | `1.21.10-fabric` | 21+ |
| 1.21.11 | `1.21.11-fabric` | 21+ |
| 26.1-26.1.2 | `26.1.2-fabric` | 25+ |
| 26.2 | `26.2-fabric` | 25+ |
| 26.3 | `26.3-fabric` | 25+ |

## Configuration

BookPaste creates `config/bookpaste.json` on the client and server. The generated file includes `#` comments explaining the settings; BookPaste supports this commented JSON format.

Default values:

```json
{
  "customBookPages": false,
  "maxPages": 0,
  "lastCustomMaxPages": 0,
  "formatMode": "SMART_FILL"
}
```

| Setting | Where it is used | Meaning |
|---|---|---|
| `customBookPages` | Client and server | Enables a custom page limit. `false` uses 100 pages. |
| `maxPages` | Client and server | Page limit when enabled. `0` means no BookPaste page cap. Negative values are rejected. |
| `lastCustomMaxPages` | Client only | Remembers the last custom value when the custom limit is switched off. |
| `formatMode` | Client only | `SMART_FILL` wraps text at suitable boundaries; `WHOLE_PARAGRAPHS` keeps paragraphs together. |

### Singleplayer

Edit page settings through Mod Menu. Turning the custom limit off restores 100 pages; turning it back on restores the remembered value, including after restarting Minecraft.

### Dedicated servers

Only `customBookPages` and `maxPages` are needed on a server. For a 750-page limit:

```json
{
  "customBookPages": true,
  "maxPages": 750
}
```

Restart the server after editing the file. Connected BookPaste clients automatically use the advertised limit. Their page controls are locked while connected; after disconnecting, their personal settings return.

If a saved writable book exceeds a reduced limit, saving or signing it is blocked with a warning above the hotbar. BookPaste does not automatically cut pages from the book. Existing signed books remain readable. Minecraft's packet and NBT size limits still apply when the BookPaste page cap is disabled.

Older `unlimitedBookPages` settings and `bookpaste-server.json` files are migrated automatically when needed, preserving existing settings and previous server limits.

## Building from source

Install **JDK 25** and set `JAVA_HOME` to its installation directory. JDK 25 runs the build; each Minecraft target has its own Java bytecode level, listed above.

The project uses Gradle 9.4.0, Fabric Loom and Stonecutter 0.9.7. The first build requires internet access to download dependencies.

On Windows, from the repository root:

```powershell
.\gradlew.bat :26.3-fabric:build
.\gradlew.bat buildAll --continue
```

The first command builds only Fabric 26.3. The second builds all 18 configured Fabric targets. Replace `26.3-fabric` with another build target from the table to select a different version.

On Linux and macOS:

```sh
bash ./gradlew :26.3-fabric:build
bash ./gradlew buildAll --continue
```

Collected output:

```text
build/
  fabric/
    bookpaste-fabric-2.2+1.16-1.16.1.jar
    bookpaste-fabric-2.2+1.16.2-1.16.5.jar
    ...
    bookpaste-fabric-2.2+26.3.jar
    sources/
      bookpaste-fabric-2.2+1.16-1.16.1-sources.jar
      ...
      bookpaste-fabric-2.2+26.3-sources.jar
```

Install the regular JAR in Minecraft. The `sources` folder contains separate source archives for reading the code. To build the mod yourself, use the full repository, including its Gradle wrapper, build scripts and resources.

Per-target Gradle output is also available under `versions/<target>/build/libs/`. Generated files are excluded from Git.

### Project structure

- `src/main/`: shared Java sources and resources, with Stonecutter version branches.
- `versions/dependencies/`: Minecraft, Fabric, mappings and Java settings for each target.
- `gradle/`, `gradlew`, `gradlew.bat`: Gradle wrapper files.
- `settings.gradle.kts`: target registration and plugin configuration.
- `stonecutter.gradle.kts`: active editing target and tasks for building all targets.
- `build.fabric.gradle.kts`: Fabric build and JAR collection.
- `gradle.properties`: mod version and shared build settings.

The active Stonecutter editing target is `1.21.11-fabric`. Running the wrapper without a task shows Gradle help. Build and package checks do not replace in-game testing of each Minecraft and optional-mod combination.

## License

[Apache License 2.0](LICENSE). Preserve the [NOTICE](NOTICE) when redistributing the project.
