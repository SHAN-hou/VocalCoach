package com.vocalcoach.app.data.local.dao

import androidx.room.*
import com.vocalcoach.app.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressOnce(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgress)

    @Update
    suspend fun updateProgress(progress: UserProgress)

    @Query("UPDATE user_progress SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_progress SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END WHERE id = 1")
    suspend fun updateStreak(streak: Int)

    @Query("UPDATE user_progress SET currentDay = currentDay + 1 WHERE id = 1")
    suspend fun advanceDay()

    @Query("UPDATE user_progress SET lessonsCompleted = lessonsCompleted + 1 WHERE id = 1")
    suspend fun incrementLessonsCompleted()

    @Query("UPDATE user_progress SET totalPracticeMinutes = totalPracticeMinutes + :minutes WHERE id = 1")
    suspend fun addPracticeMinutes(minutes: Int)

    @Query("UPDATE user_progress SET level = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE user_progress SET lastPracticeDate = :date WHERE id = 1")
    suspend fun updateLastPracticeDate(date: String)
}
