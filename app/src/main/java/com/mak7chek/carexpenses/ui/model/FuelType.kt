package com.mak7chek.carexpenses.ui.model

enum class FuelType {
    PETROL,
    DIESEL,
    GAS;

    fun toDisplayName(): String {
        return when (this) {
            PETROL -> "Бензин"
            DIESEL -> "Дизель"
            GAS -> "Газ"
        }
    }
}