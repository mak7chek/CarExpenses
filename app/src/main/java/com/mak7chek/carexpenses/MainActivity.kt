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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mak7chek.carexpenses.ui.navigation.Routes
import com.mak7chek.carexpenses.ui.screens.MainScreen
import com.mak7chek.carexpenses.ui.screens.auth.AuthScreen
import com.mak7chek.carexpenses.ui.screens.vehicles.AddVehicleScreen
import com.mak7chek.carexpenses.ui.screens.vehicles.EditVehicleScreen
import com.mak7chek.carexpenses.ui.splash.SplashScreen
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
                        startDestination = Routes.SPLASH
                    ){
                        composable(route = Routes.SPLASH) {
                            SplashScreen(
                                onNavigateToAuth = {
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                },
                                onNavigateToMain = {
                                    navController.navigate(Routes.MAIN) {
                                        popUpTo(Routes.SPLASH) { inclusive = true }
                                    }
                                }
                            )
                        }
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
                            MainScreen(navController=navController,
                                onNavigateToAuth = {
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(Routes.MAIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(route = Routes.ADD_VEHICLE){
                            AddVehicleScreen(navController = navController)
                        }

                        composable(
                            route = Routes.EDIT_VEHICLE,
                            arguments = listOf(navArgument("vehicleId"){
                                type = NavType.LongType
                            })
                        ){
                            EditVehicleScreen(navController = navController)
                        }

                    }
                }

            }
        }
    }
}

