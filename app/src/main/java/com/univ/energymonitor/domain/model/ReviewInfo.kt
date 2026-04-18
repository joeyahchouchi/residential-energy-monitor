package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class ReviewInfo(
    val confirmAccuracy: Boolean,
    val finalNotes: String
)