package com.example.unido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unido.data.TarefaDatabase
import com.example.unido.data.TarefaRepository
import com.example.unido.ui.DetalhesTarefaScreen
import com.example.unido.ui.ListaTarefasScreen
import com.example.unido.viewmodel.TarefaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TarefaDatabase.getInstance(applicationContext)
        val repository = TarefaRepository(database.tarefaDao())

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val viewModel: TarefaViewModel = viewModel(
                        factory = TarefaViewModel.factory(repository)
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "lista"
                    ) {
                        composable("lista") {
                            ListaTarefasScreen(
                                viewModel = viewModel,
                                onNovaTarefa = { navController.navigate("detalhes/-1") },
                                onTarefaClick = { id -> navController.navigate("detalhes/$id") }
                            )
                        }

                        composable(
                            route = "detalhes/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: -1
                            DetalhesTarefaScreen(
                                tarefaId = id,
                                viewModel = viewModel,
                                onVoltar = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
