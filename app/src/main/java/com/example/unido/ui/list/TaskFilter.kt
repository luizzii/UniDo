package com.example.unido.ui.list

import androidx.annotation.StringRes
import com.example.unido.R
import com.example.unido.data.local.Task
import com.example.unido.util.isOverdue

/** Status filter from the filter button on the list screen. */
enum class TaskFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    PENDING(R.string.filter_pending),
    OVERDUE(R.string.filter_overdue),
    COMPLETED(R.string.filter_completed);

    fun matches(task: Task, now: Long): Boolean = when (this) {
        ALL -> true
        PENDING -> !task.completed && !task.isOverdue(now)
        OVERDUE -> task.isOverdue(now)
        COMPLETED -> task.completed
    }
}
