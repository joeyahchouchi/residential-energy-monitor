package com.univ.energymonitor.domain.model

data class ConsumptionInfo(
    val edlHoursPerDay: String,
    val usesEdl: Boolean,
    val usesGenerator: Boolean,
    val usesSolar: Boolean,
    val usesUps: Boolean,
    val usesNone: Boolean,
    val generatorSubscriptionType: String,
    val solarCapacity: String,
    val solarHasBattery: String
)