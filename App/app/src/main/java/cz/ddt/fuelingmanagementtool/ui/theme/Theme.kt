
package cz.ddt.fuelingmanagementtool.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Svetle barevne schema podle Figmy
private val LightColorScheme = lightColorScheme(
    primary = FuelPrimary,
    onPrimary = FuelOnPrimary,

    secondary = FuelPrimary,
    onSecondary = FuelOnPrimary,

    tertiary = FuelPrimary,
    onTertiary = FuelOnPrimary,

    background = FuelBackground,
    onBackground = FuelOnBackground,

    surface = FuelSurface,
    onSurface = FuelOnSurface,

    surfaceVariant = FuelSecondarySurface,
    onSurfaceVariant = FuelSecondaryText,

    primaryContainer = FuelSecondarySurface,
    onPrimaryContainer = FuelOnSurface
)

// Tmave barevne schema
private val DarkColorScheme = darkColorScheme(
    primary = FuelDarkPrimary,
    onPrimary = FuelDarkOnPrimary,

    secondary = FuelDarkPrimary,
    onSecondary = FuelDarkOnPrimary,

    tertiary = FuelDarkPrimary,
    onTertiary = FuelDarkOnPrimary,

    background = FuelDarkBackground,
    onBackground = FuelDarkOnBackground,

    surface = FuelDarkSurface,
    onSurface = FuelDarkOnSurface,

    surfaceVariant = FuelDarkSecondarySurface,
    onSurfaceVariant = FuelDarkSecondaryText,

    primaryContainer = FuelDarkSecondarySurface,
    onPrimaryContainer = FuelDarkOnSurface
)

// Zaobleni komponent aplikace
private val FuelShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Hlavni tema aplikace
@Composable
fun FuelingManagementToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = FuelShapes,
        content = content
    )
}
