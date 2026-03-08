package dev.yuua.due_today

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.Url
import net.fortuna.ical4j.transform.recurrence.Frequency
import java.net.URI
import java.time.ZonedDateTime

fun getFrequencyOf(intervalType: SubscriptionIntervalType): Frequency {
    return when (intervalType) {
        SubscriptionIntervalType.DAILY -> Frequency.DAILY
        SubscriptionIntervalType.WEEKLY -> Frequency.WEEKLY
        SubscriptionIntervalType.MONTHLY -> Frequency.MONTHLY
        SubscriptionIntervalType.ANNUALLY -> Frequency.YEARLY
    }
}

fun getDateTimeUnitOf(intervalType: SubscriptionIntervalType): DateTimeUnit.DateBased {
    return when (intervalType) {
        SubscriptionIntervalType.DAILY -> DateTimeUnit.DAY
        SubscriptionIntervalType.WEEKLY -> DateTimeUnit.WEEK
        SubscriptionIntervalType.MONTHLY -> DateTimeUnit.MONTH
        SubscriptionIntervalType.ANNUALLY -> DateTimeUnit.YEAR
    }
}

const val SHORTEST_MONTH_DAYS = 28

fun createVEvent(
    id: String, summary: String,
    startDate: LocalDate, endDate: LocalDate? = null,
    interval: SubscriptionInterval? = null,
    description: String? = null,
    url: String? = null,
): CalendarComponent {
    val uid = Uid(id)

    return VEvent(startDate.toJavaLocalDate(), summary)
        .withProperty(uid)
        .apply {
            if (description != null) {
                withProperty(Description(description))
            }

            if (url != null) {
                withProperty(Url(URI(url)))
            }

            if (interval == null) return@apply

            val frequency = getFrequencyOf(interval.type)
            val recurBuilder = Recur.Builder<ZonedDateTime>()
                .frequency(frequency)
                .interval(interval.value)
                .setPosList(-1) // choose the last valid candidate of the list
                .apply {
                    if (frequency == Frequency.MONTHLY) {
                        if (startDate.day > SHORTEST_MONTH_DAYS) { // 29th, 30th, 31st
                            monthDayList((SHORTEST_MONTH_DAYS..startDate.day).toList())
                        } else {
                            monthDayList(listOf(startDate.day))
                        }
                    }
                }

            if (endDate != null) {
                recurBuilder.until(endDate.toJavaLocalDate().atStartOfDay(ZonedDateTime.now().zone))
            }

            val rRule = RRule(recurBuilder.build())

            withProperty(rRule)
        }
        .fluentTarget as CalendarComponent
}

suspend fun getVEventsOf(id: String, subscription: SubscriptionData): List<CalendarComponent> {
    val interval = subscription.interval
    val exchanged = subscription.price.exchangeConfigured()
    val exchangeDescription = if (exchanged.exchange != null) {
        "Original price is ${subscription.price}, which is $exchanged at the rate at ${exchanged.exchange.at}."
    } else {
        "The price is ${subscription.price}."
    }
    val intervalCap = subscription.interval.type.capitalized()
    val vEvent = createVEvent(
        id,
        summary = Store.config.stringsConfig.formatDueDate(
            subscription.service,
            subscription.price.exchangeConfigured().toString()
        ),
        startDate = if (subscription.trial == null) {
            subscription.startDate
        } else {
            subscription.trial.endDate.plus(interval.value, getDateTimeUnitOf(interval.type))
        },
        endDate = subscription.endDate,
        interval,
        description = """
            $intervalCap payment of ${subscription.service} due today.
            $exchangeDescription
        """.trimIndent(),
        url = subscription.url
    )

    val trialVEvents = getTrialVEventsOf(id, subscription)

    return listOf(vEvent, *trialVEvents.toTypedArray())
}

suspend fun getTrialVEventsOf(id: String, subscription: SubscriptionData): List<CalendarComponent> {
    if (subscription.trial == null) return emptyList()

    val startVEvent = createVEvent(
        "$id-trial-start",
        summary = Store.config.stringsConfig.formatTrialStart(subscription.service),
        startDate = subscription.startDate
    )

    val endVEvent = createVEvent(
        "$id-trial-end",
        summary = Store.config.stringsConfig.formatTrialEnd(
            subscription.service,
            subscription.price.exchangeConfigured().toString()
        ),
        startDate = subscription.trial.endDate
    )

    return listOf(startVEvent, endVEvent)
}

