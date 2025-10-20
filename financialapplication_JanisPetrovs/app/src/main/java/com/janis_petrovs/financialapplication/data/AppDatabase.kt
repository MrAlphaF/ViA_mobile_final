package com.janis_petrovs.financialapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.janis_petrovs.financialapplication.data.Task
import com.janis_petrovs.financialapplication.data.TaskDao

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}