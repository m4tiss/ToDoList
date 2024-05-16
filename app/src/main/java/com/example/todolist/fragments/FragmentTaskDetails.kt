package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.app.ActivityManager.TaskDescription
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepositoryImpl
import com.example.todolist.viewModels.TasksViewModel

class FragmentTaskDetails(private val task: TaskModel, private var tasksViewModel: TasksViewModel) : Fragment() {

    private lateinit var taskTitle: TextView
    private lateinit var taskDescription: TextView
    private lateinit var executionTime: TextView
    private lateinit var creationTime: TextView
    private lateinit var completedImageView: ImageView
    private lateinit var notificationSwitch: SwitchCompat

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)


        taskTitle = view.findViewById(R.id.titleTextView)
        taskDescription = view.findViewById(R.id.descriptionTextView)
        executionTime = view.findViewById(R.id.executionTimeTextView)
        creationTime = view.findViewById(R.id.creationTimeTextView)
        completedImageView = view.findViewById(R.id.completedImageView)
        notificationSwitch = view.findViewById(R.id.notificationSwitch)


        taskTitle.text = task.title
        taskDescription.text = task.description
        creationTime.text = task.creationTime.toString()
        executionTime.text = task.executionTime.toString()
        if (task.completed == 1) {
            completedImageView.setImageResource(R.drawable.ic_done)
        } else {
            completedImageView.setImageResource(R.drawable.ic_not_done)
        }
        notificationSwitch.isChecked = task.notificationEnabled == 1
        println(task.completed)

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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.addTask?.visibility = View.VISIBLE
    }
}
