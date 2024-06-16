package me.fabichan.autoquoter.events

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent

private val logger = KotlinLogging.logger { }

@BService
class JoinLeaveLogging {

    @BEventListener
    suspend fun onJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val logWebhookUrl = Config.instance.joinLeaveLogWebhook
        val webhook = WebhookClient.createClient(event.jda, logWebhookUrl)

        logger.info { "Joined guild ${guild.name} (${guild.id}) with ${guild.memberCount} members" }

        val embed = Embed {
            title = "Joined Guild"
            description = "Joined guild ${guild.name} (${guild.id}) with ${guild.memberCount} members"
            field {
                name = "Owner"
                value = "``${event.jda.retrieveUserById(guild.ownerIdLong).await().name}`` (``${guild.ownerId}``)"
                inline = false
            }
            field {
                name = "Usercount"
                value = event.guild.memberCount.toString()
                inline = false
            }
            field {
                name = "Guild Creation Date"
                value = guild.timeCreated.toString()
                inline = false
            }
            color = Config.Constants.EMBED_COLOR
        }

        webhook.sendMessageEmbeds(embed).queue()
    }


    @BEventListener
    suspend fun onLeave(event: GuildLeaveEvent) {
        val guild = event.guild
        val logWebhookUrl = Config.instance.joinLeaveLogWebhook
        val webhook = WebhookClient.createClient(event.jda, logWebhookUrl)

        logger.info { "Left guild ${guild.name} (${guild.id}) with ${guild.memberCount} members" }

        val embed = Embed {
            title = "Left Guild"
            description = "Left guild ${guild.name} (${guild.id}) with ${guild.memberCount} members"
            field {
                name = "Owner"
                value = "``${event.jda.retrieveUserById(guild.ownerIdLong).await().name}`` (``${guild.ownerId}``)"
                inline = false
            }
            field {
                name = "Usercount"
                value = event.guild.memberCount.toString()
                inline = false
            }
            field {
                name = "Guild Creation Date"
                value = guild.timeCreated.toString()
                inline = false
            }
            color = Config.Constants.EMBED_COLOR
        }

        webhook.sendMessageEmbeds(embed).queue()
    }
}