package com.example.winenotes

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.winenotes.database.AppDatabase
import com.example.winenotes.database.Note
import com.example.winenotes.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: MyAdapter
    private val notes = mutableListOf<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerview.setLayoutManager(layoutManager)

        val dividerItemDecoration = DividerItemDecoration(
            applicationContext, layoutManager.getOrientation()
        )
        binding.recyclerview.addItemDecoration(dividerItemDecoration)

        adapter = MyAdapter()
        binding.recyclerview.setAdapter(adapter)

        loadAllNotes(1)

    }

    private fun loadAllNotes(option: Int){
        CoroutineScope(Dispatchers.IO).launch {


            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.noteDao()
            var results : List<Note>
            if(option == 1) {
                results = dao.getAllNotes()
            } else {
                results = dao.getAllNotesOrderedByTime()
            }
            withContext(Dispatchers.Main){
                notes.clear()
                notes.addAll(results)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.getItemId() == R.id.menu_item_add){
            addNewNote()
            return true
        }
        if(item.getItemId() == R.id.menu_item_sort_modified){
            loadAllNotes(2)
            return true
        }
        if(item.getItemId() == R.id.menu_item_sort_title){
            loadAllNotes(1)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val startForAddResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result : ActivityResult ->

            if(result.resultCode == Activity.RESULT_OK) {
                loadAllNotes(1)
            }
        }

    private val startForUpdateResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result : ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                loadAllNotes(1)
            }
        }

    private fun addNewNote() {
        val intent = Intent(applicationContext, NoteActivity::class.java)
        intent.putExtra(
            getString(R.string.intent_purpose_key),
            getString(R.string.intent_purpose_add_note))

        startForAddResult.launch(intent)
    }


    inner class MyViewHolder(val view: TextView) :
            RecyclerView.ViewHolder(view),
            View.OnClickListener, View.OnLongClickListener {

                init {
                    view.setOnClickListener(this)
                    view.setOnLongClickListener(this)
                }

        override fun onClick(v: View?) {
            val intent = Intent(applicationContext, NoteActivity::class.java)

            intent.putExtra(
                getString(R.string.intent_purpose_key),
                getString(R.string.intent_purpose_update_note)
            )

            val note = notes[adapterPosition]
            intent.putExtra(
                getString(R.string.intent_key_note_id),
                note.id
            )

            startForUpdateResult.launch(intent)


        }
        override fun onLongClick(view: View?):Boolean {
            Log.i("STATUS", "Clicked item")
            val note = notes[adapterPosition]
            val message = "Title: ${note.title}\n" +
                    "Note: ${note.notes}\n" +
                    "Last Modified: ${note.lastModified}"

            val builder = AlertDialog.Builder(view!!.context)
                .setTitle("Note Info")
                .setPositiveButton("Close", null)
                .setMessage(message)

            builder.show()
            return true
        }
            }

    inner class MyAdapter() : RecyclerView.Adapter<MyViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false) as TextView
            return MyViewHolder(view)
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if(notes.size > 0){
                holder.view.setText(notes[position].title + " " + notes[position].lastModified)
            }
        }
        override fun getItemCount(): Int {
            return notes.size
        }
    }
}