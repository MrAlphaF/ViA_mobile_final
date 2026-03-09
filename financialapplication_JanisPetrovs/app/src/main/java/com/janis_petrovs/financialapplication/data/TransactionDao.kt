package com.janis_petrovs.financialapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startOfMonth AND :endOfMonth")
    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>>

    // --- JAUNĀS FUNKCIJAS TAVAM PLĀNAM ---

    // 1. Widgetam: Saskaita kopējos tēriņus mēnesī
    @Query("SELECT SUM(amount) FROM transactions WHERE date BETWEEN :start AND :end")
    fun getTotalSpentForMonth(start: Long, end: Long): Flow<Double?>

    // 2. Kartei: Atrod visus darījumus, kuriem ir koordinātas
    @Query("SELECT * FROM transactions WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    fun getTransactionsWithLocation(): Flow<List<Transaction>>

    // 3. Iestatījumiem/Limitam: Mēs varam izmantot atsevišķu tabulu profilam
    // (Ja izveidosi UserProfile entītiju, pievienosim šeit arī to)
}