package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.ApplianceItem

data class ApplianceSurveyUiState(
    val appliances: List<ApplianceItem> = defaultAppliances(),
    val customAppliances: List<ApplianceItem> = emptyList(),
    val showErrors: Boolean = false
)

fun defaultAppliances(): List<ApplianceItem> = listOf(
    ApplianceItem("Fridge"),
    ApplianceItem("Air Fryer"),
    ApplianceItem("Electric Eye"),
    ApplianceItem("Press Grill"),
    ApplianceItem("Electric Oven"),
    ApplianceItem("Washing Machine"),
    ApplianceItem("Phone Charger"),
    ApplianceItem("Laptop Charger"),
    ApplianceItem("Iron"),
    ApplianceItem("Vacuum"),
    ApplianceItem("Television"),
    ApplianceItem("Water Heater"),
    ApplianceItem("Router / WiFi"),
    ApplianceItem("Blender"),
    ApplianceItem("TV Decoder"),
    ApplianceItem("Microwave"),
    ApplianceItem("Electric Kettle"),
    ApplianceItem("Coffee Maker"),
    ApplianceItem("Dishwasher"),
    ApplianceItem("Hair Dryer"),
    ApplianceItem("Hair Straightener"),
    ApplianceItem("Clothes Dryer"),
    ApplianceItem("Robot Vacuum"),
    ApplianceItem("Water Pump"),
    ApplianceItem("Interphone"),
    ApplianceItem("CCTV Camera"),
    ApplianceItem("Fan"),
    ApplianceItem("EV Charger")
)

fun ApplianceItem.requiresEfficiencyInfo(): Boolean {
    return name in listOf(
        "Fridge",
        "Washing Machine",
        "Dishwasher",
        "Television",
        "Electric Oven",
        "Water Heater"
    )
}

fun ApplianceSurveyUiState.isValid(): Boolean {
    val activeAppliances = appliances.filter { it.exists }

    for (app in activeAppliances) {
        val power = app.powerWatts.toDoubleOrNull() ?: -1.0
        if (power <= 0) return false

        val hours = app.dailyUsageHours.toDoubleOrNull() ?: -1.0
        if (hours !in 0.0..24.0) return false

        if (app.requiresEfficiencyInfo()) {
            // User must either pick a real label OR pick "Unknown" + provide a year
            if (app.efficiencyLabel.isBlank()) {
                if (!app.userPickedUnknown) return false  // nothing selected at all
                val year = app.purchaseYear.toIntOrNull() ?: -1
                if (year !in 1980..2100) return false  // picked Unknown but no valid year
            }
        }
    }

    for (app in customAppliances) {
        if (app.name.isBlank()) return false

        val power = app.powerWatts.toDoubleOrNull() ?: -1.0
        if (power <= 0) return false

        val hours = app.dailyUsageHours.toDoubleOrNull() ?: -1.0
        if (hours !in 0.0..24.0) return false
    }

    return true
}