package com.example.unido.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unido.R
import com.example.unido.data.local.Task
import com.example.unido.ui.components.rememberCurrentTimeMillis
import com.example.unido.ui.theme.CompletedGreen
import com.example.unido.ui.theme.CompletedGreenContainer
import com.example.unido.ui.theme.OverdueRed
import com.example.unido.ui.theme.OverdueRedContainer
import com.example.unido.ui.theme.PendingContainer
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

    val filteredTasks = tasks.filter {
        statusFilter.matches(it, now) &&
            (it.title.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Header()
        Spacer(Modifier.height(12.dp))
        Text("Minhas tarefas", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNewTask) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar tarefa", modifier = Modifier.size(30.dp))
            }
            FilterButton(selected = statusFilter, onSelect = { statusFilter = it })
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF777777), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            val suffix = if (statusFilter == TaskFilter.ALL) "" else " · ${statusFilter.label.uppercase()}"
            Text("TAREFAS$suffix", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        when {
            tasks.isEmpty() -> EmptyState("Nenhuma tarefa cadastrada.") {
                TextButton(onClick = onNewTask) { Text("Adicionar primeira tarefa") }
            }
            filteredTasks.isEmpty() -> EmptyState("Nenhuma tarefa encontrada.") {
                TextButton(onClick = {
                    searchQuery = ""
                    statusFilter = TaskFilter.ALL
                }) { Text("Limpar busca e filtro") }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        overdue = task.isOverdue(now),
                        onClick = { onTaskClick(task.id) },
                        onComplete = { viewModel.completeTask(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_unido_logo),
            contentDescription = "Logo UniDo",
            modifier = Modifier.size(76.dp),
            tint = Color.Unspecified
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text("UniDo", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Sua rotina acadêmica", fontSize = 12.sp)
            Text("organizada.", fontSize = 12.sp)
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
        placeholder = { Text("Buscar título ou disciplina") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
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
                Icon(Icons.Default.FilterList, contentDescription = "Filtrar por status")
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = {
                        onSelect(filter)
                        expanded = false
                    },
                    trailingIcon = {
                        if (filter == selected) Icon(Icons.Default.Check, contentDescription = "Selecionado")
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
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor = when {
        task.completed -> CompletedGreenContainer
        overdue -> OverdueRedContainer
        else -> PendingContainer
    }
    val border = when {
        task.completed -> BorderStroke(1.dp, CompletedGreen)
        overdue -> BorderStroke(1.dp, OverdueRed)
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { if (it) onComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CompletedGreen,
                    uncheckedColor = if (overdue) OverdueRed else Color.DarkGray
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(task.subject, fontSize = 12.sp)
                Text(task.createdBy, fontSize = 10.sp, color = Color.DarkGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                when {
                    task.completed -> StatusLabel("CONCLUÍDA", CompletedGreen)
                    overdue -> {
                        StatusLabel("ATRASADA", OverdueRed)
                        StatusLabel(task.dateTime, OverdueRed)
                    }
                    else -> StatusLabel(task.dateTime, Color.Black)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa")
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(text: String, color: Color) {
    Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
}
