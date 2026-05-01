package com.univ.energymonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ConsumptionInfo(
    val edlHoursPerDay: String = "",
    val usesEdl: Boolean = false,
    val usesGenerator: Boolean = false,
    val usesSolar: Boolean = false,
    val usesUps: Boolean = false,
    val usesNone: Boolean = false,
    val generatorSubscriptionType: String = "",
    val solarCapacity: String = "",
    val solarHasBattery: String = "",

    // Yearly bills + per-kWh prices (replace the old range dropdowns)
    val yearlyEdlBillUsd: String = "",
    val edlPricePerKwhUsd: String = "",
    val yearlyGeneratorBillUsd: String = "",
    val generatorPricePerKwhUsd: String = "",

    val solarYearlyKwh: String = ""
)