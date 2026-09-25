package ru.dgis.sdk.demo.compose.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class HomeScreenColors(
    val backgroundColor: Color,
    var textColor: Color,
    val tabBackground: Color,
    var tabTextColor: Color,
    val tabIndicatorColor: Color,
    val switchCheckedBackgroundColor: Color,
    val switchUncheckedBackgroundColor: Color,
    val switchCheckedThumbColor: Color,
    val switchUncheckedThumbColor: Color,
)

object HomeScreenDefaults {
    val lightColors = HomeScreenColors(
        backgroundColor = Color.White,
        textColor = Color.Black,
        tabBackground = Color.White,
        tabTextColor = Color.Black,
        tabIndicatorColor = Color(0xFF58A600),
        switchCheckedBackgroundColor = Color(0xFF1DB93C),
        switchUncheckedBackgroundColor = Color(0x0F000000),
        switchCheckedThumbColor = Color.White,
        switchUncheckedThumbColor = Color(0xFF3C3C3C),
    )

    val darkColors = HomeScreenColors(
        backgroundColor = Color(0xFF262626),
        textColor = Color.White,
        tabBackground = Color(0xFF262626),
        tabTextColor = Color.White,
        tabIndicatorColor = Color(0xFF50801A),
        switchCheckedBackgroundColor = Color(0xFF1BA136),
        switchUncheckedBackgroundColor = Color(0x0fffffff),
        switchCheckedThumbColor = Color.White,
        switchUncheckedThumbColor = Color(0xFFB8B8B8),
    )

    @Composable
    fun colors() =
        if (isSystemInDarkTheme()) darkColors else lightColors
}
