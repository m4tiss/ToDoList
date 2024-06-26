package com.example.todolist.database

import java.util.*

data class TaskModel(
    val id: Int,
    val title: String,
    val description: String,
    val creationTime: Date,
    val executionTime: Date?,
    val completed: Int,
    var notificationEnabled: Int,
    val category: String,
    val attachments: List<String>
) {
    fun hasAttachments(): Boolean {
        return attachments.isNotEmpty() && attachments.get(0) != ""
    }
}
