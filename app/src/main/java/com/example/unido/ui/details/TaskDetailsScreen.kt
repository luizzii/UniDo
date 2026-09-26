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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.unido.viewmodel.TaskViewModel

@Composable
fun TaskDetailsScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    var task by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        if (taskId > 0) {
            task = viewModel.getById(taskId)
            task?.let {
                title = it.title
                subject = it.subject
                dateTime = it.dateTime
                description = it.description
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            Text("Retornar à lista", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        if (taskId <= 0) {
            Text("Nova tarefa", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            FormField("Título", title) { title = it }
            FormField("Disciplina", subject) { subject = it }
            FormField("Data e hora", dateTime) { dateTime = it }
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
        } else if (task == null) {
            Text("Carregando...", fontSize = 18.sp)
        } else {
            val current = task!!
            Text(
                if (current.completed) "Atividade concluída" else "Atividade pendente",
                modifier = Modifier.fillMaxWidth().background(Color(0xFFD0D0D0)).padding(8.dp),
                fontSize = 15.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(current.title, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(current.subject, fontSize = 20.sp)
            Text(current.createdBy, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(Modifier.height(12.dp))
            Text(
                if (current.completed) "Terminou em ${current.dateTime}" else "Termina em ${current.dateTime}",
                modifier = Modifier.fillMaxWidth().background(Color(0xFFD0D0D0)).padding(8.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text("Descrição", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                current.description,
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp)).padding(12.dp),
                minLines = 6
            )
            Spacer(Modifier.height(18.dp))

            if (!current.completed) {
                Button(
                    onClick = { viewModel.completeTask(current) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("CONCLUIR")
                }
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Tarefa concluída",
                    modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally),
                    tint = Color.DarkGray
                )
            }
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
