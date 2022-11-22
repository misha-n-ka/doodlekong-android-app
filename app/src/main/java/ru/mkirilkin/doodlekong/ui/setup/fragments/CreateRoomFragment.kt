package ru.mkirilkin.doodlekong.ui.setup.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.FragmentCreateRoomBinding

class CreateRoomFragment : Fragment(R.layout.fragment_create_room) {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding: FragmentCreateRoomBinding
        get() = requireNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRoomBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
