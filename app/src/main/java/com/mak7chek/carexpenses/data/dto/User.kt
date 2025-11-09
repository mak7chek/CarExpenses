package com.mak7chek.carexpenses.data.dto

data class UpdateNameRequest(
    val newName: String
)

data class UpdatePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
