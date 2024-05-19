package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.todolist.R
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTaskDialogFragment(
    private val task: TaskModel,
    private val tasksViewModel: TasksViewModel,
    private val onCloseFragment: () -> Unit
) : DialogFragment() {

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

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedAttachments.add(it)
            addImageView(it)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.fragment_edit_task, null)

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

        when (task.category) {
            "Family" -> categoryEditTask.check(catFamily.id)
            "Job" -> categoryEditTask.check(catJob.id)
            "Sport" -> categoryEditTask.check(catSport.id)
        }

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
                addImageView(uri)
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

                tasksViewModel.updateTask(updatedTask)
                onCloseFragment()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }

        return builder.create()
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

        taskAttachments.addView(imageView)

        imageView.setOnLongClickListener {
            removeImageView(imageView, uri)
            true
        }
    }

    private fun formatDate(date: Date?): String {
        return date?.let {
            val sdf = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            sdf.format(it)
        } ?: "Set execution time"
    }

    private fun removeImageView(imageView: ImageView, uri: Uri) {
        taskAttachments.removeView(imageView)
        selectedAttachments.remove(uri)
    }

    private fun removeLeadingSemicolon(uriString: String): String {
        return if (uriString.startsWith(";")) {
            uriString.substring(1)
        } else {
            uriString
        }
    }

}
