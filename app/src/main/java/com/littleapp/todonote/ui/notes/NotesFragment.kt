package com.littleapp.todonote.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.littleapp.todonote.R
import com.littleapp.todonote.data.Notes
import com.littleapp.todonote.data.SortOrder
import com.littleapp.todonote.databinding.FragmentNotesBinding
import com.littleapp.todonote.util.exhaustive
import com.littleapp.todonote.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), NotesAdapter.OnItemClickListener {

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentNotesBinding.bind(view)
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

        viewModel.notes.observe(viewLifecycleOwner) { notesAdapter.differ.submitList(it) }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.noteEvent.collect { event ->
                when (event) {
                    is NotesViewModel.NotesEvent.NavigateToAddScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                "New Note", null
                            )
                        findNavController().navigate(action)
                    }

                    is NotesViewModel.NotesEvent.NavigateToEditNoteScreen -> {
                        val action =
                            NotesFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                                "Edit Note", event.note
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
                }

            }
        }.exhaustive

        setHasOptionsMenu(true)
    }

    override fun onItemClick(note: Notes) {
        viewModel.onNoteSelected(note)
    }

    override fun onDeleteNoteClick(note: Notes) {
        viewModel.deleteNote(note)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_notes, menu)

        val searchItem = menu.findItem(R.id.action_search_notes)
        searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged { viewModel.searchQuery.value = it }

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

            else -> super.onOptionsItemSelected(item)
        }
    }
}