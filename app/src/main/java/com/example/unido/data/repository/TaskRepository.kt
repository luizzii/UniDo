package com.example.unido.data.repository

import com.example.unido.data.local.Task
import com.example.unido.data.local.TaskDao

class TaskRepository(private val dao: TaskDao) {
    val tasks = dao.getAllTasks()

    suspend fun insert(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    fun getById(id: Int) = dao.getById(id)
}
