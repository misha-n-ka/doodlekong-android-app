package ru.mkirilkin.doodlekong.ui.drawing

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.ActivityDrawingBinding
import com.plcourse.mkirilkin.data.PlayerData
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mkirilkin.doodlekong.adapters.ChatMessageAdapter
import ru.mkirilkin.doodlekong.adapters.PlayerAdapter
import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import ru.mkirilkin.doodlekong.data.remote.websocket.models.messages.*
import ru.mkirilkin.doodlekong.util.Constants
import ru.mkirilkin.doodlekong.util.hideKeyboard
import javax.inject.Inject

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity(R.layout.activity_drawing) {

    @Inject
    lateinit var clientId: String

    private var _binding: ActivityDrawingBinding? = null
    private val binding: ActivityDrawingBinding
        get() = requireNotNull(_binding)

    private val viewModel: DrawingViewModel by viewModels()

    private val args: DrawingActivityArgs by navArgs()

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var rvPlayers: RecyclerView

    private lateinit var chatMessageAdapter: ChatMessageAdapter

    @Inject
    lateinit var playerAdapter: PlayerAdapter

    private var updateChatJob: Job? = null
    private var updatePlayersJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToUiStateUpdates()
        listenToConnectionEvents()
        listenToSocketEvents()
        setupRecyclerView()

        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        toggle.syncState()

        chatMessageAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val header = layoutInflater.inflate(R.layout.nav_drawer_header, binding.navView)
        rvPlayers = header.findViewById(R.id.rvPlayers)
        binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        rvPlayers.apply {
            adapter = playerAdapter
            layoutManager = LinearLayoutManager(this@DrawingActivity)
        }

        binding.ibPlayers.setOnClickListener {
            binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.root.openDrawer(GravityCompat.START)
        }

        binding.root.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

            override fun onDrawerOpened(drawerView: View) = Unit

            override fun onDrawerClosed(drawerView: View) {
                binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }

            override fun onDrawerStateChanged(newState: Int) = Unit
        })

        binding.ibUndo.setOnClickListener {
            if (binding.drawingView.isUserDrawing) {
                binding.drawingView.undo()
                viewModel.sendBaseModel(DrawAction(DrawAction.ACTION_UNDO))
            }
        }

        binding.drawingView.setPathDataChangedListener {
            viewModel.setPathData(it)
        }

        binding.ibClearText.setOnClickListener {
            binding.etMessage.text?.clear()
        }

        binding.ibSend.setOnClickListener {
            viewModel.sendChatMessage(
                ChatMessage(
                    from = args.username,
                    roomName = args.roomName,
                    message = binding.etMessage.text.toString(),
                    System.currentTimeMillis()
                )
            )
            binding.etMessage.text?.clear()
            hideKeyboard(binding.root)
        }

        binding.colorGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.checkRadioButton(checkedId)
        }
        binding.drawingView.roomName = args.roomName
        binding.drawingView.setOnDrawListener {
            if (binding.drawingView.isUserDrawing) {
                viewModel.sendBaseModel(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.rvChat.layoutManager?.onSaveInstanceState()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun subscribeToUiStateUpdates() {
        lifecycleScope.launchWhenStarted {
            viewModel.selectedColorButtonId.collectLatest { id ->
                binding.colorGroup.check(id)
                when (id) {
                    R.id.rbBlack -> selectColor(Color.BLACK)
                    R.id.rbBlue -> selectColor(
                        ContextCompat.getColor(this@DrawingActivity, R.color.blue_dark)
                    )
                    R.id.rbGreen -> selectColor(Color.GREEN)
                    R.id.rbOrange -> selectColor(
                        ContextCompat.getColor(this@DrawingActivity, R.color.orange)
                    )
                    R.id.rbRed -> selectColor(Color.RED)
                    R.id.rbYellow -> selectColor(Color.YELLOW)
                    R.id.rbEraser -> {
                        binding.drawingView.setColor(Color.WHITE)
                        binding.drawingView.setThickness(40f)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.connectionProgressBarVisible.collect { isVisible ->
                binding.connectionProgressBar.isVisible = isVisible
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.chooseWordOverlayVisible.collect { isVisible ->
                binding.chooseWordOverlay.isVisible = isVisible
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.chat.collect { chat ->
                if (chatMessageAdapter.chatObjects.isEmpty()) {
                    updateChatMessageList(chat)
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.newWords.collect {
                val newWords = it.newWords
                if (newWords.isEmpty()) {
                    return@collect
                }
                binding.apply {
                    btnFirstWord.text = newWords[0]
                    btnSecondWord.text = newWords[1]
                    btnThirdWord.text = newWords[2]

                    btnFirstWord.setOnClickListener {
                        viewModel.chooseWord(newWords[0], args.roomName)
                        viewModel.setChooseWordOverlayVisible(false)
                    }
                    btnSecondWord.setOnClickListener {
                        viewModel.chooseWord(newWords[1], args.roomName)
                        viewModel.setChooseWordOverlayVisible(false)
                    }
                    btnThirdWord.setOnClickListener {
                        viewModel.chooseWord(newWords[2], args.roomName)
                        viewModel.setChooseWordOverlayVisible(false)
                    }
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.players.collect { players ->
                updatePlayersList(players)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.phaseTime.collect { time ->
                binding.roundTimerProgressBar.progress = time.toInt()
                binding.tvRemainingTimeChooseWord.text = (time / 1_000L).toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.gameState.collect { gameState ->
                binding.apply {
                    tvCurWord.text = gameState.word
                    val isUserDrawing = gameState.drawingPlayer == args.username
                    setColorGroupVisibility(isUserDrawing)
                    setMessageInputVisibility(!isUserDrawing)
                    drawingView.isUserDrawing = isUserDrawing
                    ibMic.isVisible = !isUserDrawing
                    drawingView.isEnabled = isUserDrawing
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.phase.collect { phase ->
                when (phase.phase) {
                    Room.Phase.WAITING_FOR_PLAYERS -> {
                        binding.tvCurWord.text = getString(R.string.waiting_for_players)
                        viewModel.cancelTimer()
                        viewModel.setConnectionProgressBarVisible(false)
                        binding.roundTimerProgressBar.progress = binding.roundTimerProgressBar.max
                    }
                    Room.Phase.WAITING_FOR_START -> {
                        binding.roundTimerProgressBar.max = phase.time.toInt()
                        binding.tvCurWord.text = getString(R.string.waiting_for_start)
                    }
                    Room.Phase.NEW_ROUND -> {
                        phase.drawingPlayer?.let { player ->
                            binding.tvCurWord.text =
                                getString(R.string.player_is_drawing, phase.drawingPlayer)
                        }
                        binding.apply {
                            drawingView.isEnabled = false
                            drawingView.setColor(Color.BLACK)
                            drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
                            roundTimerProgressBar.max = phase.time.toInt()
                            val isUserDrawingPlayer = phase.drawingPlayer == args.username
                            binding.chooseWordOverlay.isVisible = isUserDrawingPlayer
                        }
                    }
                    Room.Phase.GAME_RUNNING -> {
                        binding.chooseWordOverlay.isVisible = false
                        binding.roundTimerProgressBar.max = phase.time.toInt()
                    }
                    Room.Phase.SHOW_WORD -> {
                        binding.apply {
                            if (drawingView.isDrawing) {
                                drawingView.finishOffDrawing()
                            }
                            drawingView.isEnabled = false
                            drawingView.setColor(Color.BLACK)
                            drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
                            roundTimerProgressBar.max = phase.time.toInt()
                        }
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun selectColor(color: Int) {
        binding.drawingView.setColor(color)
        binding.drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
    }

    private fun listenToConnectionEvents() = lifecycleScope.launchWhenStarted {
        viewModel.connectionEvent.collect { event ->
            when (event) {
                is WebSocket.Event.OnConnectionOpened<*> -> {
                    viewModel.sendBaseModel(
                        JoinRoomHandshake(args.username, args.roomName, clientId)
                    )
                    viewModel.setConnectionProgressBarVisible(false)
                }
                is WebSocket.Event.OnConnectionFailed -> {
                    viewModel.setConnectionProgressBarVisible(false)
                    Snackbar.make(
                        binding.root,
                        R.string.error_connection_failed,
                        Snackbar.LENGTH_LONG
                    ).show()
                    event.throwable.printStackTrace()
                }
                is WebSocket.Event.OnConnectionClosed -> {
                    viewModel.setConnectionProgressBarVisible(false)
                }
                else -> Unit
            }
        }
    }

    private fun listenToSocketEvents() = lifecycleScope.launchWhenStarted {
        viewModel.socketEvent.collect { event ->
            when (event) {
                is DrawingViewModel.SocketEvent.DrawDataEvent -> {
                    val drawData = event.data
                    if (!binding.drawingView.isUserDrawing) {
                        when (drawData.motionEvent) {
                            MotionEvent.ACTION_DOWN -> binding.drawingView.startedTouchExternally(
                                drawData
                            )
                            MotionEvent.ACTION_MOVE -> binding.drawingView.movedTouchExternally(
                                drawData
                            )
                            MotionEvent.ACTION_UP -> binding.drawingView.releaseTouchExternally(
                                drawData
                            )
                        }
                    }
                }
                is DrawingViewModel.SocketEvent.UndoEvent -> {
                    binding.drawingView.undo()
                }
                is DrawingViewModel.SocketEvent.GameErrorEvent -> {
                    when (event.data.errorType) {
                        GameError.ERROR_ROOM_NOT_FOUND -> finish()
                    }
                }
                is DrawingViewModel.SocketEvent.ChatMessageEvent -> {
                    addChatObjectToRecyclerView(event.data)
                }
                is DrawingViewModel.SocketEvent.AnnouncementEvent -> {
                    addChatObjectToRecyclerView(event.data)
                }
                is DrawingViewModel.SocketEvent.ChosenWordEvent -> {
                    binding.tvCurWord.text = event.data.chosenWord
                    binding.ibUndo.isEnabled = false
                }
                is DrawingViewModel.SocketEvent.GameStateEvent -> {
                    binding.drawingView.clear()
                }
                is DrawingViewModel.SocketEvent.RoundDrawInfoEvent -> {
                    binding.drawingView.update(event.data)
                }
                else -> Unit
            }
        }
    }

    private fun setupRecyclerView() = binding.rvChat.apply {
        chatMessageAdapter = ChatMessageAdapter(args.username)
        adapter = chatMessageAdapter
        layoutManager = LinearLayoutManager(this@DrawingActivity)
    }

    private fun updateChatMessageList(chat: List<BaseModel>) {
        updateChatJob?.cancel()
        updateChatJob = lifecycleScope.launch {
            chatMessageAdapter.updateDataset(chat)
        }
    }

    private suspend fun addChatObjectToRecyclerView(chatObject: BaseModel) {
        val canScrollDown = binding.rvChat.canScrollVertically(1)
        updateChatMessageList(chatMessageAdapter.chatObjects + chatObject)
        updateChatJob?.join()
        if (!canScrollDown) {
            binding.rvChat.scrollToPosition(chatMessageAdapter.chatObjects.lastIndex)
        }
    }

    private fun setColorGroupVisibility(isVisible: Boolean) {
        binding.colorGroup.isVisible = isVisible
        binding.ibUndo.isVisible = isVisible
    }

    private fun setMessageInputVisibility(isVisible: Boolean) {
        binding.apply {
            tilMessage.isVisible = isVisible
            ibSend.isVisible = isVisible
            ibClearText.isVisible = isVisible
        }
    }

    private fun updatePlayersList(players: List<PlayerData>) {
        updatePlayersJob?.cancel()
        updatePlayersJob = lifecycleScope.launch {
            playerAdapter.updateDataset(players)
        }
    }
}
