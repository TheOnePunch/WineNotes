package com.example.winenotes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityNoteBinding
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity(), View.OnClickListener{
    private lateinit var binding: ActivityNoteBinding

    private var purpose : String? = ""
    private var noteId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.deleteButton.setOnClickListener(this)

        val intent = getIntent()
        purpose = intent.getStringExtra(
            getString(R.string.intent_purpose_key)
        )

        if(purpose.equals(getString(R.string.intent_purpose_update_note))) {

            noteId = intent.getLongExtra(getString(R.string.intent_key_note_id),-1)

            CoroutineScope(Dispatchers.IO).launch {
                val note = AppDatabase.getDatabase(applicationContext).noteDao().getNote(noteId)

                withContext(Dispatchers.Main) {
                    binding.noteTitle.setText(note.title)
                    binding.noteText.setText(note.notes)
                }
            }
        }

        setTitle("${purpose} Note")
    }

    override fun onClick(v: View?) {
        deleteNote()
    }

    private fun deleteNote() {
        //if(AppDatabase.getDatabase(applicationContext).noteDao().getNote(noteId) != )
        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()
            val note = AppDatabase.getDatabase(applicationContext).noteDao().getNote(noteId)

            noteDao.deleteNote(note)
        }
    }

    override fun onBackPressed() {
        var noteTitle = binding.noteTitle.getText().toString().trim()
        if(noteTitle.isEmpty()) {
            Toast.makeText(applicationContext,
            "Title cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        var noteText = binding.noteText.getText().toString().trim()
        if(noteText.isEmpty()) {
            noteText = ""
        }



        CoroutineScope(Dispatchers.IO).launch {
            val noteDao = AppDatabase.getDatabase(applicationContext).noteDao()


            if(purpose.equals(getString(R.string.intent_purpose_add_note))) {
                //add

                val note = Note(0,noteTitle, noteText, timeForScreen(getTime()))
                noteId = noteDao.addNote(note)
               // Log.i("Status_NAME", "inserted new note: ${noteTitle}")

            } else {
                //update
                val note = Note(noteId, noteTitle, noteText, timeForScreen(getTime()))
                noteDao.updateNote(note)
            }

            val intent = Intent()

            intent.putExtra(
                getString(R.string.intent_key_note_id),
                noteId
            )

            withContext(Dispatchers.Main) {
                setResult(RESULT_OK,intent)
                super.onBackPressed()
            }
        }
    }

    private fun timeForScreen(dateString: String) : String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        parser.setTimeZone(TimeZone.getTimeZone("UTC"))

        val dateInDatabase : Date = parser.parse(dateString)

        val displayFormat = SimpleDateFormat("HH:mm a MM/yyyy")

        val displayString : String = displayFormat.format(dateInDatabase)

        return displayString
    }

    private fun getTime(): String {
        var currentTime : String
        val now : Date = Date()
        val databaseDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        databaseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        currentTime = databaseDateFormat.format(now)
        return currentTime

    }
}