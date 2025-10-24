package com.tuempresa.gastos.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Paletas seguras (sin depender de resources.xml)
private val LightColors = lightColorScheme()
private val DarkColors  = darkColorScheme()

@Composable
fun GastosAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // usa colores dinámicos en Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        } else {
            if (darkTheme) DarkColors else LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // usa el Typography por defecto de abajo
        content = content
    )
}
