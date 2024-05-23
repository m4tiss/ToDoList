package com.example.todolist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.fragments.FragmentSettings
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet(
                       private val recyclerTasks: RecyclerView,
                       private val adapterRecycler: TasksAdapter) : BottomSheetDialogFragment(R.layout.bottom_sheet) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.peekHeight = 3000
        behavior.isDraggable = true

        val preferencesFragmentContainer = view.findViewById<FragmentContainerView>(R.id.settingsFragment)
        val fragmentSettings = FragmentSettings(recyclerTasks, adapterRecycler) {
            onCloseModal()
        }

        childFragmentManager.beginTransaction()
            .replace(preferencesFragmentContainer.id, fragmentSettings)
            .commit()
    }
    fun onCloseModal() {
        dismiss()
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}