package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.CategoryResult

object LebanonDefaults {

    const val EDL_PRICE_PER_KWH_USD = 0.10
    const val CO2_KG_PER_KWH = 0.50
    const val COOLING_SEASON_DAYS = 150
    const val HEATING_SEASON_DAYS = 90
    const val DAYS_PER_YEAR = 365
    const val BTU_PER_KW = 3412.14
    const val WATER_HEATER_DEFAULT_KW = 1.5
    const val SOLAR_BACKUP_FACTOR = 0.30
    const val ELECTRIC_HEATER_DEFAULT_KW = 1.5
    const val GAS_HEATER_KW = 0.0
    const val DIESEL_HEATER_KW = 0.0
    const val OTHER_HEATER_DEFAULT_KW = 1.0
}

object EnergyCalculator {

    fun calculate(survey: SurveyData): EnergyReport {

        val hvacCooling = calculateHvacCooling(survey)
        val hvacHeating = calculateHvacHeating(survey)
        val waterHeating = calculateWaterHeating(survey)
        val lighting = calculateLighting(survey)
        val appliances = calculateAppliances(survey)

        val totalYearlyKwh = hvacCooling.yearlyKwh +
                hvacHeating.yearlyKwh +
                waterHeating.yearlyKwh +
                lighting.yearlyKwh +
                appliances.yearlyKwh

        val totalDailyKwh = totalYearlyKwh / LebanonDefaults.DAYS_PER_YEAR
        val totalYearlyCostUsd = totalYearlyKwh * LebanonDefaults.EDL_PRICE_PER_KWH_USD
        val totalYearlyCo2Kg = totalYearlyKwh * LebanonDefaults.CO2_KG_PER_KWH

        val hvacCoolingPct: Double
        val hvacHeatingPct: Double
        val waterHeatingPct: Double
        val lightingPct: Double
        val appliancesPct: Double

        if (totalYearlyKwh > 0) {
            hvacCoolingPct = hvacCooling.yearlyKwh / totalYearlyKwh * 100
            hvacHeatingPct = hvacHeating.yearlyKwh / totalYearlyKwh * 100
            waterHeatingPct = waterHeating.yearlyKwh / totalYearlyKwh * 100
            lightingPct = lighting.yearlyKwh / totalYearlyKwh * 100
            appliancesPct = appliances.yearlyKwh / totalYearlyKwh * 100
        } else {
            hvacCoolingPct = 0.0
            hvacHeatingPct = 0.0
            waterHeatingPct = 0.0
            lightingPct = 0.0
            appliancesPct = 0.0
        }

        return EnergyReport(
            hvacCooling = hvacCooling,
            hvacHeating = hvacHeating,
            waterHeating = waterHeating,
            lighting = lighting,
            appliances = appliances,

            totalDailyKwh = round2(totalDailyKwh),
            totalYearlyKwh = round2(totalYearlyKwh),
            totalYearlyCostUsd = round2(totalYearlyCostUsd),
            totalYearlyCo2Kg = round2(totalYearlyCo2Kg),

            avgMonthlyKwh = round2(totalYearlyKwh / 12.0),
            avgMonthlyCostUsd = round2(totalYearlyCostUsd / 12.0),
            avgMonthlyCo2Kg = round2(totalYearlyCo2Kg / 12.0),

            hvacCoolingPercent = round2(hvacCoolingPct),
            hvacHeatingPercent = round2(hvacHeatingPct),
            waterHeatingPercent = round2(waterHeatingPct),
            lightingPercent = round2(lightingPct),
            appliancesPercent = round2(appliancesPct)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HVAC Cooling
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateHvacCooling(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("HVAC cooling")
        if (hvac.acUnits.isEmpty()) return emptyResult("HVAC cooling")

        var totalYearlyKwh = 0.0

        for (unit in hvac.acUnits) {
            totalYearlyKwh += calculateAcUnitYearlyKwh(unit)
        }

        return buildResult("HVAC cooling", totalYearlyKwh)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HVAC Heating
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateHvacHeating(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("HVAC heating")

        if (hvac.heatingSystemType == "None" || hvac.heatingSystemType.isBlank()) {
            return emptyResult("HVAC heating")
        }

        // AC-based heating
        if (hvac.heatingSystemType == "AC") {
            if (hvac.heatingAcUnits.isEmpty()) return emptyResult("HVAC heating")

            var totalYearlyKwh = 0.0
            for (unit in hvac.heatingAcUnits) {
                totalYearlyKwh += calculateAcUnitYearlyKwh(unit)
            }
            return buildResult("HVAC heating", totalYearlyKwh)
        }

        // Non-AC heating (electric, diesel, gas, other)
        val numUnits = hvac.numberOfHeatingUnits.toSafeDouble()
        val dailyHours = hvac.heatingDailyUsageHours.toSafeDouble()

        val powerKwPerUnit = when (hvac.heatingSystemType) {
            "Electric heater" -> LebanonDefaults.ELECTRIC_HEATER_DEFAULT_KW
            "Gas" -> LebanonDefaults.GAS_HEATER_KW
            "Diesel" -> LebanonDefaults.DIESEL_HEATER_KW
            else -> LebanonDefaults.OTHER_HEATER_DEFAULT_KW
        }

        val yearlyKwh = powerKwPerUnit * dailyHours * numUnits *
                LebanonDefaults.HEATING_SEASON_DAYS

        return buildResult("HVAC heating", yearlyKwh)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared AC unit calculation (used by both cooling and heating)
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateAcUnitYearlyKwh(unit: AcUnitInfo): Double {
        val rawCapacity = unit.capacityValue.toSafeDouble()
        val capacityKw = when (unit.capacityUnit) {
            "BTU/hr" -> rawCapacity / LebanonDefaults.BTU_PER_KW
            "Tons" -> rawCapacity * 3.517
            else -> rawCapacity
        }

        val cop = if (unit.knowsCop) {
            unit.cop.toSafeDouble().let { if (it > 0) it else 3.0 }
        } else {
            copFromYear(unit.acYear)
        }

        val electricalKw = if (cop > 0) capacityKw / cop else 0.0
        val dailyHours = unit.dailyUsageHours.toSafeDouble()
        val daysPerYear = unit.daysPerYear.toSafeDouble()

        return electricalKw * dailyHours * daysPerYear
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COP estimation from year
    // ─────────────────────────────────────────────────────────────────────────
    private fun copFromYear(year: String): Double {
        return when (year) {
            "2000–2012" -> 2.8
            "2012–2015" -> 3.3
            "2015–2020" -> 3.5
            "After 2020" -> 4.0
            else -> 3.0
        }
    }

    fun estimateCopFromYear(year: String): String {
        return "${copFromYear(year)} (estimated)"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Water Heating
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateWaterHeating(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("Water heating")

        if (hvac.waterHeaterType == "None" || hvac.waterHeaterType.isBlank()) {
            return emptyResult("Water heating")
        }

        if (hvac.waterHeaterType == "Gas") {
            return emptyResult("Water heating")
        }

        val powerKw = hvac.waterHeaterPowerKw.toSafeDouble().let {
            if (it > 0) it else LebanonDefaults.WATER_HEATER_DEFAULT_KW
        }
        val dailyHours = hvac.waterHeaterDailyHours.toSafeDouble()

        val effectivePower = if (hvac.waterHeaterType == "Solar") {
            powerKw * LebanonDefaults.SOLAR_BACKUP_FACTOR
        } else {
            powerKw
        }

        val yearlyKwh = effectivePower * dailyHours * LebanonDefaults.DAYS_PER_YEAR

        return buildResult("Water heating", yearlyKwh)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lighting
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateLighting(survey: SurveyData): CategoryResult {
        val light = survey.lightingInfo ?: return emptyResult("Lighting")

        var totalYearlyKwh = 0.0

        for (lamp in light.indoorLamps) {
            val powerW = lamp.powerWatts.toSafeDouble()
            val hours = lamp.dailyUsageHours.toSafeDouble()
            totalYearlyKwh += (powerW * hours) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
        }

        for (lamp in light.outdoorLamps) {
            val powerW = lamp.powerWatts.toSafeDouble()
            val hours = lamp.dailyUsageHours.toSafeDouble()
            totalYearlyKwh += (powerW * hours) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
        }

        return buildResult("Lighting", totalYearlyKwh)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Appliances
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateAppliances(survey: SurveyData): CategoryResult {
        val app = survey.applianceInfo ?: return emptyResult("Appliances")

        var totalDailyWh = 0.0

        for (appliance in app.appliances) {
            if (appliance.exists) {
                val powerW = appliance.powerWatts.toSafeDouble()
                val hours = appliance.dailyUsageHours.toSafeDouble()
                totalDailyWh += powerW * hours
            }
        }

        for (custom in app.customAppliances) {
            val powerW = custom.powerWatts.toSafeDouble()
            val hours = custom.dailyUsageHours.toSafeDouble()
            totalDailyWh += powerW * hours
        }

        val yearlyKwh = (totalDailyWh / 1000.0) * LebanonDefaults.DAYS_PER_YEAR

        return buildResult("Appliances", yearlyKwh)
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Helper functions
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildResult(name: String, yearlyKwh: Double): CategoryResult {
        val safeYearly = if (yearlyKwh > 0) yearlyKwh else 0.0
        return CategoryResult(
            name = name,
            dailyKwh = round2(safeYearly / LebanonDefaults.DAYS_PER_YEAR),
            yearlyKwh = round2(safeYearly),
            yearlyCostUsd = round2(safeYearly * LebanonDefaults.EDL_PRICE_PER_KWH_USD),
            yearlyCo2Kg = round2(safeYearly * LebanonDefaults.CO2_KG_PER_KWH)
        )
    }

    private fun emptyResult(name: String): CategoryResult {
        return CategoryResult(
            name = name,
            dailyKwh = 0.0,
            yearlyKwh = 0.0,
            yearlyCostUsd = 0.0,
            yearlyCo2Kg = 0.0
        )
    }

    private fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }

    private fun String.toSafeDouble(): Double {
        return this.trim().toDoubleOrNull() ?: 0.0
    }
}