package me.fabichan.autoquoter


import dev.reformator.stacktracedecoroutinator.runtime.DecoroutinatorRuntime
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.freya022.botcommands.api.core.config.DevConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.lang.management.ManagementFactory
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import ch.qos.logback.classic.ClassicConstants as LogbackConstants

private val logger by lazy { KotlinLogging.logger {} }

private const val mainPackageName = "me.fabichan.autoquoter"

object Main {
    @JvmStatic
    fun main(args: Array<out String>) {

        try {
            System.setProperty(
                LogbackConstants.CONFIG_FILE_PROPERTY,
                Environment.logbackConfigPath.absolutePathString()
            )
            logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }
            if ("-XX:+AllowEnhancedClassRedefinition" in ManagementFactory.getRuntimeMXBean().inputArguments) {
                logger.info { "Skipping stacktrace-decoroutinator as enhanced hotswap is active" }
            } else if ("--no-decoroutinator" in args) {
                logger.info { "Skipping stacktrace-decoroutinator as --no-decoroutinator is specified" }
            } else {
                DecoroutinatorRuntime.load()
            }
            logger.info { "Running on Java ${System.getProperty("java.version")}" }

            val config = Config.instance
            if (Environment.isDev) {
                logger.warn { "Running in development mode" }
            }

            BotCommands.create {
                if (Environment.isDev) {
                    disableExceptionsInDMs = true
                    @OptIn(DevConfig::class)
                    disableAutocompleteCache = true
                }

                addOwners(config.ownerIds)

                addSearchPath(mainPackageName)

                applicationCommands {
                    @OptIn(DevConfig::class)
                    onlineAppCommandCheckEnabled = Environment.isDev
                    testGuildIds += config.testGuildIds
                }

                components {
                    useComponents = true
                }
            }

        } catch (e: Exception) {
            logger.error(e) { "Unable to start the bot" }
            exitProcess(1)
        }
    }
}
