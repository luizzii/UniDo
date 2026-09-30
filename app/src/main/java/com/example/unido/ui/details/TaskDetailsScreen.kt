package com.example.unido.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unido.data.local.Task
import com.example.unido.ui.components.rememberCurrentTimeMillis
import com.example.unido.ui.theme.CompletedGreen
import com.example.unido.ui.theme.CompletedGreenContainer
import com.example.unido.ui.theme.OverdueRed
import com.example.unido.ui.theme.OverdueRedContainer
import com.example.unido.ui.theme.PendingBanner
import com.example.unido.util.isOverdue
import com.example.unido.viewmodel.TaskViewModel

@Composable
fun TaskDetailsScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            Text("Retornar à lista", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        if (taskId <= 0) {
            NewTaskForm(viewModel, onBack)
        } else {
            val task by remember(taskId) { viewModel.getTask(taskId) }.collectAsState(initial = null)
            val current = task
            if (current == null) {
                Text("Carregando...", fontSize = 18.sp)
            } else {
                TaskDetails(current, onComplete = { viewModel.completeTask(current) })
            }
        }
    }
}

@Composable
private fun NewTaskForm(viewModel: TaskViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Text("Nova tarefa", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    FormField("Título", title) { title = it }
    FormField("Disciplina", subject) { subject = it }
    DateTimePickerField("Data e hora", dateTime) { dateTime = it }
    FormField("Descrição", description, singleLine = false) { description = it }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = {
            viewModel.addTask(title, subject, dateTime, description)
            onBack()
        },
        enabled = title.isNotBlank(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("SALVAR")
    }
}

@Composable
private fun TaskDetails(task: Task, onComplete: () -> Unit) {
    val now by rememberCurrentTimeMillis()
    val overdue = task.isOverdue(now)

    val (statusText, bannerColor, textColor) = when {
        task.completed -> Triple("Atividade concluída", CompletedGreenContainer, CompletedGreen)
        overdue -> Triple("Atividade atrasada", OverdueRedContainer, OverdueRed)
        else -> Triple("Atividade pendente", PendingBanner, Color.Black)
    }

    Column {
        Text(
            statusText,
            modifier = Modifier.fillMaxWidth().background(bannerColor).padding(8.dp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(task.title, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(task.subject, fontSize = 20.sp)
        Text(task.createdBy, fontSize = 14.sp, color = Color.DarkGray)
        Spacer(Modifier.height(12.dp))
        Text(
            when {
                task.completed -> "Terminou em ${task.dateTime}"
                overdue -> "Prazo encerrado em ${task.dateTime}"
                else -> "Termina em ${task.dateTime}"
            },
            modifier = Modifier.fillMaxWidth().background(bannerColor).padding(8.dp),
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text("Descrição", fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(
            task.description,
            modifier = Modifier.fillMaxWidth().background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp)).padding(12.dp),
            minLines = 6
        )
        Spacer(Modifier.height(18.dp))

        if (!task.completed) {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = CompletedGreen),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("CONCLUIR")
            }
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Tarefa concluída",
                modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally),
                tint = CompletedGreen
            )
        }
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
