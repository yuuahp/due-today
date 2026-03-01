package dev.yuua

import com.akuleshov7.ktoml.Toml
import kotlinx.serialization.serializer
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Uid
import java.io.File
import java.time.LocalDate
import java.util.Calendar
import net.fortuna.ical4j.model.Calendar as ICalendar

suspend fun main() {
    val file = File("./subscriptions.toml")
    val result = Toml.decodeFromString<Map<String, SubscriptionData>>(serializer(), file.readText())
    println(result)

    val calendar = ICalendar().withProdId("-//yuua//Due Today//EN").withDefaults().fluentTarget

    calendar.withComponent(
        VEvent(LocalDate.of(2026, 3, 3), "Test Event 2")
            .withProperty(Uid("powa"))
            .fluentTarget as CalendarComponent?
    ).fluentTarget

    CalendarServer(calendar).run()
}