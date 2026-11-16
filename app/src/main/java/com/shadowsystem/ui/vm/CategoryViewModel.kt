package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.CategoryRules
import com.shadowsystem.data.db.CategoryEntity
import com.shadowsystem.data.repo.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Category editor state: every category (archived included), and the result of the last add or rename. */
class CategoryViewModel(private val repo: CategoryRepository) : ViewModel() {

    val all: StateFlow<List<CategoryEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _notice = MutableStateFlow<CategoryRules.Check?>(null)
    val notice: StateFlow<CategoryRules.Check?> = _notice.asStateFlow()

    fun add(name: String, isIncome: Boolean) {
        viewModelScope.launch { _notice.value = repo.add(name, isIncome) }
    }

    fun rename(from: String, to: String) {
        viewModelScope.launch { _notice.value = repo.rename(from, to) }
    }

    fun setArchived(name: String, archived: Boolean) {
        viewModelScope.launch { repo.setArchived(name, archived) }
    }

    fun clearNotice() {
        _notice.value = null
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                CategoryViewModel(app.container.categories) as T
        }
    }
}
