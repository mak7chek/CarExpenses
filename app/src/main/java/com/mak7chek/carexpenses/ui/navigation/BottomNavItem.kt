package com.mak7chek.carexpenses.ui.navigation
import androidx.compose.material.icons.Icons
import androidx.annotation.DrawableRes
import com.mak7chek.carexpenses.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Settings
sealed class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconResId: Int
) {
    object Journal : BottomNavItem(
        route = "journal",
        label = "Журнал",
        iconResId = R.drawable.ic_journal_filled
    )
    object Map : BottomNavItem(
        route = "map",
        label = "Карти",
        iconResId = R.drawable.ic_map_custom
    )

    object Vehicles : BottomNavItem(
        route = "vehicles",
        label = "Авто",
        iconResId = R.drawable.ic_car_custom
    )
    object Settings : BottomNavItem(
        route = "settings",
        label = "Налашт...",
        iconResId = R.drawable.ic_settings_custom)
}