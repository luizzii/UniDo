package com.example.unido.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unido.R
import com.example.unido.data.local.Task
import com.example.unido.ui.components.BackHeader
import com.example.unido.viewmodel.TaskViewModel

/** Creates a new task when [taskId] is not a valid id (<= 0); edits the existing task otherwise. */
@Composable
fun TaskFormScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val isNew = taskId <= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Also keeps the fields above the keyboard.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        BackHeader(title = stringResource(R.string.back_to_list), onBack = onBack)
        Spacer(Modifier.height(12.dp))

        if (isNew) {
            TaskForm(
                heading = stringResource(R.string.new_task),
                existing = null,
                onSave = { title, subject, dueAt, description ->
                    viewModel.addTask(title, subject, dueAt, description)
                    onBack()
                }
            )
        } else {
            val task by remember(taskId) { viewModel.getTask(taskId) }.collectAsState(initial = null)
            val current = task
            if (current == null) {
                Text(stringResource(R.string.loading), fontSize = 18.sp)
            } else {
                TaskForm(
                    heading = stringResource(R.string.edit_task),
                    existing = current,
                    onSave = { title, subject, dueAt, description ->
                        viewModel.updateTask(current, title, subject, dueAt, description)
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskForm(
    heading: String,
    existing: Task?,
    onSave: (title: String, subject: String, dueAt: Long?, description: String) -> Unit
) {
    // rememberSaveable keeps what was typed when the screen rotates or the app goes to the background.
    var title by rememberSaveable { mutableStateOf(existing?.title.orEmpty()) }
    var subject by rememberSaveable { mutableStateOf(existing?.subject.orEmpty()) }
    var dueAt by rememberSaveable { mutableStateOf(existing?.dueAt) }
    var description by rememberSaveable { mutableStateOf(existing?.description.orEmpty()) }

    Text(heading, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    FormField(stringResource(R.string.field_title), title) { title = it }
    FormField(stringResource(R.string.field_subject), subject) { subject = it }
    DateTimePickerField(stringResource(R.string.field_due), dueAt) { dueAt = it }
    FormField(stringResource(R.string.description_label), description, singleLine = false) { description = it }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { onSave(title, subject, dueAt, description) },
        enabled = title.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.save))
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4
    )
}
