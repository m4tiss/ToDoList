package com.example.todolist

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import java.util.Calendar
import java.util.Date


class FragmentAddTask: Fragment() {


    private lateinit var executionDateButton : Button
    private lateinit var executionDate : Date

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



        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.addTask?.visibility = View.VISIBLE
    }
}