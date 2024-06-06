package me.fabichan.autoquoter.commands

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.BContext
import me.fabichan.autoquoter.config.Config
import me.fabichan.autoquoter.utils.UpdateTimer
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.TimeFormat
import java.lang.management.ManagementFactory
import kotlin.time.Duration.Companion.minutes

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