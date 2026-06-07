package com.littleapp.todonote.ui.todo.tasks.delete

import androidx.lifecycle.ViewModel
import com.littleapp.todonote.data.TaskDao
import com.littleapp.todonote.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val dao: TaskDao, @ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    fun confirmClick() = applicationScope.launch {
        dao.deleteCompletedTasks()
    }
}