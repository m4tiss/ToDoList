package com.example.todolist.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.todolist.NotificationUtils
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepository
import java.io.File

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {
    private val _tasksData = MutableLiveData<List<TaskModel>>()
    val tasksData: MutableLiveData<List<TaskModel>>
        get() = _tasksData


    fun fetchTasksFromDatabase() {
        val tasksFromDatabase = repository.getAllTasks()
        _tasksData.value = tasksFromDatabase
    }

    fun addTask(task: TaskModel):TaskModel {
        val newRowId = repository.insertTask(task)
        val insertedTask = task.copy(id = newRowId)
        val currentTasks = _tasksData.value ?: emptyList()
        _tasksData.value = currentTasks + insertedTask
        return insertedTask
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

    fun updateNotification(taskId: Int, newNotification: Int) {
        repository.updateNotificationTask(taskId, newNotification)
        val currentTasks = _tasksData.value ?: return
        val updatedTasks = currentTasks.map {
            if (it.id == taskId) {
                it.copy(notificationEnabled = newNotification)
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

    fun updateTask(task: TaskModel) {
        repository.updateTask(task)
        val currentTasks = _tasksData.value ?: return
        val updatedTasks = currentTasks.map {
            if (it.id == task.id) {
                task
            } else {
                it
            }
        }
        _tasksData.value = updatedTasks
    }



}