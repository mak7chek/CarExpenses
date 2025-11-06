// ui/splash/SplashScreen.kt
package com.mak7chek.carexpenses.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mak7chek.carexpenses.ui.navigation.Routes

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(key1 = authState) {
        when (authState) {
            AuthState.AUTHENTICATED -> {
                onNavigateToMain()
            }
            AuthState.UNAUTHENTICATED -> {
                onNavigateToAuth()
            }
            AuthState.LOADING -> {
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}