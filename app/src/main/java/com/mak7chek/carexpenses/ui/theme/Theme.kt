package com.mak7chek.carexpenses.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,               // Головний колір (кнопки, FAB)
    onPrimary = Color.White,             // Текст на головному кольорі
    primaryContainer = TealLight,        // "Легкий" контейнер (напр. фон актив. елемента)
    onPrimaryContainer = TealDark,       // Текст на цьому контейнері

    secondary = AmberAccent,             // Другорядний акцент (напр. чіпси, перемикачі)
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = AmberDark,

    tertiary = NeutralBlue,              // Третій колір (додаткові акценти)
    onTertiary = Color.White,
    tertiaryContainer = NeutralBlueLight,
    onTertiaryContainer = NeutralBlueDark,

    error = RedError,
    onError = Color.White,
    errorContainer = RedErrorContainer,
    onErrorContainer = Color(0xFF410002),

    background = LightBackground,        // Фон всього додатку
    onBackground = LightOnSurface,       // Текст на фоні
    surface = LightSurface,              // Колір "поверхонь" (картки, діалоги)
    onSurface = LightOnSurface,          // Текст на поверхнях
    surfaceVariant = NeutralBlueLight,   // Варіант поверхні (напр. фон Text Field)
    onSurfaceVariant = NeutralBlue,      // Текст/іконки на цьому варіанті
    outline = Color(0xFF6F7978)          // Обводки (напр. Text Field)
)

// --- 2. Схема для ТЕМНОЇ теми ---
private val DarkColorScheme = darkColorScheme(
    primary = TealLight,                 // Головний колір стає світлішим
    onPrimary = TealDark,                // Текст на ньому
    primaryContainer = TealPrimary,      // Контейнер стає темнішим
    onPrimaryContainer = TealLight,

    secondary = AmberLight,
    onSecondary = AmberDark,
    secondaryContainer = AmberAccent,
    onSecondaryContainer = AmberLight,

    tertiary = NeutralBlueLight,
    onTertiary = NeutralBlueDark,
    tertiaryContainer = NeutralBlue,
    onTertiaryContainer = NeutralBlueLight,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = NeutralBlueDark,
    onSurfaceVariant = NeutralBlueLight,
    outline = Color(0xFF899392)
)

@Composable
fun CarExpensesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}