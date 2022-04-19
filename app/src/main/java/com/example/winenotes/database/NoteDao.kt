package com.example.winenotes.database

import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    fun addNote(note : Note) : Long

    @Update
    fun updateNote(note: Note)

    @Delete
    fun deleteNote(note: Note)

    @Query("SELECT * FROM Note ORDER BY title")
    fun getAllNotes(): List<Note>

    @Query("SELECT * FROM Note ORDER BY lastModified DESC")
    fun getAllNotesOrderedByTime(): List<Note>

    @Query("SELECT * FROM Note WHERE id = :noteId")
    fun getNote(noteId : Long) : Note
}