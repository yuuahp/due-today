package dev.yuua.due_today

suspend fun main() {
    Store.init("./config.toml") // todo: take arguments
    DueTodayInstance("./subscriptions.toml").serve()
}