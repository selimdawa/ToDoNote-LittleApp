package com.littleapp.todonote.ui.tasks.edit

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.littleapp.todonote.R
import com.littleapp.todonote.databinding.FragmentAddEditTaskBinding
import com.littleapp.todonote.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()
    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            taskEditText.setText(viewModel.taskName)
            editTaskCheck.isChecked = viewModel.taskImportance
            editTaskCheck.jumpDrawablesToCurrentState()
            dateCreatedTextView.isVisible = viewModel.task != null
            dateCreatedTextView.text =  getString(R.string.created, viewModel.task?.createdDateFormatted)

            taskEditText.addTextChangedListener { viewModel.taskName = it.toString() }
            editTaskCheck.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }
            floatingActionButtonEditTask.setOnClickListener { viewModel.onSaveClick() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }

                        is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                            binding.taskEditText.clearFocus()
                            val resultBundle = Bundle().apply {
                                putInt("add_edit_result", event.result)
                            }
                            setFragmentResult("add_edit_request", resultBundle)
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}