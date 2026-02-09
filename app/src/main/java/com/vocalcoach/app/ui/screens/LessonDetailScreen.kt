package com.vocalcoach.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onStartPractice: () -> Unit
) {
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val lesson = selectedLesson ?: return

    val categoryColor = getCategoryColor(lesson.category)
    val scrollState = rememberScrollState()

    // Breathing animation for the emoji
    val infiniteTransition = rememberInfiniteTransition(label = "emoji_pulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Hero section with emoji and gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                categoryColor.copy(alpha = 0.15f),
                                categoryColor.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Large emoji with pulse animation
                    Box(
                        modifier = Modifier
                            .size((100 * emojiScale).dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lesson.thumbnailEmoji,
                            fontSize = (48 * emojiScale).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category + Difficulty + Duration row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = getCategoryLabel(lesson.category),
                                style = MaterialTheme.typography.labelMedium,
                                color = categoryColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = getDifficultyLabel(lesson.difficulty),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "⏱ ${lesson.durationMinutes}分钟",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Description card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📖 课程介绍",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = lesson.description,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Learning objectives card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PracticeGreen.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🎯 学习目标",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val objectives = getLessonObjectives(lesson)
                    objectives.forEach { objective ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✅ ",
                                fontSize = 14.sp
                            )
                            Text(
                                text = objective,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tips card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TertiaryLight.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "💡 小贴士",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = getLessonTip(lesson),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start practice button
            Button(
                onClick = onStartPractice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryColor
                )
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "开始练习",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun getCategoryColor(category: LessonCategory): Color {
    return when (category) {
        LessonCategory.BREATHING -> RewardBlue
        LessonCategory.PITCH -> ChallengePink
        LessonCategory.RHYTHM -> TertiaryLight
        LessonCategory.TONE -> LevelUpPurple
        LessonCategory.TECHNIQUE -> StreakFire
        LessonCategory.SONG_PRACTICE -> PracticeGreen
        LessonCategory.WARM_UP -> StarGold
        LessonCategory.COOL_DOWN -> SecondaryLight
    }
}

private fun getLessonObjectives(lesson: Lesson): List<String> {
    return when (lesson.category) {
        LessonCategory.BREATHING -> listOf(
            "掌握正确的呼吸方法",
            "增强气息控制能力",
            "为歌唱建立稳定的气息基础"
        )
        LessonCategory.WARM_UP -> listOf(
            "正确打开口腔和声音通道",
            "激活发声肌肉群",
            "为正式练习做好充分准备"
        )
        LessonCategory.PITCH -> listOf(
            "提升音准感知能力",
            "训练耳朵辨别音高",
            "准确唱出目标音高"
        )
        LessonCategory.RHYTHM -> listOf(
            "建立稳定的节奏感",
            "掌握不同节奏型",
            "身体与音乐的律动协调"
        )
        LessonCategory.TONE -> listOf(
            "找到并运用共鸣腔体",
            "改善声音的质感和厚度",
            "让声音更加圆润饱满"
        )
        LessonCategory.TECHNIQUE -> listOf(
            "学习流行演唱的核心技巧",
            "提升演唱的表现力",
            "在实际歌曲中运用技巧"
        )
        LessonCategory.SONG_PRACTICE -> listOf(
            "完整演绎一首歌曲",
            "综合运用所学技巧",
            "培养舞台表现力和自信心"
        )
        LessonCategory.COOL_DOWN -> listOf(
            "放松紧张的声带肌肉",
            "学会保护嗓子的方法",
            "建立良好的练习收尾习惯"
        )
    }
}

private fun getLessonTip(lesson: Lesson): String {
    return when (lesson.category) {
        LessonCategory.BREATHING -> "练习呼吸时保持肩膀放松，把注意力放在腹部的起伏上。每天坚持5分钟，效果就会很明显哦！"
        LessonCategory.WARM_UP -> "热身非常重要！就像运动前要拉伸一样，唱歌前也要让声带热起来。不要跳过这一步～"
        LessonCategory.PITCH -> "音准训练需要耐心，先用耳朵听，在脑海里想象那个音，然后再唱出来。慢慢来，不着急！"
        LessonCategory.RHYTHM -> "练节奏的秘诀：先用手拍，再用脚踩，最后加上嘴巴唱。一步一步来，很快就能协调啦！"
        LessonCategory.TONE -> "找共鸣的感觉就像找WiFi信号——慢慢调整位置，突然就连上了！多尝试不同的发声位置吧～"
        LessonCategory.TECHNIQUE -> "技巧需要大量的肌肉记忆训练。别急，每天练一点点，量变一定会带来质变！"
        LessonCategory.SONG_PRACTICE -> "唱歌最重要的是投入情感！技巧只是工具，真正打动人的是你的真情实感。放开唱吧！"
        LessonCategory.COOL_DOWN -> "练完嗓子后多喝温水，避免冷饮和辛辣食物。你的嗓子是最珍贵的乐器，要好好爱护它！"
    }
}
