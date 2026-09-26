package com.example.unido

import android.app.Application
import com.example.unido.data.local.TaskDatabase
import com.example.unido.data.repository.TaskRepository

class UniDoApplication : Application() {
    val repository: TaskRepository by lazy {
        TaskRepository(TaskDatabase.getInstance(this).taskDao())
    }
}
