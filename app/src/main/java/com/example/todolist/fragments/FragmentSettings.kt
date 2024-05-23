package com.example.todolist.fragments

import DatabaseHandler
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.MainActivity
import com.example.todolist.ModalBottomSheet
import com.example.todolist.R
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepositoryImpl
import com.example.todolist.viewModels.TasksViewModel
import com.example.todolist.viewModels.TasksViewModelFactory
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FragmentSettings: Fragment() {


    lateinit var mainActivity : MainActivity
    lateinit var recyclerTasks: RecyclerView
    lateinit var adapterRecycler: TasksAdapter
    lateinit var bottomSheetFragment: ModalBottomSheet

    lateinit var databaseHandler: DatabaseHandler
    lateinit var tasksViewModel: TasksViewModel
    lateinit var tasksRepositoryImpl: TasksRepositoryImpl
    private lateinit var spinner: Spinner
    private lateinit var spinnerNotification: Spinner
    private lateinit var toggleCategoryButton: MaterialButtonToggleGroup
    private lateinit var toggleSortButton: MaterialButtonToggleGroup
    private lateinit var saveChangesButton: ExtendedFloatingActionButton
    private lateinit var AllButton: Button
    private lateinit var SportButton: Button
    private lateinit var FamilyButton: Button
    private lateinit var JobButton: Button
    private lateinit var UrgentButton: Button
    private lateinit var NonUrgentButton: Button
    private var isProgrammaticChangeCategory = false
    private var isProgrammaticChangeSort = false
    private var selectedCategory = "All"
    private var selectedSort = "Urgent"



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        databaseHandler = DatabaseHandler(requireContext())
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        val factory = TasksViewModelFactory(tasksRepositoryImpl)
        tasksViewModel = ViewModelProvider(requireActivity(), factory).get(TasksViewModel::class.java)

        spinner =  view.findViewById(R.id.spinner)
        spinnerNotification =  view.findViewById(R.id.spinnerNotification)
        saveChangesButton = view.findViewById(R.id.saveChanges)

        val options = arrayOf("All", "In process", "Finished")
        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner


        val optionsNotifications = arrayOf("1 minute", "5 minutes", "10 minutes", "30 minutes")
        val adapterNotificationsSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, optionsNotifications)
        adapterNotificationsSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNotification.adapter = adapterNotificationsSpinner

        val prefs = requireContext().getSharedPreferences("com.example.todolist.preferences", Context.MODE_PRIVATE)
        if (!prefs.contains("NotificationTime")) {
            prefs.edit().putInt("NotificationTime", 1).apply()
        }

        val savedNotificationTime = prefs.getInt("NotificationTime", 1)
        val spinnerPosition = optionsNotifications.indexOf("$savedNotificationTime minute${if (savedNotificationTime > 1) "s" else ""}")
        if (spinnerPosition >= 0) {
            spinnerNotification.setSelection(spinnerPosition)
        }



        toggleCategoryButton = view.findViewById(R.id.toggleButton)
        toggleSortButton = view.findViewById(R.id.sortToggleButton)
        AllButton = view.findViewById(R.id.All)
        SportButton = view.findViewById(R.id.Sport)
        FamilyButton = view.findViewById(R.id.Family)
        JobButton = view.findViewById(R.id.Job)
        UrgentButton = view.findViewById(R.id.Urgent)
        NonUrgentButton = view.findViewById(R.id.NonUrgent)

        toggleCategoryButton.check(AllButton.id)

        toggleCategoryButton.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeCategory && isSelected) {
                isProgrammaticChangeCategory = true
                val checkedButton = group.findViewById<Button>(selectedId)
                selectedCategory = checkedButton.tag as? String ?: "All"
                toggleCategoryButton.clearChecked()
                toggleCategoryButton.check(selectedId)
                isProgrammaticChangeCategory = false
            }
        }


        toggleSortButton.check(UrgentButton.id)

        toggleSortButton.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChangeSort && isSelected) {
                isProgrammaticChangeSort = true
                val checkedButton = group.findViewById<Button>(selectedId)
                selectedSort = checkedButton.tag as? String ?: "Urgent"
                toggleSortButton.clearChecked()
                toggleSortButton.check(selectedId)
                isProgrammaticChangeSort = false
            }
        }


        saveChangesButton.setOnClickListener {

            val selectedNotificationTime = spinnerNotification.selectedItem.toString()
            val notificationTimeInMinutes = when (selectedNotificationTime) {
                "1 minute" -> 1
                "5 minutes" -> 5
                "10 minutes" -> 10
                "30 minutes" -> 30
                else -> 1
            }
            prefs.edit().putInt("NotificationTime", notificationTimeInMinutes).apply()

            val selectedStatus = spinner.selectedItem.toString()
            filterTasks(selectedCategory, selectedStatus,selectedSort)
            bottomSheetFragment.dismiss()

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        recyclerTasks = mainActivity.recyclerTasks
        adapterRecycler = mainActivity.adapterRecycler
    }

    private fun filterTasks(selectedCategory: String, selectedStatus: String,selectedSort: String) {
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


}