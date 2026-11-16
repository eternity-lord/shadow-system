package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.data.db.InboxEntity
import com.shadowsystem.data.repo.InboxRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Inbox list and quick-add on the Now tab. Deleting asks first. */
class InboxViewModel(private val inbox: InboxRepository) : ViewModel() {
    val items: StateFlow<List<InboxEntity>> = inbox.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingDelete = MutableStateFlow<Long?>(null)
    val pendingDelete: StateFlow<Long?> = _pendingDelete.asStateFlow()

    fun add(text: String) {
        viewModelScope.launch { inbox.add(text, System.currentTimeMillis()) }
    }

    fun askDelete(id: Long) {
        _pendingDelete.value = id
    }

    fun cancelDelete() {
        _pendingDelete.value = null
    }

    fun confirmDelete() {
        val id = _pendingDelete.value ?: return
        _pendingDelete.value = null
        viewModelScope.launch { inbox.delete(id) }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                InboxViewModel(app.container.inbox) as T
        }
    }
}
