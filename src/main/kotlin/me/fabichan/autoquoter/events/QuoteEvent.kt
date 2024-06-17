package me.fabichan.autoquoter.events

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.generics.getChannel
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.MessageCreate
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.awaitOrNullOn
import io.github.oshai.kotlinlogging.KotlinLogging
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.AttachedFile
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import nonapi.io.github.classgraph.json.Id
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days

private val logger = KotlinLogging.logger { }

@BService
class QuoteEvent(private val database: Database) {

    @BEventListener
    suspend fun onMessage(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        if (!event.isFromGuild) return
        if (event.message.contentRaw.startsWith("!")) return // message starting with ! are not quoted ("Bypass Quoting") 

        processMessageWithLinks(event)
    }

    private val messageUrlRegex = Regex(
        "(?:https?://)?(?:\\w+\\.)?discord(?:app)?\\.com/channels/(\\d+)/(\\d+)/(\\d+)",
        RegexOption.IGNORE_CASE
    )
    
    private suspend fun isCrossGuildPostingEnabled(guildid: String): Boolean {
        return database.preparedStatement("SELECT crossguildposting FROM guildsettings WHERE guild_id = ?") {
            executeQuery(guildid.toLong()).map { it.getBoolean("crossguildposting") }.firstOrNull() ?: false
        }
    }


    private suspend fun retrieveMessagesByLink(content: String, jda: JDA, postedGuildId: String): List<Message> {
        val processedMessageIdsHashSet = HashSet<String>()
        return messageUrlRegex.findAll(content).toList()
            .map { it.destructured }
            .mapNotNull { (guildId, channelId, messageId) ->
                if (processedMessageIdsHashSet.contains(messageId)) {
                    return@mapNotNull null
                }

                val currentGuild = jda.guilds.firstOrNull { it.id == guildId }

                if (currentGuild == null || (!isCrossGuildPostingEnabled(currentGuild.id) && currentGuild.id != postedGuildId)) {
                    return@mapNotNull null
                }

                val channel = currentGuild.getChannel<GuildMessageChannel>(channelId)
                    ?: return@mapNotNull null

                val message = channel.retrieveMessageById(messageId).awaitOrNullOn(ErrorResponse.UNKNOWN_MESSAGE)

                if (message != null) {
                    processedMessageIdsHashSet.add(messageId)
                }

                message
            }
    }




    private suspend fun processMessageWithLinks(event: MessageReceivedEvent, guildid: String = event.guild.id) {
        val messages = retrieveMessagesByLink(event.message.contentRaw, event.jda, guildid)
        for (i in 0 until minOf(3, messages.size)) {
            val message = messages[i]
            try {
                
                if (message.guild.id == event.guild.id){
                    val button = Button.link("https://discord.com/channels/${message.guild.id}/${message.channel.id}/${message.id}", "Jump to message")
                    val m = BuildQuoteEmbed(message, event.guild, event.member)
                    event.message.reply(m).setActionRow(button).mentionRepliedUser(false).queue()
                    recordQuoteStats(message, event)
                    continue
                }
                val m = BuildQuoteEmbed(message, event.guild, event.member)
                event.message.reply(m).mentionRepliedUser(false).queue()
                recordQuoteStats(message, event)
            } catch (e: Exception) {

            }
        }
    }

    private suspend fun BuildQuoteEmbed(quotedMessage: Message, eventGuild: Guild, selfUser: Member?): MessageCreateData {
        var ftitle = "AutoQuoter"
        
        if (quotedMessage.guild.id != eventGuild.id) {
            ftitle += " - External Message from ${quotedMessage.guild.name}"
        }
        val eb = EmbedBuilder { }


        if (quotedMessage.embeds.isEmpty()) {
            eb.footer {
                name = ftitle
            }
            eb.timestamp = quotedMessage.timeCreated

            if (quotedMessage.contentRaw.isNotEmpty()) {
                val text = "\"" + quotedMessage.contentRaw + "\""
                if (quotedMessage.contentRaw.length > 4096) {
                    eb.description = text.truncate(4089) + " [...]"
                } else {
                    eb.description = text
                }

                if (quotedMessage.attachments.isNotEmpty()) {
                    val attachment = quotedMessage.attachments[0]
                    if (attachment.isImage) {
                        eb.image = attachment.url
                    }
                    
                    if (attachment.isVideo) {
                        eb.description = "Videos can't be quoted"
                    }
                }
            }

            else if (quotedMessage.stickers.isNotEmpty() && quotedMessage.contentRaw.isEmpty()) {
                eb.image = quotedMessage.stickers[0].iconUrl
            }
            
            else if (quotedMessage.attachments.isNotEmpty() && quotedMessage.contentRaw.isEmpty()) {
                val attachment = quotedMessage.attachments[0]
                if (attachment.isImage) {
                    eb.image = attachment.url
                }
                if (attachment.isVideo) {
                    eb.description = "*Videos can't be quoted*"
                }
            }
        } else {
            val oldEmbed = quotedMessage.embeds[0]
            
            // check image 
            if (oldEmbed.image != null) {
                eb.image = oldEmbed.image?.url
            } else if (quotedMessage.attachments.isNotEmpty()) {
                val attachment = quotedMessage.attachments[0]
                if (attachment.isImage) {
                    eb.image = attachment.url
                }
            }

            for (field in oldEmbed.fields) {
                eb.field {
                    name = field.name.toString()
                    value = field.value.toString()
                    inline = field.isInline
                }
            }

            eb.description = oldEmbed.description


            if (oldEmbed.footer != null) {
                eb.footer {
                    name = (oldEmbed.footer?.text + " - " + ftitle).truncate(256)
                }
            } else {
                eb.footer {
                    name = ftitle
                }
            }

            eb.timestamp = quotedMessage.timeCreated

        }

        eb.author {
            name = "Sent by " + getUserName(quotedMessage.author)
            iconUrl = quotedMessage.author.effectiveAvatarUrl
        }
        
        eb.color = selfUser?.colorRaw ?: Config.Constants.EMBED_COLOR

        val embed = eb.build()

        val msgdata = MessageCreate {
            embeds += embed
        }
        return msgdata
    }


    private fun String.truncate(length: Int): String {
        return if (this.length > length) {
            this.substring(0, length)
        } else {
            this
        }
    }

    @Suppress("DEPRECATION")
    private fun getUserName(user: User): String = when {
        user.isBot -> user.asTag
        else -> user.name
    }


    private suspend fun recordQuoteStats(quotedMessage: Message, event: MessageReceivedEvent) {
        logger.info { "Quoted message from ${quotedMessage.author.name} (${quotedMessage.author.id}) from ${quotedMessage.guild.name}/${quotedMessage.channel.name} (${quotedMessage.guild.id}/${quotedMessage.channel.id}) in ${event.guild.name}/${event.channel.name} (${event.guild.id}/${event.channel.id})" }
        database.preparedStatement("INSERT INTO public.qoutestats (user_id, channel_id, guild_id, timestamp) VALUES (?, ?, ?, ?)") {
            executeUpdate(
                quotedMessage.author.idLong,
                quotedMessage.channel.idLong,
                quotedMessage.guild.idLong,
                quotedMessage.timeCreated.toEpochSecond()
            )
        }
    }
}