package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FragmentTaskDetails(private val task: TaskModel, private var tasksViewModel: TasksViewModel) : Fragment() {

    private lateinit var taskTitle: TextView
    private lateinit var taskDescription: TextView
    private lateinit var executionTime: TextView
    private lateinit var creationTime: TextView
    private lateinit var taskCategory: TextView
    private lateinit var completedImageView: ImageView
    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var taskAttachments: LinearLayout
    private lateinit var textVisibility: TextView
    private lateinit var editTask: FloatingActionButton


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        editTask = view.findViewById(R.id.editTask)
        taskAttachments = view.findViewById(R.id.taskAttachments)
        taskTitle = view.findViewById(R.id.titleTextView)
        taskDescription = view.findViewById(R.id.descriptionTextView)
        executionTime = view.findViewById(R.id.executionTimeTextView)
        creationTime = view.findViewById(R.id.creationTimeTextView)
        completedImageView = view.findViewById(R.id.completedImageView)
        taskCategory = view.findViewById(R.id.categoryTextView)
        notificationSwitch = view.findViewById(R.id.notificationSwitch)
        textVisibility = view.findViewById(R.id.textVisibility)


        taskTitle.text = task.title
        taskDescription.text = task.description
        taskCategory.text = task.category
        creationTime.text = formatDate(task.creationTime)
        executionTime.text = task.executionTime?.let { formatDate(it) } ?: "No execution time"
        if (task.completed == 1) {
            completedImageView.setImageResource(R.drawable.ic_done)
        } else {
            completedImageView.setImageResource(R.drawable.ic_not_done)
        }
        notificationSwitch.isChecked = task.notificationEnabled == 1

        editTask.setOnClickListener {
            val dialog = EditTaskDialogFragment(task,tasksViewModel)
            dialog.show(childFragmentManager, "EditTaskDialogFragment")
        }

        displayAttachments()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        completedSwitch.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked != (task.completed == 1)) {
//                val newStatus = if (isChecked) 1 else 0
//                tasksViewModel.updateStatus(task.id, newStatus)
//            }
//        }

    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        return sdf.format(date)
    }
    private fun displayAttachments() {
        val attachments = task.attachments

        if (attachments.get(0) != "") {
            textVisibility.visibility = View.VISIBLE
            for (attachment in attachments) {
                val imageView = ImageView(context)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                imageView.layoutParams = layoutParams
                imageView.setPadding(0, 2, 0, 2)

                Glide.with(this)
                    .load(Uri.parse(attachment))
                    .into(imageView)

                taskAttachments.addView(imageView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.addTask?.visibility = View.VISIBLE
    }
}
