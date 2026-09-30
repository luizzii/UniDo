package com.example.unido.ui.details

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unido.util.DueDate
import java.util.Calendar
import java.util.TimeZone

/**
 * Read-only field that opens a calendar and then a clock.
 * [onValueChange] receives the deadline formatted as [DueDate.PATTERN].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    // UTC midnight of the chosen day, as returned by the DatePicker.
    var pickedDateUtc by remember { mutableStateOf<Long?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) showDatePicker = true
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text("Toque para escolher") },
        trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Escolher data e hora") },
        interactionSource = interactionSource,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    )

    val current = remember(value) { DueDate.parse(value) }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = toUtcMidnight(current ?: System.currentTimeMillis())
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateUtc = dateState.selectedDateMillis
                        showDatePicker = false
                    },
                    enabled = dateState.selectedDateMillis != null
                ) { Text("PRÓXIMO") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("CANCELAR") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    pickedDateUtc?.let { dateUtc ->
        val initial = Calendar.getInstance().apply { current?.let { timeInMillis = it } }
        val timeState = rememberTimePickerState(
            initialHour = initial.get(Calendar.HOUR_OF_DAY),
            initialMinute = initial.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { pickedDateUtc = null },
            title = { Text("Selecione o horário") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(DueDate.format(combine(dateUtc, timeState.hour, timeState.minute)))
                    pickedDateUtc = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickedDateUtc = null }) { Text("CANCELAR") }
            }
        )
    }
}

/** The DatePicker works with UTC midnight; converts a local instant to that representation. */
private fun toUtcMidnight(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Joins the day chosen in the DatePicker (UTC midnight) with the hour/minute, in local time. */
private fun combine(dateUtc: Long, hour: Int, minute: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = dateUtc }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute)
    }.timeInMillis
}
