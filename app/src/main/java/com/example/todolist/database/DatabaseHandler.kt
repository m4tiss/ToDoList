import android.annotation.SuppressLint
import android.app.Notification
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.todolist.database.TaskModel
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TaskDatabase"
        private const val TABLE_NAME = "tasks"
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_CREATION_TIME = "creation_time"
        private const val KEY_EXECUTION_TIME = "execution_time"
        private const val KEY_COMPLETED = "completed"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_CATEGORY = "category"
        private const val KEY_ATTACHMENTS = "attachments"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        println("przed baza")

        val createTableQuery = "CREATE TABLE $TABLE_NAME (" +
                "$KEY_ID INTEGER PRIMARY KEY," +
                "$KEY_TITLE TEXT," +
                "$KEY_DESCRIPTION TEXT," +
                "$KEY_CREATION_TIME TEXT," +
                "$KEY_EXECUTION_TIME TEXT," +
                "$KEY_COMPLETED INTEGER," +
                "$KEY_NOTIFICATION_ENABLED INTEGER," +
                "$KEY_CATEGORY TEXT," +
                "$KEY_ATTACHMENTS TEXT" +
                ")"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addTask(task: TaskModel): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TITLE, task.title)
        values.put(KEY_DESCRIPTION, task.description)
        values.put(KEY_CREATION_TIME, formatDate(task.creationTime))
        values.put(KEY_EXECUTION_TIME, task.executionTime?.let { formatDate(it) })
        values.put(KEY_COMPLETED, task.completed)
        values.put(KEY_NOTIFICATION_ENABLED,task.notificationEnabled)
        values.put(KEY_CATEGORY, task.category)
        if(task.attachments.isNotEmpty())values.put(KEY_ATTACHMENTS, serializeAttachments(task.attachments))
        else values.put(KEY_ATTACHMENTS, "")
        val newRowId = db.insert(TABLE_NAME, null, values)
        db.close()
        return newRowId.toInt()
    }

    fun deleteTask(taskId: Int): Int {
        val db = writableDatabase
        val selection = "$KEY_ID = ?"
        val selectionArgs = arrayOf(taskId.toString())
        val deletedRows = db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()
        return deletedRows
    }

    fun updateStatus(taskId: Int, newStatus: Int): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put(KEY_COMPLETED, newStatus)

        println("zmieniam na: " + newStatus)
        val selection = "$KEY_ID = ?"
        val selectionArgs = arrayOf(taskId.toString())

        val updatedRows = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return updatedRows
    }

    fun updateNotification(taskId: Int, newNotification: Int): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put(KEY_NOTIFICATION_ENABLED, newNotification)

        println("zmieniam na: " + newNotification)
        val selection = "$KEY_ID = ?"
        val selectionArgs = arrayOf(taskId.toString())

        val updatedRows = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return updatedRows
    }


    @SuppressLint("Range")
    fun getAllTasks(): MutableList<TaskModel?> {
        val taskList = mutableListOf<TaskModel?>()
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            println("czytamy")
            do {
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                val title = cursor.getString(cursor.getColumnIndex(KEY_TITLE))
                val description = cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION))
                val creationTime = cursor.getString(cursor.getColumnIndex(KEY_CREATION_TIME))
                val executionTime = cursor.getString(cursor.getColumnIndex(KEY_EXECUTION_TIME))?.takeIf { it.isNotBlank() }
                val completed = cursor.getInt(cursor.getColumnIndex(KEY_COMPLETED))
                val notificationEnabled = cursor.getInt(cursor.getColumnIndex(KEY_NOTIFICATION_ENABLED))
                val category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                val attachments = cursor.getString(cursor.getColumnIndex(KEY_ATTACHMENTS))

                val task: TaskModel? = if (title.isNullOrEmpty() || description.isNullOrEmpty() || creationTime.isNullOrEmpty() ||
                    completed < 0 || notificationEnabled < 0 || category.isNullOrEmpty()) {
                    null
                } else {
                    TaskModel(
                        id,
                        title,
                        description,
                        parseDate(creationTime),
                        executionTime?.let { parseDate(it) },
                        completed,
                        notificationEnabled,
                        category,
                        deserializeAttachments(attachments)
                    )
                }

                taskList.add(task)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return taskList
    }

    fun updateTask(task: TaskModel): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, task.title)
            put(KEY_DESCRIPTION, task.description)
            put(KEY_CREATION_TIME, formatDate(task.creationTime))
            put(KEY_EXECUTION_TIME, task.executionTime?.let { formatDate(it) })
            put(KEY_COMPLETED, task.completed)
            put(KEY_NOTIFICATION_ENABLED, task.notificationEnabled)
            put(KEY_CATEGORY, task.category)
            if(task.attachments.isNotEmpty())put(KEY_ATTACHMENTS, serializeAttachments(task.attachments))
            else put(KEY_ATTACHMENTS, "")
        }

        val selection = "$KEY_ID = ?"
        val selectionArgs = arrayOf(task.id.toString())

        val updatedRows = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return updatedRows
    }



    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

    private fun parseDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.parse(dateString) ?: Date()
    }

    private fun serializeAttachments(attachments: List<String>): String {
        return attachments.joinToString(";")
    }

    private fun deserializeAttachments(serialized: String): List<String> {
        return serialized.split(";")
    }
}
