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

@Composable
fun ScoreScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onBackToHome: () -> Unit
) {
    val scoreResult by viewModel.lastScoreResult.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val record = scoreResult ?: return

    var animatedScore by remember { mutableIntStateOf(0) }
    var showDetails by remember { mutableStateOf(false) }

    // Animate score counting up
    LaunchedEffect(record.score) {
        animatedScore = 0
        for (i in 0..record.score) {
            animatedScore = i
            delay(20)
        }
        delay(300)
        showDetails = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Score display
            Text(
                text = getScoreEmoji(record.score),
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated score number
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                fontWeight = FontWeight.Bold,
                color = when {
                    record.score >= 90 -> StarGold
                    record.score >= 80 -> PracticeGreen
                    record.score >= 70 -> SecondaryLight
                    record.score >= 60 -> TertiaryLight
                    else -> PrimaryLight
                }
            )

            Text(
                text = "分",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getScoreComment(record.score),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Detailed scores with animation
            AnimatedVisibility(
                visible = showDetails,
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    // Score breakdown
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BubbleLavender.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "📊 评分详情",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            ScoreBar(
                                label = "🎯 音准",
                                value = record.pitchAccuracy,
                                color = RewardBlue
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ScoreBar(
                                label = "🥁 节奏",
                                value = record.rhythmAccuracy,
                                color = PracticeGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ScoreBar(
                                label = "🔔 音色",
                                value = record.toneQuality,
                                color = LevelUpPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BubbleMint.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "🎓 老师点评",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = record.feedback,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 26.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.List, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("继续任务", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onBackToHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("回首页", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Celebration overlay
        if (showCelebration) {
            CelebrationOverlay(
                onDismiss = { viewModel.dismissCelebration() }
            )
        }
    }
}

@Composable
private fun ScoreBar(
    label: String,
    value: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
        )
    }
}

@Composable
private fun CelebrationOverlay(
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000)
        visible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎉🎊🥳", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "太棒了！",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "你的表现超级出色！",
                    style = MaterialTheme.typography.titleLarge,
                    color = StarGold,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
