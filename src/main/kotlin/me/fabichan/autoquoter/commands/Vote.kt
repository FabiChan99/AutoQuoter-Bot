package me.fabichan.autoquoter.commands

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.components.buttons.Button

@Command
class Vote : GlobalApplicationCommandProvider {
    suspend fun onCommand(
        event: GuildSlashEvent,
    ) {
        val selfUserId = event.jda.selfUser.id
        
        val topgg = "https://top.gg/bot/$selfUserId/vote"
        val wumpusstore = "https://wumpus.store/bot/$selfUserId/vote"
        
        val topggbutton = Button.link(
            topgg,
            "Vote on Top.gg"
        )
        
        val wumpusstorebutton = Button.link(
            wumpusstore,
            "Vote on Wumpus Store"
        )
        val embed = Embed { 
            title = "Vote for me!"
            description = "Vote for me on the following platforms:"
        }
        
        event.reply("Vote for me on the following platforms:")
            .addActionRow(topggbutton, wumpusstorebutton).addEmbeds(embed)
            .await()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("vote", function = ::onCommand) {
            description = "Vote for the bot on various platforms."

            botPermissions += Permission.MESSAGE_SEND
            botPermissions += Permission.MESSAGE_EMBED_LINKS
        }
    }
}