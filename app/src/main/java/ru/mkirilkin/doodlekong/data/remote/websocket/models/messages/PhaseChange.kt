package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import ru.mkirilkin.doodlekong.util.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
    var phase: Room.Phase?,
    var time: Long,
    val drawingPlayer: String? = null
) : BaseModel(TYPE_PHASE_CHANGE)
