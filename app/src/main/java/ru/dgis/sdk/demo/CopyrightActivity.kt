package ru.dgis.sdk.demo

import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.compose.map.CopyrightMargins
import android.view.Gravity
import ru.dgis.sdk.demo.databinding.ActivityCopyrightBinding

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

        val toggles = listOf(
            binding.toggleTopLeft,
            binding.toggleTopRight,
            binding.toggleBottomLeft,
            binding.toggleBottomRight
        )
        val gravityList = listOf(
            Gravity.TOP or Gravity.START,
            Gravity.TOP or Gravity.END,
            Gravity.BOTTOM or Gravity.START,
            Gravity.BOTTOM or Gravity.END
        )
        toggles.forEachIndexed { index, btn ->
            btn.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    toggles.forEachIndexed { i, other -> if (i != index) other.isChecked = false }
                    copyrightGravity = gravityList[index]
                    updateCopyrightGravity()
                }
            }
        }

        binding.versionCheckBox.setOnCheckedChangeListener { _, isChecked ->
            showVersion = isChecked
            updateShowVersion()
        }
    }

    override fun onResume() {
        super.onResume()
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
