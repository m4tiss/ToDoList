package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditTaskDialogFragment(private val task: TaskModel, private var tasksViewModel: TasksViewModel) : DialogFragment() {

    private lateinit var taskTitle: TextView
    private lateinit var taskDescription: TextView
    private lateinit var executionTime: TextView
    private lateinit var categoryEditTask: MaterialButtonToggleGroup
    private lateinit var taskAttachments: LinearLayout
    private lateinit var catSport: Button
    private lateinit var catFamily: Button
    private lateinit var catJob: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_task, null)


        taskTitle = view.findViewById(R.id.titleEditTask)
        taskDescription = view.findViewById(R.id.descriptionEditTask)
        executionTime = view.findViewById(R.id.executionDateButton)
        categoryEditTask = view.findViewById(R.id.categoryEditTask)
        catSport = view.findViewById(R.id.catSport)
        catFamily = view.findViewById(R.id.catFamily)
        catJob = view.findViewById(R.id.catJob)
        taskAttachments = view.findViewById(R.id.attachmentsLayout)



        taskTitle.text = task.title
        taskDescription.text = task.description
        executionTime.text = formatDate(task.executionTime)

        when(task.category){
            "Family" -> categoryEditTask.check(catFamily.id)
            "Job" -> categoryEditTask.check(catJob.id)
            "Sport" -> categoryEditTask.check(catSport.id)
        }






        builder.setView(view)
            .setTitle("Edit Task")
            .setPositiveButton("Save") { dialog, id ->
                // Save task
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }

        return builder.create()
    }
    private fun formatDate(date: Date?): String {
        return date?.let {
            val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            sdf.format(it)
        } ?: "Set execution time"
    }
}
