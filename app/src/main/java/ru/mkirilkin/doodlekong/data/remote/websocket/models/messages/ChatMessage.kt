package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import ru.mkirilkin.doodlekong.util.Constants.TYPE_CHAT_MESSAGE

data class ChatMessage(
    val from: String,
    val roomName: String,
    val message: String,
    val timeStamp: Long
) : BaseModel(TYPE_CHAT_MESSAGE)
