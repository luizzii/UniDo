package com.example.unido.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay

/** Current time, refreshed every minute, so tasks turn red as soon as their deadline passes. */
@Composable
fun rememberCurrentTimeMillis(): State<Long> =
    produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            delay(60_000)
            value = System.currentTimeMillis()
        }
    }
