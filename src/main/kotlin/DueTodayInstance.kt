package dev.yuua

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.io.path.Path
import kotlin.io.path.notExists
import net.fortuna.ical4j.model.Calendar as ICalendar

private val logger = KotlinLogging.logger {}

typealias SubscriptionMap = Map<String, SubscriptionData>

class DueTodayInstance {

    val subWatcher: FileWatcher<SubscriptionMap>
    val server: CalendarServer

    private val calendarTemplate = ICalendar().withProdId("-//yuua//Due Today//EN").withDefaults().fluentTarget

    constructor(subPathString: String) {
        val subPath = Path(subPathString)


        if (subPath.notExists()) {
            throw IllegalArgumentException("Subscription file not found: $subPath")
        }


        subWatcher = FileWatcher(subPath, MapSerializer(String.serializer(), SubscriptionData.serializer()))

        runBlocking {
            subWatcher.start()
        }

        server = CalendarServer {
            val calendar = calendarTemplate.copy()

            val subMap = subWatcher.data ?: run {
                logger.warn { "Subscription file not found, or not valid: $subPath" }
                return@CalendarServer calendar
            }

            for ((id, subscription) in subMap) {
                val events = getVEventsOf(id, subscription)
                for (event in events) {
                    calendar.withComponent(event)
                }
            }

            calendar
        }
    }

    suspend fun serve() {
        server.run()
    }
}