package dev.yuua.due_today

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.ContentType
import io.ktor.http.HeaderValueParam
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar as ICalendar

private val logger = KotlinLogging.logger {}

class CalendarServer(val calendarProvider: suspend () -> ICalendar) {
    suspend fun run() {
        logger.info { "Starting calendar server..." }
        embeddedServer(Netty, port = Store.config.port) {
            routing {
                get("/") {
                    val calendar = try {
                        calendarProvider()
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to generate calendar" }
                        call.respond(HttpStatusCode.InternalServerError, "Failed to generate calendar: ${e.message}")
                        return@get
                    }

                    call.respondOutputStream(
                        // text/calendar; charset=utf-8
                        ContentType("text", "calendar", listOf(HeaderValueParam("charset", "utf-8"))),
                        HttpStatusCode.OK
                    ) {
                        CalendarOutputter().output(calendar, this)
                    }
                }
            }
        }.startSuspend(wait = true)
    }
}