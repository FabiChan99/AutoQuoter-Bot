package me.fabichan.autoquoter.commands


import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.TimeFormat
import me.fabichan.autoquoter.config.Config
import me.fabichan.autoquoter.utils.UpdateTimer
import net.dv8tion.jda.api.interactions.components.buttons.Button
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes


private val logger = KotlinLogging.logger { }

@Command
class BotInfo(
    private val context: BContext,

    private val combinedUserCountTimer: UpdateTimer = UpdateTimer(5.minutes),
    private var combinedUserCount: Int = 0,

    ) : GlobalApplicationCommandProvider {
    suspend fun onCommand(
        event: GuildSlashEvent,
    ) {
        event.deferReply().await()

        val embed = Embed {
            author {
                name = event.jda.selfUser.name
                iconUrl = event.jda.selfUser.effectiveAvatarUrl
            }

            field {
                name = "Bot Version"
                value = Config.Constants.BOT_VERSION
                inline = true
            }

            field {
                name = "JDA Version"
                @Suppress("SENSELESS_COMPARISON")
                value = when {
                    JDAInfo.COMMIT_HASH == null || JDAInfo.COMMIT_HASH.endsWith("DEV") -> "[${JDAInfo.VERSION}](https://github.com/discord-jda/JDA)"
                    else -> "[${JDAInfo.VERSION}](https://github.com/discord-jda/JDA/commit/${JDAInfo.COMMIT_HASH})"
                }
                inline = true
            }

            field {
                name = "Discord Gateway Version"
                value = JDAInfo.DISCORD_GATEWAY_VERSION.toString()
                inline = true
            }

            field {
                name = "Bot Uptime"
                value = TimeFormat.RELATIVE.format(ManagementFactory.getRuntimeMXBean().startTime)
                inline = true
            }

            field {
                name = "Rest Ping"
                value = "${event.jda.restPing.await()} ms"
                inline = true
            }

            field {
                name = "Gateway Ping"
                value = "${event.jda.gatewayPing} ms"
                inline = true
            }

            field {
                name = "Guild Count"
                value = (event.jda.guildCache.size() + event.jda.unavailableGuilds.size).toString()
                inline = true
            }

            field {
                name = "User Count"
                value = getBotUserCount().toString()
                inline = true
            }
            
            field {
                name = "Quote Count"
                value = "```"+Config.Constants.quotes+"```"
                inline = true
            }

            field {
                name = "Memory(RAM) Usage"
                val memoryUsage = ManagementFactory.getMemoryMXBean().heapMemoryUsage
                value = "%.2f / %d MB".format(
                    memoryUsage.used / 1024.0 / 1024.0,
                    (memoryUsage.max / 1024.0 / 1024.0).toInt()
                )
                inline = true
            }

            footer {
                name = "AutoQuoter Bot"
            }
            color = Config.Constants.EMBED_COLOR
        }
        
        val userid = event.jda.selfUser.id
        
        val inviteButton = Button.link("https://discord.com/api/oauth2/authorize?client_id=$userid&permissions=412317239360&scope=applications.commands+bots", "Invite me")
        val supportButton = Button.link(Config.instance.supportGuildInvite, "Support Server")
        
        event.hook.sendMessageEmbeds(embed).setActionRow(inviteButton, supportButton).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("botinfo", function = ::onCommand) {
            description = "Shows information about the bot."

            botPermissions += Permission.MESSAGE_SEND
            botPermissions += Permission.MESSAGE_EMBED_LINKS
        }
    }

    private suspend fun getBotUserCount(): Int {
        if (combinedUserCountTimer.shouldUpdate()) {
            combinedUserCount = context.jda.guilds
                .map { it.retrieveMetaData() }
                .let { RestAction.allOf(it) }
                .await()
                .sumOf { it.approximateMembers }
        }
        return combinedUserCount
    }
}