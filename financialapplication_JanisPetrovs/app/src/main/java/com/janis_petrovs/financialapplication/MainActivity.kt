package com.janis_petrovs.financialapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodoApp(DatabaseProvider.getDatabase(this).taskDao())
            }
        }
    }
}

@Composable
fun TodoApp(dao: TaskDao) {
    val viewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(dao))
    var taskTitle by remember { mutableStateOf("") }
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter task") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        viewModel.addTask(taskTitle)
                        taskTitle = ""
                    }
                }
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(task.title, modifier = Modifier.weight(1f))
                    Button(onClick = { viewModel.deleteTask(task) }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}