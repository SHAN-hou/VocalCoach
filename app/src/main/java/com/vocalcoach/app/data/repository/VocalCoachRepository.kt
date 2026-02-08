package com.vocalcoach.app.data.repository

import com.vocalcoach.app.data.local.AppDatabase
import com.vocalcoach.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

class VocalCoachRepository(private val database: AppDatabase) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Lessons
    fun getAllLessons(): Flow<List<Lesson>> = database.lessonDao().getAllLessons()
    fun getLessonsByCategory(category: LessonCategory): Flow<List<Lesson>> =
        database.lessonDao().getLessonsByCategory(category)
    suspend fun getLessonById(id: Long): Lesson? = database.lessonDao().getLessonById(id)

    // Daily Tasks
    fun getTasksForDay(dayNumber: Int): Flow<List<DailyTask>> =
        database.dailyTaskDao().getTasksForDay(dayNumber)
    suspend fun getTaskById(id: Long): DailyTask? = database.dailyTaskDao().getTaskById(id)
    fun getCompletedTasks(): Flow<List<DailyTask>> = database.dailyTaskDao().getCompletedTasks()

    suspend fun completeTask(taskId: Long): PracticeRecord {
        val task = database.dailyTaskDao().getTaskById(taskId)
            ?: throw IllegalArgumentException("Task not found")

        // Simulate AI scoring
        val scoreResult = simulateScoring(task)
        val today = LocalDate.now().format(dateFormatter)

        // Update task
        database.dailyTaskDao().completeTask(
            taskId = taskId,
            score = scoreResult.score,
            feedback = scoreResult.feedback,
            date = today
        )

        // Record practice
        val record = PracticeRecord(
            taskId = taskId,
            score = scoreResult.score,
            pitchAccuracy = scoreResult.pitchAccuracy,
            rhythmAccuracy = scoreResult.rhythmAccuracy,
            toneQuality = scoreResult.toneQuality,
            feedback = scoreResult.feedback,
            practiceDate = today,
            durationSeconds = task.let {
                when (it.taskType) {
                    TaskType.WATCH_VIDEO -> 300
                    TaskType.LISTEN_AUDIO -> 180
                    TaskType.SING_ALONG -> 240
                    TaskType.RECORD_SELF -> 120
                    TaskType.QUIZ -> 60
                    TaskType.FREE_PRACTICE -> 300
                }
            }
        )
        database.practiceRecordDao().insertRecord(record)

        // Update user progress
        val progress = database.userProgressDao().getUserProgressOnce()
        if (progress != null) {
            database.userProgressDao().addXp(task.xpReward)
            database.userProgressDao().addPracticeMinutes(record.durationSeconds / 60)
            database.userProgressDao().updateLastPracticeDate(today)

            // Check level up
            val newTotalXp = progress.totalXp + task.xpReward
            val newLevel = calculateLevel(newTotalXp)
            if (newLevel > progress.level) {
                database.userProgressDao().updateLevel(newLevel)
            }

            // Update streak
            val lastDate = progress.lastPracticeDate
            if (lastDate.isNotEmpty()) {
                val last = LocalDate.parse(lastDate, dateFormatter)
                val todayDate = LocalDate.now()
                if (last == todayDate.minusDays(1) || last == todayDate) {
                    val newStreak = if (last == todayDate.minusDays(1)) progress.currentStreak + 1 else progress.currentStreak
                    database.userProgressDao().updateStreak(newStreak)
                } else {
                    database.userProgressDao().updateStreak(1)
                }
            } else {
                database.userProgressDao().updateStreak(1)
            }

            // Check day completion
            val dayNumber = task.dayNumber
            val completedCount = database.dailyTaskDao().getCompletedCountForDay(dayNumber)
            val totalCount = database.dailyTaskDao().getTotalCountForDay(dayNumber)
            if (completedCount >= totalCount) {
                database.userProgressDao().incrementLessonsCompleted()
                database.userProgressDao().advanceDay()
            }

            // Check achievements
            checkAndUnlockAchievements(progress.copy(totalXp = newTotalXp))
        }

        return record
    }

    private fun simulateScoring(task: DailyTask): ScoreResult {
        val baseScore = when (task.taskType) {
            TaskType.WATCH_VIDEO -> Random.nextInt(90, 101)
            TaskType.LISTEN_AUDIO -> Random.nextInt(85, 101)
            TaskType.SING_ALONG -> Random.nextInt(65, 96)
            TaskType.RECORD_SELF -> Random.nextInt(55, 96)
            TaskType.QUIZ -> Random.nextInt(70, 101)
            TaskType.FREE_PRACTICE -> Random.nextInt(75, 101)
        }

        val pitchAccuracy = (baseScore + Random.nextInt(-5, 6)).coerceIn(0, 100) / 100f
        val rhythmAccuracy = (baseScore + Random.nextInt(-5, 6)).coerceIn(0, 100) / 100f
        val toneQuality = (baseScore + Random.nextInt(-8, 6)).coerceIn(0, 100) / 100f

        val feedback = buildString {
            append(getScoreComment(baseScore))
            append("\n\n")
            when {
                pitchAccuracy >= 0.9f -> append("🎯 音准: 太准了！你的耳朵真好！\n")
                pitchAccuracy >= 0.75f -> append("🎯 音准: 基本到位，个别地方可以再注意一下\n")
                else -> append("🎯 音准: 音准还需要多练习哦，试试用钢琴对照着唱\n")
            }
            when {
                rhythmAccuracy >= 0.9f -> append("🥁 节奏: 节奏感超棒！\n")
                rhythmAccuracy >= 0.75f -> append("🥁 节奏: 节奏整体不错，注意不要抢拍哦\n")
                else -> append("🥁 节奏: 跟着节拍器多练练，节奏会更稳！\n")
            }
            when {
                toneQuality >= 0.9f -> append("🔔 音色: 声音真好听！有歌手范儿！\n")
                toneQuality >= 0.75f -> append("🔔 音色: 音色不错，继续保持放松的状态\n")
                else -> append("🔔 音色: 放松喉咙，不要太用力，自然的声音最好听\n")
            }
            append("\n💡 小贴士: ")
            append(
                listOf(
                    "唱歌前记得喝温水润嗓哦！",
                    "保持微笑的状态唱歌，音色会更明亮！",
                    "想象声音从头顶飞出去，共鸣会更好！",
                    "录下来自己听听，进步会更快！",
                    "每天坚持练习，你一定会越来越棒！",
                    "深呼吸，放松肩膀，享受唱歌的过程！"
                ).random()
            )
        }

        return ScoreResult(baseScore, pitchAccuracy, rhythmAccuracy, toneQuality, feedback)
    }

    private fun calculateLevel(totalXp: Int): Int {
        var level = 1
        var xpNeeded = 100
        var xpAccumulated = 0
        while (xpAccumulated + xpNeeded <= totalXp) {
            xpAccumulated += xpNeeded
            level++
            xpNeeded = level * 100 + (level - 1) * 50
        }
        return level
    }

    private suspend fun checkAndUnlockAchievements(progress: UserProgress) {
        val today = LocalDate.now().format(dateFormatter)

        // First task completed
        if (progress.lessonsCompleted >= 0) {
            database.achievementDao().unlockAchievement(1, today)
        }
        // 3-day streak
        if (progress.currentStreak >= 3) {
            database.achievementDao().unlockAchievement(2, today)
        }
        // 7-day streak
        if (progress.currentStreak >= 7) {
            database.achievementDao().unlockAchievement(3, today)
        }
        // 500 XP
        if (progress.totalXp >= 500) {
            database.achievementDao().unlockAchievement(5, today)
        }
        // 60 minutes
        if (progress.totalPracticeMinutes >= 60) {
            database.achievementDao().unlockAchievement(6, today)
        }
        // Level 5
        if (progress.level >= 5) {
            database.achievementDao().unlockAchievement(7, today)
        }
        // 14-day streak
        if (progress.currentStreak >= 14) {
            database.achievementDao().unlockAchievement(9, today)
        }
        // Level 10
        if (progress.level >= 10) {
            database.achievementDao().unlockAchievement(10, today)
        }
    }

    // User Progress
    fun getUserProgress(): Flow<UserProgress?> = database.userProgressDao().getUserProgress()
    suspend fun getUserProgressOnce(): UserProgress? = database.userProgressDao().getUserProgressOnce()

    // Achievements
    fun getAllAchievements(): Flow<List<Achievement>> = database.achievementDao().getAllAchievements()
    fun getUnlockedAchievements(): Flow<List<Achievement>> = database.achievementDao().getUnlockedAchievements()

    // Practice Records
    fun getRecentRecords(limit: Int = 10): Flow<List<PracticeRecord>> =
        database.practiceRecordDao().getRecentRecords(limit)

    data class ScoreResult(
        val score: Int,
        val pitchAccuracy: Float,
        val rhythmAccuracy: Float,
        val toneQuality: Float,
        val feedback: String
    )
}
