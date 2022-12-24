package ru.mkirilkin.doodlekong.ui.setup.create_room

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.FragmentCreateRoomBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import ru.mkirilkin.doodlekong.util.Constants
import ru.mkirilkin.doodlekong.util.hideKeyboard
import ru.mkirilkin.doodlekong.util.navigateSafely
import ru.mkirilkin.doodlekong.util.snackbar

@AndroidEntryPoint
class CreateRoomFragment : Fragment(R.layout.fragment_create_room) {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding: FragmentCreateRoomBinding
        get() = requireNotNull(_binding)

    private val viewModel: CreateRoomViewModel by viewModels()
    private val args: CreateRoomFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRoomBinding.bind(view)
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setupRoomSizeSpinner()
        listenToEvents()

        binding.btnCreateRoom.setOnClickListener {
            binding.createRoomProgressBar.isVisible = true
            viewModel.createRoom(
                Room(
                    binding.etRoomName.text.toString(),
                    binding.tvMaxPersons.text.toString().toInt(),
                )
            )
            requireActivity().hideKeyboard(binding.root)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupRoomSizeSpinner() {
        val roomSize = resources.getStringArray(R.array.room_size_array)
        val adapter = ArrayAdapter(requireContext(), R.layout.textview_room_size, roomSize)
        binding.tvMaxPersons.setAdapter(adapter)
    }

    private fun listenToEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.setupEvent.collect { event ->
                when (event) {
                    is CreateRoomViewModel.SetupEvent.CreateRoomEvent -> {
                        viewModel.joinRoom(args.username, event.room.name)
                    }
                    is CreateRoomViewModel.SetupEvent.InputEmptyError -> {
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(R.string.error_field_empty)
                    }
                    is CreateRoomViewModel.SetupEvent.InputTooShortError -> {
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(
                            getString(
                                R.string.error_room_name_too_short,
                                Constants.MIN_ROOM_NAME_LENGTH
                            )
                        )
                    }
                    is CreateRoomViewModel.SetupEvent.InputTooLongError -> {
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(
                            getString(
                                R.string.error_room_name_too_long,
                                Constants.MAX_ROOM_NAME_LENGTH
                            )
                        )
                    }
                    is CreateRoomViewModel.SetupEvent.CreateRoomErrorEvent -> {
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(event.error)
                    }
                    is CreateRoomViewModel.SetupEvent.JoinRoomEvent -> {
                        binding.createRoomProgressBar.isVisible = false
                        findNavController().navigateSafely(
                            R.id.action_createRoomFragment_to_drawingActivity, args = bundleOf(
                                "username" to args.username, "roomName" to event.roomName
                            )
                        )
                    }
                    is CreateRoomViewModel.SetupEvent.JoinRoomErrorEvent -> {
                        binding.createRoomProgressBar.isVisible = false
                        snackbar(event.error)
                    }
                }
            }
        }
    }
}
