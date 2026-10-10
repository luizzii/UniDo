package com.example.unido.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Status colors of the app, with a light and a dark variant each. */
object UniDoColors {
    val completed: Color
        @Composable get() = pick(light = Color(0xFF2E7D32), dark = Color(0xFF81C784))
    val completedContainer: Color
        @Composable get() = pick(light = Color(0xFFE3F4E4), dark = Color(0xFF1B3A1E))

    val overdue: Color
        @Composable get() = pick(light = Color(0xFFC62828), dark = Color(0xFFEF9A9A))
    val overdueContainer: Color
        @Composable get() = pick(light = Color(0xFFFDE7E7), dark = Color(0xFF4A1C1C))

    val pendingContainer: Color
        @Composable get() = pick(light = Color(0xFFF2F2F2), dark = Color(0xFF2B2B2B))
    val pendingBanner: Color
        @Composable get() = pick(light = Color(0xFFD0D0D0), dark = Color(0xFF3D3D3D))

    /** Bar above the task list; the text on it is always white. */
    val sectionBar: Color
        @Composable get() = pick(light = Color(0xFF777777), dark = Color(0xFF555555))

    @Composable
    private fun pick(light: Color, dark: Color): Color = if (isSystemInDarkTheme()) dark else light
}
