package ru.mkirilkin.doodlekong.data.remote.websocket.models

import com.google.gson.annotations.SerializedName

data class Room(
    @SerializedName("name") val name: String,
    @SerializedName("maxPlayers") val maxPlayers: Int,
    @SerializedName("playersCount") val playersCount: Int = 1,
) {
    enum class Phase {
        WAITING_FOR_PLAYERS,
        WAITING_FOR_START,
        NEW_ROUND,
        GAME_RUNNING,
        SHOW_WORD
    }
}
