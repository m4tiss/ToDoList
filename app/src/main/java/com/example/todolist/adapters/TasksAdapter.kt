package com.example.todolist.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel

class TasksAdapter(private val context: Context,
                   private var taskList: List<TaskModel>,
                   private val tasksViewModel: TasksViewModel,
                    private val onTaskItemClick: (Int) -> Unit):
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleTextView: TextView = itemView.findViewById(R.id.taskTitle)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val attachmentIcon: ImageView = itemView.findViewById(R.id.attachmentIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = taskList[position]
        holder.taskTitleTextView.text = currentTask.title
        holder.checkBox.isChecked = currentTask.completed == 1

        holder.checkBox.setOnClickListener {
            val taskId = currentTask.id
            val newStatus = if (currentTask.completed == 1) 0 else 1
            tasksViewModel.updateStatus(taskId, newStatus)
        }
        holder.itemView.setOnClickListener {
            val taskId = currentTask.id
            onTaskItemClick(taskId)
        }

        println(currentTask.hasAttachments())
        if (currentTask.hasAttachments()) {
            holder.attachmentIcon.visibility = View.VISIBLE
        } else {
            holder.attachmentIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun deleteTask(position: Int) {
        if (position >= 0 && position < taskList.size) {
            val task = taskList[position]
            tasksViewModel.deleteTask(task.id)
            notifyItemRemoved(position)
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete this task?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteTask(position)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setOnCancelListener {
        }
        val dialog = builder.create()
        dialog.show()
    }


    val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    showDeleteConfirmationDialog(position)
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(newTaskList: List<TaskModel>) {
        taskList = newTaskList
        notifyDataSetChanged()
    }

}
