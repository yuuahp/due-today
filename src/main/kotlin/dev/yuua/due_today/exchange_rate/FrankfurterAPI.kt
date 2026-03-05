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

    private suspend fun request(resource: FrankfurterLatestResource): FrankfurterAPIResponse {
        val response = client.get(resource)
        return response.body<FrankfurterAPIResponse>()
    }

    data class ExchangeReturn(
        val amount: Double,
        val date: LocalDate
    )

    suspend fun exchange(amount: Double, from: String, to: String): ExchangeReturn {
        val latest = request(FrankfurterLatestResource(from, listOf(to)))
        val rate = latest.rates[to] ?: throw IllegalStateException("No rate for $from")
        return ExchangeReturn(amount * rate, latest.date)
    }
}