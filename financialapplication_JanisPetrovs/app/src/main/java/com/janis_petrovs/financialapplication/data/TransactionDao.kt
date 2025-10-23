package com.janis_petrovs.financialapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startOfMonth AND :endOfMonth")
    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>>
}