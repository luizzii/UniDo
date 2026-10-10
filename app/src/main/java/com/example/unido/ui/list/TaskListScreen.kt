package com.example.unido.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unido.R
import com.example.unido.data.local.Task
import com.example.unido.ui.components.rememberCurrentTimeMillis
import com.example.unido.ui.theme.UniDoColors
import com.example.unido.util.DueDate
import com.example.unido.util.isOverdue
import com.example.unido.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNewTask: () -> Unit,
    onTaskClick: (Int) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val now by rememberCurrentTimeMillis()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = remember(tasks, statusFilter, searchQuery, now) {
        tasks.filter {
            statusFilter.matches(it, now) &&
                (it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subject.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Keeps the content clear of the status bar, navigation bar and keyboard.
            .safeDrawingPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Header()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.my_tasks), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNewTask) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task),
                    modifier = Modifier.size(30.dp)
                )
            }
            FilterButton(selected = statusFilter, onSelect = { statusFilter = it })
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(UniDoColors.sectionBar, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            val header = if (statusFilter == TaskFilter.ALL) {
                stringResource(R.string.tasks_header)
            } else {
                stringResource(R.string.tasks_header_filtered, stringResource(statusFilter.labelRes).uppercase())
            }
            Text(header, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        when {
            tasks.isEmpty() -> EmptyState(stringResource(R.string.empty_no_tasks)) {
                TextButton(onClick = onNewTask) { Text(stringResource(R.string.add_first_task)) }
            }
            filteredTasks.isEmpty() -> EmptyState(stringResource(R.string.empty_no_results)) {
                TextButton(onClick = {
                    searchQuery = ""
                    statusFilter = TaskFilter.ALL
                }) { Text(stringResource(R.string.clear_search_and_filter)) }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        overdue = task.isOverdue(now),
                        onClick = { onTaskClick(task.id) },
                        onCompletedChange = { viewModel.setCompleted(task, it) },
                        onDelete = { taskToDelete = task }
                    )
                }
            }
        }
    }

    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message, task.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                }) { Text(stringResource(R.string.delete), color = UniDoColors.overdue) }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_unido_logo),
            contentDescription = stringResource(R.string.logo_description),
            modifier = Modifier.size(76.dp),
            tint = Color.Unspecified
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(stringResource(R.string.app_name), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.tagline_line1), fontSize = 12.sp)
            Text(stringResource(R.string.tagline_line2), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun FilterButton(selected: TaskFilter, onSelect: (TaskFilter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            BadgedBox(badge = { if (selected != TaskFilter.ALL) Badge() }) {
                Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter_by_status))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(stringResource(filter.labelRes)) },
                    onClick = {
                        onSelect(filter)
                        expanded = false
                    },
                    trailingIcon = {
                        if (filter == selected) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.filter_selected))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, action: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        action()
    }
}

@Composable
private fun TaskCard(
    task: Task,
    overdue: Boolean,
    onClick: () -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val containerColor = when {
        task.completed -> UniDoColors.completedContainer
        overdue -> UniDoColors.overdueContainer
        else -> UniDoColors.pendingContainer
    }
    val border = when {
        task.completed -> BorderStroke(1.dp, UniDoColors.completed)
        overdue -> BorderStroke(1.dp, UniDoColors.overdue)
        else -> null
    }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondaryText = MaterialTheme.colorScheme.onSurfaceVariant
    val dueText = task.dueAt?.let { DueDate.format(it) } ?: stringResource(R.string.no_due_date)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = onSurface),
        border = border
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCompletedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = UniDoColors.completed,
                    uncheckedColor = if (overdue) UniDoColors.overdue else secondaryText
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(task.subject.ifBlank { stringResource(R.string.no_subject) }, fontSize = 12.sp)
                Text(task.createdBy, fontSize = 10.sp, color = secondaryText)
            }
            Column(horizontalAlignment = Alignment.End) {
                when {
                    task.completed -> StatusLabel(stringResource(R.string.status_completed), UniDoColors.completed)
                    overdue -> {
                        StatusLabel(stringResource(R.string.status_overdue), UniDoColors.overdue)
                        StatusLabel(dueText, UniDoColors.overdue)
                    }
                    else -> StatusLabel(dueText, onSurface)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_task))
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(text: String, color: Color) {
    Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
}
