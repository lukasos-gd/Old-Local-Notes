package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object FileSyncHelper {
    private const val TAG = "FileSyncHelper"
    private const val BACKUP_FILE_NAME = "notes_backup.json"

    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun getSyncDirectory(context: Context): File {
        val mediaDirs = context.externalMediaDirs
        val baseDir = if (!mediaDirs.isNullOrEmpty() && mediaDirs[0] != null) {
            mediaDirs[0]
        } else {
            context.filesDir
        }
        val syncDir = File(baseDir, "NotesBackup")
        if (!syncDir.exists()) {
            syncDir.mkdirs()
        }
        return syncDir
    }

    fun getBackupFile(context: Context): File {
        return File(getSyncDirectory(context), BACKUP_FILE_NAME)
    }

    fun getBackupFileLastModified(context: Context): Long {
        val file = getBackupFile(context)
        return if (file.exists()) file.lastModified() else 0L
    }

    fun backupNotes(context: Context, notes: List<Note>): File? {
        return try {
            val file = getBackupFile(context)
            val jsonString = jsonConfig.encodeToString(notes)
            file.writeText(jsonString, Charsets.UTF_8)
            Log.d(TAG, "Successfully backed up ${notes.size} notes to ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to backup notes: ${e.message}", e)
            null
        }
    }

    fun restoreNotesFromBackup(context: Context): List<Note> {
        val file = getBackupFile(context)
        if (!file.exists() || file.length() == 0L) {
            Log.w(TAG, "Backup file does not exist or is empty")
            return emptyList()
        }
        return try {
            val jsonString = file.readText(Charsets.UTF_8)
            val notes = jsonConfig.decodeFromString<List<Note>>(jsonString)
            Log.d(TAG, "Successfully restored ${notes.size} notes from ${file.absolutePath}")
            notes
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse backup notes: ${e.message}", e)
            emptyList()
        }
    }
}
