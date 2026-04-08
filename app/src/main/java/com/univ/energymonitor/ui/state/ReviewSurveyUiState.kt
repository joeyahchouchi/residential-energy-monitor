package com.univ.energymonitor.ui.state

data class ReviewSurveyUiState(
    val confirmAccuracy: Boolean = false,
    val finalNotes: String = "",
    val showErrors: Boolean = false
)

fun ReviewSurveyUiState.isValid(): Boolean {
    return confirmAccuracy
}