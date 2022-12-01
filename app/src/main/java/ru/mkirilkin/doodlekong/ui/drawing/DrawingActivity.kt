package ru.mkirilkin.doodlekong.ui.drawing

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mkirilkin.doodlekong.R
import com.mkirilkin.doodlekong.databinding.ActivityDrawingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.mkirilkin.doodlekong.util.Constants

@AndroidEntryPoint
class DrawingActivity : AppCompatActivity() {

    private var _binding: ActivityDrawingBinding? = null
    private val binding: ActivityDrawingBinding
        get() = requireNotNull(_binding)

    private val viewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToUiStateUpdates()
        binding.colorGroup.setOnCheckedChangeListener { _, checkedId ->
            viewModel.selectRadioButton(checkedId)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
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
    }

    private fun selectColor(color: Int) {
        binding.drawingView.setColor(color)
        binding.drawingView.setThickness(Constants.DEFAULT_PAINT_THICKNESS)
    }
}
