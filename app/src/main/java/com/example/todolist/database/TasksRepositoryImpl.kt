package com.example.todolist.database

import DatabaseHandler

class TasksRepositoryImpl(private val databaseHandler: DatabaseHandler) : TasksRepository {
    override fun getAllTasks(): List<TaskModel> {
        return databaseHandler.getAllTasks().filterNotNull()
    }

    override fun insertTask(task: TaskModel) {
        databaseHandler.addTask(task)
    }

    override fun deleteTask(task: TaskModel) {
        databaseHandler.deleteTask(task.id)
    }

    override fun updateStatusTask(taskId:Int,newStatus:Int){
        databaseHandler.updateStatus(taskId, newStatus)
    }
}
