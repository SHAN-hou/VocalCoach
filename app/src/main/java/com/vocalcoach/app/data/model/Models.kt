package com.vocalcoach.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: LessonCategory,
    val difficulty: Difficulty,
    val durationMinutes: Int,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val thumbnailEmoji: String = "🎤",
    val orderIndex: Int = 0
)

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lessonId: Long,
    val dayNumber: Int,
    val title: String,
    val description: String,
    val taskType: TaskType,
    val isCompleted: Boolean = false,
    val score: Int? = null,
    val feedback: String? = null,
    val dateAssigned: String = "",
    val dateCompleted: String? = null,
    val xpReward: Int = 10,
    val streakBonus: Boolean = false
)

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey
    val id: Long = 1,
    val currentDay: Int = 1,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val level: Int = 1,
    val totalPracticeMinutes: Int = 0,
    val lessonsCompleted: Int = 0,
    val averageScore: Float = 0f,
    val lastPracticeDate: String = "",
    val userName: String = "歌手小白",
    val avatarEmoji: String = "🎤"
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val requiredXp: Int = 0,
    val category: AchievementCategory = AchievementCategory.GENERAL
)

@Entity(tableName = "practice_records")
data class PracticeRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val recordingPath: String? = null,
    val score: Int = 0,
    val pitchAccuracy: Float = 0f,
    val rhythmAccuracy: Float = 0f,
    val toneQuality: Float = 0f,
    val feedback: String = "",
    val practiceDate: String = "",
    val durationSeconds: Int = 0
)

enum class LessonCategory {
    BREATHING,      // 呼吸训练
    PITCH,          // 音准训练
    RHYTHM,         // 节奏训练
    TONE,           // 音色训练
    TECHNIQUE,      // 技巧训练
    SONG_PRACTICE,  // 歌曲练习
    WARM_UP,        // 热身
    COOL_DOWN       // 放松
}

enum class Difficulty {
    BEGINNER,       // 入门
    ELEMENTARY,     // 初级
    INTERMEDIATE,   // 中级
    ADVANCED,       // 高级
    EXPERT          // 专家
}

enum class TaskType {
    WATCH_VIDEO,    // 观看视频
    LISTEN_AUDIO,   // 听音频示范
    SING_ALONG,     // 跟唱练习
    RECORD_SELF,    // 录音提交
    QUIZ,           // 小测验
    FREE_PRACTICE   // 自由练习
}

enum class AchievementCategory {
    GENERAL,        // 通用
    STREAK,         // 连续打卡
    SCORE,          // 高分
    PRACTICE,       // 练习时长
    LEVEL           // 等级
}

fun getLevelTitle(level: Int): String = when {
    level <= 3 -> "🐣 声乐萌新"
    level <= 6 -> "🎵 音乐学徒"
    level <= 10 -> "🎤 歌唱达人"
    level <= 15 -> "⭐ 舞台新星"
    level <= 20 -> "🌟 实力歌手"
    level <= 30 -> "💫 声乐大师"
    else -> "👑 传奇歌王"
}

fun getXpForLevel(level: Int): Int = level * 100 + (level - 1) * 50

fun getDifficultyLabel(difficulty: Difficulty): String = when (difficulty) {
    Difficulty.BEGINNER -> "🟢 入门"
    Difficulty.ELEMENTARY -> "🔵 初级"
    Difficulty.INTERMEDIATE -> "🟡 中级"
    Difficulty.ADVANCED -> "🟠 高级"
    Difficulty.EXPERT -> "🔴 专家"
}

fun getTaskTypeLabel(taskType: TaskType): String = when (taskType) {
    TaskType.WATCH_VIDEO -> "📺 观看视频"
    TaskType.LISTEN_AUDIO -> "🎧 听音频"
    TaskType.SING_ALONG -> "🎤 跟唱"
    TaskType.RECORD_SELF -> "🎙️ 录音"
    TaskType.QUIZ -> "📝 测验"
    TaskType.FREE_PRACTICE -> "🎶 自由练习"
}

fun getCategoryLabel(category: LessonCategory): String = when (category) {
    LessonCategory.BREATHING -> "🌬️ 呼吸"
    LessonCategory.PITCH -> "🎯 音准"
    LessonCategory.RHYTHM -> "🥁 节奏"
    LessonCategory.TONE -> "🔔 音色"
    LessonCategory.TECHNIQUE -> "⚡ 技巧"
    LessonCategory.SONG_PRACTICE -> "🎵 歌曲"
    LessonCategory.WARM_UP -> "☀️ 热身"
    LessonCategory.COOL_DOWN -> "🌙 放松"
}

fun getScoreEmoji(score: Int): String = when {
    score >= 95 -> "🏆"
    score >= 90 -> "🌟"
    score >= 80 -> "⭐"
    score >= 70 -> "👍"
    score >= 60 -> "💪"
    else -> "🎯"
}

fun getScoreComment(score: Int): String = when {
    score >= 95 -> "完美！你就是天生的歌手！"
    score >= 90 -> "太棒了！几乎无可挑剔！"
    score >= 80 -> "很不错！继续保持哦～"
    score >= 70 -> "有进步！再练几次会更好！"
    score >= 60 -> "加油！你正在进步的路上！"
    else -> "别灰心！每个歌手都是从零开始的！"
}
