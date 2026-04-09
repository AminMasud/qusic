package com.qusic.noteguesstrainer.navigation

enum class AppDestination(
    val route: String,
    val title: String,
) {
    HOME(route = "home", title = "Note Guess Trainer"),
    GAME(route = "game", title = "Play"),
    PROGRESS(route = "progress", title = "Progress"),
    STATS(route = "stats", title = "Stats"),
    SETTINGS(route = "settings", title = "Settings"),
}
