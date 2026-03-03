package dev.yuua

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StringsData(
    @SerialName("trial_start")
    val trialStart: String,
    @SerialName("trial_end")
    val trialEnd: String,
    @SerialName("due_date")
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

    fun formatTrialEnd(service: String, currency: String, amount: String): String {
        return applyTemplate(trialEnd, "service" to service, "currency" to currency, "amount" to amount)
    }

    fun formatDueDate(service: String, currency: String, amount: String): String {
        return applyTemplate(dueDate, "service" to service, "currency" to currency, "amount" to amount)
    }
}

@Serializable
data class ConfigData(
    val port: Int = 8080,
    val rate: String? = null,
    val strings: StringsData = StringsData(
        trialStart = "Trial for {service} start",
        trialEnd = "[{currency} {amount}] Trial for {service} end",
        dueDate = "[{currency} {amount}] {service} due today",
    ),
)