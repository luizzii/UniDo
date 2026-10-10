package com.example.unido.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.unido.util.DueDate

/**
 * Version 1 stored the deadline as text ("dd/MM/yyyy HH:mm") in `dataHora` and wrote placeholder
 * phrases into empty fields. Version 2 stores the deadline as epoch milliseconds in `prazo`
 * (null = no deadline) and keeps empty fields empty.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE Tarefa_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, " +
                "disciplina TEXT NOT NULL, " +
                "criadoPor TEXT NOT NULL, " +
                "prazo INTEGER, " +
                "descricao TEXT NOT NULL, " +
                "concluida INTEGER NOT NULL)"
        )

        db.query("SELECT id, titulo, disciplina, criadoPor, dataHora, descricao, concluida FROM Tarefa").use { cursor ->
            while (cursor.moveToNext()) {
                val subject = cursor.getString(2).takeUnless { it == LEGACY_NO_SUBJECT }.orEmpty()
                val description = cursor.getString(5).takeUnless { it == LEGACY_NO_DESCRIPTION }.orEmpty()
                // Text that is not a valid date (e.g. "Data não informada") becomes "no deadline".
                val dueAt: Long? = DueDate.parse(cursor.getString(4).orEmpty())

                db.execSQL(
                    "INSERT INTO Tarefa_new (id, titulo, disciplina, criadoPor, prazo, descricao, concluida) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(
                        cursor.getInt(0),
                        cursor.getString(1),
                        subject,
                        cursor.getString(3),
                        dueAt,
                        description,
                        cursor.getInt(6)
                    )
                )
            }
        }

        db.execSQL("DROP TABLE Tarefa")
        db.execSQL("ALTER TABLE Tarefa_new RENAME TO Tarefa")
    }
}

private const val LEGACY_NO_SUBJECT = "Sem disciplina"
private const val LEGACY_NO_DESCRIPTION = "Sem descrição"
