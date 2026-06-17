package com.littleapp.todonote.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.littleapp.todonote.R
import com.littleapp.todonote.data.SortOrder
import com.littleapp.todonote.data.Task
import com.littleapp.todonote.databinding.FragmentTasksBinding
import com.littleapp.todonote.util.exhaustive
import com.littleapp.todonote.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TaskAdapter.OnItemClickListener {

    private val viewModel: TasksViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentTasksBinding.bind(view)
        val taskAdapter = TaskAdapter(this)

        binding.apply {
            tasksRec.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            floatAddButton.setOnClickListener { viewModel.onAddNewTaskClick() }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.bindingAdapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(tasksRec)
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskEvent.collect { event ->
                    when (event) {
                        is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                            Snackbar.make(
                                requireView(),
                                getString(R.string.msg_task_deleted),
                                Snackbar.LENGTH_SHORT
                            )
                                .setAction(getString(R.string.action_undo)) {
                                    viewModel.onUndoDeleteClick(event.task)
                                }.show()
                        }

                        is TasksViewModel.TasksEvent.NavigateToAddScreen -> {
                            val action =
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                    task = null, title = getString(R.string.title_new_task)
                                )
                            findNavController().navigate(action)
                        }

                        is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                            val action =
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                    task = event.task, title = getString(R.string.title_edit_task)
                                )
                            findNavController().navigate(action)
                        }

                        is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                        }

                        is TasksViewModel.TasksEvent.NavigateToDeleteAllCompletedTasksScreen -> {
                            val action =
                                TasksFragmentDirections.actionGlobalDeleteAllCompletedTasksDialogFragment()
                            findNavController().navigate(action)
                        }
                    }.exhaustive
                }
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_tasks, menu)

                val searchItem = menu.findItem(R.id.action_search)
                searchView = searchItem.actionView as SearchView

                searchView.onQueryTextChanged { viewModel.searchQuery.value = it }

                val pendingQuery = viewModel.searchQuery.value
                if (!pendingQuery.isNullOrEmpty()) {
                    searchItem.expandActionView()
                    searchView.setQuery(pendingQuery, false)
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        menu.findItem(R.id.action_hide_cpmpleted_items).isChecked =
                            viewModel.preferencesFlow.first().hideCompleted
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort_byname -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                        true
                    }

                    R.id.action_sort_bydatecreated -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                        true
                    }

                    R.id.action_hide_cpmpleted_items -> {
                        menuItem.isChecked = !menuItem.isChecked
                        viewModel.onHideCompletedClick(menuItem.isChecked)
                        true
                    }

                    R.id.action_delete_all_comp_tasks -> {
                        val action =
                            TasksFragmentDirections.actionGlobalDeleteAllCompletedTasksDialogFragment()
                        findNavController().navigate(action)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onChecBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::searchView.isInitialized) {
            searchView.setOnQueryTextListener(null)
        }
    }
}