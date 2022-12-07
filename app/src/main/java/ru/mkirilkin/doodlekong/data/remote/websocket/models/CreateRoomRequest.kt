package ru.mkirilkin.doodlekong.data.remote.websocket.models

data class CreateRoomRequest(
    val name: String,
    val maxPlayers: Int
)
