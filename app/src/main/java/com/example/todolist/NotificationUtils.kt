package com.example.todolist

import android.annotation.SuppressLint
import android.app.ActivityManager.TaskDescription
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.todolist.database.TaskModel

object NotificationUtils {

    private const val CHANNEL_ID = "ToDoAppNotificationChannel"
    private const val CHANNEL_NAME = "To Do Notifications"
    private const val NOTIFICATION_ID = 123
    private const val TAG = "NotificationUtils"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }



    @SuppressLint("ScheduleExactAlarm")
    fun setNotification(context: Context, task: TaskModel) {
        if (task.notificationEnabled == 1 && task.executionTime != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_description", task.description)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationTime = getNotificationTime(context)

            val executionTime = task.executionTime.time
            val notifyTime = executionTime - notificationTime
            if(notifyTime > 0) alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent)
        }
    }

    private fun getNotificationTime(context: Context): Long {
        val prefs = context.getSharedPreferences("com.example.todolist.preferences", Context.MODE_PRIVATE)
        val notificationTime = prefs.getInt("NotificationTime", 1)
        return notificationTime * 60 * 1000L
    }

    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                val taskId = intent?.getIntExtra("task_id", 0) ?: 0
                val taskTitle = intent?.getStringExtra("task_title")
                val taskDescription  = intent?.getStringExtra("task_description")
                showNotification(it, taskId, taskTitle ?: "Task Title", taskDescription ?: "Task Description")
            }
        }

        private fun showNotification(context: Context, taskId: Int, taskTitle: String, taskDescription: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(taskTitle)
                .setContentText(taskDescription)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            notificationManager.notify(taskId, builder.build())
        }
    }
}
