package com.example.servicesapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B4B5A),
    secondary = Color(0xFF2E4A4B),
    background = Color(0xFF1B4B5A),
    surface = Color(0xFF2E4A4B),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1B4B5A),
    secondary = Color(0xFF2E4A4B),
    background = Color(0xFF1B4B5A),
    surface = Color(0xFF2E4A4B),
)

@Composable
fun ServicesAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
