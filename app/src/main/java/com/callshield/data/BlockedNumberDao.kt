package com.callshield.data

import androidx.room.*

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY createdAt DESC")
    suspend fun getAll(): List<BlockedNumber>

    @Query("SELECT * FROM blocked_numbers WHERE phoneNumber = :number LIMIT 1")
    suspend fun getByNumber(number: String): BlockedNumber?

    @Query("SELECT * FROM blocked_numbers WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getByCategory(category: String): List<BlockedNumber>

    @Query("SELECT * FROM blocked_numbers WHERE isTemporary = 1 AND unblockTime < :currentTime")
    suspend fun getExpiredBlocks(currentTime: Long): List<BlockedNumber>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(number: BlockedNumber)

    @Update
    suspend fun update(number: BlockedNumber)

    @Delete
    suspend fun delete(number: BlockedNumber)

    @Query("DELETE FROM blocked_numbers WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE blocked_numbers SET callAttempts = callAttempts + 1, lastAttemptTime = :time WHERE phoneNumber = :number")
    suspend fun incrementAttempts(number: String, time: Long)

    @Query("SELECT COUNT(*) FROM blocked_numbers")
    suspend fun getCount(): Int
}
