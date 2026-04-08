package com.univ.energymonitor.domain.model

data class NewUser(
    val fullName: String,
    val email: String,
    val username: String,
    val password: String
)