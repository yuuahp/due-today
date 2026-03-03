package dev.yuua

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import net.fortuna.ical4j.model.Recur
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.transform.recurrence.Frequency
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
    interval: SubscriptionInterval? = null
): CalendarComponent {
    val uid = Uid(id)

    return VEvent(startDate.toJavaLocalDate(), summary)
        .withProperty(uid)
        .apply {
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

fun getVEventsOf(id: String, subscription: SubscriptionData): List<CalendarComponent> {
    val interval = subscription.interval
    val vEvent = createVEvent(
        id,
        summary = "due(${subscription.priceTag}): ${subscription.service}",
        startDate = if (subscription.trial == null) {
            subscription.startDate
        } else {
            subscription.trial.endDate.plus(interval.value, getDateTimeUnitOf(interval.type))
        },
        endDate = subscription.endDate,
        interval
    )

    val trialVEvents = getTrialVEventsOf(id, subscription)

    return listOf(vEvent, *trialVEvents.toTypedArray())
}

fun getTrialVEventsOf(id: String, subscription: SubscriptionData): List<CalendarComponent> {
    if (subscription.trial == null) return emptyList()

    val startVEvent = createVEvent(
        "$id-trial-start",
        summary = "sub: Trial for ${subscription.service}",
        startDate = subscription.startDate
    )

    val endVEvent = createVEvent(
        "$id-trial-end",
        summary = "due(${subscription.priceTag}): Trial for ${subscription.service} ends",
        startDate = subscription.trial.endDate
    )

    return listOf(startVEvent, endVEvent)
}

