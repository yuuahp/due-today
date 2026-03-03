package dev.yuua

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.io.path.notExists
import net.fortuna.ical4j.model.Calendar as ICalendar

private val logger = KotlinLogging.logger {}

class DueTodayInstance {
    val watcher: FileWatcher
    val server: CalendarServer

    private val calendarTemplate = ICalendar().withProdId("-//yuua//Due Today//EN").withDefaults().fluentTarget

    constructor(pathString: String) {
        val path = Path(pathString)

        if (path.notExists()) {
            throw IllegalArgumentException("Config file not found: $path")
        }

        watcher = FileWatcher(path)

        runBlocking {
            watcher.start()
        }

        server = CalendarServer {
            val calendar = calendarTemplate.copy()

            val config = watcher.config ?: run {
                logger.warn { "Config file not found, or not valid: $path" }
                return@CalendarServer calendar
            }

            for ((id, subscription) in config) {
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