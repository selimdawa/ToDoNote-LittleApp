package com.littleapp.todonote.ui.tasks

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.littleapp.todonote.R
import com.littleapp.todonote.databinding.DialogCustomDeleteTasksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedTasksDialogFragment : DialogFragment() {

    private val viewModel: TasksViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding: DialogCustomDeleteTasksBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.dialog_custom_delete_tasks,
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
                viewModel.onDeleteAllCompletedConfirmed()
                dismiss()
            }
        }

        return dialog
    }
}