package com.mak7chek.carexpenses.ui.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.mak7chek.carexpenses.data.local.entities.VehicleEntity
import com.mak7chek.carexpenses.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel =hiltViewModel(),
    navController: NavHostController
) {
    val vehicles by viewModel.vehicles.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.ADD_VEHICLE)
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    "Додати автомобіль"
                )
            }
        }
    ) { padding ->
        if (vehicles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "У вас ще не має жодного авто",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }

        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                items(
                    vehicles,
                    key = { it.id }
                ) { vehicles ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onVehicleSwiped(vehicles)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        // Дозволяємо змахувати тільки в одному напрямку (вліво)
                        enableDismissFromStartToEnd = false,

                        // 5. Це те, що буде ПОЗАДУ картки, поки ми тягнемо
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 4.dp).background(
                                        MaterialTheme.colorScheme.error,
                                        shape = CardDefaults.shape
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Видалити",
                                    tint = Color.White
                                )
                            }
                        }
                    ) {
                        VehicleItem(
                            vehicle = vehicles,
                            onClick = {
                                val route = Routes.editVehicleRoute(vehicles.id)
                                navController.navigate(route)

                            }
                        )
                    }
                }
            }

        }
    }
}


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VehicleItem(vehicle: VehicleEntity, onClick: () -> Unit) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${vehicle.make} ${vehicle.model}",
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${vehicle.year} рік",
                    color = MaterialTheme.colorScheme.primary
                )
            }

        }
    }
