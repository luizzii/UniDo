package com.example.unido.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
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
import com.example.unido.data.Tarefa
import com.example.unido.viewmodel.TarefaViewModel

@Composable
fun DetalhesTarefaScreen(
    tarefaId: Int,
    viewModel: TarefaViewModel,
    onVoltar: () -> Unit
) {
    var tarefa by remember { mutableStateOf<Tarefa?>(null) }
    var titulo by remember { mutableStateOf("") }
    var disciplina by remember { mutableStateOf("") }
    var dataHora by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    LaunchedEffect(tarefaId) {
        if (tarefaId > 0) {
            tarefa = viewModel.buscarPorId(tarefaId)
            tarefa?.let {
                titulo = it.titulo
                disciplina = it.disciplina
                dataHora = it.dataHora
                descricao = it.descricao
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            Text("Retornar à lista", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        if (tarefaId <= 0) {
            Text("Nova tarefa", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            Campo("Título", titulo) { titulo = it }
            Campo("Disciplina", disciplina) { disciplina = it }
            Campo("Data e hora", dataHora) { dataHora = it }
            Campo("Descrição", descricao, singleLine = false) { descricao = it }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.adicionarTarefa(titulo, disciplina, dataHora, descricao)
                    onVoltar()
                },
                enabled = titulo.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR")
            }
        } else if (tarefa == null) {
            Text("Carregando...", fontSize = 18.sp)
        } else {
            val atual = tarefa!!
            Text(
                if (atual.concluida) "Atividade concluída" else "Atividade pendente",
                modifier = Modifier.fillMaxWidth().background(Color(0xFFD0D0D0)).padding(8.dp),
                fontSize = 15.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(atual.titulo, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(atual.disciplina, fontSize = 20.sp)
            Text(atual.criadoPor, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(Modifier.height(12.dp))
            Text(
                if (atual.concluida) "Terminou em ${atual.dataHora}" else "Termina em ${atual.dataHora}",
                modifier = Modifier.fillMaxWidth().background(Color(0xFFD0D0D0)).padding(8.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text("Descrição", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                atual.descricao,
                modifier = Modifier.fillMaxWidth().background(Color(0xFFE8E8E8), RoundedCornerShape(8.dp)).padding(12.dp),
                minLines = 6
            )
            Spacer(Modifier.height(18.dp))

            if (!atual.concluida) {
                Button(
                    onClick = { viewModel.concluirTarefa(atual) },
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
private fun Campo(
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
