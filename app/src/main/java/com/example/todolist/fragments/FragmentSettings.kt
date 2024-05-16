package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.adapters.TasksAdapter
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

//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                val selectedItem = options[position]
//                filterTasks(selectedItem)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//        }

        toggleButton = view.findViewById(R.id.toggleButton)

        toggleButton.check(R.id.All)

        toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId != R.id.All) {
                    toggleButton.uncheck(R.id.All)
                }
                if (checkedId != R.id.Sport) {
                    toggleButton.uncheck(R.id.Sport)
                }
                if (checkedId != R.id.Family) {
                    toggleButton.uncheck(R.id.Family)
                }
                if (checkedId != R.id.Job) {
                    toggleButton.uncheck(R.id.Job)
                }
            }
        }

        saveChangesButton.setOnClickListener {

            val selectedCategory = when (toggleButton.checkedButtonId) {
                R.id.All -> "All"
                R.id.Sport -> "Sport"
                R.id.Family -> "Family"
                R.id.Job -> "Job"
                else -> "All"
            }

            val selectedStatus = spinner.selectedItem.toString()


            filterTasks(selectedCategory,selectedStatus)
            onCloseModal()
        }
        return view
    }


    private fun filterTasks(selectedCategory: String, selectedStatus: String) {
        val filteredTasks = when (selectedCategory) {
            "All" -> {
                when (selectedStatus) {
                    "All" -> tasksViewModel.tasksData.value ?: listOf()
                    "In Progress" -> tasksViewModel.tasksData.value?.filter { it.completed == 0 } ?: listOf()
                    "Completed" -> tasksViewModel.tasksData.value?.filter { it.completed == 1 } ?: listOf()
                    else -> listOf()
                }
            }
            "Sport" -> {
                when (selectedStatus) {
                    "All" -> tasksViewModel.tasksData.value ?: listOf()
                    "In Progress" -> tasksViewModel.tasksData.value?.filter { it.completed == 0 } ?: listOf()
                    "Completed" -> tasksViewModel.tasksData.value?.filter { it.completed == 1 } ?: listOf()
                    else -> listOf()
                }
            }
            "Family" -> {
                when (selectedStatus) {
                    "All" -> tasksViewModel.tasksData.value ?: listOf()
                    "In Progress" -> tasksViewModel.tasksData.value?.filter { it.completed == 0 } ?: listOf()
                    "Completed" -> tasksViewModel.tasksData.value?.filter { it.completed == 1 } ?: listOf()
                    else -> listOf()
                }
            }
            "Job" -> {
                when (selectedStatus) {
                    "All" -> tasksViewModel.tasksData.value ?: listOf()
                    "In Progress" -> tasksViewModel.tasksData.value?.filter { it.completed == 0 } ?: listOf()
                    "Completed" -> tasksViewModel.tasksData.value?.filter { it.completed == 1 } ?: listOf()
                    else -> listOf()
                }
            }
            else -> listOf()
        }

        if (!recyclerTasks.isComputingLayout && !recyclerTasks.isAnimating) {
            adapterRecycler.setData(filteredTasks)
            adapterRecycler.notifyDataSetChanged()
        }
    }

}