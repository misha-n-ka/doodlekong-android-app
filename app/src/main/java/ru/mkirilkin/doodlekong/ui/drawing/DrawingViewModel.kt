package ru.mkirilkin.doodlekong.ui.drawing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mkirilkin.doodlekong.R
import com.plcourse.mkirilkin.data.models.messages.Ping
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mkirilkin.doodlekong.data.remote.websocket.DrawingApi
import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.*
import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.DrawAction.Companion.ACTION_UNDO
import ru.mkirilkin.doodlekong.util.CoroutineTimer
import ru.mkirilkin.doodlekong.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val gson: Gson,
    private val drawingApi: DrawingApi
) : ViewModel() {

    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class GameStateEvent(val data: GameState) : SocketEvent()
        data class DrawDataEvent(val data: DrawData) : SocketEvent()
        data class NewWordsEvent(val data: NewWords) : SocketEvent()
        data class ChosenWordEvent(val data: ChosenWord) : SocketEvent()
        data class GameErrorEvent(val data: GameError) : SocketEvent()
        data class RoundDrawInfoEvent(val data: RoundDrawInfo) : SocketEvent()
        object UndoEvent : SocketEvent()
    }

    private val _newWords = MutableStateFlow(NewWords(listOf()))
    val newWords: StateFlow<NewWords> = _newWords

    private val _chat = MutableStateFlow<List<BaseModel>>(listOf())
    val chat: StateFlow<List<BaseModel>> = _chat

    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId: StateFlow<Int> = _selectedColorButtonId

    private val _phase = MutableStateFlow(PhaseChange(null, 0L, null))
    val phase: StateFlow<PhaseChange> = _phase

    private val _phaseTime = MutableStateFlow(0L)
    val phaseTime: StateFlow<Long> = _phaseTime

    private val _connectionProgressBarVisible = MutableStateFlow(true)
    val connectionProgressBarVisible: StateFlow<Boolean> = _connectionProgressBarVisible

    private val _chooseWordOverlayVisible = MutableStateFlow(false)
    val chooseWordOverlayVisible: StateFlow<Boolean> = _chooseWordOverlayVisible

    private val connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    private val socketEventChannel = Channel<SocketEvent>()
    val socketEvent = socketEventChannel.receiveAsFlow().flowOn(dispatchers.io)

    private val timer = CoroutineTimer()
    private var timerJob: Job? = null


    init {
        observeBaseModels()
        observeEvents()
    }

    fun checkRadioButton(id: Int) {
        _selectedColorButtonId.value = id
    }

    fun sendBaseModel(data: BaseModel) {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.sendBaseModel(data)
        }
    }

    fun sendChatMessage(message: ChatMessage) {
        if (message.message.trim().isEmpty()) {
            return
        }
        viewModelScope.launch(dispatchers.io) {
            drawingApi.sendBaseModel(message)
        }
    }

    fun chooseWord(word: String, roomName: String) {
        val chosenWord = ChosenWord(word, roomName)
        sendBaseModel(chosenWord)
    }

    fun setChooseWordOverlayVisible(isVisible: Boolean) {
        _chooseWordOverlayVisible.value = isVisible
    }

    fun setConnectionProgressBarVisible(isVisible: Boolean) {
        _connectionProgressBarVisible.value = isVisible
    }

    fun cancelTimer() {
        timerJob?.cancel()
    }

    private fun setTimer(duration: Long) {
        timerJob?.cancel()
        timerJob = timer.timeAndEmit(duration, viewModelScope) {
            _phaseTime.value = it
        }
    }

    private fun observeEvents() {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.observeEvents().collect { event ->
                connectionEventChannel.send(event)
            }
        }
    }

    private fun observeBaseModels() {
        viewModelScope.launch(dispatchers.io) {
            drawingApi.observeBaseModels().collect { data ->
                when (data) {
                    is DrawData -> {
                        socketEventChannel.send(SocketEvent.DrawDataEvent(data))
                    }
                    is DrawAction -> {
                        when (data.action) {
                            ACTION_UNDO -> socketEventChannel.send(SocketEvent.UndoEvent)
                        }
                    }
                    is ChatMessage -> {
                        socketEventChannel.send(SocketEvent.ChatMessageEvent(data))
                    }
                    is Announcement -> {
                        socketEventChannel.send(SocketEvent.AnnouncementEvent(data))
                    }
                    is NewWords -> {
                        _newWords.value = data
                        socketEventChannel.send(SocketEvent.NewWordsEvent(data))
                    }
                    is PhaseChange -> {
                        data.phase?.let {
                            _phase.value = data
                        }
                        _phaseTime.value =  data.time
                        if (data.phase != Room.Phase.WAITING_FOR_PLAYERS) {
                            setTimer(data.time)
                        }
                    }
                    is ChosenWord -> socketEventChannel.send(SocketEvent.ChosenWordEvent(data))
                    is GameError -> socketEventChannel.send(SocketEvent.GameErrorEvent(data))
                    is Ping -> sendBaseModel(Ping())
                }
            }
        }
    }
}
