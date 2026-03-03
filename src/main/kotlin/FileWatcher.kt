package dev.yuua

import com.akuleshov7.ktoml.Toml
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.*
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

class FileWatcher<T>(val path: Path, val serializer: KSerializer<T>) {
    var data: T? = null
        private set

    private val scope = CoroutineScope(Dispatchers.IO)
    private val watcher = FileSystems.getDefault().newWatchService()

    /**
     * Refreshes the configuration. If the file does not exist, sets [data] to null.
     * If the file exists but cannot be parsed, logs an error and sets [data] to null.
     */
    private fun refresh() {
        val file = File(path.toString())

        if (!file.exists()) {
            logger.info { "File not found: $path" }
            data = null
            return
        }

        val text = file.readText()
        val parsed = try {
            Toml.decodeFromString<T>(serializer, text)
        } catch (e: SerializationException) {
            logger.error(e) { "Failed to parse file: $path" }
            null
        }

        data = parsed
        logger.info { "File loaded: $path" }
    }

    suspend fun start(): FileWatcher<T> {
        refresh()

        withContext(Dispatchers.IO) {
            Paths.get(path.parent.toString()).register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
        }
        scope.launch {
                while (true) {
                    val key = watcher.take()
                    for (event in key.pollEvents()) {
                        val eventPath = event.context() as Path
                        if (eventPath.name != path.name) continue
                        when (event.kind()) {
                            ENTRY_CREATE, ENTRY_MODIFY -> {
                                logger.info { "File changed: $path" }
                                refresh()
                            }

                            ENTRY_DELETE -> {
                                logger.info { "File deleted: $path" }
                                data = null
                            }
                        }
                    }
                    key.reset()
                }
        }

        return this
    }
}