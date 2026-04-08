package com.univ.energymonitor.ui.state

data class ConsumptionSurveyUiState(
    val edlHoursPerDay: String = "",
    val usesEdl: Boolean = false,
    val usesGenerator: Boolean = false,
    val usesSolar: Boolean = false,
    val usesUps: Boolean = false,
    val usesNone: Boolean = false,
    val generatorSubscriptionType: String = "",
    val solarCapacity: String = "",
    val solarHasBattery: String = "",
    val showErrors: Boolean = false
)

fun ConsumptionSurveyUiState.isValid(): Boolean {
    if (edlHoursPerDay.isBlank()) return false
    if (!usesEdl && !usesGenerator && !usesSolar && !usesUps && !usesNone) return false
    if (usesGenerator && generatorSubscriptionType.isBlank()) return false
    if (usesSolar) {
        if (solarCapacity.isBlank()) return false
        if (solarHasBattery.isBlank()) return false
    }
    return true
}