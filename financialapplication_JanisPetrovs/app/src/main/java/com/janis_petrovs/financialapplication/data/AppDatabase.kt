// Updated content for AppDatabase.kt
package com.janis_petrovs.financialapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1) // <-- Change here
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao // <-- And change here
}