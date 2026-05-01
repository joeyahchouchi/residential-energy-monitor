package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.CategoryResult
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData

object LebanonDefaults {
    const val CO2_KG_PER_KWH = 0.684
    const val DAYS_PER_YEAR = 365
    const val BTU_PER_KW = 3412.14
    const val WATER_HEATER_DEFAULT_KW = 1.5

    const val SOLAR_PANEL_REFERENCE_AREA_M2 = 1.79
    const val SOLAR_PANEL_REFERENCE_KWH_PER_DAY = 4.65
    const val HOT_WATER_LITERS_PER_PERSON_PER_DAY = 35.0
    const val HOT_WATER_KWH_PER_PERSON_PER_DAY = 1.63

    const val GAS_TANK_KG = 10.0
    const val LPG_PCI_KWH_PER_KG = 12.64
    const val GAS_WATER_HEATER_EFFICIENCY = 0.87
    const val LPG_CO2_G_PER_KWH = 495.5

    const val ELECTRIC_HEATING_DEFAULT_EFFICIENCY = 0.75
    const val GAS_HEATING_DEFAULT_EFFICIENCY = 0.72
    const val DIESEL_HEATING_DEFAULT_EFFICIENCY = 0.72

    const val GAS_HEATING_KWH_PER_KG = 12.64
    const val GAS_HEATING_COST_USD_PER_KWH = 0.0913
    const val GAS_HEATING_CO2_KG_PER_KWH = 0.4955

    const val DIESEL_TANK_LITERS = 20.0
    const val DIESEL_KWH_PER_LITER = 8.0
    const val DIESEL_HEATING_COST_USD_PER_KWH = 0.0925
    const val DIESEL_HEATING_CO2_KG_PER_KWH = 0.35
}

object EnergyCalculator {

    private data class WaterHeatingBreakdown(
        val result: CategoryResult,
        val electricYearlyKwh: Double,
        val gasCostUsd: Double,
        val gasCo2Kg: Double
    )

