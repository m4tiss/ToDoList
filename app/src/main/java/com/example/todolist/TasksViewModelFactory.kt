package com.example.todolist.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.database.TasksRepositoryImpl

class TasksViewModelFactory(private val repository: TasksRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel( repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
