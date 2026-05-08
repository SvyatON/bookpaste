param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"

$snapshotsRoot = Join-Path $ProjectRoot "build-source-snapshots"

$targets = @(
    @{ Module = "mc2612"; Label = "26.1.x" },
    @{ Module = "mc1161"; Label = "1.16-1.16.1" },
    @{ Module = "mc1165"; Label = "1.16.2-1.16.5" },
    @{ Module = "mc1171"; Label = "1.17-1.17.1" },
    @{ Module = "mc1181"; Label = "1.18-1.18.1" },
    @{ Module = "mc1182"; Label = "1.18.2" },
    @{ Module = "mc1192"; Label = "1.19-1.19.2" },
    @{ Module = "mc1193"; Label = "1.19.3" },
    @{ Module = "mc1194"; Label = "1.19.4" },
    @{ Module = "mc1201"; Label = "1.20-1.20.1" },
    @{ Module = "mc1204"; Label = "1.20.2-1.20.4" },
    @{ Module = "mc1206"; Label = "1.20.5-1.20.6" },
    @{ Module = "mc1214"; Label = "1.21-1.21.4" },
    @{ Module = "mc1218"; Label = "1.21.5-1.21.8" },
    @{ Module = "mc12110"; Label = "1.21.9-1.21.10" },
    @{ Module = "mc12111"; Label = "1.21.11" }
)

function Copy-Path {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (Test-Path $Destination) {
        try {
            Get-ChildItem -LiteralPath $Destination -Recurse -Force -ErrorAction SilentlyContinue |
                ForEach-Object {
                    if ($_.Attributes -band [IO.FileAttributes]::ReadOnly) {
                        $_.Attributes = $_.Attributes -bxor [IO.FileAttributes]::ReadOnly
                    }
                }
            Remove-Item -LiteralPath $Destination -Recurse -Force -ErrorAction Stop
        } catch {
            cmd /c "rmdir /s /q ""$Destination""" | Out-Null
        }
    }

    Copy-Item -LiteralPath $Source -Destination $Destination -Recurse -Force
}

function Copy-VersionSources {
    param(
        [string]$Source,
        [string]$Destination
    )

    if (Test-Path $Destination) {
        try {
            Get-ChildItem -LiteralPath $Destination -Recurse -Force -ErrorAction SilentlyContinue |
                ForEach-Object {
                    if ($_.Attributes -band [IO.FileAttributes]::ReadOnly) {
                        $_.Attributes = $_.Attributes -bxor [IO.FileAttributes]::ReadOnly
                    }
                }
            Remove-Item -LiteralPath $Destination -Recurse -Force -ErrorAction Stop
        } catch {
            cmd /c "rmdir /s /q ""$Destination""" | Out-Null
        }
    }

    New-Item -ItemType Directory -Path $Destination -Force | Out-Null

    foreach ($file in Get-ChildItem -LiteralPath $Source -File -Force) {
        Copy-Item -LiteralPath $file.FullName -Destination (Join-Path $Destination $file.Name) -Force
    }

    $sourceSrc = Join-Path $Source "src"
    if (Test-Path $sourceSrc) {
        Copy-Path -Source $sourceSrc -Destination (Join-Path $Destination "src")
    }
}

New-Item -ItemType Directory -Path $snapshotsRoot -Force | Out-Null

foreach ($target in $targets) {
    $snapshotDir = Join-Path $snapshotsRoot $target.Label
    New-Item -ItemType Directory -Path $snapshotDir -Force | Out-Null

    foreach ($fileName in @(
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
        ".gitignore",
        "LICENSE",
        "NOTICE",
        "README.md"
    )) {
        $sourceFile = Join-Path $ProjectRoot $fileName
        if (Test-Path $sourceFile) {
            Copy-Item -LiteralPath $sourceFile -Destination (Join-Path $snapshotDir $fileName) -Force
        }
    }

    $gradleDir = Join-Path $ProjectRoot "gradle"
    if (Test-Path $gradleDir) {
        Copy-Path -Source $gradleDir -Destination (Join-Path $snapshotDir "gradle")
    }

    $sharedSrcDir = Join-Path $ProjectRoot "src"
    if (Test-Path $sharedSrcDir) {
        Copy-Path -Source $sharedSrcDir -Destination (Join-Path $snapshotDir "src")
    }

    $versionDir = Join-Path $ProjectRoot ("versions\" + $target.Module)
    if (Test-Path $versionDir) {
        $versionsRoot = Join-Path $snapshotDir "versions"
        New-Item -ItemType Directory -Path $versionsRoot -Force | Out-Null
        Copy-VersionSources -Source $versionDir -Destination (Join-Path $versionsRoot $target.Module)
    }
}

New-Item -ItemType Directory -Path (Join-Path $snapshotsRoot "26.x") -Force | Out-Null
New-Item -ItemType Directory -Path (Join-Path $snapshotsRoot "26.1.x") -Force | Out-Null
