package com.vocalcoach.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocalcoach.app.data.local.dao.*
import com.vocalcoach.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Lesson::class,
        DailyTask::class,
        UserProgress::class,
        Achievement::class,
        PracticeRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lessonDao(): LessonDao
    abstract fun dailyTaskDao(): DailyTaskDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun achievementDao(): AchievementDao
    abstract fun practiceRecordDao(): PracticeRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocal_coach_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
    }
}

suspend fun populateDatabase(database: AppDatabase) {
    // Initialize user progress
    database.userProgressDao().insertProgress(
        UserProgress(
            id = 1,
            currentDay = 1,
            totalXp = 0,
            currentStreak = 0,
            longestStreak = 0,
            level = 1,
            userName = "歌手小白",
            avatarEmoji = "🎤"
        )
    )

    // Seed lessons
    val lessons = listOf(
        Lesson(1, "腹式呼吸基础", "学习如何使用腹部力量进行呼吸，这是唱歌的基础！", LessonCategory.BREATHING, Difficulty.BEGINNER, 10, thumbnailEmoji = "🌬️", orderIndex = 1),
        Lesson(2, "音阶热身操", "跟着钢琴音阶热身，打开你的声音通道！", LessonCategory.WARM_UP, Difficulty.BEGINNER, 8, thumbnailEmoji = "☀️", orderIndex = 2),
        Lesson(3, "音准训练·Do Re Mi", "从最基础的音阶开始，训练你的耳朵和嗓子！", LessonCategory.PITCH, Difficulty.BEGINNER, 15, thumbnailEmoji = "🎯", orderIndex = 3),
        Lesson(4, "节奏感训练", "拍手打节奏，让你的身体感受音乐律动！", LessonCategory.RHYTHM, Difficulty.BEGINNER, 12, thumbnailEmoji = "🥁", orderIndex = 4),
        Lesson(5, "气息控制·长音练习", "练习长音保持，增强你的气息控制能力！", LessonCategory.BREATHING, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "💨", orderIndex = 5),
        Lesson(6, "音色打磨·共鸣训练", "找到你的头腔共鸣和胸腔共鸣！", LessonCategory.TONE, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "🔔", orderIndex = 6),
        Lesson(7, "流行唱法·气声技巧", "学习流行歌曲中常用的气声唱法！", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 20, thumbnailEmoji = "✨", orderIndex = 7),
        Lesson(8, "经典歌曲练习·《小幸运》", "用学到的技巧来演绎这首经典歌曲！", LessonCategory.SONG_PRACTICE, Difficulty.INTERMEDIATE, 25, thumbnailEmoji = "🎵", orderIndex = 8),
        Lesson(9, "转音与滑音技巧", "让你的演唱更加流畅有感觉！", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 20, thumbnailEmoji = "🌊", orderIndex = 9),
        Lesson(10, "情感表达训练", "学会用声音传达情感，打动听众！", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "❤️", orderIndex = 10),
        Lesson(11, "假声与真假声转换", "掌握假声技巧，拓展你的音域！", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "⚡", orderIndex = 11),
        Lesson(12, "放松与护嗓", "练习结束后的放松操，保护你珍贵的嗓子！", LessonCategory.COOL_DOWN, Difficulty.BEGINNER, 8, thumbnailEmoji = "🌙", orderIndex = 12)
    )
    database.lessonDao().insertLessons(lessons)

    // Seed daily tasks for first 7 days
    val dailyTasks = listOf(
        // Day 1
        DailyTask(1, 1, 1, "认识你的呼吸", "观看腹式呼吸教学视频，了解正确的呼吸方式", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(2, 1, 1, "跟着做！腹式呼吸", "跟着视频练习腹式呼吸5分钟", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(3, 2, 1, "听听音阶长什么样", "听一遍钢琴音阶示范", TaskType.LISTEN_AUDIO, xpReward = 10),

        // Day 2
        DailyTask(4, 2, 2, "音阶热身", "跟着钢琴弹奏的音阶热身", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(5, 1, 2, "呼吸复习+进阶", "复习昨天的腹式呼吸，今天试试更长的气息保持", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(6, 3, 2, "音准小挑战", "听音辨高低，测试你的音感！", TaskType.QUIZ, xpReward = 20),

        // Day 3
        DailyTask(7, 3, 3, "Do Re Mi 跟唱", "跟着钢琴唱出 Do Re Mi Fa Sol La Si Do！", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(8, 3, 3, "录音挑战！", "录一段你唱的音阶，让老师给你打分！", TaskType.RECORD_SELF, xpReward = 25),
        DailyTask(9, 4, 3, "拍手打节奏", "跟着节奏拍手，感受2/4和4/4拍！", TaskType.SING_ALONG, xpReward = 15),

        // Day 4
        DailyTask(10, 5, 4, "长音挑战", "看看你能把一个音唱多长！", TaskType.RECORD_SELF, xpReward = 25),
        DailyTask(11, 4, 4, "节奏进阶", "尝试更复杂的节奏型！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(12, 2, 4, "热身回顾", "完成一次完整的热身操", TaskType.SING_ALONG, xpReward = 10),

        // Day 5
        DailyTask(13, 6, 5, "共鸣是什么？", "观看共鸣训练教学视频", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(14, 6, 5, "哼鸣练习", "用 \"嗯\" 来找到你的共鸣位置", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(15, 5, 5, "气息+音准综合", "用稳定的气息唱准每个音", TaskType.RECORD_SELF, xpReward = 25),

        // Day 6
        DailyTask(16, 7, 6, "认识气声唱法", "观看气声技巧教学视频", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(17, 7, 6, "气声跟练", "跟着示范练习气声唱法", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(18, 6, 6, "共鸣+气声录音", "录一段运用共鸣和气声的片段", TaskType.RECORD_SELF, xpReward = 30),

        // Day 7
        DailyTask(19, 8, 7, "《小幸运》学唱", "听一遍原版，再跟着伴奏学唱第一段", TaskType.LISTEN_AUDIO, xpReward = 15),
        DailyTask(20, 8, 7, "第一段跟唱", "跟着伴奏唱出《小幸运》第一段", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(21, 8, 7, "🎤 第一周毕业录音！", "录下你唱的《小幸运》第一段，看看这一周你进步了多少！", TaskType.RECORD_SELF, xpReward = 50)
    )
    database.dailyTaskDao().insertTasks(dailyTasks)

    // Seed achievements
    val achievements = listOf(
        Achievement(1, "初次开嗓", "完成你的第一个练习任务", "🎤", requiredXp = 0, category = AchievementCategory.GENERAL),
        Achievement(2, "连续三天", "连续3天完成练习", "🔥", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(3, "一周坚持", "连续7天完成练习", "⭐", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(4, "首个高分", "获得90分以上的评分", "🏆", requiredXp = 0, category = AchievementCategory.SCORE),
        Achievement(5, "经验达人", "累计获得500XP", "💎", requiredXp = 500, category = AchievementCategory.GENERAL),
        Achievement(6, "练习狂人", "累计练习时长超过60分钟", "⏰", requiredXp = 0, category = AchievementCategory.PRACTICE),
        Achievement(7, "升级啦！", "达到5级", "🌟", requiredXp = 0, category = AchievementCategory.LEVEL),
        Achievement(8, "满分歌手", "获得一次100分！", "👑", requiredXp = 0, category = AchievementCategory.SCORE),
        Achievement(9, "两周战士", "连续14天完成练习", "💪", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(10, "声乐大师", "达到10级", "🎭", requiredXp = 0, category = AchievementCategory.LEVEL)
    )
    database.achievementDao().insertAchievements(achievements)
}
