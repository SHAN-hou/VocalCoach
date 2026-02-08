# 🎤 声乐教练 (VocalCoach)

一款专为**流行演唱学习者**设计的 Android 原生应用，特别针对 **ADHD 学习者**优化，画风轻松活泼，帮助用户每天坚持声乐练习。

## ✨ 功能特色

### 🎯 每日任务系统
- 每天安排 3 个精心设计的学习和练习任务
- 任务类型丰富：观看视频、听音频示范、跟唱练习、录音提交、小测验
- 循序渐进的课程设计，从呼吸训练到歌曲演绎

### 🎓 AI 老师自动打分
- 练习提交后自动评分
- 从**音准**、**节奏**、**音色**三个维度给出详细评分
- 个性化反馈和改进建议
- 动画化评分展示，增强成就感

### 🔥 激励系统（ADHD 友好）
- **连续打卡**：记录连续练习天数，培养习惯
- **经验值 (XP)**：每完成任务获得 XP 奖励
- **等级系统**：从"声乐萌新"到"传奇歌王"
- **成就墙**：10+ 成就等你解锁
- **即时正反馈**：完成任务立刻看到分数和鼓励

### 🎨 活泼设计
- 色彩丰富的 UI，珊瑚粉 + 薄荷绿 + 阳光黄
- 大量 Emoji 和动画效果
- 简洁的卡片式布局，降低认知负担
- 明确的进度展示，一目了然

## 📱 课程内容

| 课程 | 类别 | 难度 |
|------|------|------|
| 腹式呼吸基础 | 🌬️ 呼吸 | 🟢 入门 |
| 音阶热身操 | ☀️ 热身 | 🟢 入门 |
| 音准训练·Do Re Mi | 🎯 音准 | 🟢 入门 |
| 节奏感训练 | 🥁 节奏 | 🟢 入门 |
| 气息控制·长音练习 | 🌬️ 呼吸 | 🔵 初级 |
| 音色打磨·共鸣训练 | 🔔 音色 | 🔵 初级 |
| 流行唱法·气声技巧 | ⚡ 技巧 | 🟡 中级 |
| 经典歌曲练习·《小幸运》 | 🎵 歌曲 | 🟡 中级 |
| 转音与滑音技巧 | ⚡ 技巧 | 🟡 中级 |
| 情感表达训练 | ⚡ 技巧 | 🟠 高级 |
| 假声与真假声转换 | ⚡ 技巧 | 🟠 高级 |
| 放松与护嗓 | 🌙 放松 | 🟢 入门 |

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material 3
- **架构**: MVVM
- **本地数据库**: Room
- **媒体播放**: Media3 (ExoPlayer)
- **动画**: Compose Animation + Lottie
- **图片加载**: Coil
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)

## 🚀 如何运行

### 前置要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 步骤
1. 克隆仓库
```bash
git clone https://github.com/SHAN-hou/VocalCoach.git
```

2. 用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器

5. 点击 Run ▶️

## 📂 项目结构

```
app/src/main/java/com/vocalcoach/app/
├── VocalCoachApp.kt          # Application 类
├── MainActivity.kt            # 主 Activity
├── data/
│   ├── model/
│   │   └── Models.kt          # 数据模型（Lesson, DailyTask, UserProgress 等）
│   ├── local/
│   │   ├── AppDatabase.kt     # Room 数据库 + 种子数据
│   │   ├── Converters.kt      # Room 类型转换器
│   │   └── dao/               # 数据访问对象
│   │       ├── LessonDao.kt
│   │       ├── DailyTaskDao.kt
│   │       ├── UserProgressDao.kt
│   │       ├── AchievementDao.kt
│   │       └── PracticeRecordDao.kt
│   └── repository/
│       └── VocalCoachRepository.kt  # 数据仓库 + AI 评分模拟
├── ui/
│   ├── VocalCoachMainApp.kt   # 主应用组合函数 + 底部导航
│   ├── theme/
│   │   ├── Color.kt           # 色彩定义
│   │   ├── Theme.kt           # Material 3 主题
│   │   └── Type.kt            # 字体排版
│   ├── navigation/
│   │   └── NavGraph.kt        # 导航路由
│   ├── viewmodel/
│   │   └── MainViewModel.kt   # 主 ViewModel
│   └── screens/
│       ├── HomeScreen.kt      # 首页（问候、进度、快捷操作）
│       ├── TasksScreen.kt     # 每日任务列表
│       ├── PracticeScreen.kt  # 练习界面（计时、引导）
│       ├── ScoreScreen.kt     # 评分结果（动画、详细反馈）
│       ├── LessonsScreen.kt   # 课程浏览
│       ├── ProfileScreen.kt   # 个人中心（统计、等级）
│       └── AchievementsScreen.kt  # 成就墙
```

## 🔮 未来规划

- [ ] 真实音频录制和播放
- [ ] 真实 AI 音准检测（TensorFlow Lite）
- [ ] 更多课程内容（30 天完整课程计划）
- [ ] 社区功能（分享录音、互相点评）
- [ ] 自定义歌曲导入练习
- [ ] 多语言支持
- [ ] 深色模式优化
- [ ] Widget 桌面小组件提醒练习

## 📄 License

MIT License

---

🎵 *让每一天都充满歌声！* 🎵
