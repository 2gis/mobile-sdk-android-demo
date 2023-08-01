package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.demo.databinding.ActivityParkingBinding
import ru.dgis.sdk.map.AttributeValue
import ru.dgis.sdk.map.Map

private const val PARKING_ATTRIBUTE = "parkingOn"

/**
 * Showcase for visualize parkings on the map.
 * Core principle here is to use "parkingOn" map attribute. To learn more about attributes see
 * [AttributeValue](https://docs.2gis.com/ru/android/sdk/reference/7.0/ru.dgis.sdk.map.AttributeValue)
 *
 * Demonstration: open activity and toggle parking button to see parkings on map
 */
class ParkingActivity : AppCompatActivity() {
    private val binding: ActivityParkingBinding by lazy {
        ActivityParkingBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.mapView.getMapAsync { map ->
            enableToggleParkings(map)
        }
    }

    /**
     * Core logic is here. We are checking current value of boolean attribute
     * and setting new value, which is opposite.
     * You must pay attention to a nullability of AttributeValue
     */
    private fun enableToggleParkings(map: Map) {
        binding.parkingButton.setOnClickListener {
            val oldValue = map.attributes.getAttributeValue(PARKING_ATTRIBUTE).asBoolean ?: false
            map.attributes.setAttributeValue(PARKING_ATTRIBUTE, AttributeValue(!oldValue))
            it.isActivated = !oldValue
        }
    }
}
