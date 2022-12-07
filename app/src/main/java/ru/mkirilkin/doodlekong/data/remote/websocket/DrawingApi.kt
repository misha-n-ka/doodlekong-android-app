package ru.mkirilkin.doodlekong.data.remote.websocket

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow
import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.BaseModel


interface DrawingApi {

    @Receive
    fun observeEvents(): Flow<WebSocket.Event>

    @Send
    fun sendBaseModel(baseModel: BaseModel): Boolean

    @Receive
    fun observeBaseModels(): Flow<BaseModel>
}
