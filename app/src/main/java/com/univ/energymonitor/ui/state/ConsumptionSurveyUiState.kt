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

    val yearlyEdlBillUsd: String = "",
    val edlPricePerKwhUsd: String = "",
    val yearlyGeneratorBillUsd: String = "",
    val generatorPricePerKwhUsd: String = "",

    val solarYearlyKwh: String = "",
    val showErrors: Boolean = false
)


fun ConsumptionSurveyUiState.isValid(): Boolean {
    // EDL hours required for everyone
    if (edlHoursPerDay.isBlank()) return false

    // Must select at least one source
    if (!usesEdl && !usesGenerator && !usesSolar && !usesUps && !usesNone) return false

    // EDL fields required when EDL is on
    if (usesEdl) {
        val bill = yearlyEdlBillUsd.toDoubleOrNull() ?: -1.0
        if (bill <= 0) return false

        val price = edlPricePerKwhUsd.toDoubleOrNull() ?: -1.0
        if (price <= 0) return false
    }

    // Generator fields required when Generator is on
    if (usesGenerator) {
        if (generatorSubscriptionType.isBlank()) return false

        val bill = yearlyGeneratorBillUsd.toDoubleOrNull() ?: -1.0
        if (bill <= 0) return false

        val price = generatorPricePerKwhUsd.toDoubleOrNull() ?: -1.0
        if (price <= 0) return false
    }

    // Solar fields required when Solar is on
    if (usesSolar) {
        if (solarCapacity.isBlank()) return false
        if (solarHasBattery.isBlank()) return false

        val solarKwh = solarYearlyKwh.toDoubleOrNull() ?: -1.0
        if (solarKwh <= 0) return false
    }

    return true
}