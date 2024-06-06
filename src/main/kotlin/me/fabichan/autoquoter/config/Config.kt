package me.fabichan.autoquoter.config

import com.google.gson.Gson
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import me.fabichan.autoquoter.Environment
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.system.exitProcess

data class DatabaseConfig(
    val serverName: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
) {
    val url: String
        get() = "jdbc:postgresql://$serverName:$port/$name"
}

data class Config(
    val token: String,
    val ownerIds: List<Long>,
    val testGuildIds: List<Long>,
    val databaseConfig: DatabaseConfig,
    val botApiKey: String? = null,
    val supportGuildInvite: String,
    val joinLeaveLogWebhook: String,
) {
    companion object {
        private val logger = KotlinLogging.logger { }

        private val configFilePath: Path = Environment.configFolder.resolve("config.json") ?: run {
            logger.error { "Configuration folder not found at ${Environment.configFolder.absolutePathString()}" }
            exitProcess(1)
        }

        @get:BService
        val instance: Config by lazy {
            logger.info { "Loading configuration at ${configFilePath.absolutePathString()}" }
            if (configFilePath.notExists()) {
                logger.error { "Configuration file not found at ${configFilePath.absolutePathString()}" }
                exitProcess(1)
            }

            return@lazy Gson().fromJson(configFilePath.readText(), Config::class.java)
        }
    }

    object Constants {
        const val EMBED_COLOR = 0x00FFFF
        const val BOT_VERSION = "1.0.0"
        var quotes = "Loading..."
    }
}
