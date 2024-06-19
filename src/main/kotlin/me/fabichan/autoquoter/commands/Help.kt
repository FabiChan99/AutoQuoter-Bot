package me.fabichan.autoquoter.commands

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.Permission

@Command
class Help : GlobalApplicationCommandProvider {
    suspend fun onCommand(
        event: GuildSlashEvent,
    ) {
        val selfUserId = event.jda.selfUser.id
        val text = """
            Welcome to AutoQuoter! Here is a list of commands and features to help you get started:
            
            **Commands:**
            ─ `/help`: Displays this help message.
            ─ `/vote`: Vote for AutoQuoter on various platforms.
            ─ `/support`: Get the invite link to the AutoQuoter support server.
            ─ `/botinfo`: Get information about AutoQuoter.
            ─ `/guildcrossposting`: Enable or disable cross-guild posting.
            
            **Features:**
            ─ **Effortless Crossposting:** Automatically embed messages from provided links with no additional commands needed. Just paste the link, and AutoQuoter will embed it for you.
            ─ **Organized Conversations:** Easily share and reference important messages across different channels by embedding message links.
            
            **Coming Soon:**
            ─ **Bookmarking:** Save and access your favorite quotes quickly with the upcoming bookmarking feature.
            
            **FAQ:**
            ─ **How do I use AutoQuoter?**
              Simply paste the message link you want to embed in the chat, and AutoQuoter will automatically embed it for you.
            
            ─ **Can I disable AutoQuoter in specific channels?**
              Yes, server admins can configure AutoQuoter settings per channel using Discord's built-in permissions system.
            
            **Support:**
            For further assistance, visit our [Support Server](https://support.autoquoter.xyz).
            
            [**Invite AutoQuoter to your server now and enjoy effortless crossposting!**](https://get.autoquoter.xyz)
            
            Thank you for using AutoQuoter!"""

        val embed = Embed {
            title = "AutoQuoter Help"
            description = text
            color = Config.Constants.EMBED_COLOR
        }

        event.replyEmbeds(embed).await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("help", function = ::onCommand) {
            description = "Get help with the bot."

            botPermissions += Permission.MESSAGE_SEND
            botPermissions += Permission.MESSAGE_EMBED_LINKS
        }
    }
}
