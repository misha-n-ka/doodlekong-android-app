package ru.mkirilkin.doodlekong.ui.setup.set_username

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.FragmentUsernameBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.mkirilkin.doodlekong.util.Constants
import ru.mkirilkin.doodlekong.util.navigateSafely
import ru.mkirilkin.doodlekong.util.snackbar

@AndroidEntryPoint
class UsernameFragment : Fragment(R.layout.fragment_username) {

    private var _binding: FragmentUsernameBinding? = null
    private val binding: FragmentUsernameBinding
        get() = requireNotNull(_binding)

    private val viewModel: UsernameViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsernameBinding.bind(view)

        listenToEvents()

        binding.btnNext.setOnClickListener {
            viewModel.validateUsernameAndNavigateToSelectRoom(
                binding.etUsername.text.toString()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun listenToEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.setupEvent.collect { event ->
                when (event) {
                    is UsernameViewModel.SetupEvent.NavigateToSelectRoomEvent -> {
                        findNavController().navigateSafely(
                            R.id.action_usernameFragment_to_selectRoomFragment,
                            args = bundleOf("username" to event.username)
                        )
                    }
                    is UsernameViewModel.SetupEvent.InputEmptyError -> {
                        snackbar(R.string.error_field_empty)
                    }
                    is UsernameViewModel.SetupEvent.InputTooShortError -> {
                        snackbar(
                            getString(
                                R.string.error_username_too_short,
                                Constants.MIN_USERNAME_LENGTH
                            )
                        )
                    }
                    is UsernameViewModel.SetupEvent.InputTooLongError -> {
                        snackbar(
                            getString(
                                R.string.error_username_too_long,
                                Constants.MAX_USERNAME_LENGTH
                            )
                        )
                    }
                }
            }
        }
    }
}
