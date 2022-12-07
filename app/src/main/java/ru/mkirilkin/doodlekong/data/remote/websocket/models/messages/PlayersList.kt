package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import com.plcourse.mkirilkin.data.PlayerData
import ru.mkirilkin.doodlekong.util.Constants.TYPE_PLAYERS_LIST

data class PlayersList(
    val players: List<PlayerData>
) : BaseModel(TYPE_PLAYERS_LIST)
