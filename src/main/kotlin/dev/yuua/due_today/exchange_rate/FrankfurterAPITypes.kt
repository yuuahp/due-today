package dev.yuua.due_today.exchange_rate

import io.ktor.resources.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Resource("/v1/latest")
class FrankfurterLatestResource(
    val base: String? = null,
    val symbols: List<String>? = null,
)

@Serializable
data class FrankfurterLatestResponse(
    val base: String,
    val date: LocalDate,
    val rates: Map<String, Double>,
)
