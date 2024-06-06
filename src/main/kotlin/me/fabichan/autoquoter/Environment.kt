package me.fabichan.autoquoter

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

object Environment {
    val isDev: Boolean = Path("dev-config").exists()

    val folder: Path = Path("")

    val configFolder: Path = folder.resolve("config")
    val logbackConfigPath: Path = configFolder.resolve("logback.xml")
}