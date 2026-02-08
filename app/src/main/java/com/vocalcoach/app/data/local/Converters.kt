package com.vocalcoach.app.data.local

import androidx.room.TypeConverter
import com.vocalcoach.app.data.model.*

class Converters {
    @TypeConverter
    fun fromLessonCategory(value: LessonCategory): String = value.name

    @TypeConverter
    fun toLessonCategory(value: String): LessonCategory = LessonCategory.valueOf(value)

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String = value.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = Difficulty.valueOf(value)

    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = TaskType.valueOf(value)

    @TypeConverter
    fun fromAchievementCategory(value: AchievementCategory): String = value.name

    @TypeConverter
    fun toAchievementCategory(value: String): AchievementCategory = AchievementCategory.valueOf(value)
}
