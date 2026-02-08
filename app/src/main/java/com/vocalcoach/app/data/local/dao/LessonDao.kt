package com.vocalcoach.app.data.local.dao

import androidx.room.*
import com.vocalcoach.app.data.model.Lesson
import com.vocalcoach.app.data.model.LessonCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY orderIndex ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE category = :category ORDER BY orderIndex ASC")
    fun getLessonsByCategory(category: LessonCategory): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Long): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Delete
    suspend fun deleteLesson(lesson: Lesson)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonCount(): Int
}
