package dev.yuua

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the type of subscription cycle.
 *
 * DAILY:
 *     Daily basis. E.g., every day, every 3 days, etc.
 *
 * WEEKLY:
 *     Weekly basis. E.g., every week, every 2 weeks, etc.
 *
 * MONTHLY:
 *     Monthly basis on a relative day. E.g., every month on the 15th, every month on the 31st, etc.
 *     Note: If a subscription starts on the 31st of a month, the billing date will be the 30th on shorter months, or 28th of February.
 *
 * ANNUALLY:
 *     Yearly basis on a relative day. E.g., every year on the 15th of March, every year on the 31st of January, etc.
 *     Note: If a subscription starts on the 29th of February, the billing date will be the 28th of February on non-leap years.
 */
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