    fun calculate(survey: SurveyData): EnergyReport {
        val hvacCooling = calculateHvacCooling(survey)
        val hvacHeating = calculateHvacHeating(survey)
        val waterHeatingBreakdown = calculateWaterHeating(survey)
        val waterHeating = waterHeatingBreakdown.result
        val lighting = calculateLighting(survey)
        val appliances = calculateAppliances(survey)

        val totalYearlyKwh = hvacCooling.yearlyKwh +
                hvacHeating.yearlyKwh +
                waterHeating.yearlyKwh +
                lighting.yearlyKwh +
                appliances.yearlyKwh

        val totalDailyKwh = totalYearlyKwh / LebanonDefaults.DAYS_PER_YEAR

        val electricHeatingKwh = if (isElectricHeating(survey)) {
            hvacHeating.yearlyKwh
        } else {
            0.0
        }

        val totalElectricYearlyKwh = hvacCooling.yearlyKwh +
                electricHeatingKwh +
                waterHeatingBreakdown.electricYearlyKwh +
                lighting.yearlyKwh +
                appliances.yearlyKwh

        val costBreakdown = EnergyCostCalculator.computeCost(
            totalCalculatedKwh = totalElectricYearlyKwh,
            consumption = survey.consumptionInfo
        )

        val pricePerKwh = costBreakdown.weightedAvgPricePerKwh
        val co2PerKwh = costBreakdown.weightedAvgCo2KgPerKwh

        val hvacCoolingCosted = applyElectricCostAndCo2(hvacCooling, pricePerKwh, co2PerKwh)

        val hvacHeatingCosted = if (isElectricHeating(survey)) {
            applyElectricCostAndCo2(hvacHeating, pricePerKwh, co2PerKwh)
        } else {
            hvacHeating
        }

        val waterHeatingCosted = waterHeating.copy(
            yearlyCostUsd = round2(
                waterHeatingBreakdown.electricYearlyKwh * pricePerKwh +
                        waterHeatingBreakdown.gasCostUsd
            ),
            yearlyCo2Kg = round2(
                waterHeatingBreakdown.electricYearlyKwh * co2PerKwh +
                        waterHeatingBreakdown.gasCo2Kg
            )
        )

        val lightingCosted = applyElectricCostAndCo2(lighting, pricePerKwh, co2PerKwh)
        val appliancesCosted = applyElectricCostAndCo2(appliances, pricePerKwh, co2PerKwh)

        val totalYearlyCostUsd = hvacCoolingCosted.yearlyCostUsd +
                hvacHeatingCosted.yearlyCostUsd +
                waterHeatingCosted.yearlyCostUsd +
                lightingCosted.yearlyCostUsd +
                appliancesCosted.yearlyCostUsd

        val totalYearlyCo2Kg = hvacCoolingCosted.yearlyCo2Kg +
                hvacHeatingCosted.yearlyCo2Kg +
                waterHeatingCosted.yearlyCo2Kg +
                lightingCosted.yearlyCo2Kg +
                appliancesCosted.yearlyCo2Kg

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
            hvacCooling = hvacCoolingCosted,
            hvacHeating = hvacHeatingCosted,
            waterHeating = waterHeatingCosted,
            lighting = lightingCosted,
            appliances = appliancesCosted,
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

    private fun isElectricHeating(survey: SurveyData): Boolean {
        val type = survey.hvacInfo?.heatingSystemType ?: return false
        return type == "AC" || type == "Electric Heater"
    }

    private fun applyElectricCostAndCo2(
        result: CategoryResult,
        pricePerKwh: Double,
        co2PerKwh: Double
    ): CategoryResult {
        return result.copy(
            yearlyCostUsd = round2(result.yearlyKwh * pricePerKwh),
            yearlyCo2Kg = round2(result.yearlyKwh * co2PerKwh)
        )
    }

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

    private fun calculateHvacHeating(survey: SurveyData): CategoryResult {
        val hvac = survey.hvacInfo ?: return emptyResult("HVAC heating")

        if (hvac.heatingSystemType == "None" || hvac.heatingSystemType.isBlank()) {
            return emptyResult("HVAC heating")
        }

        return when (hvac.heatingSystemType) {
            "AC" -> {
                if (hvac.heatingAcUnits.isEmpty()) return emptyResult("HVAC heating")

                val buildingAge = survey.houseInfo?.buildingAge ?: ""
                var totalYearlyKwh = 0.0

                for (unit in hvac.heatingAcUnits) {
                    totalYearlyKwh += calculateAcUnitYearlyKwh(unit, buildingAge)
                }

                buildResult("HVAC heating", totalYearlyKwh)
            }

            "Electric Heater" -> {
                val numUnits = hvac.numberOfHeatingUnits.toSafeDouble()
                val powerKw = hvac.heatingPowerKw.toSafeDouble()
                val dailyHours = hvac.heatingDailyUsageHours.toSafeDouble()
                val daysPerYear = hvac.heatingDaysPerYear.toSafeDouble()
                val yearlyKwh = numUnits * powerKw * dailyHours * daysPerYear

                buildResult("HVAC heating", yearlyKwh)
            }

            "Gas Heater" -> {
                val efficiency = resolveHeatingEfficiency(
                    heatingType = hvac.heatingSystemType,
                    method = hvac.heatingEfficiencyMethod,
                    efficiencyPercent = hvac.heatingEfficiencyPercent,
                    installationYear = hvac.heatingInstallationYear
                )

                calculateGasHeating(
                    gasKgPerYearInput = hvac.heatingGasKgPerYear,
                    efficiency = efficiency
                )
            }

            "Diesel/Fuel Heater" -> {
                val efficiency = resolveHeatingEfficiency(
                    heatingType = hvac.heatingSystemType,
                    method = hvac.heatingEfficiencyMethod,
                    efficiencyPercent = hvac.heatingEfficiencyPercent,
                    installationYear = hvac.heatingInstallationYear
                )

                calculateDieselHeating(
                    tanksPerYearInput = hvac.heatingFuelLitersPerYear,
                    efficiency = efficiency
                )
            }

            else -> emptyResult("HVAC heating")
        }
    }

    private fun calculateGasHeating(
        gasKgPerYearInput: String,
        efficiency: Double
    ): CategoryResult {
        val gasKgPerYear = gasKgPerYearInput.toSafeDouble()
        val totalFuelEnergyKwh = gasKgPerYear * LebanonDefaults.GAS_HEATING_KWH_PER_KG
        val usefulHeatingKwh = totalFuelEnergyKwh * efficiency

        return CategoryResult(
            name = "HVAC heating",
            dailyKwh = round2(usefulHeatingKwh / LebanonDefaults.DAYS_PER_YEAR),
            yearlyKwh = round2(usefulHeatingKwh),
            yearlyCostUsd = round2(totalFuelEnergyKwh * LebanonDefaults.GAS_HEATING_COST_USD_PER_KWH),
            yearlyCo2Kg = round2(totalFuelEnergyKwh * LebanonDefaults.GAS_HEATING_CO2_KG_PER_KWH)
        )
    }

    private fun calculateDieselHeating(
        tanksPerYearInput: String,
        efficiency: Double
    ): CategoryResult {
        val tanksPerYear = tanksPerYearInput.toSafeDouble()
        val litersPerYear = tanksPerYear * LebanonDefaults.DIESEL_TANK_LITERS
        val totalFuelEnergyKwh = litersPerYear * LebanonDefaults.DIESEL_KWH_PER_LITER
        val usefulHeatingKwh = totalFuelEnergyKwh * efficiency

        return CategoryResult(
            name = "HVAC heating",
            dailyKwh = round2(usefulHeatingKwh / LebanonDefaults.DAYS_PER_YEAR),
            yearlyKwh = round2(usefulHeatingKwh),
            yearlyCostUsd = round2(totalFuelEnergyKwh * LebanonDefaults.DIESEL_HEATING_COST_USD_PER_KWH),
            yearlyCo2Kg = round2(totalFuelEnergyKwh * LebanonDefaults.DIESEL_HEATING_CO2_KG_PER_KWH)
        )
    }

    private fun resolveHeatingEfficiency(
        heatingType: String,
        method: String,
        efficiencyPercent: String,
        installationYear: String
    ): Double {
        return when (method) {
            "I know the efficiency" -> {
                val percent = efficiencyPercent.toSafeDouble()
                if (percent > 0) {
                    (percent / 100.0).coerceIn(0.01, 1.0)
                } else {
                    defaultHeatingEfficiency(heatingType)
                }
            }

            "I know the installation year" -> {
                efficiencyFromInstallationYear(
                    heatingType = heatingType,
                    installationYear = installationYear
                )
            }

            "I don't know" -> defaultHeatingEfficiency(heatingType)

            else -> defaultHeatingEfficiency(heatingType)
        }
    }

    private fun defaultHeatingEfficiency(heatingType: String): Double {
        return when (heatingType) {
            "Electric Heater" -> LebanonDefaults.ELECTRIC_HEATING_DEFAULT_EFFICIENCY
            "Gas Heater" -> LebanonDefaults.GAS_HEATING_DEFAULT_EFFICIENCY
            "Diesel/Fuel Heater" -> LebanonDefaults.DIESEL_HEATING_DEFAULT_EFFICIENCY
            else -> 1.0
        }
    }

    private fun efficiencyFromInstallationYear(
        heatingType: String,
        installationYear: String
    ): Double {
        val normalizedYear = installationYear.trim()

        return when (heatingType) {
            "Electric Heater" -> when (normalizedYear) {
                "2021–2026", "2021-2026", "2020" -> 0.85
                "2019" -> 0.84
                "2018" -> 0.83
                "2017" -> 0.82
                "2016" -> 0.81
                "2015" -> 0.80
                "2014" -> 0.79
                "2013" -> 0.78
                "2012" -> 0.77
                "2011" -> 0.76
                "2010", "Before 2010" -> 0.75
                else -> LebanonDefaults.ELECTRIC_HEATING_DEFAULT_EFFICIENCY
            }

            "Gas Heater", "Diesel/Fuel Heater" -> when (normalizedYear) {
                "2021–2026", "2021-2026", "2020" -> 0.82
                "2019" -> 0.81
                "2018" -> 0.80
                "2017" -> 0.79
                "2016" -> 0.78
                "2015" -> 0.77
                "2014" -> 0.76
                "2013" -> 0.75
                "2012" -> 0.74
                "2011" -> 0.73
                "2010", "Before 2010" -> 0.72
                else -> defaultHeatingEfficiency(heatingType)
            }

            else -> defaultHeatingEfficiency(heatingType)
        }
    }

    private fun calculateWaterHeating(survey: SurveyData): WaterHeatingBreakdown {
        val hvac = survey.hvacInfo ?: return emptyWaterHeatingBreakdown()
        if (hvac.waterHeaters.isEmpty()) return emptyWaterHeatingBreakdown()

        var totalYearlyKwh = 0.0
        var electricYearlyKwh = 0.0
        var totalYearlyCo2Kg = 0.0
        var gasCostUsd = 0.0
        var gasCo2Kg = 0.0

        hvac.waterHeaters.forEach { heater ->
            when (heater.type) {
                "Electrical Resistance" -> {
                    val powerKw = heater.powerKw.toSafeDouble().let {
                        if (it > 0) it else LebanonDefaults.WATER_HEATER_DEFAULT_KW
                    }
                    val dailyHours = heater.dailyHours.toSafeDouble()
                    val daysPerYear = heater.daysPerYear.toSafeDouble().let {
                        if (it > 0) it else LebanonDefaults.DAYS_PER_YEAR.toDouble()
                    }
                    val yearlyKwh = powerKw * dailyHours * daysPerYear

                    totalYearlyKwh += yearlyKwh
                    electricYearlyKwh += yearlyKwh
                    totalYearlyCo2Kg += yearlyKwh * LebanonDefaults.CO2_KG_PER_KWH
                }

                "Solar Heater" -> {
                    if (heater.solarBackupType == "Electric") {
                        val backupPowerKw = LebanonDefaults.WATER_HEATER_DEFAULT_KW
                        val backupHours = heater.solarBackupHoursPerDay.toSafeDouble()
                        val yearlyKwh = backupPowerKw * backupHours * LebanonDefaults.DAYS_PER_YEAR

                        totalYearlyKwh += yearlyKwh
                        electricYearlyKwh += yearlyKwh
                        totalYearlyCo2Kg += yearlyKwh * LebanonDefaults.CO2_KG_PER_KWH
                    }
                }

                "Gas Tank" -> {
                    val usefulYearlyKwh = calculateGasWaterHeaterUsefulKwhPerYear(
                        heater.gasTankCountPerYear
                    )
                    val gasHeaterCo2 = calculateGasWaterHeaterCo2KgPerYear(
                        heater.gasTankCountPerYear
                    )

                    totalYearlyKwh += usefulYearlyKwh
                    totalYearlyCo2Kg += gasHeaterCo2
                    gasCo2Kg += gasHeaterCo2

                    gasCostUsd += calculateGasWaterHeaterCostUsdPerYear(
                        heater.gasTankCountPerYear,
                        heater.gasTankCostUsd
                    )
                }
            }
        }

        val safeYearly = if (totalYearlyKwh > 0) totalYearlyKwh else 0.0

        return WaterHeatingBreakdown(
            result = CategoryResult(
                name = "Water heating",
                dailyKwh = round2(safeYearly / LebanonDefaults.DAYS_PER_YEAR),
                yearlyKwh = round2(safeYearly),
                yearlyCostUsd = round2(gasCostUsd),
                yearlyCo2Kg = round2(totalYearlyCo2Kg)
            ),
            electricYearlyKwh = electricYearlyKwh,
            gasCostUsd = gasCostUsd,
            gasCo2Kg = gasCo2Kg
        )
    }

    fun calculateSolarPanelAreaM2(lengthMeters: String, widthMeters: String): Double {
        val length = lengthMeters.toSafeDouble()
        val width = widthMeters.toSafeDouble()
        return if (length > 0 && width > 0) length * width else 0.0
    }

    fun calculateSolarHotWaterDailyKwh(lengthMeters: String, widthMeters: String): Double {
        val panelArea = calculateSolarPanelAreaM2(lengthMeters, widthMeters)
        val kwhPerSquareMeter =
            LebanonDefaults.SOLAR_PANEL_REFERENCE_KWH_PER_DAY /
                    LebanonDefaults.SOLAR_PANEL_REFERENCE_AREA_M2

        return panelArea * kwhPerSquareMeter
    }

    fun calculateHotWaterDemandDailyKwh(numberOfOccupants: String): Double {
        val occupants = numberOfOccupants.toSafeDouble()
        return if (occupants > 0) {
            occupants * LebanonDefaults.HOT_WATER_KWH_PER_PERSON_PER_DAY
        } else {
            0.0
        }
    }

    fun calculateSolarBackupDailyKwh(survey: SurveyData): Double {
        val hvac = survey.hvacInfo ?: return 0.0

        return hvac.waterHeaters.sumOf { heater ->
            if (heater.type == "Solar Heater" && heater.solarBackupType == "Electric") {
                LebanonDefaults.WATER_HEATER_DEFAULT_KW * heater.solarBackupHoursPerDay.toSafeDouble()
            } else {
                0.0
            }
        }
    }

    fun calculateGasKgPerYear(numberOfTanksPerYear: String): Double {
        val tanks = numberOfTanksPerYear.toSafeDouble()
        return tanks * LebanonDefaults.GAS_TANK_KG
    }

    fun calculateGasWaterHeaterTotalKwhPerYear(numberOfTanksPerYear: String): Double {
        val gasKgPerYear = calculateGasKgPerYear(numberOfTanksPerYear)
        return gasKgPerYear * LebanonDefaults.LPG_PCI_KWH_PER_KG
    }

    fun calculateGasWaterHeaterUsefulKwhPerYear(numberOfTanksPerYear: String): Double {
        val totalEnergyKwhPerYear = calculateGasWaterHeaterTotalKwhPerYear(numberOfTanksPerYear)
        return totalEnergyKwhPerYear * LebanonDefaults.GAS_WATER_HEATER_EFFICIENCY
    }

    fun calculateGasWaterHeaterUsefulKwhPerDay(numberOfTanksPerYear: String): Double {
        return calculateGasWaterHeaterUsefulKwhPerYear(numberOfTanksPerYear) /
                LebanonDefaults.DAYS_PER_YEAR
    }

    fun calculateGasWaterHeaterCostUsdPerYear(
        numberOfTanksPerYear: String,
        tankCostUsd: String
    ): Double {
        val tanks = numberOfTanksPerYear.toSafeDouble()
        val costPerTank = tankCostUsd.toSafeDouble()
        return tanks * costPerTank
    }

    fun calculateGasWaterHeaterCo2KgPerYear(numberOfTanksPerYear: String): Double {
        val gasKwhPerYear = calculateGasWaterHeaterTotalKwhPerYear(numberOfTanksPerYear)
        return gasKwhPerYear * LebanonDefaults.LPG_CO2_G_PER_KWH * 0.001
    }

    private fun calculateAcUnitYearlyKwh(unit: AcUnitInfo, buildingAge: String = ""): Double {
        val rawCapacity = unit.capacityValue.toSafeDouble()

        val normalizedUnit = unit.capacityUnit
            .trim()
            .lowercase()
            .replace(" ", "")

        val capacityKw = when (normalizedUnit) {
            "btu/h", "btu/hr", "btuh", "btu" -> rawCapacity / LebanonDefaults.BTU_PER_KW
            "tons", "ton" -> rawCapacity * 3.517
            "kw", "kilowatt", "kilowatts" -> rawCapacity
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

    private fun estimateCapacityFromRoomSize(roomM2: Double): Double {
        val btuPerHour = when {
            roomM2 <= 22 -> 9000.0
            roomM2 <= 30 -> 12000.0
            roomM2 <= 45 -> 18000.0
            else -> 24000.0
        }
        return btuPerHour / LebanonDefaults.BTU_PER_KW
    }

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

    private fun calculateLighting(survey: SurveyData): CategoryResult {
        val light = survey.lightingInfo ?: return emptyResult("Lighting")
        var totalYearlyKwh = 0.0

        val directCount = light.numberOfDirectLamps.toIntOrNull() ?: 0
        if (directCount > 0 && light.directLampSamples.isNotEmpty()) {
            val lampsPerType = directCount.toDouble() / light.directLampSamples.size
            for (sample in light.directLampSamples) {
                val powerW = sample.powerWatts.toSafeDouble()
                val hours = sample.dailyUsageHours.toSafeDouble()
                totalYearlyKwh += (powerW * hours * lampsPerType) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
            }
        }

        if (light.hasIndirectLighting) {
            for (room in light.indirectRooms) {
                val lengthMeters = room.lengthMeters.toSafeDouble()
                val powerWPerMeter = room.powerWatts.toSafeDouble()
                val hours = room.dailyUsageHours.toSafeDouble()

                totalYearlyKwh +=
                    (powerWPerMeter * lengthMeters * hours) /
                            1000.0 * LebanonDefaults.DAYS_PER_YEAR
            }
        }

        for (lamp in light.outdoorLamps) {
            val powerW = lamp.powerWatts.toSafeDouble()
            val hours = lamp.dailyUsageHours.toSafeDouble()
            totalYearlyKwh += (powerW * hours) / 1000.0 * LebanonDefaults.DAYS_PER_YEAR
        }

        return buildResult("Lighting", totalYearlyKwh)
    }

    private fun calculateAppliances(survey: SurveyData): CategoryResult {
        val app = survey.applianceInfo ?: return emptyResult("Appliances")

        var totalDailyWh = 0.0

        for (appliance in app.appliances) {
            if (appliance.exists) {
                val powerW = appliance.powerWatts.toSafeDouble()
                val hours = appliance.dailyUsageHours.toSafeDouble()

                val dutyFactor = when (appliance.name.trim().lowercase()) {
                    "fridge", "refrigerator" -> 0.35
                    else -> 1.0
                }

                totalDailyWh += powerW * hours * dutyFactor
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

    private fun buildResult(name: String, yearlyKwh: Double): CategoryResult {
        val safeYearly = if (yearlyKwh > 0) yearlyKwh else 0.0
        return CategoryResult(
            name = name,
            dailyKwh = round2(safeYearly / LebanonDefaults.DAYS_PER_YEAR),
            yearlyKwh = round2(safeYearly),
            yearlyCostUsd = 0.0,
            yearlyCo2Kg = round2(safeYearly * LebanonDefaults.CO2_KG_PER_KWH)
        )
    }

    private fun emptyWaterHeatingBreakdown(): WaterHeatingBreakdown {
        return WaterHeatingBreakdown(
            result = emptyResult("Water heating"),
            electricYearlyKwh = 0.0,
            gasCostUsd = 0.0,
            gasCo2Kg = 0.0
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
