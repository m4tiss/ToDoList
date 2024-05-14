package com.example.todolist
import DatabaseHandler
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TaskModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var settingsImageView: ImageView
    private lateinit var spinner: Spinner
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var databaseHandler: DatabaseHandler


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

        databaseHandler = DatabaseHandler(this)

        val currentTime = Calendar.getInstance().time

        val exampleTask = TaskModel(
            id = 1,
            title = "Obejrzeć ligę mistrzów",
            description = "REAL-BVB",
            creationTime = currentTime,
            executionTime = null,
            completed = 0,
            notificationEnabled = 1,
            category = "Sport",
            attachments = emptyList()
        )

//        databaseHandler.addTask(exampleTask)


        val tasksFromDatabase = databaseHandler.getAllTasks()

        val adapterRecycler = TasksAdapter(this,tasksFromDatabase) { taskId ->
            databaseHandler.deleteTask(taskId)
        }
        recyclerTasks.adapter = adapterRecycler
        adapterRecycler.itemTouchHelper.attachToRecyclerView(recyclerTasks)

        settingsImageView.setOnClickListener {
            showBottomSheet()
        }
    }
    private fun showBottomSheet() {
        val modalBottomSheet = ModalBottomSheet()
        modalBottomSheet.show(supportFragmentManager, ModalBottomSheet.TAG)
    }
}