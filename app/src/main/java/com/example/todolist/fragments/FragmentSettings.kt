package com.example.todolist.fragments

import android.annotation.SuppressLint
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
import com.example.todolist.R
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TaskModel
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class FragmentSettings(
    private val tasksViewModel: TasksViewModel,
    private val recyclerTasks: RecyclerView,
    private val adapterRecycler: TasksAdapter,
    private val onCloseModal: () -> Unit
): Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var toggleButton: MaterialButtonToggleGroup
    private lateinit var saveChangesButton: ExtendedFloatingActionButton
    private lateinit var AllButton: Button
    private lateinit var SportButton: Button
    private lateinit var FamilyButton: Button
    private lateinit var JobButton: Button
    var isProgrammaticChange = false
    var selectedCategory = "All"



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        spinner =  view.findViewById(R.id.spinner)
        saveChangesButton = view.findViewById(R.id.saveChanges)

        val options = arrayOf("All", "In process", "Finished")

        val adapterSpinner = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner


        toggleButton = view.findViewById(R.id.toggleButton)
        AllButton = view.findViewById(R.id.All)
        SportButton = view.findViewById(R.id.Sport)
        FamilyButton = view.findViewById(R.id.Family)
        JobButton = view.findViewById(R.id.Job)

        toggleButton.check(AllButton.id)

        toggleButton.addOnButtonCheckedListener { group, selectedId, isSelected ->
            if (!isProgrammaticChange && isSelected) {
                isProgrammaticChange = true
                val checkedButton = group.findViewById<Button>(selectedId)
                selectedCategory = checkedButton.tag as? String ?: "All"
                toggleButton.clearChecked()
                toggleButton.check(selectedId)
                isProgrammaticChange = false
            }
        }

        saveChangesButton.setOnClickListener {
            val selectedStatus = spinner.selectedItem.toString()
            filterTasks(selectedCategory, selectedStatus)
            onCloseModal()
        }

        return view
    }


    private fun filterTasks(selectedCategory: String, selectedStatus: String) {
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

        if (!recyclerTasks.isComputingLayout && !recyclerTasks.isAnimating) {
            adapterRecycler.setData(filteredTasks)
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