package com.littleapp.todonote.ui.notes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.littleapp.todonote.Activity.ADD_RESULT_OK
import com.littleapp.todonote.Activity.EDIT_RESULT_OK
import com.littleapp.todonote.R
import com.littleapp.todonote.Unit.DATA
import com.littleapp.todonote.data.NoteDao
import com.littleapp.todonote.data.Notes
import com.littleapp.todonote.data.PreferencesManager
import com.littleapp.todonote.data.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val searchQuery = state.getLiveData("noteSearchQuery", DATA.EMPTY)

    private val noteEventChannel = Channel<NotesEvent>()
    val noteEvent = noteEventChannel.receiveAsFlow()

    val preferencesFlow = preferencesManager.notesPreferencesFlow

    private val noteFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        noteDao.getNotes(query, filterPreferences.sortOrder)
    }

    val notes = noteFlow.asLiveData()

    fun onAddNewNoteClick() = viewModelScope.launch {
        noteEventChannel.send(NotesEvent.NavigateToAddScreen)
    }

    fun onAddEditNoteResult(result: Int) {
        when (result) {
            ADD_RESULT_OK -> {
                val message = context.getString(R.string.msg_note_added)
                showNoteSavedConfirmationMessage(message)
            }

            EDIT_RESULT_OK -> {
                val message = context.getString(R.string.msg_note_updated)
                showNoteSavedConfirmationMessage(message)
            }
        }
    }

    fun onNoteSelected(note: Notes) = viewModelScope.launch {
        noteEventChannel.send(NotesEvent.NavigateToEditNoteScreen(note))
    }

    fun deleteNote(note: Notes) = viewModelScope.launch {
        noteDao.deleteNote(note)
        noteEventChannel.send(NotesEvent.ShowUndoDeleteNoteMessage(note))
    }

    fun deleteAllNotes() = viewModelScope.launch {
        noteEventChannel.send(NotesEvent.NavigateToDeleteAllScreen)
    }

    fun onDeleteAllNotesConfirmed() = viewModelScope.launch {
        noteDao.deleteAllNotes()
        val message = context.getString(R.string.msg_all_notes_deleted)
        showNoteSavedConfirmationMessage(message)
    }

    private fun showNoteSavedConfirmationMessage(msg: String) = viewModelScope.launch {
        noteEventChannel.send(NotesEvent.ShowNoteSavedConfirmationMessage(msg))
    }

    fun onUndoDeleteClick(note: Notes) = viewModelScope.launch {
        noteDao.insertNote(note)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrderNotes(sortOrder)
    }

    sealed class NotesEvent {
        data object NavigateToAddScreen : NotesEvent()
        data class NavigateToEditNoteScreen(val note: Notes) : NotesEvent()
        data class ShowUndoDeleteNoteMessage(val note: Notes) : NotesEvent()
        data class ShowNoteSavedConfirmationMessage(val msg: String) : NotesEvent()
        data object NavigateToDeleteAllScreen : NotesEvent()
    }
}