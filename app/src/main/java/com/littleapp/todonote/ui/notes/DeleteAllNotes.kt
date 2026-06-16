package com.littleapp.todonote.ui.notes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.littleapp.todonote.R
import com.littleapp.todonote.databinding.DialogCustomDeleteNotesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllNotes : DialogFragment() {

    private val viewModel: NotesViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogCustomDeleteNotesBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_custom_delete_notes,
            null,
            false
        )

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()

        binding.apply {
            btnCancel.setOnClickListener {
                dismiss()
            }

            btnDelete.setOnClickListener {
                viewModel.onDeleteAllNotesConfirmed()
                dismiss()
            }
        }

        return dialog
    }
}