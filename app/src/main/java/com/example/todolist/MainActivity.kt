package com.example.todolist
import DatabaseHandler
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TaskModel
import com.example.todolist.database.TasksRepositoryImpl
import com.example.todolist.fragments.FragmentSettings
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var settingsImageView: ImageView
    private lateinit var spinner: Spinner
    private lateinit var recyclerTasks: RecyclerView
    lateinit var addTask: FloatingActionButton
    private lateinit var databaseHandler: DatabaseHandler
    private lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasksRepositoryImpl: TasksRepositoryImpl


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
        addTask = findViewById(R.id.addTask)
        val options = arrayOf("All", "In process", "Finished")

        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner

        recyclerTasks = findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(this)


        databaseHandler = DatabaseHandler(this)
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        tasksViewModel = TasksViewModel(tasksRepositoryImpl)


        val currentTime = Calendar.getInstance().time

        addTask.setOnClickListener {
//            val exampleTask = TaskModel(
//                id = 1,
//                title = "Obejrzeć ligę mistrzów",
//                description = "REAL-BVB",
//                creationTime = currentTime,
//                executionTime = null,
//                completed = 0,
//                notificationEnabled = 1,
//                category = "Sport",
//                attachments = emptyList()
//            )
//            tasksViewModel.addTask(exampleTask)
            val fragment = FragmentAddTask()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
            addTask.visibility = View.GONE
        }


        val adapterRecycler = TasksAdapter(
            this,
            tasksViewModel.tasksData.value ?: listOf(),
            onDeleteTask = { taskId ->
                tasksViewModel.deleteTask(taskId)
            },
            onClickCheckBox = { taskId ->
                val task = tasksViewModel.tasksData.value?.find { it.id == taskId }
                task?.let {
                    val newStatus = if (it.completed == 1) 0 else 1
                    tasksViewModel.updateStatus(taskId, newStatus)
                }
            }
        )

        tasksViewModel.tasksData.observe(this) { tasks ->
            adapterRecycler.setData(tasks)
        }

        recyclerTasks.adapter = adapterRecycler
        adapterRecycler.itemTouchHelper.attachToRecyclerView(recyclerTasks)

        settingsImageView.setOnClickListener {
            showBottomSheet()
        }
    }
    private fun showBottomSheet() {
        val bottomSheetFragment = ModalBottomSheet(supportFragmentManager)
        bottomSheetFragment.show(supportFragmentManager, ModalBottomSheet.TAG)
    }
}