package dev.yuua.due_today

import dev.yuua.due_today.exchange_rate.FrankfurterAPI
import kotlinx.coroutines.runBlocking
import kotlin.getValue
import kotlin.io.path.Path
import kotlin.io.path.notExists

object Store {
    lateinit var configWatcher: FileWatcher<ConfigData>
    val config: ConfigData
        get() = configWatcher.data ?: throw IllegalStateException("Config data is not loaded")

    val frankfurterAPI by lazy {
        val exchangeRateConfig = config.exchangeRateConfig ?: return@lazy null
        FrankfurterAPI(exchangeRateConfig.host)
    }

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