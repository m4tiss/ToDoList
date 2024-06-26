package com.example.todolist.database

import DatabaseHandler

class TasksRepositoryImpl(private val databaseHandler: DatabaseHandler) : TasksRepository {
    override fun getAllTasks(): List<TaskModel> {
        return databaseHandler.getAllTasks().filterNotNull()
    }

    override fun insertTask(task: TaskModel): Int {
        return databaseHandler.addTask(task)
    }

    override fun deleteTask(task: TaskModel) {
        databaseHandler.deleteTask(task.id)
    }
    override fun updateTask(task: TaskModel) {
        databaseHandler.updateTask(task)
    }
    override fun updateStatusTask(taskId:Int,newStatus:Int){
        databaseHandler.updateStatus(taskId, newStatus)
    }

    override fun updateNotificationTask(taskId:Int,newNotification:Int){
        databaseHandler.updateNotification(taskId, newNotification)
    }
}
