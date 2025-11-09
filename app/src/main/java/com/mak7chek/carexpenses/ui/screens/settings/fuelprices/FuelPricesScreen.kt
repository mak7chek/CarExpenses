package com.mak7chek.carexpenses.ui.screens.settings.fuelprices



import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPricesScreen(
    navController: NavController,
    viewModel: FuelPricesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { navController.popBackStack() }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ціни на паливо") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Text(
                "Вкажіть ваші поточні ціни (грн/л). Вони будуть використовуватись для автоматичного розрахунку вартості поїздок.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = uiState.petrolPrice,
                onValueChange = viewModel::onPetrolChange,
                label = { Text("Бензин (А-95/А-92)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                value = uiState.dieselPrice,
                onValueChange = viewModel::onDieselChange,
                label = { Text("Дизель") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !uiState.isLoading
            )

            OutlinedTextField(
                value = uiState.gasPrice,
                onValueChange = viewModel::onGasChange,
                label = { Text("Газ") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !uiState.isLoading
            )

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::onSaveClick,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Зберегти ціни")
            }
        }
    }
}

