package ru.mkirilkin.doodlekong.ui.setup.set_username

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import ru.mkirilkin.doodlekong.util.Constants.MAX_USERNAME_LENGTH
import ru.mkirilkin.doodlekong.util.Constants.MIN_USERNAME_LENGTH
import ru.mkirilkin.doodlekong.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class UsernameViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    sealed class SetupEvent {
        object InputEmptyError : SetupEvent()
        object InputTooShortError : SetupEvent()
        object InputTooLongError : SetupEvent()

        data class NavigateToSelectRoomEvent(val username: String) : SetupEvent()
    }

    private val _setupEvent = MutableSharedFlow<SetupEvent>()
    val setupEvent: SharedFlow<SetupEvent> = _setupEvent

    fun validateUsernameAndNavigateToSelectRoom(username: String) {
        viewModelScope.launch(dispatchers.main) {
            val trimmedUsername = username.trim()
            when {
                trimmedUsername.isEmpty() -> {
                    _setupEvent.emit(SetupEvent.InputEmptyError)
                }
                trimmedUsername.length < MIN_USERNAME_LENGTH -> {
                    _setupEvent.emit(SetupEvent.InputTooShortError)
                }
                trimmedUsername.length > MAX_USERNAME_LENGTH -> {
                    _setupEvent.emit(SetupEvent.InputTooLongError)
                }
                else -> _setupEvent.emit(SetupEvent.NavigateToSelectRoomEvent(username))
            }
        }
    }
}
