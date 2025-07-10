package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.Gravity
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.compose.map.CopyrightMargins
import ru.dgis.sdk.demo.databinding.ActivityCopyrightBinding
import ru.dgis.sdk.map.Map

/**
 * Sample activity to demonstrate copyright control in the DGis SDK map.
 *
 * Features:
 * - Dynamically change copyright margins
 * - Change copyright position (gravity)
 * - Show/hide SDK version in the copyright view
 *
 */
class CopyrightActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCopyrightBinding.inflate(layoutInflater) }
    private val mapView by lazy { binding.mapView }
    private lateinit var map: Map

    private var copyrightMargins = CopyrightMargins(0, 0, 0, 0)
    private var copyrightGravity = Gravity.BOTTOM or Gravity.START
    private var showVersion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.seekLeft.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                copyrightMargins = copyrightMargins.copy(left = progress)
                updateCopyrightMargins()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekTop.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                copyrightMargins = copyrightMargins.copy(top = progress)
                updateCopyrightMargins()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekRight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                copyrightMargins = copyrightMargins.copy(right = progress)
                updateCopyrightMargins()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBottom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                copyrightMargins = copyrightMargins.copy(bottom = progress)
                updateCopyrightMargins()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val gravityList = listOf(
            Gravity.TOP or Gravity.START,
            Gravity.TOP or Gravity.END,
            Gravity.BOTTOM or Gravity.START,
            Gravity.BOTTOM or Gravity.END
        )

        binding.gravityRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val index = when (checkedId) {
                R.id.radioTopLeft -> 0
                R.id.radioTopRight -> 1
                R.id.radioBottomLeft -> 2
                R.id.radioBottomRight -> 3
                else -> 0
            }
            copyrightGravity = gravityList[index]
            updateCopyrightGravity()
        }

        binding.versionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            showVersion = isChecked
            updateShowVersion()
        }

        mapView.getMapAsync {
            this.map = it
            updateCopyrightMargins()
            updateCopyrightGravity()
            updateShowVersion()
        }
    }

    private fun updateCopyrightMargins() {
        mapView.setCopyrightMargins(
            copyrightMargins.left,
            copyrightMargins.top,
            copyrightMargins.right,
            copyrightMargins.bottom
        )
    }

    private fun updateCopyrightGravity() {
        mapView.setCopyrightGravity(copyrightGravity)
    }

    private fun updateShowVersion() {
        mapView.showApiVersionInCopyrightView = showVersion
    }
}
