package com.konnecta.app.ui.theme

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Theme ViewModel to handle persistent state
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("konnecta_prefs", Context.MODE_PRIVATE)
    private val _isDarkTheme = MutableStateFlow<Boolean?>(
        if (prefs.contains("is_dark_theme")) prefs.getBoolean("is_dark_theme", false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme

    fun toggleTheme(currentDark: Boolean) {
        val newValue = !currentDark
        _isDarkTheme.value = newValue
        prefs.edit().putBoolean("is_dark_theme", newValue).apply()
    }
}

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel> { error("No ThemeViewModel provided") }

private val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    background = Black,
    onBackground = White,
    surface = Zinc900,
    onSurface = White,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Zinc400,
    outline = Zinc800
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    background = White,
    onBackground = Black,
    surface = Zinc50,
    onSurface = Black,
    surfaceVariant = Zinc100,
    onSurfaceVariant = Zinc500,
    outline = Zinc200
)

@Composable
fun KonnectaTheme(
    themeViewModel: ThemeViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val isDarkThemeOverride by themeViewModel.isDarkTheme.collectAsState()
    val darkTheme = isDarkThemeOverride ?: isSystemInDarkTheme()

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemeViewModel provides themeViewModel) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
