package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.repo.AppRepository
import com.example.ui.screens.TajrubahApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TajrubahViewModel
import com.example.ui.viewmodel.TajrubahViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room database & repository
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = AppRepository(database)

        // Initialize Tajrubah ViewModel
        val factory = TajrubahViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[TajrubahViewModel::class.java]

        setContent {
            MyApplicationTheme {
                TajrubahApp(viewModel = viewModel)
            }
        }
    }
}
