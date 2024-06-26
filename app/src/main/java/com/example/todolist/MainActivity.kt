package com.example.todolist
import DatabaseHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.adapters.TasksAdapter
import com.example.todolist.database.TasksRepositoryImpl
import com.example.todolist.fragments.FragmentTaskDetails
import com.example.todolist.viewModels.TasksViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import com.example.todolist.database.TaskModel

class MainActivity : AppCompatActivity() {

    lateinit var bottomSheetFragment : ModalBottomSheet
    private lateinit var settingsActionButton: FloatingActionButton
    lateinit var recyclerTasks: RecyclerView
    lateinit var addTask: FloatingActionButton
    private lateinit var colorActionButton: FloatingActionButton
    private lateinit var searchText: EditText
    private lateinit var searchButton: ImageView
    private lateinit var databaseHandler: DatabaseHandler
    lateinit var tasksViewModel: TasksViewModel
    private lateinit var tasksRepositoryImpl: TasksRepositoryImpl
    lateinit var adapterRecycler: TasksAdapter
    lateinit var sharedPreferences: SharedPreferences
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        NotificationUtils.createNotificationChannel(this)



        settingsActionButton = findViewById(R.id.settingsIcon)
        colorActionButton = findViewById(R.id.colorIcon)
        addTask = findViewById(R.id.addTask)
        searchButton = findViewById(R.id.searchButton)
        searchText = findViewById(R.id.searchText)


        setUpSharedPreferences()

        recyclerTasks = findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(this)


        databaseHandler = DatabaseHandler(this)
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        tasksViewModel = TasksViewModel(tasksRepositoryImpl)
        tasksViewModel.fetchTasksFromDatabase()
        bottomSheetFragment = ModalBottomSheet()


        addTask.setOnClickListener {
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
        }

        searchButton.setOnClickListener {
            val query = searchText.text.toString().trim()
            if (query.isNotEmpty()) {
                val filteredTasks = tasksViewModel.tasksData.value?.filter { it.title.contains(query, ignoreCase = true) }
                filteredTasks?.let {
                    adapterRecycler.setData(it)
                }
            } else {
                tasksViewModel.tasksData.value?.let {
                    adapterRecycler.setData(it)
                }
            }
            setDefaultPreferences()
        }

        adapterRecycler = TasksAdapter(
            this,
            tasksViewModel.tasksData.value ?: listOf(),
            tasksViewModel = tasksViewModel,
            onTaskItemClick = { taskId ->
                tasksViewModel.tasksData.value?.find { it.id == taskId }?.let { task ->
                    val fragment = FragmentTaskDetails.newInstance(task)
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
                }
            }
        )

        tasksViewModel.tasksData.observe(this) { tasks ->
                adapterRecycler.setData(tasks)
                filterTasks()
        }



        recyclerTasks.adapter = adapterRecycler
        adapterRecycler.itemTouchHelper.attachToRecyclerView(recyclerTasks)

        settingsActionButton.setOnClickListener {
            searchText.text.clear()
            showBottomSheet()
        }
        colorActionButton.setOnClickListener {
            val currentColorName = sharedPreferences.getString("TaskColor", TaskColor.RED.colorName)
            val currentColor = TaskColor.entries.find { it.colorName.equals(currentColorName, ignoreCase = true) }
                ?: TaskColor.RED

            val nextColor = TaskColor.nextColor(currentColor)
            sharedPreferences.edit().putString("TaskColor", nextColor.colorName).apply()

            adapterRecycler.notifyDataSetChanged()
        }

        if (!checkPermission()) {
            requestPermission()
        }

        if (!checkNotificationPermission()) {
            showNotificationPermissionDialog()
        }
        val taskId = intent.getIntExtra("task_id", 0)

        if (taskId != 0) {
            tasksViewModel.tasksData.value?.find { it.id == taskId }?.let { task ->
                val fragment = FragmentTaskDetails.newInstance(task)
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
            }
        }

    }
    private fun showBottomSheet() {
        bottomSheetFragment.show(supportFragmentManager, ModalBottomSheet.TAG)
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Do you want to enable notifications for this app?")
            .setPositiveButton("Yes") { dialog, _ ->
                navigateToNotificationSettings()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun checkNotificationPermission(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

    private fun navigateToNotificationSettings() {
        val intent = Intent().apply {
            action = "android.settings.APP_NOTIFICATION_SETTINGS"
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }
        startActivity(intent)
    }

    private fun setDefaultPreferences(){
        sharedPreferences.edit().putString("Status", "All").apply()
        sharedPreferences.edit().putString("Category", "All").apply()
        sharedPreferences.edit().putString("SortType", "Urgent").apply()
    }
    private fun setUpSharedPreferences(){
        sharedPreferences = getSharedPreferences("com.example.todolist.preferences", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("NotificationTime")) {
            sharedPreferences.edit().putInt("NotificationTime", 1).apply()
        }
        if (!sharedPreferences.contains("Category")) {
            sharedPreferences.edit().putString("Category", "All").apply()
        }
        if (!sharedPreferences.contains("SortType")) {
            sharedPreferences.edit().putString("SortType", "Urgent").apply()
        }
        if (!sharedPreferences.contains("Status")) {
            sharedPreferences.edit().putString("Status", "All").apply()
        }
        if (!sharedPreferences.contains("TaskColor")) {
            sharedPreferences.edit().putString("TaskColor", "RED").apply()
        }
    }

    fun filterTasks() {

        val selectedCategory = sharedPreferences.getString("Category","All")
        val selectedSort = sharedPreferences.getString("SortType","Urgent")
        val selectedStatus = sharedPreferences.getString("Status","All")?: "All"

        val filteredTasks = tasksViewModel.tasksData.value?.let { allTasks ->
            when (selectedCategory) {
                "All" -> filterTasksByStatus(allTasks, selectedStatus)
                "Sport", "Family", "Job" -> {
                    val categoryTasks = allTasks.filter { it.category == selectedCategory }
                    filterTasksByStatus(categoryTasks, selectedStatus)
                }
                else -> listOf()
            }
        } ?: listOf()

        val sortedTasks = when (selectedSort) {
            "Urgent" -> {
                filteredTasks.sortedWith(compareBy { task ->
                    task.executionTime?.time ?: Long.MAX_VALUE
                })
            }
            "NonUrgent" -> {
                filteredTasks.sortedWith(compareByDescending { task ->
                    task.executionTime?.time ?: Long.MIN_VALUE
                })
            }
            else -> filteredTasks
        }

        if (!recyclerTasks.isComputingLayout && !recyclerTasks.isAnimating) {
            adapterRecycler.setData(sortedTasks)
            adapterRecycler.notifyDataSetChanged()
        }
    }

    private fun filterTasksByStatus(tasks: List<TaskModel>, selectedStatus: String): List<TaskModel> {
        return when (selectedStatus) {
            "All" -> tasks
            "In process" -> tasks.filter { it.completed == 0 }
            "Finished" -> tasks.filter { it.completed == 1 }
            else -> listOf()
        }
    }

}