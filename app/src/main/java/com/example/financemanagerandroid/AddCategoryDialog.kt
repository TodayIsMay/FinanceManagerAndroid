package com.example.financemanagerandroid

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment

class AddCategoryDialog: DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.add_category_layout, null)
            builder.setView(view)
                .setPositiveButton(
                    "Ok"
                ) { _, _ ->
                    val act = activity as MainActivity
                    val categoryNameTextView = view.findViewById<TextView>(R.id.category_name_text)
                    act.saveCategoryClicked(categoryNameTextView.text.toString())
                }
                .setNegativeButton(
                    "Cancel"
                ) { _, _ ->
                    dialog?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}