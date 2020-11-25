package ru.dgis.sdk.app

import android.graphics.Color
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.Elevation
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.geometry.Geometry
import ru.dgis.sdk.geometry.GeometryCreator
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map
import kotlin.random.Random

private fun Random.nextFloat(min: Float, max: Float): Float {
    return min + nextFloat() * (max - min)
}

private fun Random.nextFloat(max: Float): Float = nextFloat(0.0f, max)

class GeometryDemo(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val map: Map)
{
    private val random = Random(12345)
    private val colorGen = ColorGenerator(this)
    private var isMonochrome = false
    private val objectOrder = mutableListOf<GeometryMapObject>()

    private val source: GeometryMapObjectSource? by lazy {
        val source = GeometryMapObjectSourceBuilder(context).createSource()
        source?.let { map.addSource(it) }
        source
    }

    fun addPolygon() {
        val (fillColor, strokeColor) = colorGen.getAreaColors()
        GeometryMapObjectBuilder()
            .setGeometry(createPolygonSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_polygon")
            .setObjectAttribute("color", fillColor)
            .setObjectAttribute("border_color", strokeColor)
            .setObjectAttribute("border_width_zpt", 0.5)
            .setObjectAttribute("is_monochrome", isMonochrome)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addPolyline() {
        GeometryMapObjectBuilder()
            .setGeometry(createPolylineSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_polyline")
            .setObjectAttribute("color", colorGen.getLineColor())
            .setObjectAttribute("width_zpt", 1.0)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addPoint() {
        GeometryMapObjectBuilder()
            .setGeometry(createPointSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_point")
            .setObjectAttribute("icon_width_zpt", 8.0)
            .setObjectAttribute("db_icon_priority", 65000)
            .setObjectAttribute("object_group_priority", 65000)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addPoi() {
        val (fillColor, strokeColor) = colorGen.getFontColors()
        GeometryMapObjectBuilder()
            .setGeometry(createPointSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_poi")
            .setObjectAttribute("db_label", "Point")
            .setObjectAttribute("font_color", fillColor)
            .setObjectAttribute("halo_color", strokeColor)
            .setObjectAttribute("halo_radius_zpt", 0.3)
            .setObjectAttribute("font_size_zpt", 3.5)
            .setObjectAttribute("icon_width_zpt", 8.0)
            .setObjectAttribute("db_icon_priority", 65000)
            .setObjectAttribute("object_group_priority", 65000)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addPointWithElevation() {
        GeometryMapObjectBuilder()
            .setGeometry(createPointWithElevationSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_point")
            .setObjectAttribute("icon_width_zpt", 8.0)
            .setObjectAttribute("db_icon_priority", 65000)
            .setObjectAttribute("object_group_priority", 65000)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addPoiWithElevation() {
        val (fillColor, strokeColor) = colorGen.getFontColors()
        GeometryMapObjectBuilder()
            .setGeometry(createPointWithElevationSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_poi")
            .setObjectAttribute("db_label", "Elevated point")
            .setObjectAttribute("font_color", fillColor)
            .setObjectAttribute("halo_color", strokeColor)
            .setObjectAttribute("halo_radius_zpt", 0.3)
            .setObjectAttribute("font_size_zpt", 3.5)
            .setObjectAttribute("icon_width_zpt", 8.0)
            .setObjectAttribute("db_icon_priority", 65000)
            .setObjectAttribute("object_group_priority", 65000)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun addComplexObject() {
        val color = colorGen.getLineColor()
        GeometryMapObjectBuilder()
            .setGeometry(createComplexObjectSampleGeometry())
            .setObjectAttribute("db_sublayer", "s_dynamic_complex_object")
            .setObjectAttribute("color", color)
            .setObjectAttribute("width_zpt", 1.5)
            .setObjectAttribute("border_color", color)
            .setObjectAttribute("border_width_zpt", 0.5)
            .createObject()
            ?.let {
                source?.addObject(it)
                objectOrder.add(it)
            }
    }

    fun shiftObjects() {
        val gen = PointGenerator(this)
        source?.objects()?.forEach { obj ->
            try {
                obj?.setShift(gen.getShift())
            } catch (e: Throwable) {
            }
        }
    }

    fun removeLastObject() {
        if (objectOrder.isEmpty()) {
            return
        }
        source?.removeObject(objectOrder.last())
        objectOrder.removeAt(objectOrder.size - 1)
    }

    fun removeAllObjects() {
        source?.clear()
        objectOrder.clear()
    }

    fun toggleVisibility() {
        source?.objects()?.forEach {
            it?.setVisible(!it.isVisible().value)
        }
    }

    fun toggleColor() {
        isMonochrome = !isMonochrome
        source?.objects()?.forEach {
            it?.objectAttributes()?.setAttributeValue("is_monochrome",
                if (isMonochrome) true else null)
        }
    }

    class PointGenerator(private val parent: GeometryDemo) {
        private val size = ViewportSize(parent.width, parent.height)
        private val unit: Float = minOf(size.width, size.height).toFloat() / 4
        private val projection = parent.map.camera.projection()
        private val origin: ViewportPoint

        init {
            while (true) {
                val x = parent.random.nextFloat(unit / 2, size.width - unit / 2)
                val y = parent.random.nextFloat(unit / 2, size.height - unit / 2)
                val pt = ViewportPoint(x, y)
                if (checkOriginBounds(pt)) {
                    origin = pt
                    break
                }
            }
        }

        private fun areBoundsValid(minLongitude: Double, maxLongitude: Double): Boolean {
            return minLongitude >= -180.0 && maxLongitude <= 180.0 && maxLongitude - minLongitude < 180.0
        }

        private fun checkOriginBounds(pt: ViewportPoint): Boolean {
            val points = arrayOf(
                ViewportPoint(pt.x - unit / 2, pt.y - unit / 2),
                ViewportPoint(pt.x + unit / 2, pt.y - unit / 2),
                ViewportPoint(pt.x + unit / 2, pt.y + unit / 2),
                ViewportPoint(pt.x - unit / 2, pt.y + unit / 2)
            ).map { projection.screenToMap(it) }
            if (points.all { it != null }) {
                val maxLon = points.maxBy { it!!.longitude.value }
                val minLon = points.minBy { it!!.longitude.value }
                if (areBoundsValid(minLon!!.longitude.value, maxLon!!.longitude.value)) {
                    return true
                }
            }
            return false
        }

        fun getPoint(x: Float, y: Float): GeoPoint {
            return projection.screenToMap(
                ViewportPoint(origin.x + unit * (x - 0.5f), origin.y + unit * (y - 0.5f))
            ) ?: GeoPoint(Arcdegree(0.0), Arcdegree(0.0))
        }

        fun getShift(): GeoPoint {
            val viewportX = parent.random.nextFloat(size.width.toFloat())
            val viewportY = parent.random.nextFloat(size.height.toFloat())
            return parent.map.camera.position().value.point.let { center ->
                projection.screenToMap(ViewportPoint(viewportX, viewportY))?.let {
                    GeoPoint(
                        Arcdegree(it.latitude.value - center.latitude.value),
                        Arcdegree(it.longitude.value - center.longitude.value)
                    )
                }
            } ?: GeoPoint(Arcdegree(0.0), Arcdegree(0.0))
        }
    }

    class ColorGenerator(private val parent: GeometryDemo) {
        fun getLineColor(): Long {
            val hsv = floatArrayOf(parent.random.nextFloat(360.0f), 1.0f, 1.0f)
            return colorToAttributeValue(Color.HSVToColor(255, hsv))
        }

        fun getAreaColors(): Pair<Long, Long> {
            val hue = parent.random.nextFloat(360.0f)
            val hsvFill = floatArrayOf(hue, 0.5f, 1.0f)
            val hsvStroke = floatArrayOf(hue, 1.0f, 0.7f)
            return Pair(colorToAttributeValue(Color.HSVToColor(160, hsvFill)),
                colorToAttributeValue(Color.HSVToColor(255, hsvStroke))
            )
        }

        fun getFontColors(): Pair<Long, Long> {
            val hue = parent.random.nextFloat(360.0f)
            val hsvFill = floatArrayOf(hue, 1.0f, 0.6f)
            val hsvStroke = floatArrayOf(hue, 0.1f, 1.0f)
            return Pair(colorToAttributeValue(Color.HSVToColor(255, hsvFill)),
                colorToAttributeValue(Color.HSVToColor(224, hsvStroke))
            )
        }
    }

    private fun createPolygonSampleGeometry(): Geometry? {
        val gen = PointGenerator(this)
        return GeometryCreator.createPolygonGeometry(
            listOf(
                listOf(
                    gen.getPoint(0.631f, 0.006f),
                    gen.getPoint(0.247f, 0.060f),
                    gen.getPoint(0.013f, 0.369f),
                    gen.getPoint(0.067f, 0.753f),
                    gen.getPoint(0.376f, 0.987f),
                    gen.getPoint(0.760f, 0.933f),
                    gen.getPoint(0.994f, 0.624f),
                    gen.getPoint(0.940f, 0.240f)
                ),
                listOf(
                    gen.getPoint(0.552f, 0.096f),
                    gen.getPoint(0.685f, 0.248f),
                    gen.getPoint(0.884f, 0.286f),
                    gen.getPoint(0.780f, 0.460f),
                    gen.getPoint(0.805f, 0.660f),
                    gen.getPoint(0.608f, 0.615f),
                    gen.getPoint(0.426f, 0.701f),
                    gen.getPoint(0.407f, 0.500f),
                    gen.getPoint(0.269f, 0.353f),
                    gen.getPoint(0.455f, 0.273f)
                )
            )
        )
    }

    private fun createPolylineSampleGeometry(): Geometry? {
        val gen = PointGenerator(this)
        return GeometryCreator.createPolylineGeometry(
            listOf(
                gen.getPoint(0.467f, 0.445f),
                gen.getPoint(0.443f, 0.484f),
                gen.getPoint(0.388f, 0.398f),
                gen.getPoint(0.537f, 0.327f),
                gen.getPoint(0.623f, 0.538f),
                gen.getPoint(0.349f, 0.640f),
                gen.getPoint(0.232f, 0.304f),
                gen.getPoint(0.631f, 0.171f),
                gen.getPoint(0.780f, 0.632f),
                gen.getPoint(0.255f, 0.797f),
                gen.getPoint(0.075f, 0.210f),
                gen.getPoint(0.725f, 0.014f),
                gen.getPoint(0.936f, 0.726f),
                gen.getPoint(0.210f, 0.977f)
            )
        )
    }

    private fun createPointSampleGeometry(): Geometry? {
        return GeometryCreator.createPointGeometry(PointGenerator(this).getPoint(0.5f, 0.5f))
    }

    private fun createPointWithElevationSampleGeometry(): Geometry? {
        val pt = PointGenerator(this).getPoint(0.5f, 0.5f)
        return GeometryCreator.createPointGeometry(
            GeoPointWithElevation(pt.latitude, pt.longitude, Elevation(50.0f))
        )
    }

    private fun createComplexObjectSampleGeometry(): Geometry? {
        val gen = PointGenerator(this)
        return GeometryCreator.createComplexGeometry(
            listOf(
                GeometryCreator.createPolygonGeometry(
                    listOf(
                        listOf(
                            gen.getPoint(0.671f, 0.571f),
                            gen.getPoint(0.575f, 0.628f),
                            gen.getPoint(0.463f, 0.617f),
                            gen.getPoint(0.379f, 0.543f),
                            gen.getPoint(0.354f, 0.434f),
                            gen.getPoint(0.399f, 0.331f),
                            gen.getPoint(0.495f, 0.273f),
                            gen.getPoint(0.607f, 0.284f),
                            gen.getPoint(0.691f, 0.358f),
                            gen.getPoint(0.716f, 0.468f)
                        )
                    )
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.415f, 0.279f), gen.getPoint(0.259f, 0.006f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.543f, 0.254f), gen.getPoint(0.555f, 0.143f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.660f, 0.309f), gen.getPoint(0.874f, 0.074f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.723f, 0.421f), gen.getPoint(0.835f, 0.398f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.711f, 0.550f), gen.getPoint(1.000f, 0.680f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.625f, 0.648f), gen.getPoint(0.680f, 0.743f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.496f, 0.678f), gen.getPoint(0.462f, 0.987f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.375f, 0.622f), gen.getPoint(0.303f, 0.702f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.311f, 0.507f), gen.getPoint(0.004f, 0.570f))
                ),
                GeometryCreator.createPolylineGeometry(
                    listOf(gen.getPoint(0.326f, 0.376f), gen.getPoint(0.226f, 0.331f))
                )
            )
        )
    }
}