package com.vocalcoach.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocalcoach.app.data.model.*
import com.vocalcoach.app.ui.theme.*
import com.vocalcoach.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onTaskClick: (DailyTask) -> Unit
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val currentDayTasks by viewModel.currentDayTasks.collectAsState()

    val progress = userProgress
    val dayNumber = progress?.currentDay ?: 1
    val completedCount = currentDayTasks.count { it.isCompleted }
    val totalCount = currentDayTasks.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SecondaryLight.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "📋 第 $dayNumber 天",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "完成进度: $completedCount / $totalCount",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    if (completedCount == totalCount && totalCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "✅", fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (completedCount == totalCount && totalCount > 0)
                        PracticeGreen else SecondaryLight,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            }
        }

        // Task List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(currentDayTasks) { index, task ->
                TaskCard(
                    task = task,
                    index = index + 1,
                    onClick = { if (!task.isCompleted) onTaskClick(task) }
                )
            }

            if (currentDayTasks.isEmpty()) {
                item {
                    EmptyTasksPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: DailyTask,
    index: Int,
    onClick: () -> Unit
) {
    val backgroundColor = if (task.isCompleted) {
        PracticeGreen.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val taskTypeColor = when (task.taskType) {
        TaskType.WATCH_VIDEO -> RewardBlue
        TaskType.LISTEN_AUDIO -> LevelUpPurple
        TaskType.SING_ALONG -> SecondaryLight
        TaskType.RECORD_SELF -> ChallengePink
        TaskType.QUIZ -> TertiaryLight
        TaskType.FREE_PRACTICE -> PracticeGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !task.isCompleted, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task number / check
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) PracticeGreen
                        else taskTypeColor.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = taskTypeColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Task type badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = taskTypeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = getTaskTypeLabel(task.taskType),
                            style = MaterialTheme.typography.labelSmall,
                            color = taskTypeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // XP reward
                    Text(
                        text = "+${task.xpReward} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = StarGold,
                        fontWeight = FontWeight.Bold
                    )

                    // Score if completed
                    if (task.isCompleted && task.score != null) {
                        Text(
                            text = "${getScoreEmoji(task.score)} ${task.score}分",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PracticeGreen
                        )
                    }
                }
            }

            // Arrow or score
            if (!task.isCompleted) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎉", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "今天的任务已全部完成！",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "明天再来继续你的声乐之旅吧～",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
