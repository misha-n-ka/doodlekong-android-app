package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import ru.mkirilkin.doodlekong.util.Constants.TYPE_GAME_STATE

data class GameState(
    val drawingPlayer: String,
    val word: String,
) : BaseModel(TYPE_GAME_STATE)
