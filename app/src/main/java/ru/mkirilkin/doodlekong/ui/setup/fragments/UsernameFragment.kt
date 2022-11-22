package ru.mkirilkin.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.FragmentUsernameBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.mkirilkin.doodlekong.ui.setup.SetupViewModel
import ru.mkirilkin.doodlekong.util.Constants
import ru.mkirilkin.doodlekong.util.navigateSafely
import ru.mkirilkin.doodlekong.util.snackbar

@AndroidEntryPoint
class UsernameFragment : Fragment(R.layout.fragment_username) {

    private var _binding: FragmentUsernameBinding? = null
    private val binding: FragmentUsernameBinding
        get() = requireNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsernameBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
