package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.CategoryResult

object LebanonDefaults {
    const val EDL_PRICE_PER_KWH_USD = 0.10
    const val CO2_KG_PER_KWH = 0.684
    const val DAYS_PER_YEAR = 365
    const val BTU_PER_KW = 3412.14
    const val WATER_HEATER_DEFAULT_KW = 1.5

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

        val buildingAge = survey.houseInfo?.buildingAge ?: ""
        var totalYearlyKwh = 0.0

        for (unit in hvac.acUnits) {
            totalYearlyKwh += calculateAcUnitYearlyKwh(unit, buildingAge)
        }

        return buildResult("HVAC cooling", totalYearlyKwh)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HVAC Heating (only electric sources count toward kWh)
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateHvacHeating(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("HVAC heating")

        if (hvac.heatingSystemType == "None" || hvac.heatingSystemType.isBlank()) {
            return emptyResult("HVAC heating")
        }

        when (hvac.heatingSystemType) {
            "AC" -> {
                if (hvac.heatingAcUnits.isEmpty()) return emptyResult("HVAC heating")
                val buildingAge = survey.houseInfo?.buildingAge ?: ""
                var totalYearlyKwh = 0.0
                for (unit in hvac.heatingAcUnits) {
                    totalYearlyKwh += calculateAcUnitYearlyKwh(unit, buildingAge)
                }
                return buildResult("HVAC heating", totalYearlyKwh)
            }
            "Electric Heater" -> {
                val numUnits = hvac.numberOfHeatingUnits.toSafeDouble()
                val powerKw = hvac.heatingPowerKw.toSafeDouble()
                val dailyHours = hvac.heatingDailyUsageHours.toSafeDouble()
                val daysPerYear = hvac.heatingDaysPerYear.toSafeDouble()
                val yearlyKwh = numUnits * powerKw * dailyHours * daysPerYear
                return buildResult("HVAC heating", yearlyKwh)
            }
            // Gas and Diesel/Fuel are non-electric — 0 kWh for electrical monitoring
            "Gas Heater" -> return emptyResult("HVAC heating")
            "Diesel/Fuel Heater" -> return emptyResult("HVAC heating")
            else -> return emptyResult("HVAC heating")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Water Heating (only electric sources count toward kWh)
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateWaterHeating(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("Water heating")

        if (hvac.waterHeaterType == "None" || hvac.waterHeaterType.isBlank()) {
            return emptyResult("Water heating")
        }

        when (hvac.waterHeaterType) {
            "Electrical Resistance" -> {
                val powerKw = hvac.waterHeaterPowerKw.toSafeDouble().let {
                    if (it > 0) it else LebanonDefaults.WATER_HEATER_DEFAULT_KW
                }
                val dailyHours = hvac.waterHeaterDailyHours.toSafeDouble()
                val daysPerYear = hvac.waterHeaterDaysPerYear.toSafeDouble().let {
                    if (it > 0) it else LebanonDefaults.DAYS_PER_YEAR.toDouble()
                }
                val yearlyKwh = powerKw * dailyHours * daysPerYear
                return buildResult("Water heating", yearlyKwh)
            }
            "Solar Heater" -> {
                // Only Electric backup counts toward electrical kWh
                if (hvac.solarWaterBackupType == "Electric") {
                    val backupPowerKw = LebanonDefaults.WATER_HEATER_DEFAULT_KW
                    val backupHours = hvac.solarWaterBackupHoursPerDay.toSafeDouble()
                    val yearlyKwh = backupPowerKw * backupHours * LebanonDefaults.DAYS_PER_YEAR
                    return buildResult("Water heating", yearlyKwh)
                }
                return emptyResult("Water heating")
            }
            // Gas and Fuel are non-electric — 0 kWh
            "Gas Tank" -> return emptyResult("Water heating")
            "Fuel Heating" -> return emptyResult("Water heating")
            else -> return emptyResult("Water heating")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared AC unit calculation
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateAcUnitYearlyKwh(unit: AcUnitInfo, buildingAge: String = ""): Double {
        val rawCapacity = unit.capacityValue.toSafeDouble()
        val capacityKw = when (unit.capacityUnit) {
            "BTU/hr" -> rawCapacity / LebanonDefaults.BTU_PER_KW
            "Tons" -> rawCapacity * 3.517
            else -> rawCapacity
        }

        val cop = when (unit.copMethod) {
            "I know the COP" -> unit.cop.toSafeDouble().let { if (it > 0) it else 3.0 }
            "I know the AC year" -> copFromAcAge(unit.acYear)
            else -> copFromBuildingAge(buildingAge)
        }

        val effectiveCapacityKw = if (capacityKw > 0) {
            capacityKw
        } else {
            estimateCapacityFromRoomSize(unit.roomSizeM2.toSafeDouble())
        }

        val electricalKw = if (cop > 0) effectiveCapacityKw / cop else 0.0
        val dailyHours = unit.dailyUsageHours.toSafeDouble()
        val daysPerYear = unit.daysPerYear.toSafeDouble()

        return electricalKw * dailyHours * daysPerYear
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Capacity estimation from room size
    // ─────────────────────────────────────────────────────────────────────────
    private fun estimateCapacityFromRoomSize(roomM2: Double): Double {
        val btuPerHour = when {
            roomM2 <= 22 -> 9000.0
            roomM2 <= 30 -> 12000.0
            roomM2 <= 45 -> 18000.0
            else -> 24000.0
        }
        return btuPerHour / LebanonDefaults.BTU_PER_KW
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COP estimation
    // ─────────────────────────────────────────────────────────────────────────
    private fun copFromAcAge(acAge: String): Double {
        return when (acAge) {
            "After 2020" -> 4.0
            "2015–2020" -> 3.5
            "2012–2015" -> 3.2
            "2000–2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    fun estimateCopFromAcAge(acAge: String): String {
        return "${copFromAcAge(acAge)} (estimated)"
    }

    private fun copFromBuildingAge(buildingAge: String): Double {
        return when (buildingAge) {
            "After 2020" -> 4.0
            "2015–2020" -> 3.5
            "2012–2015" -> 3.2
            "2000–2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lighting
    // ─────────────────────────────────────────────────────────────────────────
    private fun calculateLighting(survey: SurveyData): CategoryResult {
        val light = survey.lightingInfo ?: return emptyResult("Lighting")
        var totalYearlyKwh = 0.0

        // Direct lamps
        val directCount = light.numberOfDirectLamps.toIntOrNull() ?: 0
        if (directCount > 0 && light.directLampSamples.isNotEmpty()) {
            val lampsPerType = directCount.toDouble() / light.directLampSamples.size
            for (sample in light.directLampSamples) {
                val powerW = sample.powerWatts.toSafeDouble()
                val hours = sample.dailyUsageHours.toSafeDouble()
                totalYearlyKwh += (powerW * hours * lampsPerType) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
            }
        }

        // Indirect lighting (single installation)
        // Indirect lighting (per room)
        if (light.hasIndirectLighting) {
            for (room in light.indirectRooms) {
                val powerW = room.powerWatts.toSafeDouble()
                val hours = room.dailyUsageHours.toSafeDouble()
                totalYearlyKwh += (powerW * hours) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
            }
        }

        // Outdoor lamps
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