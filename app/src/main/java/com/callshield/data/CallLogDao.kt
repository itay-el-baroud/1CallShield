package com.callshield.data

import androidx.room.*

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    suspend fun getAll(): List<CallLog>

    @Query("SELECT * FROM call_logs WHERE phoneNumber = :number ORDER BY timestamp DESC")
    suspend fun getByNumber(number: String): List<CallLog>

    @Query("SELECT * FROM call_logs WHERE isRead = 0 ORDER BY timestamp DESC")
    suspend fun getUnread(): List<CallLog>

    @Query("SELECT COUNT(*) FROM call_logs WHERE phoneNumber = :number AND timestamp > :since")
    suspend fun getAttemptsSince(number: String, since: Long): Int

    @Insert
    suspend fun insert(log: CallLog)

    @Query("UPDATE call_logs SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM call_logs WHERE timestamp < :before")
    suspend fun deleteOld(before: Long)
}
