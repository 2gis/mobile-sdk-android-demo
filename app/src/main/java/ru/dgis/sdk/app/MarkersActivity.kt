package ru.dgis.sdk.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map


class MarkersActivity : AppCompatActivity() {
    private lateinit var sdkContext: Context
    private lateinit var mapView: MapView
    private lateinit var map: Map

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_markers)
        val mapContainer = findViewById<LinearLayout>(R.id.map_container)
        val mapOptions = MapOptions().apply {
            position = CameraPosition(
                GeoPoint(Arcdegree(55.740444), Arcdegree(37.619524)),
                Zoom(9.0f),
                Tilt(0.0f),
                Arcdegree(0.0)
            )
        }

        mapView = MapView(this, mapOptions)
        mapContainer.addView(mapView)
        lifecycle.addObserver(mapView)
        mapView.getMapAsync {
            map = it
            createMarkers()
        }
    }

    private fun createMarkers() {
        val source = GeometryMapObjectSourceBuilder(sdkContext).createSource()!!
        map.addSource(source)

        data class MarkerInfo(
            @DrawableRes val resourceId: Int,
            val latitude: Double,
            val longitude: Double,
            val anchor: Pair<Float, Float>? = null
        )

        val markers = listOf(
            MarkerInfo(R.drawable.ic_crown, 55.53739580689267, 37.66256779432297),
            MarkerInfo(R.drawable.ic_crown, 55.72400750556595, 37.79786495491862),
            MarkerInfo(R.drawable.ic_crown, 55.7984068608551, 37.47958129271865),
            MarkerInfo(R.drawable.ic_crown, 55.8649130408431, 37.77402866631746),
            MarkerInfo(R.drawable.ic_crown, 55.89121083673136, 37.82323840074241),
            MarkerInfo(R.drawable.ic_pin, 55.57043582413208, 37.441149428486824),
            MarkerInfo(R.drawable.ic_pin, 55.60650179489436, 37.71176059730351),
            MarkerInfo(R.drawable.ic_pin, 55.64512768391863, 37.80017928220332),
            MarkerInfo(R.drawable.ic_pin, 55.662478298114024, 37.79479038901627),
            MarkerInfo(R.drawable.ic_pin, 55.67895765839564, 37.86552191711962),
            MarkerInfo(R.drawable.ic_pin, 55.68242877178772, 37.787104016169906),
            MarkerInfo(R.drawable.ic_pin, 55.70018593279514, 37.56876013241708),
            MarkerInfo(R.drawable.ic_pin, 55.71837485931056, 37.63104513287544),
            MarkerInfo(R.drawable.ic_pin, 55.74306440383858, 37.70946311764419),
            MarkerInfo(R.drawable.ic_pin, 55.759070448307966, 37.81555202789605),
            MarkerInfo(R.drawable.ic_pin, 55.77030621423892, 37.6402688305825),
            MarkerInfo(R.drawable.ic_pin, 55.77983300860179, 37.79556739144027),
            MarkerInfo(R.drawable.ic_pin, 55.78371738427256, 37.77095410041511),
            MarkerInfo(R.drawable.ic_pin, 55.78415647267489, 37.73867128416896),
            MarkerInfo(R.drawable.ic_pin, 55.79192392771075, 37.441149428486824),
            MarkerInfo(R.drawable.ic_pin, 55.80618843260796, 37.435760451480746),
            MarkerInfo(R.drawable.ic_pin, 55.816128229350724, 37.72559601813555),
            MarkerInfo(R.drawable.ic_pin, 55.82000898617757, 37.45498484931886),
            MarkerInfo(R.drawable.ic_pin, 55.83814210402061, 37.48419309966266),
            MarkerInfo(R.drawable.ic_pin, 55.84375741825776, 37.761730402708054),
            MarkerInfo(R.drawable.ic_pin, 55.85454741803957, 37.75250678882003),
            MarkerInfo(R.drawable.ic_pin, 55.88086906385545, 37.48189562000334),
            MarkerInfo(R.drawable.ic_pin, 55.90242536833114, 37.386567648500204),
            MarkerInfo(R.drawable.ic_nav_point, 55.920520053981384, 37.46420854702592),
            MarkerInfo(R.drawable.ic_blue_nav_pin, 55.950227539812964, 37.68484982661903),
            MarkerInfo(R.drawable.ic_small_pin, 55.553486118464704, 37.5579992774874),
            MarkerInfo(R.drawable.ic_small_pin, 55.691960072144575, 37.49265655875206),
            MarkerInfo(R.drawable.ic_small_pin, 55.74825400089946, 37.79325306415558),
            MarkerInfo(R.drawable.ic_small_pin, 55.78631800125226, 37.7140749245882),
            MarkerInfo(R.drawable.ic_small_pin, 55.78890152615578, 37.54570101387799),
            MarkerInfo(R.drawable.ic_small_pin, 55.809648199608844, 37.76941685937345),
            MarkerInfo(R.drawable.ic_small_pin, 55.81958715998664, 37.52110465429723),
            MarkerInfo(R.drawable.ic_small_pin, 55.85584538331202, 37.428851164877415),
            MarkerInfo(R.drawable.ic_small_pin, 55.96056762364104, 37.53416298888624),
            MarkerInfo(R.drawable.ic_scooter, 55.605775104277264, 37.51693516038358),
            MarkerInfo(R.drawable.ic_minivan, 55.645568332898335, 37.61873002164066),
            MarkerInfo(R.drawable.ic_marker, 55.7444066, 37.489922884, anchor = Pair(0.5f, 0.959f))
        )

        for (marker in markers) {
            val mapObject = MarkerBuilder()
                .setIconFromResource(marker.resourceId)
                .setPosition(marker.latitude, marker.longitude)
                .setAnchor(marker.anchor?.first ?: 0.5f, marker.anchor?.second ?: 0.5f)
                .build()
            source.addObject(mapObject)
        }

        val photo = MarkerBuilder()
            .setIconFromAsset("moscow.png")
            .setPosition(55.852402, 37.623447)
            .setSize(150, 150)
            .build()
        source.addObject(photo)
    }
}