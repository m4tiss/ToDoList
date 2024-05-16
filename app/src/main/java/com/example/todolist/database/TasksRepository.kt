package com.example.todolist.database


interface TasksRepository {
    fun getAllTasks(): List<TaskModel>
    fun insertTask(task: TaskModel)
    fun deleteTask(task: TaskModel)
    fun updateStatusTask(taskId:Int,newStatus:Int)
}
