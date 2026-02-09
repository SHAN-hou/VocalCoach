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

    // Seed lessons — 30节活泼有趣的课程，循序渐进
    val lessons = listOf(
        // === 第一周：打开声音大门 ===
        Lesson(1, "腹式呼吸基础", "唱歌第一步！把手放在肚子上，感受气息的魔力～学会这招你就赢在起跑线了！", LessonCategory.BREATHING, Difficulty.BEGINNER, 10, thumbnailEmoji = "🌬️", orderIndex = 1),
        Lesson(2, "嘴巴体操·口腔打开训练", "嘴巴也需要做热身操！夸张地张嘴、咧嘴、嘟嘴，像做鬼脸一样打开你的口腔通道～", LessonCategory.WARM_UP, Difficulty.BEGINNER, 8, thumbnailEmoji = "👄", orderIndex = 2),
        Lesson(3, "音阶热身操", "跟着钢琴爬楼梯！Do Re Mi Fa Sol～用最简单的音阶唤醒你的声音，开启元气满满的练习！", LessonCategory.WARM_UP, Difficulty.BEGINNER, 8, thumbnailEmoji = "☀️", orderIndex = 3),
        Lesson(4, "音准训练·Do Re Mi", "从最基础的七个音开始，训练你的耳朵和嗓子！像玩音乐游戏一样，听到什么唱什么～", LessonCategory.PITCH, Difficulty.BEGINNER, 15, thumbnailEmoji = "🎯", orderIndex = 4),
        Lesson(5, "节奏感训练·身体律动", "忘掉复杂的乐理！跟着节拍拍手、跺脚、摇摆，让你的身体自然跟上音乐的脉搏～", LessonCategory.RHYTHM, Difficulty.BEGINNER, 12, thumbnailEmoji = "🥁", orderIndex = 5),
        Lesson(6, "气息游戏·吹蜡烛与吸纸片", "超有趣的气息训练！想象你在吹生日蜡烛、用吸管吸纸片……在游戏中不知不觉练好气息！", LessonCategory.BREATHING, Difficulty.BEGINNER, 10, thumbnailEmoji = "🎂", orderIndex = 6),
        Lesson(7, "放松与护嗓·基础篇", "唱完歌嗓子干干的？来学学如何给嗓子做SPA！蒸汽吸入、温水润喉、颈部放松一条龙～", LessonCategory.COOL_DOWN, Difficulty.BEGINNER, 8, thumbnailEmoji = "🌙", orderIndex = 7),

        // === 第二周：技能升级站 ===
        Lesson(8, "气息控制·长音练习", "深吸一口气，看看你能把一个音唱多～长！这是气息控制的终极挑战，来破纪录吧！", LessonCategory.BREATHING, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "💨", orderIndex = 8),
        Lesson(9, "音准升级·听音辨音", "耳朵也要做训练！听两个音，哪个高哪个低？像玩猜谜一样提升你的音感灵敏度～", LessonCategory.PITCH, Difficulty.ELEMENTARY, 12, thumbnailEmoji = "👂", orderIndex = 9),
        Lesson(10, "节奏进阶·切分与附点", "节奏不只是"哒哒哒"！学会切分音和附点，你的歌声会突然变得很有"味道"哦～", LessonCategory.RHYTHM, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "🎪", orderIndex = 10),
        Lesson(11, "音色打磨·共鸣训练", "你的身体是一个乐器！找到头腔和胸腔的共鸣点，让声音从"扁"变"圆"，听起来超专业～", LessonCategory.TONE, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "🔔", orderIndex = 11),
        Lesson(12, "咬字吐字训练", "唱歌咬字不清？来练练绕口令式的咬字操！让每个字都清晰有力，观众再也不用猜歌词了～", LessonCategory.TONE, Difficulty.ELEMENTARY, 12, thumbnailEmoji = "💬", orderIndex = 12),
        Lesson(13, "弱声练习·轻声唱歌的秘密", "大声谁都会，轻声才是真功夫！学会控制音量，用气息托住每一个轻柔的音符～", LessonCategory.TECHNIQUE, Difficulty.ELEMENTARY, 15, thumbnailEmoji = "🤫", orderIndex = 13),
        Lesson(14, "放松与护嗓·进阶篇", "学会更专业的声带放松技巧！气泡音、唇颤音，让你的嗓子永远保持最佳状态～", LessonCategory.COOL_DOWN, Difficulty.ELEMENTARY, 10, thumbnailEmoji = "�", orderIndex = 14),

        // === 第三周：解锁流行唱法 ===
        Lesson(15, "流行唱法·气声技巧", "周杰伦、林俊杰都在用的气声唱法！加一点气息感，瞬间让你的声音变得慵懒又迷人～", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 20, thumbnailEmoji = "✨", orderIndex = 15),
        Lesson(16, "颤音入门·自然颤音训练", "那种好听的"颤抖"是怎么来的？不是抖下巴！学会用气息自然产生颤音，歌声瞬间升级～", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 15, thumbnailEmoji = "〰️", orderIndex = 16),
        Lesson(17, "转音与滑音技巧", "让音符之间的过渡像丝绸一样顺滑！学会转音和滑音，你的歌声会变得超有韵味和感觉～", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 20, thumbnailEmoji = "🌊", orderIndex = 17),
        Lesson(18, "音域拓展训练", "唱不上去？唱不下来？别急！用科学的方法一点点拓展你的音域，解锁更多歌曲！", LessonCategory.PITCH, Difficulty.INTERMEDIATE, 20, thumbnailEmoji = "📏", orderIndex = 18),
        Lesson(19, "即兴哼唱·创造你的旋律", "不用乐谱！随心哼一段旋律，你就是作曲家！释放创造力，享受音乐最纯粹的快乐～", LessonCategory.TECHNIQUE, Difficulty.INTERMEDIATE, 15, thumbnailEmoji = "💡", orderIndex = 19),
        Lesson(20, "歌曲实战·《小幸运》", "终于到唱歌环节了！用你学到的所有技巧来演绎这首超经典的歌曲，感受进步的快乐！", LessonCategory.SONG_PRACTICE, Difficulty.INTERMEDIATE, 25, thumbnailEmoji = "�", orderIndex = 20),
        Lesson(21, "歌曲实战·《晴天》", "周杰伦的经典必唱曲！练习咬字、节奏和情感的完美配合，唱出属于你的《晴天》～", LessonCategory.SONG_PRACTICE, Difficulty.INTERMEDIATE, 25, thumbnailEmoji = "�️", orderIndex = 21),

        // === 第四周：高阶进化 ===
        Lesson(22, "假声与真假声转换", "解锁你的"第二嗓子"！掌握假声技巧和真假声无缝切换，音域直接翻倍～", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "⚡", orderIndex = 22),
        Lesson(23, "情感表达训练", "技巧到位了，但总觉得少了点什么？那就是情感！学会用声音讲故事，让听众起鸡皮疙瘩～", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "❤️", orderIndex = 23),
        Lesson(24, "高音突破·安全飙高音", "飙高音不是扯嗓子！学会用混声和头声安全地唱高音，再也不怕副歌的高音部分了！", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "🚀", orderIndex = 24),
        Lesson(25, "R&B律动感训练", "想唱出R&B的味道？关键在于节奏的"推拉"感！学会behind the beat，你就是律动之王～", LessonCategory.RHYTHM, Difficulty.ADVANCED, 20, thumbnailEmoji = "🎧", orderIndex = 25),
        Lesson(26, "歌曲实战·《告白气球》", "甜甜的告白气球来啦！练习气声+颤音+轻柔咬字的组合技，唱出恋爱的甜蜜感～", LessonCategory.SONG_PRACTICE, Difficulty.INTERMEDIATE, 25, thumbnailEmoji = "🎈", orderIndex = 26),
        Lesson(27, "歌曲实战·《平凡之路》", "朴树的经典之作！练习如何用朴实的声音唱出深沉的情感，少即是多的演唱哲学～", LessonCategory.SONG_PRACTICE, Difficulty.INTERMEDIATE, 25, thumbnailEmoji = "🛤️", orderIndex = 27),
        Lesson(28, "和声入门·简单二声部", "一个人也能玩和声？先录一轨主旋律，再叠一轨和声，感受声音叠加的美妙魔法！", LessonCategory.PITCH, Difficulty.ADVANCED, 20, thumbnailEmoji = "🎼", orderIndex = 28),

        // === 毕业周：舞台之星 ===
        Lesson(29, "舞台表现力训练", "唱得好只是及格，表演得好才是满分！学习表情管理、肢体语言，成为舞台上最亮的星！", LessonCategory.TECHNIQUE, Difficulty.ADVANCED, 20, thumbnailEmoji = "🌟", orderIndex = 29),
        Lesson(30, "毕业音乐会·你的第一场演出", "30天的努力到了收获的时刻！选一首你最爱的歌，录下你的"毕业演出"，为自己喝彩！", LessonCategory.SONG_PRACTICE, Difficulty.ADVANCED, 30, thumbnailEmoji = "�", orderIndex = 30)
    )
    database.lessonDao().insertLessons(lessons)

    // Seed daily tasks — 30天完整课程计划，每天3个活泼有趣的任务
    val dailyTasks = listOf(
        // ======== 第一周：打开声音大门 ========

        // Day 1 — 你好，声音！
        DailyTask(1, 1, 1, "🎬 认识你的呼吸", "看完这个视频你会惊讶——原来你一直在用错误的方式呼吸！来认识腹式呼吸吧～", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(2, 1, 1, "🌬️ 跟着做！腹式呼吸", "把手放在肚子上，吸气时肚子鼓起来，呼气时肚子瘪下去。做5分钟，你能感受到区别吗？", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(3, 2, 1, "👄 嘴巴热身体操", "张嘴、咧嘴、嘟嘴！像做鬼脸一样夸张地活动你的嘴巴和下巴，打开口腔通道～", TaskType.SING_ALONG, xpReward = 10),

        // Day 2 — 找到你的声音
        DailyTask(4, 3, 2, "🎹 听听音阶长什么样", "闭上眼睛，听钢琴从低到高弹奏音阶。你能感受到音高的变化吗？", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(5, 3, 2, "☀️ 音阶跟唱热身", "跟着钢琴一起唱 Do Re Mi Fa Sol！别怕唱不准，勇敢唱出来就是最大的进步！", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(6, 1, 2, "💪 呼吸复习挑战", "还记得昨天学的腹式呼吸吗？今天试试更长的气息保持——吸4秒，呼8秒，能做到吗？", TaskType.SING_ALONG, xpReward = 15),

        // Day 3 — Do Re Mi 冒险
        DailyTask(7, 4, 3, "🎯 音准训练开始！", "观看音准训练视频，了解如何用耳朵"瞄准"每一个音！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(8, 4, 3, "🎶 Do Re Mi 大冒险", "跟着钢琴唱出 Do Re Mi Fa Sol La Si Do！像爬楼梯一样一步步上去，再一步步下来～", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(9, 4, 3, "🎤 录音初体验！", "录一段你唱的音阶！别紧张，这只是你的第一次录音，重点是勇敢唱出来！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 4 — 跟上节拍！
        DailyTask(10, 5, 4, "🥁 身体就是乐器", "观看节奏训练视频～原来拍手、跺脚、拍腿都能打节奏！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(11, 5, 4, "👏 拍手打节奏", "跟着音乐拍手！先是简单的"哒-哒-哒-哒"，然后加入"强-弱-强-弱"的变化～", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(12, 4, 4, "📝 音准小测验", "听两个音，哪个高哪个低？完成这个小测验，看看你的耳朵进步了多少！", TaskType.QUIZ, xpReward = 20),

        // Day 5 — 气息游戏日
        DailyTask(13, 6, 5, "🎂 超有趣的气息游戏", "观看视频学习气息小游戏～吹蜡烛、吸纸片、弹唇……原来练气息可以这么好玩！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(14, 6, 5, "🕯️ 吹蜡烛挑战", "想象面前有10根蜡烛，用均匀的气流一根一根吹灭它们！控制住，别一口气全吹了～", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(15, 1, 5, "🎤 气息+音阶综合录音", "深呼吸，然后用稳定的气息唱一遍音阶。录下来让老师听听你的气息是不是更稳了！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 6 — 综合复习日
        DailyTask(16, 3, 6, "🔄 热身全流程", "把这几天学的串起来！先腹式呼吸→嘴巴体操→音阶热身，完成一套完整的热身流程～", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(17, 5, 6, "🎵 节奏+音准挑战", "边拍手打节奏边唱音阶！这可不容易，但做到了你就超厉害！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(18, 4, 6, "📝 第一周知识小测验", "来测试一下你这周学到了多少！腹式呼吸的要点是什么？Do Re Mi你能唱准吗？", TaskType.QUIZ, xpReward = 20),

        // Day 7 — 放松日 + 第一周回顾
        DailyTask(19, 7, 7, "🌙 学会放松你的嗓子", "观看护嗓视频！练了一周了，该给嗓子做个SPA啦～学习蒸汽吸入和颈部放松操", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(20, 7, 7, "💆 跟做放松操", "跟着视频做一遍声带放松操。温柔地哼鸣，然后轻轻地做几组气泡音……好舒服～", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(21, 4, 7, "🎤 第一周毕业录音！", "录一段你唱的 Do Re Mi 音阶！对比第3天的录音，你一定能听出自己的进步！", TaskType.RECORD_SELF, xpReward = 50),

        // ======== 第二周：技能升级站 ========

        // Day 8 — 长音挑战
        DailyTask(22, 8, 8, "💨 长音是什么？", "观看长音练习视频。一个音能唱多长，取决于你的气息控制力！来看看专业歌手是怎么做的～", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(23, 8, 8, "⏱️ 长音计时挑战", "深吸一口气，唱一个"啊～"看看你能撑多少秒！记住你的成绩，每天挑战一下自己！", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(24, 8, 8, "🎤 长音录音PK", "录下你最长的一个音！老师会根据气息稳定度给你打分，不是越长越好，稳才是王道！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 9 — 耳朵训练日
        DailyTask(25, 9, 9, "👂 训练你的金耳朵", "观看听音训练视频～原来耳朵也能"健身"！学会听音辨音是唱准的第一步", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(26, 9, 9, "🎵 听音大挑战", "听两个音，哪个高？差多少？从简单的八度到困难的半音，一关一关闯过去！", TaskType.QUIZ, xpReward = 20),
        DailyTask(27, 9, 9, "🎤 听唱训练", "听一个音，然后唱出来！录音让老师检验你的"音准复制能力"有多强～", TaskType.RECORD_SELF, xpReward = 25),

        // Day 10 — 节奏升级
        DailyTask(28, 10, 10, "🎪 节奏新花样", "观看视频了解切分音和附点节奏。原来流行歌的节奏不是"哒哒哒"那么简单！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(29, 10, 10, "🥁 切分节奏跟打", "跟着示范拍出切分节奏！"哒-空-哒"——感受那个重音偏移的律动感～", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(30, 5, 10, "🎶 节奏+旋律综合", "边打节奏边唱旋律。难度升级了，但别怕！慢慢来，你能搞定！", TaskType.SING_ALONG, xpReward = 20),

        // Day 11 — 共鸣探索
        DailyTask(31, 11, 11, "🔔 你的身体是乐器", "观看共鸣训练视频！了解头腔共鸣和胸腔共鸣的区别，你的身体藏着巨大的音响系统！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(32, 11, 11, "🐝 哼鸣寻宝", "闭上嘴巴用"嗯～"哼鸣，然后把手放在鼻梁和胸口，感受振动！找到你的共鸣宝藏～", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(33, 11, 11, "🎤 共鸣录音对比", "先用平时说话的方式唱一句，再用共鸣的方式唱同一句。录下来听听区别有多大！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 12 — 咬字训练
        DailyTask(34, 12, 12, "💬 唱歌咬字的秘密", "观看咬字训练视频。为什么有的歌手唱歌听不清歌词？因为咬字不到位！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(35, 12, 12, "🗣️ 绕口令大挑战", "四是四，十是十……用唱歌的方式练绕口令，锻炼你的舌头灵活度！", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(36, 12, 12, "🎤 咬字清晰度录音", "唱一句"让我们荡起双桨"，每个字都要咬得清清楚楚。录下来让老师打分！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 13 — 弱声魔法
        DailyTask(37, 13, 13, "🤫 轻声唱歌的秘密", "观看弱声练习视频。大声唱谁都会，轻声唱才见真功夫！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(38, 13, 13, "🎵 蚊子飞·弱声跟练", "用最轻的声音唱音阶，但每个音都要清晰！像蚊子飞一样轻，但要"嗡"得准～", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(39, 8, 13, "🎤 强弱对比录音", "同一句歌词，先大声唱，再轻声唱。录下来感受音量控制的艺术！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 14 — 第二周回顾 + 放松
        DailyTask(40, 14, 14, "💆 进阶放松操", "学习气泡音和唇颤音！这些不仅能放松声带，还能帮你热身和找到正确发声位置", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(41, 14, 14, "🫧 气泡音+唇颤音实练", "先做20秒气泡音（像煮开水的咕噜声），再做20秒唇颤音（嘴唇打嘟噜）～好解压！", TaskType.SING_ALONG, xpReward = 15),
        DailyTask(42, 11, 14, "🎤 第二周毕业录音！", "唱一段你最有感觉的旋律，展示你的共鸣、气息和音准！两周的练习，效果惊人吧！", TaskType.RECORD_SELF, xpReward = 50),

        // ======== 第三周：解锁流行唱法 ========

        // Day 15 — 气声入门
        DailyTask(43, 15, 15, "✨ 什么是气声唱法？", "观看气声技巧视频！周杰伦、林俊杰的招牌唱法，学会了你也能唱出那种味道～", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(44, 15, 15, "🎤 气声跟练·悄悄话唱歌", "像说悄悄话一样唱歌，但要加上一点声带振动。找到那个"气多声少"的感觉！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(45, 15, 15, "🎙️ 气声录音", "用气声唱一句"你好吗我很好"。录下来听听，是不是瞬间有了"歌手范"？", TaskType.RECORD_SELF, xpReward = 25),

        // Day 16 — 颤音探索
        DailyTask(46, 16, 16, "〰️ 颤音的秘密", "观看颤音教学视频。那种好听的"抖动"到底怎么来的？不是抖下巴哦！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(47, 16, 16, "🎵 颤音从呼吸开始", "先做快速腹式呼吸：哈-哈-哈-哈，逐渐加快，感受气息推动声音的振动感！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(48, 16, 16, "🎤 颤音初尝试", "唱一个长音"啊～"，在结尾试着加上颤音。录下来看看效果！即使不完美也没关系～", TaskType.RECORD_SELF, xpReward = 25),

        // Day 17 — 转音滑音
        DailyTask(49, 17, 17, "🌊 让声音流动起来", "观看转音和滑音教学视频。学会这个技巧，你的歌声会像水一样流畅优美～", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(50, 17, 17, "🐍 滑音练习·声音蛇行", "从低音慢慢滑到高音，再从高音滑回低音。像一条声音的蛇，丝滑前进！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(51, 17, 17, "🎤 转音小片段", "唱一句"如果没有遇见你"，在"你"字上加一个小转音。录下来让老师评价！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 18 — 音域探索
        DailyTask(52, 18, 18, "📏 你的声音有多宽？", "观看音域拓展视频。了解你的音域范围，并学习安全拓展的方法！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(53, 18, 18, "⬆️ 音域探险", "从你最舒服的音开始，一个半音一个半音往上爬，看看能到多高！然后往下探底", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(54, 18, 18, "📝 音域小测验", "关于音域和声区的知识测验。头声、胸声、混声你分得清吗？", TaskType.QUIZ, xpReward = 20),

        // Day 19 — 即兴创作日
        DailyTask(55, 19, 19, "💡 释放你的音乐天性", "观看即兴哼唱视频。不需要乐谱，不需要歌词，跟着感觉哼就好！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(56, 19, 19, "🎵 自由哼唱5分钟", "放一段纯音乐伴奏，随心哼唱任何旋律！没有对错，只有你独特的音乐表达～", TaskType.FREE_PRACTICE, xpReward = 20),
        DailyTask(57, 19, 19, "🎤 录下你的原创旋律", "把刚才最好听的那段哼唱录下来！说不定这就是一首歌的开始呢？", TaskType.RECORD_SELF, xpReward = 25),

        // Day 20 — 第一首歌！
        DailyTask(58, 20, 20, "🍀 《小幸运》初体验", "先完整听一遍《小幸运》原唱，感受歌曲的旋律和情感走向～", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(59, 20, 20, "🎤 第一段跟唱", "跟着伴奏学唱《小幸运》第一段。不用完美，能跟上节奏就很棒了！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(60, 20, 20, "🎙️ 《小幸运》录音", "录下你唱的《小幸运》第一段！这是你的第一首完整歌曲练习，超有成就感！", TaskType.RECORD_SELF, xpReward = 30),

        // Day 21 — 《晴天》挑战
        DailyTask(61, 21, 21, "🌤️ 听《晴天》找感觉", "认真听一遍周杰伦的《晴天》。注意他的咬字、节奏和情感变化～", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(62, 21, 21, "🎤 《晴天》副歌跟唱", "先从最经典的副歌开始！"故事的小黄花～"跟着唱几遍找到感觉", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(63, 21, 21, "🎤 第三周毕业录音！", "选《小幸运》或《晴天》的一段录音！展示你的气声、颤音和转音技巧～", TaskType.RECORD_SELF, xpReward = 50),

        // ======== 第四周：高阶进化 ========

        // Day 22 — 假声世界
        DailyTask(64, 22, 22, "⚡ 解锁假声！", "观看假声教学视频。你的声音还有另一半没被发现！假声能让你唱到想象不到的高度", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(65, 22, 22, "🎵 假声初体验", "用"呜～"发出猫头鹰一样的声音，从高往低滑。恭喜你，这就是假声！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(66, 22, 22, "🎤 真假声切换录音", "唱一个音阶，低音区用真声，高音区切换假声。录下来让老师评价你的切换是否顺畅！", TaskType.RECORD_SELF, xpReward = 25),

        // Day 23 — 情感表达
        DailyTask(67, 23, 23, "❤️ 用声音讲故事", "观看情感表达教学视频。同一句歌词，开心地唱和悲伤地唱完全不同！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(68, 23, 23, "🎭 情感变换练习", "唱"我好想你"——先用开心的语气唱，再用思念的语气唱，再用撕心裂肺地唱。感受差别！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(69, 23, 23, "🎤 走心演唱录音", "选一句你最有感触的歌词，用最真实的情感唱出来。录下来，听听自己是否被打动了？", TaskType.RECORD_SELF, xpReward = 25),

        // Day 24 — 高音突破
        DailyTask(70, 24, 24, "🚀 高音不用扯嗓子", "观看安全飙高音教学视频！学习混声和头声技巧，科学地到达高音！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(71, 24, 24, "🎵 混声阶梯训练", "从中音区开始，用混声一步步往上爬。感受声音从胸声逐渐过渡到头声的过程～", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(72, 24, 24, "🎤 高音挑战录音", "唱一句需要高音的歌词，用今天学的技巧试试！记录你的高音突破时刻！", TaskType.RECORD_SELF, xpReward = 30),

        // Day 25 — R&B律动
        DailyTask(73, 25, 25, "🎧 感受R&B的groove", "听几首经典R&B歌曲片段。注意歌手是怎么"拖拍"和"抢拍"的，这就是律动感！", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(74, 25, 25, "🕺 Behind the beat练习", "试着在节拍稍稍后面一点开始唱，让声音像在节奏上"冲浪"。这就是R&B的味道！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(75, 25, 25, "📝 律动感测验", "关于节奏律动的知识测验。切分、推拉、bounce你都掌握了吗？", TaskType.QUIZ, xpReward = 20),

        // Day 26 — 《告白气球》
        DailyTask(76, 26, 26, "🎈 听《告白气球》", "先完整听一遍！注意周杰伦的气声、轻柔咬字和甜甜的语气～", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(77, 26, 26, "🎤 甜蜜跟唱", "跟着伴奏唱《告白气球》副歌。用气声+轻声+甜甜的感觉来唱！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(78, 26, 26, "🎙️ 《告白气球》录音", "录下你唱的《告白气球》片段！运用气声和颤音让它更有味道～", TaskType.RECORD_SELF, xpReward = 30),

        // Day 27 — 《平凡之路》
        DailyTask(79, 27, 27, "🛤️ 感受《平凡之路》", "听朴树的原唱，体会那种朴实却直击心灵的演唱方式。有时候简单就是最好的", TaskType.LISTEN_AUDIO, xpReward = 10),
        DailyTask(80, 27, 27, "🎤 朴实地唱", "跟唱《平凡之路》。不需要炫技，把情感放进每一个字里，像在讲述自己的故事", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(81, 27, 27, "🎙️ 《平凡之路》录音", "录下你的《平凡之路》。用你这四周学到的所有技巧，但最重要的是——真诚", TaskType.RECORD_SELF, xpReward = 30),

        // Day 28 — 和声初探 + 第四周回顾
        DailyTask(82, 28, 28, "🎼 和声的魔法", "观看和声入门视频。两个声音叠在一起为什么这么好听？来揭开和声的秘密！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(83, 28, 28, "🎵 简单三度和声", "唱一个音，然后唱它上方的三度音。感受两个音叠在一起的美妙共鸣感！", TaskType.SING_ALONG, xpReward = 20),
        DailyTask(84, 28, 28, "🎤 第四周毕业录音！", "选一首你这周练过的歌，完整录一遍！展示假声、高音、情感表达等进阶技巧～", TaskType.RECORD_SELF, xpReward = 50),

        // ======== 毕业周：舞台之星 ========

        // Day 29 — 舞台表现力
        DailyTask(85, 29, 29, "🌟 舞台上的你", "观看舞台表现力教学。表情管理、肢体语言、与观众互动……唱得好只是基础！", TaskType.WATCH_VIDEO, xpReward = 10),
        DailyTask(86, 29, 29, "🎭 镜子前排练", "站在镜子前唱一首你最拿手的歌。注意你的表情、眼神和手势，像真正的演出一样！", TaskType.FREE_PRACTICE, xpReward = 20),
        DailyTask(87, 29, 29, "📝 30天知识总测验", "涵盖呼吸、音准、节奏、技巧、情感表达的综合测验。看看你的声乐知识涨了多少！", TaskType.QUIZ, xpReward = 30),

        // Day 30 — 🎓 毕业日！！！
        DailyTask(88, 30, 30, "🎪 选择你的毕业曲目", "在你练过的所有歌曲中选一首最爱的！花点时间好好练习，准备你的"毕业演出"！", TaskType.FREE_PRACTICE, xpReward = 20),
        DailyTask(89, 30, 30, "🎤 彩排录音", "完整唱一遍你的毕业曲目。这是彩排，可以多录几次找到最佳状态！", TaskType.SING_ALONG, xpReward = 25),
        DailyTask(90, 30, 30, "🎓🎉 毕业演出·终极录音！", "这是你30天声乐之旅的最终成果！全情投入地唱出你最好的表现，为自己骄傲吧！", TaskType.RECORD_SELF, xpReward = 100)
    )
    database.dailyTaskDao().insertTasks(dailyTasks)

    // Seed achievements — 20个成就，覆盖各阶段里程碑
    val achievements = listOf(
        Achievement(1, "初次开嗓", "完成你的第一个练习任务", "🎤", requiredXp = 0, category = AchievementCategory.GENERAL),
        Achievement(2, "连续三天", "连续3天完成练习", "🔥", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(3, "一周坚持", "连续7天完成练习——第一周毕业啦！", "⭐", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(4, "首个高分", "获得90分以上的评分", "🏆", requiredXp = 0, category = AchievementCategory.SCORE),
        Achievement(5, "经验达人", "累计获得500XP", "💎", requiredXp = 500, category = AchievementCategory.GENERAL),
        Achievement(6, "练习狂人", "累计练习时长超过60分钟", "⏰", requiredXp = 0, category = AchievementCategory.PRACTICE),
        Achievement(7, "升级先锋", "达到5级", "🌟", requiredXp = 0, category = AchievementCategory.LEVEL),
        Achievement(8, "满分歌手", "获得一次满分100分！", "👑", requiredXp = 0, category = AchievementCategory.SCORE),
        Achievement(9, "两周战士", "连续14天完成练习——半程达人！", "💪", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(10, "声乐达人", "达到10级", "🎭", requiredXp = 0, category = AchievementCategory.LEVEL),
        Achievement(11, "三周勇士", "连续21天完成练习——即将毕业！", "🦁", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(12, "经验大师", "累计获得1500XP", "💰", requiredXp = 1500, category = AchievementCategory.GENERAL),
        Achievement(13, "千分歌王", "累计获得3000XP——你就是传说！", "🏰", requiredXp = 3000, category = AchievementCategory.GENERAL),
        Achievement(14, "录音达人", "完成20次录音提交", "🎙️", requiredXp = 0, category = AchievementCategory.PRACTICE),
        Achievement(15, "高分连连", "连续3次获得85分以上", "🔥", requiredXp = 0, category = AchievementCategory.SCORE),
        Achievement(16, "完美毕业", "连续30天完成练习——毕业啦！", "🎓", requiredXp = 0, category = AchievementCategory.STREAK),
        Achievement(17, "练习马拉松", "累计练习时长超过300分钟", "🏃", requiredXp = 0, category = AchievementCategory.PRACTICE),
        Achievement(18, "音乐学徒", "达到7级", "🎵", requiredXp = 0, category = AchievementCategory.LEVEL),
        Achievement(19, "舞台新星", "达到15级", "⭐", requiredXp = 0, category = AchievementCategory.LEVEL),
        Achievement(20, "传奇歌王", "达到20级——你已经是传奇！", "👑", requiredXp = 0, category = AchievementCategory.LEVEL)
    )
    database.achievementDao().insertAchievements(achievements)
}
