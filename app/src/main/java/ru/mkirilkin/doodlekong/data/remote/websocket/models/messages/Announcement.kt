package ru.mkirilkin.doodlekong.data.remote.websocket.models.messages

import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.BaseModel
import ru.mkirilkin.doodlekong.util.Constants.TYPE_ANNOUNCEMENT

data class Announcement(
    val message: String,
    val timeStamp: Long,
    val announcementType: Int
) : BaseModel(TYPE_ANNOUNCEMENT) {

    companion object {
        const val TYPE_PLAYER_GUESSES_WORD = 0
        const val TYPE_PLAYER_JOINED = 1
        const val TYPE_PLAYER_LEFT = 2
        const val TYPE_EVERYBODY_GUESSED_IT = 3
    }
}
