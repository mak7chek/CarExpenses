package com.mak7chek.carexpenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mak7chek.carexpenses.data.repository.SettingsRepository
import com.mak7chek.carexpenses.data.repository.ThemeSetting
import com.mak7chek.carexpenses.ui.navigation.Routes
import com.mak7chek.carexpenses.ui.screens.MainScreen
import com.mak7chek.carexpenses.ui.screens.auth.AuthScreen
import com.mak7chek.carexpenses.ui.screens.journal.TripDetailScreen
import com.mak7chek.carexpenses.ui.screens.settings.UpdateNameScreen
import com.mak7chek.carexpenses.ui.screens.settings.UpdatePasswordScreen
import com.mak7chek.carexpenses.ui.screens.vehicles.AddVehicleScreen
import com.mak7chek.carexpenses.ui.screens.vehicles.EditVehicleScreen
import com.mak7chek.carexpenses.ui.splash.SplashScreen
import com.mak7chek.carexpenses.ui.theme.CarExpensesTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val themeSetting = settingsRepository.themeSetting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeSetting.SYSTEM
        )
}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val themeSetting by viewModel.themeSetting.collectAsState()

            val useDarkTheme = when (themeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            CarExpensesTheme(
                darkTheme = useDarkTheme
            ) {
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
                        composable(
                            route = Routes.TRIP_DETAIL,
                            arguments = listOf(navArgument("tripId") {
                                type = NavType.LongType
                            })
                        ) {
                            TripDetailScreen(navController = navController)
                        }
                        composable(route = Routes.UPDATE_NAME) {
                            UpdateNameScreen(navController = navController)
                        }

                        composable(route = Routes.UPDATE_PASSWORD) {
                            UpdatePasswordScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}