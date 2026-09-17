package com.example.unido.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Tarefa::class], version = 1, exportSchema = false)
abstract class TarefaDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: TarefaDatabase? = null

        fun getInstance(context: Context): TarefaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TarefaDatabase::class.java,
                    "unido.db"
                ).build().also { INSTANCE = it }
            }
    }
}
