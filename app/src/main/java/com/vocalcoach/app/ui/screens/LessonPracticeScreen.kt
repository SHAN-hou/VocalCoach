package com.vocalcoach.app.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocalcoach.app.data.model.*
import com.vocalcoach.app.ui.theme.*
import com.vocalcoach.app.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

private enum class LessonPracticeState {
    READY, PRACTICING, PAUSED, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPracticeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val lesson = selectedLesson ?: return

    var practiceState by remember { mutableStateOf(LessonPracticeState.READY) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var currentEncouragementIndex by remember { mutableIntStateOf(0) }
    var showEncouragement by remember { mutableStateOf(false) }
    var totalXpEarned by remember { mutableIntStateOf(0) }

    val targetSeconds = lesson.durationMinutes * 60

    // Timer logic
    LaunchedEffect(practiceState) {
        if (practiceState == LessonPracticeState.PRACTICING) {
            while (practiceState == LessonPracticeState.PRACTICING) {
                delay(1000)
                elapsedSeconds++

                // Show encouragement at intervals
                if (elapsedSeconds % 30 == 0 && elapsedSeconds > 0) {
                    currentEncouragementIndex = (currentEncouragementIndex + 1) % encouragements.size
                    showEncouragement = true
                    delay(3000)
                    showEncouragement = false
                }

                // Auto-complete when target time reached
                if (elapsedSeconds >= targetSeconds) {
                    totalXpEarned = calculateXp(elapsedSeconds, targetSeconds)
                    practiceState = LessonPracticeState.COMPLETED
                }
            }
        }
    }

    // Pulse animation for practicing state
    val infiniteTransition = rememberInfiniteTransition(label = "practice_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (practiceState == LessonPracticeState.PRACTICING) {
                            practiceState = LessonPracticeState.PAUSED
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (practiceState) {
                    LessonPracticeState.READY -> ReadyContent(
                        lesson = lesson,
                        onStart = { practiceState = LessonPracticeState.PRACTICING }
                    )
                    LessonPracticeState.PRACTICING -> PracticingContent(
                        lesson = lesson,
                        elapsedSeconds = elapsedSeconds,
                        targetSeconds = targetSeconds,
                        pulseScale = pulseScale,
                        glowAlpha = glowAlpha,
                        onPause = { practiceState = LessonPracticeState.PAUSED },
                        onFinishEarly = {
                            totalXpEarned = calculateXp(elapsedSeconds, targetSeconds)
                            practiceState = LessonPracticeState.COMPLETED
                        }
                    )
                    LessonPracticeState.PAUSED -> PausedContent(
                        elapsedSeconds = elapsedSeconds,
                        onResume = { practiceState = LessonPracticeState.PRACTICING },
                        onQuit = onBack
                    )
                    LessonPracticeState.COMPLETED -> CompletedContent(
                        lesson = lesson,
                        elapsedSeconds = elapsedSeconds,
                        targetSeconds = targetSeconds,
                        xpEarned = totalXpEarned,
                        onFinish = onFinish
                    )
                }
            }

            // Floating encouragement toast
            AnimatedVisibility(
                visible = showEncouragement && practiceState == LessonPracticeState.PRACTICING,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StarGold.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = encouragements[currentEncouragementIndex],
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyContent(
    lesson: Lesson,
    onStart: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))

    // Fun emoji animation
    val infiniteTransition = rememberInfiniteTransition(label = "ready_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .offset(y = bounceOffset.dp)
            .background(
                color = PracticeGreen.copy(alpha = 0.12f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = lesson.thumbnailEmoji, fontSize = 56.sp)
    }

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = "准备好了吗？",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "本次练习目标：${lesson.durationMinutes} 分钟",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = lesson.description,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Practice tips
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubbleLavender.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌟 练习小提示",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            val tips = listOf(
                "🎯 找个安静的地方，效果更好",
                "💧 准备一杯温水润润嗓",
                "😌 深呼吸三次，放松身体",
                "🎤 跟着节奏慢慢来，不用着急"
            )
            tips.forEach { tip ->
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PracticeGreen)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("开始练习！", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PracticingContent(
    lesson: Lesson,
    elapsedSeconds: Int,
    targetSeconds: Int,
    pulseScale: Float,
    glowAlpha: Float,
    onPause: () -> Unit,
    onFinishEarly: () -> Unit
) {
    val progress = (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f)
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60

    Spacer(modifier = Modifier.height(24.dp))

    // Animated practicing indicator
    Box(
        modifier = Modifier
            .size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size((180 * pulseScale).dp)
                .alpha(glowAlpha * 0.5f)
                .background(
                    color = PracticeGreen.copy(alpha = 0.15f),
                    shape = CircleShape
                )
        )
        // Progress ring background
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(160.dp),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = Color.Transparent
        )
        // Progress ring
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(160.dp),
            strokeWidth = 8.dp,
            color = PracticeGreen,
            trackColor = Color.Transparent
        )
        // Center content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = lesson.thumbnailEmoji,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PracticeGreen
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Progress text
    Text(
        text = "练习中...",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "已完成 ${(progress * 100).toInt()}%",
        style = MaterialTheme.typography.bodyLarge,
        color = PracticeGreen,
        fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "目标：${lesson.durationMinutes} 分钟",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Current practice step card
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubbleMint.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = getPracticeStepEmoji(elapsedSeconds, targetSeconds) + " " + getPracticeStep(lesson, elapsedSeconds, targetSeconds),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getPracticeInstruction(lesson, elapsedSeconds, targetSeconds),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Milestone badges
    if (elapsedSeconds >= 60) {
        val badges = mutableListOf<String>()
        if (elapsedSeconds >= 60) badges.add("🏅 1分钟达成！")
        if (elapsedSeconds >= 180) badges.add("⭐ 3分钟坚持！")
        if (elapsedSeconds >= 300) badges.add("🔥 5分钟燃烧！")
        if (elapsedSeconds >= 600) badges.add("👑 10分钟王者！")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            badges.forEach { badge ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StarGold.copy(alpha = 0.15f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Control buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPause,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Pause, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("暂停")
        }

        if (elapsedSeconds >= 30) {
            Button(
                onClick = onFinishEarly,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StreakFire)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("完成练习")
            }
        }
    }
}

@Composable
private fun PausedContent(
    elapsedSeconds: Int,
    onResume: () -> Unit,
    onQuit: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60

    Spacer(modifier = Modifier.height(60.dp))

    Text(text = "☕", fontSize = 64.sp)

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "休息一下～",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "已练习 ${String.format("%02d:%02d", minutes, seconds)}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubblePink.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pauseEncouragements[elapsedSeconds % pauseEncouragements.size],
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onResume,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PracticeGreen)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("继续练习！", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(12.dp))

    TextButton(onClick = onQuit) {
        Text(
            "结束本次练习",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun CompletedContent(
    lesson: Lesson,
    elapsedSeconds: Int,
    targetSeconds: Int,
    xpEarned: Int,
    onFinish: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val completionRate = ((elapsedSeconds.toFloat() / targetSeconds) * 100).coerceIn(0f, 100f).toInt()

    // Celebration animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    val celebrateScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebrate_scale"
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = if (completionRate >= 100) "🎉" else "👏",
        fontSize = (64 * celebrateScale).sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = if (completionRate >= 100) "太棒了！完美完成！" else "练习完成！",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = getCompletionMessage(completionRate),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Stats cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            emoji = "⏱",
            label = "练习时长",
            value = "${String.format("%02d:%02d", minutes, seconds)}",
            color = RewardBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "📊",
            label = "完成度",
            value = "${completionRate}%",
            color = PracticeGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            emoji = "✨",
            label = "获得XP",
            value = "+${xpEarned}",
            color = StarGold,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Achievement unlocked
    if (completionRate >= 100) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = StarGold.copy(alpha = 0.12f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🏆", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "课程达标！",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "你完成了「${lesson.title}」的全部练习目标！",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Encouragement card
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubbleLavender.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💌 老师寄语",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getTeacherMessage(lesson, completionRate),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = onFinish,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PracticeGreen)
    ) {
        Text("完成！返回课程", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun StatCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

// Helper functions

private fun calculateXp(elapsed: Int, target: Int): Int {
    val baseXp = 20
    val timeRatio = (elapsed.toFloat() / target).coerceIn(0f, 1.5f)
    val bonusXp = (baseXp * timeRatio).toInt()
    val completionBonus = if (elapsed >= target) 15 else 0
    return baseXp + bonusXp + completionBonus
}

private fun getPracticeStepEmoji(elapsed: Int, target: Int): String {
    val progress = elapsed.toFloat() / target
    return when {
        progress < 0.25f -> "🌱"
        progress < 0.50f -> "🌿"
        progress < 0.75f -> "🌳"
        progress < 1.0f -> "🌟"
        else -> "🎉"
    }
}

private fun getPracticeStep(lesson: Lesson, elapsed: Int, target: Int): String {
    val progress = elapsed.toFloat() / target
    return when (lesson.category) {
        LessonCategory.BREATHING -> when {
            progress < 0.3f -> "慢慢呼吸，找到节奏"
            progress < 0.6f -> "加深呼吸，感受腹部"
            progress < 0.9f -> "配合发声练习"
            else -> "最后冲刺！"
        }
        LessonCategory.WARM_UP -> when {
            progress < 0.3f -> "轻声哼唱热身"
            progress < 0.6f -> "打开口腔，活动嘴部"
            progress < 0.9f -> "音阶跟唱"
            else -> "热身完成！"
        }
        LessonCategory.PITCH -> when {
            progress < 0.3f -> "听音辨音练习"
            progress < 0.6f -> "跟唱单音练习"
            progress < 0.9f -> "音阶连唱"
            else -> "音准大挑战！"
        }
        LessonCategory.RHYTHM -> when {
            progress < 0.3f -> "拍手打节奏"
            progress < 0.6f -> "跟着节拍摇摆"
            progress < 0.9f -> "节奏+旋律配合"
            else -> "律动大师！"
        }
        LessonCategory.TONE -> when {
            progress < 0.3f -> "哼鸣找共鸣"
            progress < 0.6f -> "元音开口练习"
            progress < 0.9f -> "混合共鸣训练"
            else -> "音色塑造！"
        }
        LessonCategory.TECHNIQUE -> when {
            progress < 0.3f -> "基础技巧热身"
            progress < 0.6f -> "核心技巧练习"
            progress < 0.9f -> "综合技巧运用"
            else -> "技巧达人！"
        }
        LessonCategory.SONG_PRACTICE -> when {
            progress < 0.3f -> "熟悉旋律和歌词"
            progress < 0.6f -> "分段跟唱练习"
            progress < 0.9f -> "完整演唱"
            else -> "舞台就是你的！"
        }
        LessonCategory.COOL_DOWN -> when {
            progress < 0.3f -> "轻声哼唱放松"
            progress < 0.6f -> "声带放松操"
            progress < 0.9f -> "深呼吸冥想"
            else -> "放松完成～"
        }
    }
}

private fun getPracticeInstruction(lesson: Lesson, elapsed: Int, target: Int): String {
    val progress = elapsed.toFloat() / target
    return when (lesson.category) {
        LessonCategory.BREATHING -> when {
            progress < 0.3f -> "用鼻子慢慢吸气4秒，感受腹部慢慢鼓起来，然后用嘴慢慢呼气6秒。重复这个过程～"
            progress < 0.6f -> "现在试着加长呼气时间到8秒。保持肩膀放松，只用腹部的力量控制气息。"
            progress < 0.9f -> "吸气后发一个稳定的「啊」音，保持气息均匀输出。感受声音和呼吸的连接！"
            else -> "最后一组！深吸慢呼，你做得越来越好了！"
        }
        LessonCategory.WARM_UP -> when {
            progress < 0.3f -> "用「嗯～」轻轻哼唱，从舒服的音高开始，上下滑动。不要用力，享受声音的振动～"
            progress < 0.6f -> "大大地张开嘴巴说「啊」，然后嘟嘴说「呜」，交替练习。让你的嘴巴灵活起来！"
            progress < 0.9f -> "跟着 Do Re Mi Fa Sol La Si Do 唱一遍，然后倒着唱回来。保持每个音清晰准确。"
            else -> "再来最后一遍音阶，这次加大音量！你的声音已经完全打开了！"
        }
        else -> when {
            progress < 0.3f -> "按照课程要求开始基础练习。不用着急，找到自己的节奏最重要～"
            progress < 0.6f -> "很好！现在进入核心练习环节。专注于每一个细节，你在不断进步！"
            progress < 0.9f -> "马上就要完成了！把学到的都用上，给自己一个完美的表现！"
            else -> "最后冲刺！你已经超越了自己，太了不起了！"
        }
    }
}

private fun getCompletionMessage(completionRate: Int): String {
    return when {
        completionRate >= 100 -> "你完成了全部练习目标！坚持就是胜利，你是最棒的！"
        completionRate >= 80 -> "完成度很高！再多练一点就能达到满分啦！"
        completionRate >= 50 -> "完成了一半以上，继续保持这个势头！"
        completionRate >= 30 -> "好的开始是成功的一半！下次试着多练一会儿～"
        else -> "每一次练习都是进步！明天继续加油！"
    }
}

private fun getTeacherMessage(lesson: Lesson, completionRate: Int): String {
    return when {
        completionRate >= 100 -> "太让老师骄傲了！「${lesson.title}」你已经掌握得很好了。记得每天都来练练，熟能生巧！继续保持这份热情吧！🌟"
        completionRate >= 70 -> "做得非常好！你在「${lesson.title}」上的表现让人印象深刻。再多练几次就能完全掌握了！加油！💪"
        completionRate >= 40 -> "不错的开始！「${lesson.title}」需要时间来消化，慢慢来不着急。每次练习都会让你变得更好！🌱"
        else -> "勇敢迈出第一步就是最大的进步！「${lesson.title}」我们下次继续，老师相信你一定能做到！❤️"
    }
}

private val encouragements = listOf(
    "🔥 你好棒！继续加油！",
    "⭐ 声音越来越好听了！",
    "💪 坚持就是胜利！",
    "🎵 享受音乐的感觉真好！",
    "🌟 你正在变得更强！",
    "🎤 未来的歌星就是你！",
    "✨ 每一秒都在进步！",
    "🏆 你今天超越了昨天的自己！",
    "🎶 音乐因你而美好！",
    "💖 老师为你骄傲！"
)

private val pauseEncouragements = listOf(
    "休息也是练习的一部分哦！\n喝口水，放松一下肩膀，\n我们马上继续！💪",
    "你已经做得很好了！\n深呼吸几次，让声带休息一下，\n然后我们继续冲！🌟",
    "短暂的休息能让练习效果更好～\n活动一下脖子和肩膀，\n准备好了就点继续！☕",
    "每个歌手都需要休息！\n就算是专业歌手也会在练习中\n适当停下来调整状态哦～🎤"
)
