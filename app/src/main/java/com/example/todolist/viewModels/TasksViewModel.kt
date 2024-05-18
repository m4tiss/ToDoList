package com.example.todolist.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepository

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {
    private val _tasksData = MutableLiveData<List<TaskModel>>()
    val tasksData: MutableLiveData<List<TaskModel>>
        get() = _tasksData


    init {
        fetchTasksFromDatabase()
    }

    private fun fetchTasksFromDatabase() {
        val tasksFromDatabase = repository.getAllTasks()
        _tasksData.value = tasksFromDatabase
    }

    fun addTask(task: TaskModel) {
        repository.insertTask(task)
        val currentTasks = _tasksData.value ?: emptyList()
        _tasksData.value = currentTasks + task
    }

    fun updateStatus(taskId: Int, newStatus: Int) {
        repository.updateStatusTask(taskId, newStatus)
        val currentTasks = _tasksData.value ?: return
        val updatedTasks = currentTasks.map {
            if (it.id == taskId) {
                it.copy(completed = newStatus)
            } else {
                it
            }
        }
        _tasksData.value = updatedTasks
    }

    fun deleteTask(taskId: Int) {
        val taskToDelete = tasksData.value?.firstOrNull { it.id == taskId } ?: return
        repository.deleteTask(taskToDelete)
        val currentTasks = _tasksData.value?.toMutableList() ?: mutableListOf()
        currentTasks.remove(taskToDelete)
        _tasksData.value = currentTasks
    }


}