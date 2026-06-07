package com.littleapp.todonote.ui.notes.delete

import androidx.lifecycle.ViewModel
import com.littleapp.todonote.data.NoteDao
import com.littleapp.todonote.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllNotesViewModel @Inject constructor(
    private val dao: NoteDao,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    fun confirmClick() = applicationScope.launch {
        dao.deleteAllNotes()
    }
}