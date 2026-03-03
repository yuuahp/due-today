package dev.yuua

import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path
import kotlin.io.path.notExists

object Store {
    lateinit var configWatcher: FileWatcher<ConfigData>
    val config: ConfigData
        get() = configWatcher.data ?: throw IllegalStateException("Config data is not loaded")

    fun init(configPathString: String) {
        val configPath = Path(configPathString)

        if (configPath.notExists()) {
            throw IllegalArgumentException("Config file not found: $configPath")
        }

        configWatcher = FileWatcher(configPath, ConfigData.serializer())
        runBlocking {
            configWatcher.start()
        }
    }
}