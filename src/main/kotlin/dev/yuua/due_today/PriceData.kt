package dev.yuua.due_today

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.net.ConnectException

private val logger = KotlinLogging.logger { }

@Serializable
data class PriceExchange(
    val at: LocalDate,
    val from: String,
)

@Serializable
data class PriceData(
    val currency: String,
    val amount: Double,
    val exchange: PriceExchange? = null,
) {
    override fun toString(): String {
        val exchangeCaret = if (exchange != null) "^" else ""
        return "$currency$exchangeCaret ${"%.2f".format(amount)}"
    }

    suspend fun exchangeToOrNull(targetCurrency: String): PriceData? {
        if (currency == targetCurrency) return this

        val frankfurterAPI = Store.frankfurterAPI ?: return null
        val result = try {
            frankfurterAPI.exchange(amount, from = currency, to = targetCurrency)
        } catch (e: ConnectException) {
            logger.error(e) { "Currency exchange from $this to $targetCurrency failed." }
            return null
        }
        return PriceData(targetCurrency, result.amount, PriceExchange(result.date, currency))
    }

    suspend fun exchangeToOrDefault(targetCurrency: String): PriceData {
        return exchangeToOrNull(targetCurrency) ?: this
    }

    suspend fun exchangeConfigured(): PriceData {
        val targetCurrency = Store.config.exchangeRateConfig?.to ?: return this
        return exchangeToOrDefault(targetCurrency)
    }
}
