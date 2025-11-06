package com.mak7chek.carexpenses.ui.navigation

object Routes {
    const val SPLASH = "splash"
const val AUTH= "auth"
    const val MAIN = "main"
    const val ADD_VEHICLE = "add_vehicle"
    const val EDIT_VEHICLE = "edit_vehicle/{vehicleId}"
    fun editVehicleRoute(id: Long) = "edit_vehicle/$id"

}