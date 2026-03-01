package dev.yuua

import io.ktor.http.ContentType
import io.ktor.http.HeaderValueParam
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar as ICalendar

class CalendarServer(val calendar: ICalendar) {
    suspend fun run() {
        embeddedServer(Netty, port = 8080) {
            routing {
                get("/") {
                    call.respondOutputStream(
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