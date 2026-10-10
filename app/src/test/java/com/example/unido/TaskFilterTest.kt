package com.example.unido

import com.example.unido.ui.list.TaskFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {
    private val pending = task(dueAt = NOW + 60_000)
    private val noDeadline = task(dueAt = null)
    private val overdue = task(dueAt = NOW - 60_000)
    private val completed = task(dueAt = NOW + 60_000, completed = true)
    private val completedLate = task(dueAt = NOW - 60_000, completed = true)
    private val all = listOf(pending, noDeadline, overdue, completed, completedLate)

    private fun filter(filter: TaskFilter) = all.filter { filter.matches(it, NOW) }

    @Test
    fun `ALL keeps every task`() {
        assertEquals(all, filter(TaskFilter.ALL))
    }

    @Test
    fun `PENDING keeps open tasks that are not late`() {
        assertEquals(listOf(pending, noDeadline), filter(TaskFilter.PENDING))
    }

    @Test
    fun `OVERDUE keeps only open tasks past their deadline`() {
        assertEquals(listOf(overdue), filter(TaskFilter.OVERDUE))
    }

    @Test
    fun `COMPLETED keeps completed tasks even when they were late`() {
        assertEquals(listOf(completed, completedLate), filter(TaskFilter.COMPLETED))
    }

    @Test
    fun `every task matches exactly one status besides ALL`() {
        val statuses = listOf(TaskFilter.PENDING, TaskFilter.OVERDUE, TaskFilter.COMPLETED)
        all.forEach { task ->
            assertEquals(1, statuses.count { it.matches(task, NOW) })
        }
    }
}
