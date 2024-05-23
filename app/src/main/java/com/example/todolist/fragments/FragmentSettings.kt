package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.MainActivity
import com.example.todolist.ModalBottomSheet
import com.example.todolist.R
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FragmentSettings: Fragment() {


    private lateinit var mainActivity : MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var adapterRecycler: TasksAdapter
    private lateinit var bottomSheetFragment: ModalBottomSheet

    private lateinit var tasksViewModel: TasksViewModel

    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerNotification: Spinner
    private lateinit var toggleCategoryButton: MaterialButtonToggleGroup
    private lateinit var toggleSortButton: MaterialButtonToggleGroup
    private lateinit var allButton: Button
    private lateinit var sportButton: Button
    private lateinit var familyButton: Button
    private lateinit var jobButton: Button
    private lateinit var urgentButton: Button
    private lateinit var nonUrgentButton: Button
    private var isProgrammaticChangeCategory = false
    private var isProgrammaticChangeSort = false




    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        spinnerStatus =  view.findViewById(R.id.spinner)
        spinnerNotification =  view.findViewById(R.id.spinnerNotification)




        toggleCategoryButton = view.findViewById(R.id.toggleButton)
        toggleSortButton = view.findViewById(R.id.sortToggleButton)
        allButton = view.findViewById(R.id.All)
        sportButton = view.findViewById(R.id.Sport)
        familyButton = view.findViewById(R.id.Family)
        jobButton = view.findViewById(R.id.Job)
        urgentButton = view.findViewById(R.id.Urgent)
        nonUrgentButton = view.findViewById(R.id.NonUrgent)





        return view
    }

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        recyclerTasks = mainActivity.recyclerTasks
        adapterRecycler = mainActivity.adapterRecycler
        bottomSheetFragment = mainActivity.bottomSheetFragment
        tasksViewModel = mainActivity.tasksViewModel
        sharedPreferences = mainActivity.sharedPreferences


        val options = arrayOf("All", "In process", "Finished")
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapterSpinner

        val status = sharedPreferences.getString("Status", "All")
        val statusPosition = options.indexOf(status)
        if (statusPosition >= 0) {
            spinnerStatus.setSelection(statusPosition)
        }

        val optionsNotifications = arrayOf("1 minute", "5 minutes", "10 minutes", "30 minutes")
        val adapterNotificationsSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsNotifications)
        adapterNotificationsSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNotification.adapter = adapterNotificationsSpinner

        val savedNotificationTime = sharedPreferences.getInt("NotificationTime", 1)
        val spinnerPosition = optionsNotifications.indexOf("$savedNotificationTime minute${if (savedNotificationTime > 1) "s" else ""}")
        if (spinnerPosition >= 0) {
            spinnerNotification.setSelection(spinnerPosition)
        }

        val category = sharedPreferences.getString("Category", "All")
        checkButtonWithTag(toggleCategoryButton, category ?: "All")


        toggleCategoryButton.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeCategory && isSelected) {
                isProgrammaticChangeCategory = true

                val checkedButton = group.findViewById<Button>(selectedId)
                sharedPreferences.edit().putString("Category", checkedButton.tag as? String ?: "All").apply()


                toggleCategoryButton.clearChecked()
                toggleCategoryButton.check(selectedId)
                isProgrammaticChangeCategory = false
                filterTasks()
            }
        }

        val sortType = sharedPreferences.getString("SortType", "All")
        checkButtonWithTag(toggleSortButton, sortType ?: "Urgent")

        toggleSortButton.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeSort && isSelected) {
                isProgrammaticChangeSort = true


                val checkedButton = group.findViewById<Button>(selectedId)
                sharedPreferences.edit().putString("SortType", checkedButton.tag as? String ?: "Urgent").apply()


                toggleSortButton.clearChecked()
                toggleSortButton.check(selectedId)
                isProgrammaticChangeSort = false
                filterTasks()
            }
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = parent.getItemAtPosition(position).toString()
                sharedPreferences.edit().putString("Status", selectedStatus).apply()
                filterTasks()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerNotification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedNotificationTime = parent.getItemAtPosition(position).toString()
                val notificationTimeInMinutes = when (selectedNotificationTime) {
                    "1 minute" -> 1
                    "5 minutes" -> 5
                    "10 minutes" -> 10
                    "30 minutes" -> 30
                    else -> 1
                }

                sharedPreferences.edit().putInt("NotificationTime", notificationTimeInMinutes).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


    }

    private fun filterTasks() {

        val selectedCategory = sharedPreferences.getString("Category","All")
        val selectedSort = sharedPreferences.getString("SortType","Urgent")
        val selectedStatus = sharedPreferences.getString("Status","All")?: "All"

        val filteredTasks = tasksViewModel.tasksData.value?.let { allTasks ->
            when (selectedCategory) {
                "All" -> filterTasksByStatus(allTasks, selectedStatus)
                "Sport", "Family", "Job" -> {
                    val categoryTasks = allTasks.filter { it.category == selectedCategory }
                    filterTasksByStatus(categoryTasks, selectedStatus)
                }
                else -> listOf()
            }
        } ?: listOf()

        val sortedTasks = when (selectedSort) {
            "Urgent" -> {
                filteredTasks.sortedWith(compareBy { task ->
                    task.executionTime?.time ?: Long.MAX_VALUE
                })
            }
            "NonUrgent" -> {
                filteredTasks.sortedWith(compareByDescending { task ->
                    task.executionTime?.time ?: Long.MIN_VALUE
                })
            }
            else -> filteredTasks
        }

        if (!recyclerTasks.isComputingLayout && !recyclerTasks.isAnimating) {
            adapterRecycler.setData(sortedTasks)
            adapterRecycler.notifyDataSetChanged()
        }
    }

    private fun filterTasksByStatus(tasks: List<TaskModel>, selectedStatus: String): List<TaskModel> {
        return when (selectedStatus) {
            "All" -> tasks
            "In process" -> tasks.filter { it.completed == 0 }
            "Finished" -> tasks.filter { it.completed == 1 }
            else -> listOf()
        }
    }
    private fun checkButtonWithTag(toggleGroup: MaterialButtonToggleGroup, tag: String) {
        for (i in 0 until toggleGroup.childCount) {
            val button = toggleGroup.getChildAt(i) as Button
            if (button.tag == tag) {
                toggleGroup.check(button.id)
                break
            }
        }
    }


}