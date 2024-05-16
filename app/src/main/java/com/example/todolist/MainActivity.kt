package com.example.todolist
import DatabaseHandler
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
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
import com.example.todolist.fragments.FragmentTaskDetails
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var settingsImageView: ImageView
    lateinit var recyclerTasks: RecyclerView
    lateinit var addTask: FloatingActionButton
    lateinit var searchButton: ImageView
    lateinit var searchText: EditText
    lateinit var databaseHandler: DatabaseHandler
    lateinit var tasksViewModel: TasksViewModel
    lateinit var tasksRepositoryImpl: TasksRepositoryImpl
    lateinit var adapterRecycler: TasksAdapter


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
        addTask = findViewById(R.id.addTask)
        searchButton = findViewById(R.id.searchButton)
        searchText = findViewById(R.id.searchText)



        recyclerTasks = findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(this)


        databaseHandler = DatabaseHandler(this)
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        tasksViewModel = TasksViewModel(tasksRepositoryImpl)


        val currentTime = Calendar.getInstance().time

        addTask.setOnClickListener {
//            val exampleTask = TaskModel(
//                id = 1,
//                title = "Testowanie",
//                description = "Testowanie Testowanie Testowanie Testowanie",
//                creationTime = currentTime,
//                executionTime = null,
//                completed = 0,
//                notificationEnabled = 1,
//                category = "Family",
//                attachments = emptyList()
//            )
//            tasksViewModel.addTask(exampleTask)
            val fragment = FragmentAddTask()
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.appcompat.R.anim.abc_shrink_fade_out_from_bottom,
                    androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom,
                    androidx.appcompat.R.anim.abc_shrink_fade_out_from_bottom
                )
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()
            addTask.visibility = View.GONE
        }

        searchButton.setOnClickListener {
            val query = searchText.text.toString().trim()
            if (query.isNotEmpty()) {
                val filteredTasks = tasksViewModel.tasksData.value?.filter { it.title.contains(query, ignoreCase = true) }
                filteredTasks?.let {
                    adapterRecycler.setData(it)
                    adapterRecycler.notifyDataSetChanged()
                }
            } else {
                tasksViewModel.tasksData.value?.let {
                    adapterRecycler.setData(it)
                    adapterRecycler.notifyDataSetChanged()
                }
            }
        }



        adapterRecycler = TasksAdapter(
            this,
            tasksViewModel.tasksData.value ?: listOf(),
            onDeleteTask = { taskId ->
                tasksViewModel.deleteTask(taskId)
            },
            tasksViewModel = tasksViewModel,
            onTaskItemClick = { taskId ->
                val task = tasksViewModel.tasksData.value?.find { it.id == taskId }
                task?.let {
                    val fragment = FragmentTaskDetails(it,tasksViewModel)
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom,
                            androidx.appcompat.R.anim.abc_shrink_fade_out_from_bottom,
                            androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom,
                            androidx.appcompat.R.anim.abc_shrink_fade_out_from_bottom
                        )
                        .replace(R.id.main, fragment)
                        .addToBackStack(null)
                        .commit()
                    addTask.visibility = View.GONE
                }
            }
        )

        tasksViewModel.tasksData.observe(this) { tasks ->
            if (!recyclerTasks.isComputingLayout && !recyclerTasks.isAnimating) {
                adapterRecycler.setData(tasks)
                adapterRecycler.notifyDataSetChanged()
            }
        }


        recyclerTasks.adapter = adapterRecycler
        adapterRecycler.itemTouchHelper.attachToRecyclerView(recyclerTasks)

        settingsImageView.setOnClickListener {
            searchText.text.clear()
            showBottomSheet()
        }
    }
    private fun showBottomSheet() {
        val bottomSheetFragment = ModalBottomSheet(tasksViewModel,recyclerTasks,adapterRecycler)
        bottomSheetFragment.show(supportFragmentManager, ModalBottomSheet.TAG)
    }

}