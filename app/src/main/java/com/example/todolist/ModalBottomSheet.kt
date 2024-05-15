package com.example.todolist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import com.example.todolist.fragments.FragmentSettings
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet(private val fragmentManager: FragmentManager) : BottomSheetDialogFragment(R.layout.bottom_sheet) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.peekHeight = 1000
        behavior.isDraggable = true

        val preferencesFragmentContainer = view.findViewById<FragmentContainerView>(R.id.settingsFragment)
        val fragmentSettings = FragmentSettings()
        childFragmentManager.beginTransaction()
            .replace(preferencesFragmentContainer.id, fragmentSettings)
            .commit()
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}