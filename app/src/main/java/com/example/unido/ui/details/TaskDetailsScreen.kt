package com.example.unido.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unido.R
import com.example.unido.data.local.Task
import com.example.unido.ui.components.BackHeader
import com.example.unido.ui.components.rememberCurrentTimeMillis
import com.example.unido.ui.theme.UniDoColors
import com.example.unido.util.DueDate
import com.example.unido.util.isOverdue
import com.example.unido.viewmodel.TaskViewModel

@Composable
fun TaskDetailsScreen(
    taskId: Int,
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val task by remember(taskId) { viewModel.getTask(taskId) }.collectAsState(initial = null)
    val current = task

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        BackHeader(title = stringResource(R.string.back_to_list), onBack = onBack) {
            if (current != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_task))
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (current == null) {
            Text(stringResource(R.string.loading), fontSize = 18.sp)
        } else {
            TaskDetails(
                task = current,
                onCompletedChange = { viewModel.setCompleted(current, it) }
            )
        }
    }
}

@Composable
private fun TaskDetails(task: Task, onCompletedChange: (Boolean) -> Unit) {
    val now by rememberCurrentTimeMillis()
    val overdue = task.isOverdue(now)

    val statusText: String
    val bannerColor: Color
    val textColor: Color
    when {
        task.completed -> {
            statusText = stringResource(R.string.banner_completed)
            bannerColor = UniDoColors.completedContainer
            textColor = UniDoColors.completed
        }
        overdue -> {
            statusText = stringResource(R.string.banner_overdue)
            bannerColor = UniDoColors.overdueContainer
            textColor = UniDoColors.overdue
        }
        else -> {
            statusText = stringResource(R.string.banner_pending)
            bannerColor = UniDoColors.pendingBanner
            textColor = MaterialTheme.colorScheme.onSurface
        }
    }

    val dueAt = task.dueAt
    val dueText = when {
        dueAt == null -> stringResource(R.string.no_due_date)
        // The stored date is the deadline, not the moment the task was completed.
        task.completed -> stringResource(R.string.due_completed, DueDate.format(dueAt))
        overdue -> stringResource(R.string.due_overdue, DueDate.format(dueAt))
        else -> stringResource(R.string.due_pending, DueDate.format(dueAt))
    }

    Column {
        Text(
            statusText,
            modifier = Modifier.fillMaxWidth().background(bannerColor).padding(8.dp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(task.title, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(task.subject.ifBlank { stringResource(R.string.no_subject) }, fontSize = 20.sp)
        Text(task.createdBy, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Text(
            dueText,
            modifier = Modifier.fillMaxWidth().background(bannerColor).padding(8.dp),
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.description_label),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            task.description.ifBlank { stringResource(R.string.no_description) },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            minLines = 6
        )
        Spacer(Modifier.height(18.dp))

        if (!task.completed) {
            Button(
                onClick = { onCompletedChange(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = UniDoColors.completed,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.complete))
            }
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.task_completed_description),
                modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally),
                tint = UniDoColors.completed
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onCompletedChange(false) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(stringResource(R.string.reopen))
            }
        }
    }
}
