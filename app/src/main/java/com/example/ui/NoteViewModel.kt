package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileSyncHelper
import com.example.data.Note
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    val repository: NoteRepository = NoteRepository(NoteDatabase.getDatabase(application).noteDao)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val syncStatus = MutableStateFlow<String?>(null)

    val categories: StateFlow<List<String>> = repository.categories
        .map { list ->
            val distinct = list.filter { it.isNotBlank() }.distinct()
            listOf("All") + distinct
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All")
        )

    val notes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedCategory
    ) { allNotes, query, category ->
        allNotes.filter { note ->
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.category.contains(query, ignoreCase = true)

            val matchesCategory = category.isNullOrBlank() || category == "All" ||
                    note.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteNotes: StateFlow<List<Note>> = repository.favoriteNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun saveNote(note: Note, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertNote(note)
            onComplete()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(isFavorite = !note.isFavorite)
            repository.insertNote(updated)
        }
    }

    fun backupNotes() {
        viewModelScope.launch {
            val currentNotes = notes.value
            val file = FileSyncHelper.backupNotes(getApplication(), currentNotes)
            if (file != null && file.exists()) {
                syncStatus.value = "Backup successful in sync directory!"
            } else {
                syncStatus.value = "Backup failed!"
            }
        }
    }

    fun restoreNotes() {
        viewModelScope.launch {
            val restored = FileSyncHelper.restoreNotesFromBackup(getApplication())
            if (restored.isNotEmpty()) {
                restored.forEach { note ->
                    repository.insertNote(note)
                }
                syncStatus.value = "Restored ${restored.size} notes successfully!"
            } else {
                syncStatus.value = "No backup found or backup is empty."
            }
        }
    }

    fun clearStatus() {
        syncStatus.value = null
    }
}
