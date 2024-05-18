package com.example.todolist

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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.util.Calendar
import java.util.Date
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide


class FragmentAddTask: Fragment() {


    private lateinit var executionDateButton : Button
    private lateinit var addAttachments : ImageView
    private lateinit var addTaskButton : Button
    private lateinit var executionDate : Date
    private lateinit var selectedAttachmentsLayout: LinearLayout
    private val selectedAttachments = mutableListOf<Uri>()

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

        executionDateButton = view.findViewById(R.id.executionDateButton)
        addAttachments = view.findViewById(R.id.addAttachments)
        addTaskButton = view.findViewById(R.id.addTaskButton)
        selectedAttachmentsLayout = view.findViewById(R.id.selectedAttachmentsLayout)

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

                    executionDateButton.text = executionDate.toString()
                    Log.d("DateTime", "Selected date and time: $executionDate")
                }
                timePicker.show(childFragmentManager, "executionTime")
            }
        }

        addAttachments.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("image/*"))
        }

        addTaskButton.setOnClickListener {
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

    private fun removeImageView(imageView: ImageView, uri: Uri) {
        selectedAttachmentsLayout.removeView(imageView)
        selectedAttachments.remove(uri)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.addTask?.visibility = View.VISIBLE
    }
}