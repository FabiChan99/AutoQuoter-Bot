package me.fabichan.autoquoter.events

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.session.ReadyEvent

private val logger = KotlinLogging.logger { }

@BService
class ReadyListener {
    @BEventListener
    fun onReady(event: ReadyEvent) {
        val jda = event.jda
        val shardInfo = jda.shardInfo
        logger.info { "Shard ${shardInfo.shardId} is ready" }
        logger.info { "Bot connected as ${jda.selfUser.name}" }
        logger.info { "The Bot is on ${jda.guildCache.size()} guilds in total" }
        logger.info { "The Bots Shard with the ID ${shardInfo.shardId} is present on the following guilds:" }
        for (guild in jda.guildCache) {
            logger.info { "\t- ${guild.name} (${guild.id}) with ${guild.memberCount} members" }
        }
    }
}