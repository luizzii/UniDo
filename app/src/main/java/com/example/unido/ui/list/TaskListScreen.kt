package com.example.unido.ui.list

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.unido.viewmodel.TaskViewModel

@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onNewTask: () -> Unit,
    onTaskClick: (Int) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredTasks = tasks.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
            it.subject.contains(searchQuery, ignoreCase = true)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Nova tarefa") },
                shape = RoundedCornerShape(8.dp)
            )
            IconButton(onClick = onNewTask) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar tarefa", modifier = Modifier.size(30.dp))
            }
            IconButton(onClick = { /* searchQuery visual; a busca já filtra */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF777777), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("TAREFAS", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))

        if (filteredTasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nenhuma tarefa cadastrada.", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onNewTask) { Text("Adicionar primeira tarefa") }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(task, onTaskClick, viewModel)
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
private fun TaskCard(
    task: Task,
    onTaskClick: (Int) -> Unit,
    viewModel: TaskViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onTaskClick(task.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F2))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { if (it) viewModel.completeTask(task) }
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
                Text(if (task.completed) "CONCLUÍDA" else task.dateTime, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                IconButton(onClick = { viewModel.deleteTask(task) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa")
                }
            }
        }
    }
}

