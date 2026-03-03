package dev.yuua

import kotlin.io.path.Path

suspend fun main() {
    Store.init("./config.toml") // todo: take arguments
    DueTodayInstance("./subscriptions.toml").serve()
}