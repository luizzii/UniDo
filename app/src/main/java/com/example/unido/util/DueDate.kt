package com.example.unido.util

import com.example.unido.data.local.Task
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/** Deadlines are stored in Task.dateTime as text in the [PATTERN] format. */
object DueDate {
    const val PATTERN = "dd/MM/yyyy HH:mm"

    private val locale = Locale("pt", "BR")

    // SimpleDateFormat is not thread-safe, so a new instance is created per call.
    private fun formatter() = SimpleDateFormat(PATTERN, locale).apply { isLenient = false }

    fun format(millis: Long): String = formatter().format(millis)

    /** Returns null for text that is not in [PATTERN] (e.g. "Data não informada"). */
    fun parse(text: String): Long? = try {
        formatter().parse(text)?.time
    } catch (e: ParseException) {
        null
    }
}

/** A task is overdue when it is still pending and its deadline has already passed. */
fun Task.isOverdue(now: Long = System.currentTimeMillis()): Boolean {
    if (completed) return false
    val due = DueDate.parse(dateTime) ?: return false
    return due < now
}
