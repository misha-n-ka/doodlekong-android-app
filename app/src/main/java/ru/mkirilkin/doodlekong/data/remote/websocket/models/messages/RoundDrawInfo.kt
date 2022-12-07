package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import ru.mkirilkin.doodlekong.util.Constants.TYPE_CURRENT_ROUND_DRAW_INFO

data class RoundDrawInfo(
    val data: List<String>
) : BaseModel(TYPE_CURRENT_ROUND_DRAW_INFO)
