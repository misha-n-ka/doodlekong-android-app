package ru.mkirilkin.doodlekong.ui.drawing

import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mkirilkin.doodlekong.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.mkirilkin.doodlekong.data.remote.websocket.DrawingApi
import ru.mkirilkin.doodlekong.util.DispatcherProvider
import javax.inject.Inject

@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val gson: Gson,
    private val drawingApi: DrawingApi
) : ViewModel() {

    private val _selectedColorButtonId = MutableStateFlow(R.id.rbBlack)
    val selectedColorButtonId: StateFlow<Int> = _selectedColorButtonId

    fun selectRadioButton(id: Int) {
        _selectedColorButtonId.value = id
    }
}
