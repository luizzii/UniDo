package com.example.unido

import com.example.unido.data.local.Task
import com.example.unido.util.DueDate
import com.example.unido.util.isOverdue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DueDateTest {
    @Test
    fun `format and parse are inverse operations`() {
        val text = "25/12/2026 18:30"
        val millis = DueDate.parse(text)

        assertNotNull(millis)
        assertEquals(text, DueDate.format(millis!!))
    }

    @Test
    fun `parse rejects text that is not a date`() {
        assertNull(DueDate.parse("Data não informada"))
        assertNull(DueDate.parse(""))
        assertNull(DueDate.parse("25/12/2026"))
    }

    @Test
    fun `parse rejects dates that do not exist`() {
        assertNull(DueDate.parse("31/02/2026 10:00"))
        assertNull(DueDate.parse("10/13/2026 10:00"))
        assertNull(DueDate.parse("10/10/2026 25:00"))
    }

    @Test
    fun `task with a past deadline is overdue`() {
        assertTrue(task(dueAt = NOW - 1).isOverdue(NOW))
    }

    @Test
    fun `task with a future deadline is not overdue`() {
        assertFalse(task(dueAt = NOW + 1).isOverdue(NOW))
        assertFalse(task(dueAt = NOW).isOverdue(NOW))
    }

    @Test
    fun `completed task is never overdue`() {
        assertFalse(task(dueAt = NOW - 1, completed = true).isOverdue(NOW))
    }

    @Test
    fun `task without a deadline is never overdue`() {
        assertFalse(task(dueAt = null).isOverdue(NOW))
    }
}

internal const val NOW = 1_800_000_000_000L

internal fun task(dueAt: Long?, completed: Boolean = false) = Task(
    title = "Trabalho",
    subject = "Cálculo",
    createdBy = "Criado pelo aluno",
    dueAt = dueAt,
    description = "",
    completed = completed
)
