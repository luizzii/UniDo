package com.example.unido.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

@Composable
fun UniDoTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        Surface(content = content)
    }
}
