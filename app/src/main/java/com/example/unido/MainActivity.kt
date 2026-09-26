package com.example.unido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unido.ui.navigation.UniDoNavHost
import com.example.unido.ui.theme.UniDoTheme
import com.example.unido.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as UniDoApplication).repository

        setContent {
            UniDoTheme {
                val viewModel: TaskViewModel = viewModel(
                    factory = TaskViewModel.factory(repository)
                )
                UniDoNavHost(viewModel = viewModel)
            }
        }
    }
}
