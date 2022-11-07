package com.s0und.sutapp.ui.theme

import android.annotation.SuppressLint
import com.s0und.sutapp.ui.theme.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable


@Composable
fun AbobaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    MaterialTheme(colors = colors, typography = Typography, shapes = Shapes, content = content)
}

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    background = Grey900,
    surface = Grey800,
    onSurface = Grey50,
    primary = Grey700,
    primaryVariant = SlightBonchBlue,
    onPrimary = Grey50,
    secondary = Grey800,
    secondaryVariant = WhiteSubColor,
    onBackground = White,
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    background = Grey100,
    surface = Grey50,
    onSurface = Grey900,
    primary = Grey700,
    primaryVariant = DarkBonchBlue,
    onPrimary = Grey900,
    secondary = Grey700,
    secondaryVariant = DarkSubColor
)
