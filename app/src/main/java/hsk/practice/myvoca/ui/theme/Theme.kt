package hsk.practice.myvoca.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Lavender3,
    primaryVariant = Lavender5,
    secondary = Shadow3,
    secondaryVariant = Shadow6,
    onPrimary = Color.White,
    onSurface = Color.White,
    onBackground = Color.White,
)

private val LightColorPalette = lightColors(
    primary = Lavender3,
    primaryVariant = Lavender6,
    secondary = Shadow3,
    secondaryVariant = Shadow6,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun MyVocaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}