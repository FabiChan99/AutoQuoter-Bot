package me.fabichan.autoquoter.tasks

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.fabichan.autoquoter.config.Config
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.session.ReadyEvent
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger { }

@BService
class UpdateBotStatusTask(private val database: Database) {
    private val updateBotStatusScope = namedDefaultScope("UpdateBotStatus", 1)

    @BEventListener
    fun onShardReady(event: ReadyEvent) {
        val shard = event.jda
        updateBotStatusScope.launch {
            logger.info { "Starting bot status update task for shard ${shard.shardInfo.shardId}" }
            while (true) {
                try {
                    updatePresence(shard)
                } catch (e: Exception) {
                    logger.error(e) { "Error updating bot status" }
                }

                delay(2.minutes)
            }
        }
    }

    private suspend fun updatePresence(shard: JDA) {
        val shardManager = shard.shardManager!!
        val qouteCount = getQuoteCount().toString()+"x"
        val status = if (shardManager.guildCache.size() > 0) OnlineStatus.ONLINE else OnlineStatus.IDLE
        val activity =
            createActivity("Quoted $qouteCount | on ${shardManager.guildCache.size()} servers | on Shard ${shard.shardInfo.shardId} • ${shard.gatewayPing}ms ping")

        shard.presence.setPresence(status, activity, false)
    }

    private fun createActivity(activityText: String): Activity =
        Activity.customStatus(activityText)
    
    
    private suspend fun getQuoteCount(): Int {
        database.preparedStatement("SELECT COUNT(*) FROM autoquoter.public.qoutestats") {
            executeQuery().use { rs ->
                rs.next()
                val i = rs.getInt(1)
                Config.Constants.quotes = i.toString()
                return i
            }
        }
    }
}