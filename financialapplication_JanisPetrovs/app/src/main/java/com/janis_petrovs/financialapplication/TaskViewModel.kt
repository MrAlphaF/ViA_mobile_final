package com.janis_petrovs.financialapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    val tasks: Flow<List<Task>> = dao.getAllTasks()

    fun addTask(title: String) {
        viewModelScope.launch {
            dao.insertTask(Task(title = title))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }
}