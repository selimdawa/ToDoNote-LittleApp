package com.littleapp.todonote.ui.notes

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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.littleapp.todonote.R
import com.littleapp.todonote.data.Notes
import com.littleapp.todonote.data.SortOrder
import com.littleapp.todonote.databinding.FragmentNotesBinding
import com.littleapp.todonote.util.exhaustive
import com.littleapp.todonote.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), NotesAdapter.OnItemClickListener {

    private val viewModel: NotesViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)

        val notesAdapter = NotesAdapter(this)

        binding.apply {
            notesRec.apply {
                adapter = notesAdapter
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                setHasFixedSize(true)
            }

            floatAddButton.setOnClickListener { viewModel.onAddNewNoteClick() }

            setFragmentResultListener("note_add_edit_request") { _, bundle ->
                val result = bundle.getInt("note_add_edit_request")
                viewModel.onAddEditNoteResult(result)
            }
        }

        viewModel.notes.observe(viewLifecycleOwner) {
            notesAdapter.differ.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noteEvent.collect { event ->
                    when (event) {
                        is NotesViewModel.NotesEvent.NavigateToAddScreen -> {
                            val action = NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                title = "New Note", Note = null
                            )
                            findNavController().navigate(action)
                        }

                        is NotesViewModel.NotesEvent.NavigateToEditNoteScreen -> {
                            val action = NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                title = "Edit Note", Note = event.note
                            )
                            findNavController().navigate(action)
                        }

                        is NotesViewModel.NotesEvent.ShowUndoDeleteNoteMessage -> {
                            Snackbar.make(requireView(), "Note Deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onUndoDeleteClick(event.note)
                                }.show()
                        }

                        is NotesViewModel.NotesEvent.ShowNoteSavedConfirmationMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                        }

                        is NotesViewModel.NotesEvent.NavigateToDeleteAllScreen -> {
                            val action = NotesFragmentDirections.actionGlobalDeleteAllNotes()
                            findNavController().navigate(action)
                        }
                    }.exhaustive
                }
            }
        }

        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_notes, menu)

                val searchItem = menu.findItem(R.id.action_search_notes)
                val currentSearchView = searchItem.actionView as SearchView
                searchView = currentSearchView

                currentSearchView.onQueryTextChanged { viewModel.searchQuery.value = it }

                val pendingQuery = viewModel.searchQuery.value
                if (!pendingQuery.isNullOrEmpty()) {
                    searchItem.expandActionView()
                    currentSearchView.setQuery(pendingQuery, false)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_all_notes -> {
                        viewModel.deleteAllNotes()
                        true
                    }

                    R.id.action_sort_byname_notes -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                        true
                    }

                    R.id.action_sort_bydatecreated_notes -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onItemClick(note: Notes) {
        viewModel.onNoteSelected(note)
    }

    override fun onDeleteNoteClick(note: Notes) {
        viewModel.deleteNote(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView?.setOnQueryTextListener(null)
        searchView = null
        _binding = null
    }
}