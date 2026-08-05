package com.andyl.iris.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val CyberpunkGreenColorScheme = darkColorScheme(
    primary = CyberGreen,
    secondary = CyberCyan,
    tertiary = CyberPink,
    background = CyberDark,
    surface = Color(0xFF0A1A0A),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = CyberGreen,
    onSurface = CyberGreen.copy(alpha = 0.8f)
)

private val CyberpunkLightColorScheme = lightColorScheme(
    primary = CyberGreenLight,
    secondary = CyberCyanLight,
    tertiary = CyberPink,
    background = Color(0xFFF5F5F0),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF1A1A1A)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

data class AccentOption(
    val key: String,
    val nameRes: Int,
    val primaryDark: Color,
    val secondaryDark: Color,
    val tertiaryDark: Color,
    val primaryLight: Color,
    val secondaryLight: Color,
    val tertiaryLight: Color
)

val AccentOptions = listOf(
    AccentOption(
        key = "green",
        nameRes = com.andyl.iris.R.string.accent_green,
        primaryDark = CyberGreen,
        secondaryDark = CyberCyan,
        tertiaryDark = CyberPink,
        primaryLight = CyberGreenLight,
        secondaryLight = CyberCyanLight,
        tertiaryLight = CyberPinkLight
    ),
    AccentOption(
        key = "cyan",
        nameRes = com.andyl.iris.R.string.accent_cyan,
        primaryDark = CyberCyan,
        secondaryDark = CyberGreen,
        tertiaryDark = CyberPink,
        primaryLight = CyberCyanLight,
        secondaryLight = CyberGreenLight,
        tertiaryLight = CyberPinkLight
    ),
    AccentOption(
        key = "blue",
        nameRes = com.andyl.iris.R.string.accent_blue,
        primaryDark = CyberBlue,
        secondaryDark = CyberCyan,
        tertiaryDark = CyberPink,
        primaryLight = CyberBlueLight,
        secondaryLight = CyberCyanLight,
        tertiaryLight = CyberPinkLight
    ),
    AccentOption(
        key = "pink",
        nameRes = com.andyl.iris.R.string.accent_pink,
        primaryDark = CyberPink,
        secondaryDark = CyberGreen,
        tertiaryDark = CyberCyan,
        primaryLight = CyberPinkLight,
        secondaryLight = CyberGreenLight,
        tertiaryLight = CyberCyanLight
    ),
    AccentOption(
        key = "purple",
        nameRes = com.andyl.iris.R.string.accent_purple,
        primaryDark = Color(0xFF7C4DFF),
        secondaryDark = CyberPink,
        tertiaryDark = CyberCyan,
        primaryLight = CyberPurpleLight,
        secondaryLight = CyberPinkLight,
        tertiaryLight = CyberCyanLight
    ),
    AccentOption(
        key = "orange",
        nameRes = com.andyl.iris.R.string.accent_orange,
        primaryDark = CyberOrange,
        secondaryDark = CyberYellow,
        tertiaryDark = CyberPink,
        primaryLight = CyberOrangeLight,
        secondaryLight = Color(0xFFFF8F00),
        tertiaryLight = CyberPinkLight
    )
)

fun accentScheme(accent: AccentOption, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = accent.primaryDark,
            secondary = accent.secondaryDark,
            tertiary = accent.tertiaryDark,
            background = CyberDark,
            surface = Color(0xFF1A1A1A),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5)
        )
    } else {
        lightColorScheme(
            primary = accent.primaryLight,
            secondary = accent.secondaryLight,
            tertiary = accent.tertiaryLight,
            background = Color(0xFFF5F5F0),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onTertiary = Color.White,
            onBackground = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A)
        )
    }
}

val LocalReduceAnimations = staticCompositionLocalOf { false }

@Composable
fun IrisWallpaperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: String? = null,
    amoled: Boolean = false,
    reduceAnimations: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        accentColor != null -> {
            val accent = AccentOptions.find { it.key == accentColor } ?: AccentOptions.first()
            accentScheme(accent, darkTheme)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CyberpunkGreenColorScheme
        else -> CyberpunkLightColorScheme
    }
    val colorScheme = if (amoled && darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF121212)
        )
    } else {
        baseScheme
    }

    val effectiveScheme = if (darkTheme) {
        colorScheme.copy(
            onSurface = Color.White,
            onBackground = Color.White,
            onSurfaceVariant = Color(0xFFCAC4D0),
            onPrimaryContainer = Color.White,
            onSecondaryContainer = Color.White,
            onTertiaryContainer = Color.White,
            primaryContainer = colorScheme.surfaceVariant.copy(alpha = 0.6f),
            secondaryContainer = colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tertiaryContainer = colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    } else {
        colorScheme
    }

    CompositionLocalProvider(
        LocalReduceAnimations provides reduceAnimations,
        LocalContentColor provides effectiveScheme.onSurface
    ) {
        MaterialTheme(
            colorScheme = effectiveScheme,
            typography = Typography,
            content = content
        )
    }
}
