package dev.yuua.due_today

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StringsConfig(
    @SerialName("trial-start")
    val trialStart: String,
    @SerialName("trial-end")
    val trialEnd: String,
    @SerialName("due-date")
    val dueDate: String,
) {
    private fun applyTemplate(template: String, vararg args: Pair<String, String>): String {
        return args.fold(template) { acc, (key, value) ->
            acc.replace("{$key}", value)
        }
    }

    fun formatTrialStart(service: String): String {
        return applyTemplate(trialStart, "service" to service)
    }

    fun formatTrialEnd(service: String, price: String): String {
        return applyTemplate(trialEnd, "service" to service, "price" to price)
    }

    fun formatDueDate(service: String, price: String): String {
        return applyTemplate(dueDate, "service" to service, "price" to price)
    }
}

@Serializable
data class ExchangeRateConfig(
    val to: String,
    val host: String = "api.frankfurter.dev",
)

@Serializable
data class AuthConfig(
    val username: String,
    val password: String,
)

@Serializable
data class ConfigData(
    val port: Int = 8080,
    val auth: AuthConfig? = null,
    @SerialName("exchange-rate")
    val exchangeRateConfig: ExchangeRateConfig? = null,
    @SerialName("strings")
    val stringsConfig: StringsConfig = StringsConfig(
        trialStart = "Trial for {service} start",
        trialEnd = "[{price}] Trial for {service} end",
        dueDate = "[{price}] {service} due today",
    ),
)