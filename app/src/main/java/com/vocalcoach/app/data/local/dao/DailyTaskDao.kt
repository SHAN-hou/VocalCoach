package com.vocalcoach.app.data.local.dao

import androidx.room.*
import com.vocalcoach.app.data.model.DailyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dayNumber = :dayNumber ORDER BY id ASC")
    fun getTasksForDay(dayNumber: Int): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): DailyTask?

    @Query("SELECT * FROM daily_tasks WHERE isCompleted = 0 AND dayNumber = :dayNumber")
    fun getPendingTasksForDay(dayNumber: Int): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isCompleted = 1 ORDER BY dateCompleted DESC")
    fun getCompletedTasks(): Flow<List<DailyTask>>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE dayNumber = :dayNumber AND isCompleted = 1")
    suspend fun getCompletedCountForDay(dayNumber: Int): Int

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE dayNumber = :dayNumber")
    suspend fun getTotalCountForDay(dayNumber: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Query("UPDATE daily_tasks SET isCompleted = 1, score = :score, feedback = :feedback, dateCompleted = :date WHERE id = :taskId")
    suspend fun completeTask(taskId: Long, score: Int, feedback: String, date: String)

    @Query("SELECT COUNT(*) FROM daily_tasks")
    suspend fun getTaskCount(): Int
}
