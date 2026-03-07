package dev.yuua.due_today.exchange_rate

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import dev.yuua.due_today.exchange_rate.FrankfurterLatestCache as CacheTable

private val logger = KotlinLogging.logger { }

const val FF_CACHE_ALIVE_MILLIS = 60 * 60 * 1000L // 1 heure

object FrankfurterLatestCache : Table("frankfurter_latest") {
    val id = integer("id").autoIncrement()
    val timestamp = long("timestamp")
    val from = varchar("from_currency", 3)
    val to = varchar("to_currency", 3)
    val rate = double("rate")
    val date = long("date")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("from_to_index", from, to)
    }
}

class FrankfurterAPICache {
    fun write(from: String, to: String, rate: Double, date: LocalDate) {
        logger.info { "Writing cache for $from to $to: $rate (${date})" }
        val timestamp = System.currentTimeMillis()
        transaction {
            CacheTable.upsert(CacheTable.from, CacheTable.to) {
                it[CacheTable.from] = from
                it[CacheTable.to] = to
                it[CacheTable.rate] = rate
                it[CacheTable.timestamp] = timestamp
                it[CacheTable.date] = date.toEpochDays()
            }
        }
    }

    data class ReadReturn(val rate: Double, val date: LocalDate, val timestamp: Long)

    fun read(from: String, to: String): ReadReturn? {
        logger.info { "Reading cache for $from to $to" }
        return transaction {
            val query = CacheTable
                .select(CacheTable.rate, CacheTable.date, CacheTable.timestamp)
                .where {
                    (CacheTable.from eq from) and (CacheTable.to eq to)
                }

            if (query.empty()) {
                return@transaction null
            }

            val result = query.first()
            val timestamp = result[CacheTable.timestamp]
            val date = result[CacheTable.date].let { LocalDate.fromEpochDays(it) }
            val rate = result[CacheTable.rate]

            return@transaction ReadReturn(rate, date, timestamp)
        }
    }

    init {
        Database.connect("jdbc:h2:mem:due_today;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(CacheTable)
            logger.info { "Table initialized" }
        }
    }
}
