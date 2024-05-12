package com.example.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TasksAdapter(private val taskList: List<String>) :
    RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitleTextView: TextView = itemView.findViewById(R.id.taskTitle)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = taskList[position]

        holder.taskTitleTextView.text = currentTask
        holder.checkBox.isChecked = false

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->

        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

//    fun setData(newTaskList: List<String>) {
//        taskList.clear()
//        taskList.addAll(newTaskList)
//        notifyDataSetChanged()
//    }
}
