package com.example.todolist
import DatabaseHandler
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat

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
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, proceed with accessing images
        } else {
            // Permission is denied, show a message to the user
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

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

        NotificationUtils.createNotificationChannel(this)



        settingsImageView = findViewById(R.id.settingsIcon)
        addTask = findViewById(R.id.addTask)
        searchButton = findViewById(R.id.searchButton)
        searchText = findViewById(R.id.searchText)


        val prefs = getSharedPreferences("com.example.todolist.preferences", Context.MODE_PRIVATE)
        if (!prefs.contains("NotificationTime")) {
            prefs.edit().putInt("NotificationTime", 1).apply()
        }

        recyclerTasks = findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(this)


        databaseHandler = DatabaseHandler(this)
        tasksRepositoryImpl = TasksRepositoryImpl(databaseHandler)
        tasksViewModel = TasksViewModel(tasksRepositoryImpl)

        addTask.setOnClickListener {
            val fragment = FragmentAddTask(tasksViewModel)
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

        if (!checkPermission()) {
            requestPermission()
        }

        if (!checkNotificationPermission()) {
            showNotificationPermissionDialog()
        }
    }
    private fun showBottomSheet() {
        val bottomSheetFragment = ModalBottomSheet(tasksViewModel,recyclerTasks,adapterRecycler)
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

}