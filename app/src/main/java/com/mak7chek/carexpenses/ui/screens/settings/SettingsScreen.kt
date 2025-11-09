package com.mak7chek.carexpenses.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mak7chek.carexpenses.R
import com.mak7chek.carexpenses.data.repository.ThemeSetting
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit,
    onNavigateToUpdateName: () -> Unit,
    onNavigateToUpdatePassword: () -> Unit,
    onNavigateToFuelPrices: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        viewModel.navigateToAuth.collect {
            onNavigateToAuth()
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.userMessage.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Налаштування") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 8.dp)
        ) {
            // --- Секція 1: Акаунт ---
            item { SectionHeader("Акаунт") }
            item {
                SettingItem(
                    icon = Icons.Default.Person,
                    title = "Змінити ім'я",
                    subtitle = "Оновіть ваше публічне ім'я",
                    onClick = onNavigateToUpdateName
                )
            }
            item {
                SettingItem(
                    icon = Icons.Default.Lock,
                    title = "Змінити пароль",
                    subtitle = "Оновіть ваш пароль безпеки",
                    onClick = onNavigateToUpdatePassword
                )
            }

            item { SectionHeader("Вигляд та Дані") }
            item {
                SettingItem(
                    title = "Ціни на паливо",
                    subtitle = "Редагувати ваші ціни для розрахунків",
                    onClick = onNavigateToFuelPrices
                )
            }
            item {
                val themeSubtitle = when (uiState.currentTheme) {
                    ThemeSetting.SYSTEM -> "Як в системі"
                    ThemeSetting.LIGHT -> "Світла"
                    ThemeSetting.DARK -> "Темна"
                }
                SettingItem(
                    iconPainter = painterResource(id = R.drawable.ic_color_lens),
                    title = "Тема",
                    subtitle = themeSubtitle,
                    onClick = viewModel::onThemeClicked
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }

            item {
                SettingItem(
                    icon = Icons.Default.Delete,
                    title = "Видалити акаунт",
                    subtitle = "Безповоротне видалення акаунта та всіх даних",
                    color = MaterialTheme.colorScheme.error,
                    onClick = viewModel::onDeleteAccountClicked
                )
            }
            item {
                SettingItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Вийти з акаунту",
                    subtitle = "Завершити поточну сесію",
                    color = MaterialTheme.colorScheme.error,
                    onClick = viewModel::onLogoutClicked
                )
            }
        }
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDialog,
            title = { Text("Вихід з акаунту") },
            text = { Text("Ви впевнені, що хочете вийти?") },
            confirmButton = {
                TextButton(onClick = viewModel::onLogoutConfirm) {
                    Text("Вийти")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDialog) {
                    Text("Скасувати")
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDialog,
            title = { Text("Видалити акаунт?") },
            text = { Text("Ця дія є БЕЗПОВОРОТНОЮ. Всі ваші поїздки та автомобілі будуть видалені назавжди.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onDeleteAccountConfirm,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Так, Видалити")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDialog) {
                    Text("Скасувати")
                }
            }
        )
    }

    if (uiState.showThemeDialog) {
        ThemeChooserDialog(
            currentTheme = uiState.currentTheme,
            onDismiss = viewModel::onDismissDialog,
            onThemeSelected = viewModel::onThemeSelected
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color
            )
            iconPainter != null -> Icon(
                painter = iconPainter,
                contentDescription = title,
                tint = color
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (color == MaterialTheme.colorScheme.error) color.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Composable для діалогу вибору теми ---
@Composable
fun ThemeChooserDialog(
    currentTheme: ThemeSetting,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Виберіть тему") },
        text = {
            Column(Modifier.selectableGroup()) {
                ThemeRadioButton(
                    text = "Світла",
                    selected = currentTheme == ThemeSetting.LIGHT,
                    onClick = { onThemeSelected(ThemeSetting.LIGHT) }
                )
                ThemeRadioButton(
                    text = "Темна",
                    selected = currentTheme == ThemeSetting.DARK,
                    onClick = { onThemeSelected(ThemeSetting.DARK) }
                )
                ThemeRadioButton(
                    text = "Як в системі",
                    selected = currentTheme == ThemeSetting.SYSTEM,
                    onClick = { onThemeSelected(ThemeSetting.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}

@Composable
private fun ThemeRadioButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Text(text)
    }
}