package com.vocalcoach.app.data.local.dao

import androidx.room.*
import com.vocalcoach.app.data.model.PracticeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeRecordDao {
    @Query("SELECT * FROM practice_records ORDER BY practiceDate DESC")
    fun getAllRecords(): Flow<List<PracticeRecord>>

    @Query("SELECT * FROM practice_records WHERE taskId = :taskId ORDER BY practiceDate DESC")
    fun getRecordsForTask(taskId: Long): Flow<List<PracticeRecord>>

    @Query("SELECT AVG(score) FROM practice_records")
    suspend fun getAverageScore(): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PracticeRecord): Long

    @Query("SELECT * FROM practice_records ORDER BY practiceDate DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<PracticeRecord>>
}
