package com.vocalcoach.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAchievements: () -> Unit
) {
    val userProgress by viewModel.userProgress.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()

    val progress = userProgress ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(progress = progress)
        }

        // Stats Grid
        item {
            StatsGrid(progress = progress)
        }

        // Level Progress
        item {
            LevelProgressCard(progress = progress)
        }

        // Achievements Preview
        item {
            AchievementsPreview(
                achievements = achievements,
                onSeeAll = onNavigateToAchievements
            )
        }

        // Recent Scores
        if (recentRecords.isNotEmpty()) {
            item {
                Text(
                    text = "📈 最近成绩",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(recentRecords.take(5)) { record ->
                RecentScoreItem(record = record)
            }
        }
    }
}

@Composable
private fun ProfileHeader(progress: UserProgress) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryLight.copy(alpha = 0.15f),
                        SecondaryLight.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryLight, SecondaryLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = progress.avatarEmoji, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = progress.userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = getLevelTitle(progress.level),
                style = MaterialTheme.typography.bodyLarge,
                color = LevelUpPurple,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatsGrid(progress: UserProgress) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                value = "${progress.currentStreak}天",
                label = "当前连续",
                color = StreakFire
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "🏆",
                value = "${progress.longestStreak}天",
                label = "最长连续",
                color = StarGold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "⭐",
                value = "${progress.totalXp}",
                label = "总经验值",
                color = StarGold
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "⏱",
                value = "${progress.totalPracticeMinutes}分钟",
                label = "练习时长",
                color = RewardBlue
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "📚",
                value = "${progress.lessonsCompleted}",
                label = "已完成课程",
                color = PracticeGreen
            )
            StatCard(
                modifier = Modifier.weight(1f),
                emoji = "💎",
                value = "Lv.${progress.level}",
                label = "当前等级",
                color = LevelUpPurple
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun LevelProgressCard(progress: UserProgress) {
    val currentXp = progress.totalXp
    val currentLevelXp = getXpForLevel(progress.level)
    val nextLevelXp = getXpForLevel(progress.level + 1)
    val progressInLevel = if (nextLevelXp > currentLevelXp) {
        ((currentXp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp)).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LevelUpPurple.copy(alpha = 0.08f)
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
                    text = "💎 等级进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lv.${progress.level} → Lv.${progress.level + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LevelUpPurple,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progressInLevel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = LevelUpPurple,
                trackColor = LevelUpPurple.copy(alpha = 0.15f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "还需 ${(nextLevelXp - currentXp).coerceAtLeast(0)} XP 升级",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AchievementsPreview(
    achievements: List<Achievement>,
    onSeeAll: () -> Unit
) {
    val unlockedCount = achievements.count { it.isUnlocked }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = StarGold.copy(alpha = 0.08f)
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
                    text = "🏆 成就 ($unlockedCount/${achievements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onSeeAll) {
                    Text("查看全部")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                achievements.take(5).forEach { achievement ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (achievement.isUnlocked) achievement.emoji else "🔒",
                            fontSize = 28.sp,
                            modifier = Modifier
                                .let {
                                    if (!achievement.isUnlocked) it
                                    else it
                                }
                        )
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            color = if (achievement.isUnlocked)
                                MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentScoreItem(record: PracticeRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getScoreEmoji(record.score),
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "练习记录",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.practiceDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${record.score}分",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    record.score >= 90 -> StarGold
                    record.score >= 80 -> PracticeGreen
                    record.score >= 70 -> SecondaryLight
                    else -> PrimaryLight
                }
            )
        }
    }
}
