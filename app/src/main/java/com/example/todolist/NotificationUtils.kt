package com.example.todolist

import android.annotation.SuppressLint
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

    private const val CHANNEL_ID = "TaskNotificationChannel"
    private const val CHANNEL_NAME = "Task Notifications"
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
                putExtra("task_title", task.title)
                putExtra("task_id", task.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val executionTime = task.executionTime.time
            val notifyTime = executionTime - 1 * 60 * 1000 // 1 minute before execution time

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyTime, pendingIntent)
        }
    }


        fun cancelNotification(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun getNotificationTime(context: Context): Long {
        val prefs = context.getSharedPreferences("com.example.todolist.preferences", Context.MODE_PRIVATE)
        val notificationTime = prefs.getInt("NotificationTime", 1)
        // Convert notification time to milliseconds as needed
        return notificationTime * 60 * 1000L  // NotificationTime is in minutes, convert to milliseconds
    }

    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                val taskTitle = intent?.getStringExtra("task_title")
                showNotification(it, taskTitle ?: "Task Notification")
            }
        }

        private fun showNotification(context: Context, taskTitle: String) {
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
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }
}
