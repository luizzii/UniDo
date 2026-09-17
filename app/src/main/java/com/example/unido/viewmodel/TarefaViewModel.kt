package com.example.unido.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.unido.data.Tarefa
import com.example.unido.data.TarefaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TarefaViewModel(private val repository: TarefaRepository) : ViewModel() {
    val tarefas: StateFlow<List<Tarefa>> = repository.tarefas.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun adicionarTarefa(titulo: String, disciplina: String, dataHora: String, descricao: String) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            repository.inserir(
                Tarefa(
                    titulo = titulo.trim(),
                    disciplina = disciplina.trim().ifBlank { "Sem disciplina" },
                    criadoPor = "Criado pelo aluno",
                    dataHora = dataHora.trim().ifBlank { "Data não informada" },
                    descricao = descricao.trim().ifBlank { "Sem descrição" }
                )
            )
        }
    }

    fun concluirTarefa(tarefa: Tarefa) {
        viewModelScope.launch { repository.atualizar(tarefa.copy(concluida = true)) }
    }

    fun deletarTarefa(tarefa: Tarefa) {
        viewModelScope.launch { repository.deletar(tarefa) }
    }

    suspend fun buscarPorId(id: Int): Tarefa? = repository.buscarPorId(id)

    companion object {
        fun factory(repository: TarefaRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TarefaViewModel(repository) as T
            }
    }
}
