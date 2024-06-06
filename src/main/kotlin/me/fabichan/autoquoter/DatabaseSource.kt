package me.fabichan.autoquoter

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import me.fabichan.autoquoter.config.Config
import org.flywaydb.core.Flyway
import kotlin.time.Duration.Companion.seconds

private val logger by lazy { KotlinLogging.logger {} }


@BService
class DatabaseSource(config: Config) : HikariSourceSupplier {
    override val source = HikariDataSource(HikariConfig().apply {
        jdbcUrl = config.databaseConfig.url
        username = config.databaseConfig.user
        password = config.databaseConfig.password

        maximumPoolSize = 5
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        createFlyway("bc", "bc_database_scripts").migrate()
        createFlyway("public", "dbmigrations").migrate()

        logger.info { "Created database source" }
    }

    private fun createFlyway(schema: String, scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(source)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()

}