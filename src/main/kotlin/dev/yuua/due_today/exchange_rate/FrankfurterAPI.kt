package dev.yuua.due_today.exchange_rate

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger { }

class FrankfurterAPI(val host: String) {
    val client = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(Resources)
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            host = this@FrankfurterAPI.host
            url {
                protocol = URLProtocol.HTTPS
            }
        }
    }

    val cache = FrankfurterAPICache()

    private suspend fun request(resource: FrankfurterLatestResource): FrankfurterLatestResponse {
        val response = client.get(resource)
        return response.body<FrankfurterLatestResponse>()
    }

    data class ExchangeReturn(
        val amount: Double,
        val date: LocalDate
    )

    suspend fun exchange(amount: Double, from: String, to: String): ExchangeReturn {
        val cached = cache.read(from, to)
        if (cached != null && System.currentTimeMillis() - cached.timestamp < FF_CACHE_ALIVE_MILLIS) {
            logger.info { "Using cached exchange rate for $from to $to: ${cached.rate} (${cached.date})" }
            return ExchangeReturn(amount * cached.rate, cached.date)
        }

        val latest = request(FrankfurterLatestResource(from, listOf(to)))
        val rate = latest.rates[to] ?: throw IllegalStateException("No rate for $from")

        logger.info { "Using fetched exchange rate for $from to $to: $rate (${latest.date})" }

        cache.write(from, to, rate, latest.date)

        return ExchangeReturn(amount * rate, latest.date)
    }
}