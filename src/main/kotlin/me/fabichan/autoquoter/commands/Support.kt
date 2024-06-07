package me.fabichan.autoquoter.commands

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.Permission

@Command
class Support : GlobalApplicationCommandProvider {
    suspend fun onCommand(
        event: GuildSlashEvent,
    ) {
        event.deferReply().await()

        val server = Config.instance.supportGuildInvite

        event.hook.sendMessage(server).setEphemeral(true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("support", function = ::onCommand) {
            description = "Support Server Invite"

            botPermissions += Permission.MESSAGE_SEND
            botPermissions += Permission.MESSAGE_EMBED_LINKS
        }
    }
}