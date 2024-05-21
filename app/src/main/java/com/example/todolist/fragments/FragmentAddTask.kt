package com.example.todolist

import DatabaseHandler
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.util.Calendar
import java.util.Date
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepositoryImpl
import com.example.todolist.viewModels.TasksViewModel
import com.example.todolist.viewModels.TasksViewModelFactory
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Locale


class FragmentAddTask: Fragment() {



    lateinit var databaseHandler: DatabaseHandler
    lateinit var tasksViewModel: TasksViewModel
    lateinit var tasksRepositoryImpl: TasksRepositoryImpl

    private lateinit var titleNewTask: TextInputEditText
    private lateinit var descriptionNewTask: TextInputEditText
    private lateinit var executionDateButton : Button
    private lateinit var addAttachments : ImageView
    private lateinit var addTaskButton : Button
    private lateinit var addNotificationSwitch: SwitchCompat
    private var executionDate: Date? = null
    private lateinit var selectedAttachmentsLayout: LinearLayout
    private lateinit var categoryNewTask: MaterialButtonToggleGroup
    private lateinit var catSport: Button
    private lateinit var catFamily: Button
    private lateinit var catJob: Button
    private val selectedAttachments = mutableListOf<Uri>()
    private var isProgrammaticChangeCategory = false
    var selectedCategory = "Family"

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedAttachments.add(it)
            addImageView(it)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_task, container, false)

        val linearLayout = view.findViewById<LinearLayout>(R.id.addTaskLayout)
        linearLayout.setOnClickListener {

        }
        titleNewTask = view.findViewById(R.id.titleNewTask)
        descriptionNewTask = view.findViewById(R.id.descriptionNewTask)
        executionDateButton = view.findViewById(R.id.executionDateButton)
        categoryNewTask = view.findViewById(R.id.categoryNewTask)
        catSport = view.findViewById(R.id.catSport)
        catFamily = view.findViewById(R.id.catFamily)
        catJob = view.findViewById(R.id.catJob)
        addNotificationSwitch = view.findViewById(R.id.addNotificationSwitch)
        addAttachments = view.findViewById(R.id.addAttachments)
        addTaskButton = view.findViewById(R.id.addTaskButton)
        selectedAttachmentsLayout = view.findViewById(R.id.selectedAttachmentsLayout)


        databaseHandler = DatabaseHandler(requireContext())
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        val factory = TasksViewModelFactory(tasksRepositoryImpl)
        tasksViewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)


        categoryNewTask.check(catFamily.id)

        categoryNewTask.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeCategory && isSelected) {
                isProgrammaticChangeCategory = true
                val checkedButton = group.findViewById<Button>(selectedId)
                selectedCategory = checkedButton.tag as? String ?: "Family"
                categoryNewTask.clearChecked()
                categoryNewTask.check(selectedId)
                isProgrammaticChangeCategory = false
            }
        }

        executionDateButton.setOnClickListener {

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .build()

            datePicker.show(childFragmentManager, "executionDate")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val timePicker =
                    MaterialTimePicker.Builder()
                        .setTitleText("Select time")
                        .build()
                timePicker.addOnPositiveButtonClickListener {

                    val selectedDateInMillis = selection as Long
                    val selectedHour = timePicker.hour
                    val selectedMinute = timePicker.minute
                    Log.d("TimePicker", "Selected time: $selectedHour:$selectedMinute")

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateInMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                    }
                    executionDate = calendar.time


                    executionDateButton.text = formatDate(executionDate)
                    Log.d("DateTime", "Selected date and time: $executionDate")
                }
                timePicker.show(childFragmentManager, "executionTime")
            }
        }

        addAttachments.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("image/*"))
        }

        addTaskButton.setOnClickListener {

            val title = titleNewTask.text.toString()
            val description = descriptionNewTask.text.toString()
            val notificationEnabled = addNotificationSwitch.isChecked


            val attachmentsList = if (selectedAttachments.isNotEmpty()) {
                selectedAttachments.map { it.toString() }
            } else {
                emptyList()
            }

            println( "Lista:" +attachmentsList)

            val currentTime = Calendar.getInstance().time

             val newTask = TaskModel(
                id = 1,
                title = title,
                description = description,
                creationTime = currentTime,
                executionTime = executionDate.takeIf { it != null },
                completed = 0,
                notificationEnabled = if (notificationEnabled) 1 else 0,
                category = selectedCategory,
                attachments = attachmentsList
            )
            val insertedTask = tasksViewModel.addTask(newTask)
            NotificationUtils.setNotification(requireContext(), insertedTask)


        parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun addImageView(uri: Uri) {
        val imageView = ImageView(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = layoutParams

        Glide.with(this)
            .load(uri)
            .into(imageView)

        selectedAttachmentsLayout.addView(imageView)

        imageView.setOnLongClickListener {
            removeImageView(imageView, uri)
            true
        }
    }

    private fun formatDate(date: Date?): String {
        return date?.let {
            val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            sdf.format(it)
        } ?: "No execution time"
    }
    private fun removeImageView(imageView: ImageView, uri: Uri) {
        selectedAttachmentsLayout.removeView(imageView)
        selectedAttachments.remove(uri)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.addTask?.visibility = View.VISIBLE
    }
}