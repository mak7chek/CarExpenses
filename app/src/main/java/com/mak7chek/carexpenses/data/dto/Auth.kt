package com.mak7chek.carexpenses.data.dto

data class AuthRequest(
    val name: String?= null,
    val email: String,
    val password: String
)
data class AuthResponse(
    val token: String
)