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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val selectedTask by viewModel.selectedTask.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastScoreResult by viewModel.lastScoreResult.collectAsState()

    val task = selectedTask ?: return
    val lesson = selectedLesson

    var practiceState by remember { mutableStateOf(PracticeState.INTRO) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Navigate to score when result is ready
    LaunchedEffect(lastScoreResult) {
        if (lastScoreResult != null && practiceState == PracticeState.SUBMITTING) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lesson info card
            if (lesson != null) {
                LessonInfoCard(lesson = lesson, task = task)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Practice content based on state
            AnimatedContent(
                targetState = practiceState,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "practiceState"
            ) { state ->
                when (state) {
                    PracticeState.INTRO -> IntroContent(
                        task = task,
                        onStart = {
                            practiceState = PracticeState.PRACTICING
                            isTimerRunning = true
                        }
                    )
                    PracticeState.PRACTICING -> PracticingContent(
                        task = task,
                        elapsedSeconds = elapsedSeconds,
                        onFinish = {
                            isTimerRunning = false
                            practiceState = PracticeState.REVIEW
                        }
                    )
                    PracticeState.REVIEW -> ReviewContent(
                        task = task,
                        elapsedSeconds = elapsedSeconds,
                        onSubmit = {
                            practiceState = PracticeState.SUBMITTING
                            viewModel.completeTask(task.id)
                        }
                    )
                    PracticeState.SUBMITTING -> SubmittingContent()
                }
            }
        }
    }
}

@Composable
private fun LessonInfoCard(lesson: Lesson, task: DailyTask) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BubblePink.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = lesson.thumbnailEmoji, fontSize = 40.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${getDifficultyLabel(lesson.difficulty)} · ${lesson.durationMinutes}分钟",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = lesson.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StarGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⭐ +${task.xpReward} XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = StarGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SecondaryLight.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = getTaskTypeLabel(task.taskType),
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroContent(
    task: DailyTask,
    onStart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Task description
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = BubbleYellow.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📝 任务说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tips based on task type
                val tips = when (task.taskType) {
                    TaskType.WATCH_VIDEO -> listOf(
                        "🎬 认真观看完整视频",
                        "📝 留意老师的示范要点",
                        "🔄 可以多看几遍加深印象"
                    )
                    TaskType.LISTEN_AUDIO -> listOf(
                        "🎧 建议佩戴耳机",
                        "👂 注意听每个音的变化",
                        "🧠 闭上眼睛会更专注哦"
                    )
                    TaskType.SING_ALONG -> listOf(
                        "🎤 找一个安静的环境",
                        "💧 先喝口水润润嗓子",
                        "😊 放轻松，不要紧张！"
                    )
                    TaskType.RECORD_SELF -> listOf(
                        "🎙️ 手机放在胸前30厘米处",
                        "🤫 确保周围比较安静",
                        "💪 大胆唱出来，不要怕！"
                    )
                    TaskType.QUIZ -> listOf(
                        "🧠 仔细审题不要着急",
                        "💡 相信你的第一直觉",
                        "📖 做错了也没关系，学到就好！"
                    )
                    TaskType.FREE_PRACTICE -> listOf(
                        "🎶 选你喜欢的方式练习",
                        "⏰ 专注练习效果更好",
                        "🌟 享受音乐的过程！"
                    )
                }

                Text(
                    text = "💡 小贴士",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TertiaryLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                tips.forEach { tip ->
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryLight
            )
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "开始练习！",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PracticingContent(
    task: DailyTask,
    elapsedSeconds: Int,
    onFinish: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated practice indicator
        Box(
            modifier = Modifier
                .size((120 * pulseScale).dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryLight.copy(alpha = 0.6f),
                            PrimaryLight.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val emoji = when (task.taskType) {
                TaskType.WATCH_VIDEO -> "📺"
                TaskType.LISTEN_AUDIO -> "🎧"
                TaskType.SING_ALONG -> "🎤"
                TaskType.RECORD_SELF -> "🎙️"
                TaskType.QUIZ -> "📝"
                TaskType.FREE_PRACTICE -> "🎶"
            }
            Text(text = emoji, fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer
        Text(
            text = formatTime(elapsedSeconds),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "正在练习中...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Encouraging messages that change
        val encouragements = listOf(
            "你做得太棒了！继续加油！ 💪",
            "放松心情，享受音乐吧～ 🎶",
            "每一秒的练习都在让你进步！ ⭐",
            "你的声音很好听哦！ 🌟",
            "坚持住！胜利就在前方！ 🏆"
        )
        val currentMessage = remember(elapsedSeconds / 10) {
            encouragements[((elapsedSeconds / 10) % encouragements.size)]
        }
        Text(
            text = currentMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = TertiaryLight,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Finish button
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PracticeGreen
            )
        ) {
            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "完成练习",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReviewContent(
    task: DailyTask,
    elapsedSeconds: Int,
    onSubmit: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "✅", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "练习完成！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "用时: ${formatTime(elapsedSeconds)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = BubbleMint.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎓 准备好让老师打分了吗？",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击提交，AI老师会根据你的表现给出评分和反馈",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "提交给老师打分！",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SubmittingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🎓 老师正在认真打分中...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请稍等一下哦～",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}

private enum class PracticeState {
    INTRO,
    PRACTICING,
    REVIEW,
    SUBMITTING
}
