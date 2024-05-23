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

class FragmentTaskDetails : Fragment() {

    private lateinit var task: TaskModel


    private lateinit var mainActivity : MainActivity
    private lateinit var tasksViewModel: TasksViewModel

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



    companion object {
        private const val ARG_TASK_ID = "task_id"
        private const val ARG_TASK_TITLE = "task_title"
        private const val ARG_TASK_DESCRIPTION = "task_description"
        private const val ARG_CREATION_TIME = "creation_time"
        private const val ARG_EXECUTION_TIME = "execution_time"
        private const val ARG_COMPLETED = "completed"
        private const val ARG_NOTIFICATION_ENABLED = "notification_enabled"
        private const val ARG_CATEGORY = "category"
        private const val ARG_ATTACHMENTS = "attachments"

        fun newInstance(task: TaskModel): FragmentTaskDetails {
            val fragment = FragmentTaskDetails()
            val args = Bundle().apply {
                putInt(ARG_TASK_ID, task.id)
                putString(ARG_TASK_TITLE, task.title)
                putString(ARG_TASK_DESCRIPTION, task.description)
                putLong(ARG_CREATION_TIME, task.creationTime.time)
                putLong(ARG_EXECUTION_TIME, task.executionTime?.time ?: -1L)
                putInt(ARG_COMPLETED, task.completed)
                putInt(ARG_NOTIFICATION_ENABLED, task.notificationEnabled)
                putString(ARG_CATEGORY, task.category)
                putStringArrayList(ARG_ATTACHMENTS, ArrayList(task.attachments))
            }
            fragment.arguments = args
            return fragment
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)

        arguments?.let {
            val taskId = it.getInt(ARG_TASK_ID)
            val taskTitle = it.getString(ARG_TASK_TITLE) ?: ""
            val taskDescription = it.getString(ARG_TASK_DESCRIPTION) ?: ""
            val creationTime = Date(it.getLong(ARG_CREATION_TIME))
            val executionTime = it.getLong(ARG_EXECUTION_TIME).takeIf { it != -1L }?.let { Date(it) }
            val completed = it.getInt(ARG_COMPLETED)
            val notificationEnabled = it.getInt(ARG_NOTIFICATION_ENABLED)
            val category = it.getString(ARG_CATEGORY) ?: ""
            val attachments = it.getStringArrayList(ARG_ATTACHMENTS) ?: listOf<String>()

            task = TaskModel(
                id = taskId,
                title = taskTitle,
                description = taskDescription,
                creationTime = creationTime,
                executionTime = executionTime,
                completed = completed,
                notificationEnabled = notificationEnabled,
                category = category,
                attachments = attachments
            )
        }

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

        return view
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
        return sdf.format(date)
    }
    private fun displayAttachments() {
        val attachments = task.attachments

        if (attachments.isNotEmpty() && attachments.get(0) != "") {
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        tasksViewModel = mainActivity.tasksViewModel
        mainActivity.addTask.visibility = View.GONE
        editTask.setOnClickListener {
            val dialog = EditTaskDialogFragment(task,tasksViewModel,::onCloseFragment)
            dialog.show(childFragmentManager, "EditTaskDialogFragment")
        }
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newNotification = if (isChecked) 1 else 0
            if (isChecked != (task.notificationEnabled == 1)) {
                task.notificationEnabled = newNotification
                tasksViewModel.updateNotification(task.id, newNotification)
            }
        }

        displayAttachments()
    }

    private fun onCloseFragment() {
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity.addTask.visibility = View.VISIBLE
    }
}
