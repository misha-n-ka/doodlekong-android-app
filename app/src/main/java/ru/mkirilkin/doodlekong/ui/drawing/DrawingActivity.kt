package ru.mkirilkin.doodlekong.ui.drawing

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.mkirilkin.doodlekong.databinding.ActivityDrawingBinding

class DrawingActivity : AppCompatActivity() {

    private var _binding: ActivityDrawingBinding? = null
    private val binding: ActivityDrawingBinding
        get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        _binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
