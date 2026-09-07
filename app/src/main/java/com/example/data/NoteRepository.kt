package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val favoriteNotes: Flow<List<Note>> = noteDao.getFavoriteNotes()
    val categories: Flow<List<String>> = noteDao.getSampleCategories()

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    fun getFavoriteNotes(): Flow<List<Note>> = noteDao.getFavoriteNotes()
    fun getCategories(): Flow<List<String>> = noteDao.getSampleCategories()
    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = noteDao.deleteNoteById(id)
}
