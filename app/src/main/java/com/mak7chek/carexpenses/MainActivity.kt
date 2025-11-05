package com.mak7chek.carexpenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mak7chek.carexpenses.ui.navigation.Routes
import com.mak7chek.carexpenses.ui.screens.auth.AuthScreen
import com.mak7chek.carexpenses.ui.theme.CarExpensesTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarExpensesTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Routes.AUTH
                    ){
                        composable(route = Routes.AUTH){
                            AuthScreen(
                                onNavigateToHome = {
                                    navController.navigate(Routes.MAIN){
                                        popUpTo(Routes.AUTH){
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                        composable(route = Routes.MAIN){
                            PlaceholderMainScreen()
                        }

                    }
                }

            }
        }
    }
}

@Composable
fun PlaceholderMainScreen(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "вітаю",
            style = MaterialTheme.typography.headlineSmall,

            color = MaterialTheme.colorScheme.primary
        )
    }
}