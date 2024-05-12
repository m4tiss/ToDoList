package com.example.todolist
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var settingsImageView: ImageView
    private lateinit var spinner: Spinner
    private lateinit var recyclerTasks: RecyclerView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        settingsImageView = findViewById(R.id.settingsIcon)
        spinner = findViewById(R.id.spinner)

        val options = arrayOf("All", "In process", "Finished")

        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        recyclerTasks = findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(this)


        val taskTitles = listOf("Zrobić zadanie", "Psa wyprowadzić", "Obejrzeć lige mistrzów", "Trening", "Czytać książke", "Zjeść megarollo")
        val adapterRecycler = TasksAdapter(taskTitles)
        recyclerTasks.adapter = adapterRecycler


        settingsImageView.setOnClickListener {
            showBottomSheet()
        }
    }
    private fun showBottomSheet() {
        val modalBottomSheet = ModalBottomSheet()
        modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
    }
}