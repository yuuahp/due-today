package dev.yuua

suspend fun main() {
    DueTodayInstance("./subscriptions.toml").serve()
}