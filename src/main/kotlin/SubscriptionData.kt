package dev.yuua

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
enum class SubscriptionIntervalType {
    @SerialName("daily")
    DAILY,
    @SerialName("weekly")
    WEEKLY,
    @SerialName("monthly")
    MONTHLY,
    @SerialName("annually")
    ANNUALLY,
}

@Serializable
data class SubscriptionInterval(
    val type: SubscriptionIntervalType,
    val value: Int = 1,
)

@Serializable
data class SubscriptionTrial(
    @SerialName("end_date")
    val endDate: LocalDate,
)

@Serializable
data class SubscriptionData(
    val service: String,
    val url: String? = null,
    val currency: String,
    val amount: Double,
    val trial: SubscriptionTrial? = null,

    @SerialName("start_date")
    val startDate: LocalDate,
    @SerialName("end_date")
    val endDate: LocalDate? = null,
    val interval: SubscriptionInterval,
)