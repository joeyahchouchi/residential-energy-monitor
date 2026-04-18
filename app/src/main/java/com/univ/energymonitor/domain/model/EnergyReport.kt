package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class CategoryResult(
    val name: String,
    val dailyKwh: Double,
    val yearlyKwh: Double,
    val yearlyCostUsd: Double,
    val yearlyCo2Kg: Double
)
@Serializable
data class EnergyReport(
    val hvacCooling: CategoryResult,
    val hvacHeating: CategoryResult,
    val waterHeating: CategoryResult,
    val lighting: CategoryResult,
    val appliances: CategoryResult,

    val totalDailyKwh: Double,
    val totalYearlyKwh: Double,
    val totalYearlyCostUsd: Double,
    val totalYearlyCo2Kg: Double,

    val avgMonthlyKwh: Double,
    val avgMonthlyCostUsd: Double,
    val avgMonthlyCo2Kg: Double,

    val hvacCoolingPercent: Double,
    val hvacHeatingPercent: Double,
    val waterHeatingPercent: Double,
    val lightingPercent: Double,
    val appliancesPercent: Double
)