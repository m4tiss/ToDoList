package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.todolist.MainActivity
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTaskDialogFragment : DialogFragment() {

    private lateinit var task: TaskModel

    private lateinit var mainActivity : MainActivity
    private lateinit var tasksViewModel: TasksViewModel

    private lateinit var taskTitle: TextView
    private lateinit var taskDescription: TextView
    private lateinit var executionTimeButton: Button
    private lateinit var categoryEditTask: MaterialButtonToggleGroup
    private lateinit var taskAttachments: LinearLayout
    private lateinit var addAttachments: ImageView
    private lateinit var catSport: Button
    private lateinit var catFamily: Button
    private lateinit var catJob: Button
    private var executionDate: Date? = null
    private val selectedAttachments = mutableListOf<Uri>()
    private var isProgrammaticChangeCategory = false
    private var selectedCategory = ""

    private val photosDirectory: File by lazy {
        File(requireContext().getExternalFilesDir(null), "photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            addImageView(it)
        }
    }

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

        fun newInstance(task: TaskModel): EditTaskDialogFragment {
            val fragment = EditTaskDialogFragment()
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
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_task, null)

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


        taskTitle = view.findViewById(R.id.titleEditTask)
        taskDescription = view.findViewById(R.id.descriptionEditTask)
        executionTimeButton = view.findViewById(R.id.executionDateButton)
        categoryEditTask = view.findViewById(R.id.categoryEditTask)
        catSport = view.findViewById(R.id.catSport)
        catFamily = view.findViewById(R.id.catFamily)
        catJob = view.findViewById(R.id.catJob)
        addAttachments = view.findViewById(R.id.addAttachments)
        taskAttachments = view.findViewById(R.id.attachmentsLayout)

        taskTitle.text = task.title
        taskDescription.text = task.description
        executionTimeButton.text = formatDate(task.executionTime)
        executionDate = task.executionTime

        mainActivity = activity as MainActivity
        tasksViewModel = mainActivity.tasksViewModel




        when (task.category) {
            "Family" -> categoryEditTask.check(catFamily.id)
            "Job" -> categoryEditTask.check(catJob.id)
            "Sport" -> categoryEditTask.check(catSport.id)
        }

        selectedCategory = task.category

            categoryEditTask.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeCategory && isSelected) {
                isProgrammaticChangeCategory = true
                val checkedButton = group.findViewById<Button>(selectedId)
                selectedCategory = checkedButton.tag.toString()
                categoryEditTask.clearChecked()
                categoryEditTask.check(selectedId)
                isProgrammaticChangeCategory = false
            }
        }

        addAttachments.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("image/*"))
        }

        executionTimeButton.setOnClickListener {
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

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateInMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                    }
                    executionDate = calendar.time

                    executionTimeButton.text = formatDate(executionDate)
                }
                timePicker.show(childFragmentManager, "executionTime")
            }
        }

        task.attachments?.let { attachments ->
            for (uriString in attachments) {
                val uri = Uri.parse(uriString)
                selectedAttachments.add(uri)
                loadImageView(uri)
            }
        }

        builder.setView(view)
            .setTitle("Edit Task")
            .setPositiveButton("Save") { dialog, id ->
                val title = taskTitle.text.toString()
                val description = taskDescription.text.toString()

                val updatedAttachments = selectedAttachments.map { uri ->
                    removeLeadingSemicolon(uri.toString())
                }

                val updatedTask = task.copy(
                    title = title,
                    description = description,
                    executionTime = executionDate.takeIf { it != null },
                    category = selectedCategory,
                    attachments = updatedAttachments
                )
                println(updatedAttachments)

                tasksViewModel.updateTask(updatedTask)
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.popBackStack()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }

        return builder.create()
    }

    private fun saveImageToInternalStorage(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(photosDirectory, "${System.currentTimeMillis()}.jpg")
        try {
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            inputStream?.close()
        }

        val filePath = Uri.fromFile(file)
        selectedAttachments.add(filePath)

        return file
    }
    private fun addImageView(uri: Uri) {
        val file = saveImageToInternalStorage(uri)
        file?.let {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            imageView.layoutParams = layoutParams

            Glide.with(this)
                .load(uri)
                .into(imageView)

            taskAttachments.addView(imageView)

            imageView.setOnLongClickListener {
                removeImageView(imageView, Uri.fromFile(file), file)
                true
            }
        }
    }

    private fun loadImageView(uri: Uri) {
        val imageView = ImageView(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = layoutParams

        Glide.with(this)
            .load(uri)
            .into(imageView)

        taskAttachments.addView(imageView)

        imageView.setOnLongClickListener {
            removeImageView(imageView, uri,null)
            true
        }
    }

    private fun removeImageView(imageView: ImageView, uri: Uri, file: File?) {
        taskAttachments.removeView(imageView)

        val iterator = selectedAttachments.iterator()
        while (iterator.hasNext()) {
            val selectedUri = iterator.next()
            if (selectedUri.equalsByString(uri)) {
                iterator.remove()
                break
            }
        }

        file?.delete()
    }

    private fun formatDate(date: Date?): String {
        return date?.let {
            val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            sdf.format(it)
        } ?: "Set execution time"
    }

    private fun removeLeadingSemicolon(uriString: String): String {
        return if (uriString.startsWith(";")) {
            uriString.substring(1)
        } else {
            uriString
        }
    }

    private fun Uri.equalsByString(other: Uri?): Boolean {
        if (this == other) return true
        return this.toString() == other.toString()
    }

}
