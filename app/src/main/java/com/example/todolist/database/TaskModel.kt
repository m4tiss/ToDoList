package com.example.todolist.database

import java.util.*

data class TaskModel(
    val id: Int,
    val title: String,
    val description: String,
    val creationTime: Date,
    val executionTime: Date?,
    val completed: Int,
    val notificationEnabled: Int,
    val category: String,
    val attachments: List<Attachment>
) {
    fun hasAttachments(): Boolean {
        return attachments.isNotEmpty()
    }
}

data class Attachment(
    val type: AttachmentType,
    val path: String
)

enum class AttachmentType {
    IMAGE,
    FILE
}
