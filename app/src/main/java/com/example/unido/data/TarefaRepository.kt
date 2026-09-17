package com.example.unido.data

class TarefaRepository(private val dao: TarefaDao) {
    val tarefas = dao.listarTarefas()

    suspend fun inserir(tarefa: Tarefa) = dao.inserir(tarefa)
    suspend fun atualizar(tarefa: Tarefa) = dao.atualizar(tarefa)
    suspend fun deletar(tarefa: Tarefa) = dao.deletar(tarefa)
    suspend fun buscarPorId(id: Int) = dao.buscarPorId(id)
}
