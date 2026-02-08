package com.vocalcoach.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocalcoach.app.data.model.*
import com.vocalcoach.app.ui.theme.*
import com.vocalcoach.app.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val currentDayTasks by viewModel.currentDayTasks.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header with greeting
        item {
            HomeHeader(
                userProgress = userProgress,
                onProfileClick = onNavigateToProfile
            )
        }

        // Streak & XP Card
        item {
            StreakXpCard(userProgress = userProgress)
        }

        // Today's Progress
        item {
            TodayProgressCard(
                tasks = currentDayTasks,
                onStartClick = onNavigateToTasks
            )
        }

        // Quick Actions
        item {
            QuickActionsRow(
                onTasksClick = onNavigateToTasks,
                onAchievementsClick = onNavigateToAchievements
            )
        }

        // Recent Achievements
        item {
            val unlockedAchievements = achievements.filter { it.isUnlocked }
            if (unlockedAchievements.isNotEmpty()) {
                RecentAchievements(
                    achievements = unlockedAchievements.take(5),
                    onSeeAll = onNavigateToAchievements
                )
            }
        }

        // Motivation Card
        item {
            MotivationCard()
        }
    }
}

@Composable
private fun HomeHeader(
    userProgress: UserProgress?,
    onProfileClick: () -> Unit
) {
    val progress = userProgress ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryLight.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = getGreeting(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${progress.avatarEmoji} ${progress.userName}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getLevelTitle(progress.level),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LevelUpPurple
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryLight, SecondaryLight)
                        )
                    )
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = progress.avatarEmoji,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun StreakXpCard(userProgress: UserProgress?) {
    val progress = userProgress ?: return
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fireScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Streak Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = StreakFire.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥",
                    fontSize = (28 * fireScale).sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.currentStreak}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = StreakFire
                )
                Text(
                    text = "连续天数",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // XP Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = StarGold.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⭐", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.totalXp}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
                Text(
                    text = "经验值",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Level Card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = LevelUpPurple.copy(alpha = 0.12f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "💎", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lv.${progress.level}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = LevelUpPurple
                )
                Text(
                    text = "等级",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun TodayProgressCard(
    tasks: List<DailyTask>,
    onStartClick: () -> Unit
) {
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 今日任务",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedCount / $totalCount",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (completedCount == totalCount && totalCount > 0)
                        PracticeGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (completedCount == totalCount && totalCount > 0)
                    PracticeGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (completedCount == totalCount && totalCount > 0) {
                Text(
                    text = "🎉 太棒了！今天的任务全部完成！",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = PracticeGreen,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (completedCount == 0) "开始今天的练习！" else "继续练习 💪",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onTasksClick: () -> Unit,
    onAchievementsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            modifier = Modifier.weight(1f),
            emoji = "🎯",
            title = "每日任务",
            subtitle = "完成今天的练习",
            backgroundColor = ChallengePink.copy(alpha = 0.1f),
            onClick = onTasksClick
        )
        QuickActionCard(
            modifier = Modifier.weight(1f),
            emoji = "🏆",
            title = "成就墙",
            subtitle = "查看你的成就",
            backgroundColor = StarGold.copy(alpha = 0.1f),
            onClick = onAchievementsClick
        )
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RecentAchievements(
    achievements: List<Achievement>,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 最近成就",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onSeeAll) {
                Text("查看全部")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievement ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BubbleLavender.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = achievement.emoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MotivationCard() {
    val motivations = listOf(
        "🌟 每天进步一点点，终有一天会闪闪发光！",
        "🎤 你的声音独一无二，用它唱出你的故事！",
        "💪 坚持就是胜利！大歌手都是这样一步步走过来的！",
        "🌈 音乐没有对错，尽情享受唱歌的快乐吧！",
        "🎵 不完美才是完美，勇敢唱出来！",
        "✨ 今天的你比昨天更棒了！继续加油！"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubbleMint.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = motivations.random(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 6 -> "🌙 夜深了"
        hour < 9 -> "🌅 早上好"
        hour < 12 -> "☀️ 上午好"
        hour < 14 -> "🌤 中午好"
        hour < 18 -> "🌇 下午好"
        hour < 21 -> "🌆 晚上好"
        else -> "🌙 夜深了"
    }
}
