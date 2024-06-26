package me.fabichan.autoquoter.commands

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import net.dv8tion.jda.api.Permission

@Command
class GuildCrosspostCommand(
    private val context: BContext,
    private val database: Database

) : GlobalApplicationCommandProvider {
    suspend fun onCommand(
        event: GuildSlashEvent,
        enable: Boolean
    ) {
        event.deferReply().await()

        if (isCrossGuildPostingEnabled(event.guild.id) == enable) {
            event.hook.sendMessage("Cross-guild posting is already ${if (enable) "enabled" else "disabled"}.").queue()
            return
        }

        if (enable) {
            setGuildCrosspost(event.guild.id, true)
            event.hook.sendMessage("Cross-guild posting has been enabled. **You accept that messages from this guild may be quoted in other guilds if the user has access to a message link.**")
                .queue()
        } else {
            setGuildCrosspost(event.guild.id, false)
            event.hook.sendMessage("Cross-guild posting has been disabled.").queue()
        }


    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("guildcrossposting", function = ::onCommand) {
            description = "Enable or disable cross-guild posting."

            userPermissions += Permission.MANAGE_SERVER
            botPermissions += Permission.MESSAGE_SEND
            botPermissions += Permission.MESSAGE_EMBED_LINKS
            option("enable", "enabled") {
                description = "Enables or Disable (True/False) cross-guild posting."
            }
        }
    }

    private suspend fun setGuildCrosspost(guildId: String, crosspost: Boolean) {
        database.preparedStatement("UPDATE guildsettings SET crossguildposting = ? WHERE guild_id = ?") {
            executeUpdate(crosspost, guildId.toLong())
        }
    }

    private suspend fun isCrossGuildPostingEnabled(guildid: String): Boolean {
        return database.preparedStatement("SELECT crossguildposting FROM guildsettings WHERE guild_id = ?") {
            executeQuery(guildid.toLong()).map { it.getBoolean("crossguildposting") }.firstOrNull() ?: false
        }
    }
}
