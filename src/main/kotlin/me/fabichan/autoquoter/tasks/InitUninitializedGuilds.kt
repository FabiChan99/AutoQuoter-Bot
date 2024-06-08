package me.fabichan.autoquoter.tasks

import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.db.Database
import io.github.freya022.botcommands.api.core.db.preparedStatement
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

private val logger = KotlinLogging.logger { }

@BService
class InitUninitializedGuilds(private val database: Database) {

    @BEventListener
    suspend fun onGuildReady(event: GuildReadyEvent) = insertGuildDefaults(event.guild, ready = true)

    @BEventListener
    suspend fun onGuildJoin(event: GuildJoinEvent) = insertGuildDefaults(event.guild, ready = false)

    private suspend fun insertGuildDefaults(guild: Guild, ready: Boolean) {
        val exists = database.preparedStatement("SELECT * FROM guildsettings WHERE guild_id = ?", readOnly = true) {
            executeQuery(guild.idLong).any()
        }

        if (exists) return

        if (ready)
            logger.info { "Guild ${guild.name} (${guild.id}) was not initialized before, initializing" }
        else
            logger.info { "Guild ${guild.name} (${guild.id}) as its the first time the bot is in this guild" }

        database.preparedStatement("INSERT INTO guildsettings (guild_id, crossguildposting) VALUES (?, ?)") {
            executeUpdate(guild.idLong, false)
        }
    }
}