package com.example.todolist

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.peekHeight = 1000
        behavior.isDraggable = true
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}