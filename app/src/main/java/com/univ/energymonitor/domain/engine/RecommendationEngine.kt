package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.Recommendation
import com.univ.energymonitor.domain.model.RecommendationCategory
import com.univ.energymonitor.domain.model.RecommendationPriority
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.domain.model.AcUnitInfo

object RecommendationEngine {
    private const val TARGET_COOLING_AC_COP = 3.3
    private const val TARGET_HEATING_AC_COP = 3.71
    private const val MIN_ACCEPTABLE_AC_COP = 3.0
    private const val COOLING_COP_SAVING_INDICATOR = 4.67
    private const val HEATING_AC_COP_SAVING_INDICATOR = 1.14


    fun generate(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        recommendations.addAll(analyzeEnvelope(survey, report))
        recommendations.addAll(analyzeCooling(survey, report))
        recommendations.addAll(analyzeHeating(survey, report))
        recommendations.addAll(analyzeWaterHeating(survey, report))
        recommendations.addAll(analyzeLighting(survey, report))
        recommendations.addAll(analyzeApplianceLabels(survey, report))
        recommendations.addAll(analyzeRenewables(survey, report))

        val appliedIds = survey.appliedRecommendationIds.toSet()

        return recommendations
            .filter { it.id !in appliedIds }
            .sortedWith(
                compareBy<Recommendation> { it.priority.ordinal }
                    .thenByDescending { it.estimatedYearlyUsdSaved }
            )

    }

    private fun electricCo2PerKwh(survey: SurveyData): Double {
        return EnergyCostCalculator.computeCost(
            totalCalculatedKwh = 1.0,
            consumption = survey.consumptionInfo
        ).weightedAvgCo2KgPerKwh
    }

    // ─────────────────────────────────────────────────────────────────────
    // Building Envelope
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeEnvelope(
        survey: SurveyData,
        report: EnergyReport
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        survey.houseInfo ?: return recs

        val wallSaving = EnvelopeCalculator.calculateWallInsulationSavingsFromSurvey(survey)
        val glassSaving = EnvelopeCalculator.calculateGlassSavingsFromSurvey(survey)

        if (wallSaving.isRecommended) {
            recs.add(
                Recommendation(
                    id = "env_wall_insulation",
                    title = "Add wall insulation layer",
                    description = buildString {
                        append("Your wall U-value is ${"%.2f".format(wallSaving.actualUValue)} W/m²·K, ")
                        append("which is higher than the recommended value. ")
                        append("Adding a gypsum board and EPS insulation layer can reduce the wall U-value to ")
                        append("${"%.2f".format(wallSaving.improvedUValue)} W/m²·K. ")
                        append("Estimated saving: ${"%.1f".format(wallSaving.yearlyKwhSaved)} kWh/year.")
                    },
                    category = RecommendationCategory.ENVELOPE,
                    priority = RecommendationPriority.HIGH,
                    icon = "🏠",
                    estimatedYearlyKwhSaved = wallSaving.yearlyKwhSaved,
                    estimatedYearlyUsdSaved = wallSaving.yearlyCostSavedUsd,
                    estimatedYearlyCo2Saved = wallSaving.yearlyCo2SavedKg,
                    actionText = "Add EPS insulation and gypsum board layer",
                    standardReference = "LEEB 2025 - Building Envelope"
                )
            )
        }

        if (glassSaving.isRecommended) {
            recs.add(
                Recommendation(
                    id = "env_double_glazing",
                    title = "Upgrade to double glazing",
                    description = buildString {
                        append("Your window U-value is ${"%.2f".format(glassSaving.actualUValue)} W/m²·K, ")
                        append("which is higher than the recommended value. ")
                        append("Replacing single glazing with double glazing can reduce the window U-value to ")
                        append("${"%.2f".format(glassSaving.improvedUValue)} W/m²·K. ")
                        append("Estimated saving: ${"%.1f".format(glassSaving.yearlyKwhSaved)} kWh/year.")
                    },
                    category = RecommendationCategory.ENVELOPE,
                    priority = RecommendationPriority.HIGH,
                    icon = "🪟",
                    estimatedYearlyKwhSaved = glassSaving.yearlyKwhSaved,
                    estimatedYearlyUsdSaved = glassSaving.yearlyCostSavedUsd,
                    estimatedYearlyCo2Saved = glassSaving.yearlyCo2SavedKg,
                    actionText = "Replace single glazing with double glazing",
                    standardReference = "LEEB 2025 - Fenestration"
                )
            )
        }

        return recs
    }
    private data class AcCopUpgradeSaving(
        val lowCopCount: Int,
        val savedKwh: Double,
        val avgCurrentCop: Double
    )

    private fun calculateAcCopUpgradeSaving(
        units: List<AcUnitInfo>,
        buildingAge: String,
        targetCop: Double,
        indicator: Double,
        maxCategoryKwh: Double
    ): AcCopUpgradeSaving? {
        val lowCopUnits = units.mapNotNull { unit ->
            val actualCop = resolveAcCop(unit, buildingAge)
            val roomSizeM2 = unit.roomSizeM2.toSafeDouble()

            if (actualCop > 0.0 &&
                actualCop < MIN_ACCEPTABLE_AC_COP &&
                roomSizeM2 > 0.0
            ) {
                actualCop to roomSizeM2
            } else {
                null
            }
        }

        if (lowCopUnits.isEmpty()) return null

        val rawSavedKwh = lowCopUnits.sumOf { (actualCop, roomSizeM2) ->
            indicator * roomSizeM2 * (targetCop - actualCop)
        }

        val savedKwh = rawSavedKwh
            .coerceAtLeast(0.0)
            .coerceAtMost(maxCategoryKwh)

        if (savedKwh <= 0.0) return null

        return AcCopUpgradeSaving(
            lowCopCount = lowCopUnits.size,
            savedKwh = savedKwh,
            avgCurrentCop = lowCopUnits.map { it.first }.average()
        )
    }

    private fun resolveAcCop(unit: AcUnitInfo, buildingAge: String): Double {
        return when (unit.copMethod) {
            "I know the COP" -> unit.cop.toSafeDouble()
            "I know the AC year" -> copFromAcAge(unit.acYear)
            else -> copFromBuildingAge(buildingAge)
        }
    }

    private fun copFromAcAge(acAge: String): Double {
        return when (acAge) {
            "After 2020" -> 4.0
            "2015–2020", "2015â€“2020" -> 3.5
            "2012–2015", "2012â€“2015" -> 3.2
            "2000–2012", "2000â€“2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    private fun copFromBuildingAge(buildingAge: String): Double {
        return when (buildingAge) {
            "After 2020" -> 4.0
            "2015–2020", "2015â€“2020" -> 3.5
            "2012–2015", "2012â€“2015" -> 3.2
            "2000–2012", "2000â€“2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Cooling
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeCooling(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs

        val coolingCopSaving = calculateAcCopUpgradeSaving(
            units = hvac.acUnits,
            buildingAge = survey.houseInfo?.buildingAge ?: "",
            targetCop = TARGET_COOLING_AC_COP,
            indicator = COOLING_COP_SAVING_INDICATOR,
            maxCategoryKwh = report.hvacCooling.yearlyKwh
        )

        if (coolingCopSaving != null) {
            val savedKwh = coolingCopSaving.savedKwh

            recs.add(
                Recommendation(
                    id = "cool_ac_cop_upgrade",
                    title = "Upgrade low-COP cooling AC units",
                    description = "You have ${coolingCopSaving.lowCopCount} cooling AC unit(s) with COP below 3.0. Upgrading them to COP 3.3 can save about ${"%.1f".format(savedKwh)} kWh/year based on room size and the cooling indicator.",
                    category = RecommendationCategory.COOLING,
                    priority = RecommendationPriority.HIGH,
                    icon = "AC",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                    estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                    actionText = "Replace low-COP AC units with models having COP 3.3 or higher",
                    standardReference = "AC efficiency indicator - cooling"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Heating
    // ─────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────
// Heating
    private fun analyzeHeating(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs

        if (hvac.heatingSystemType == "Electric Heater") {
            val savedKwh = report.hvacHeating.yearlyKwh * 0.65

            recs.add(
                Recommendation(
                    id = "heat_electric_to_ac",
                    title = "Switch to Heat Pump (AC Heating)",
                    description = "Electric resistance heaters convert 1 kWh of electricity into 1 kWh of heat. A heat pump delivers 3-4 kWh of heat per kWh of electricity, saving up to 70%.",
                    category = RecommendationCategory.HEATING,
                    priority = RecommendationPriority.HIGH,
                    icon = "Heat",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                    estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                    actionText = "Use AC in heating mode instead of electric heaters",
                    standardReference = "ASHRAE 90.1"
                )
            )
        }

        if (hvac.heatingSystemType == "AC") {
            val heatingCopSaving = calculateAcCopUpgradeSaving(
                units = hvac.heatingAcUnits,
                buildingAge = survey.houseInfo?.buildingAge ?: "",
                targetCop = TARGET_HEATING_AC_COP,
                indicator = HEATING_AC_COP_SAVING_INDICATOR,
                maxCategoryKwh = report.hvacHeating.yearlyKwh
            )

            if (heatingCopSaving != null) {
                val savedKwh = heatingCopSaving.savedKwh

                recs.add(
                    Recommendation(
                        id = "heat_ac_cop_upgrade",
                        title = "Upgrade low-COP heating AC units",
                        description = "You have ${heatingCopSaving.lowCopCount} heating AC unit(s) with COP below 3.0. Upgrading them to COP 3.71 can save about ${"%.1f".format(savedKwh)} kWh/year based on heated room size and the heating indicator.",
                        category = RecommendationCategory.HEATING,
                        priority = RecommendationPriority.HIGH,
                        icon = "AC",
                        estimatedYearlyKwhSaved = savedKwh,
                        estimatedYearlyUsdSaved = savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                        estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                        actionText = "Replace low-COP heating AC units with models having COP 3.71 or higher",
                        standardReference = "AC efficiency indicator - heating"
                    )
                )
            }
        }

        val efficiencyUpgrade = calculateHeatingEfficiencyUpgrade(survey, report)

        if (efficiencyUpgrade != null) {
            recs.add(
                Recommendation(
                    id = "heat_efficiency_upgrade",
                    title = "Upgrade ${efficiencyUpgrade.systemName} efficiency",
                    description = buildString {
                        append("Your ${efficiencyUpgrade.systemName.lowercase()} efficiency is estimated at ")
                        append("${"%.0f".format(efficiencyUpgrade.currentEfficiency * 100)}%. ")
                        append("The recommended target from the heating Excel is ")
                        append("${"%.0f".format(efficiencyUpgrade.targetEfficiency * 100)}%. ")
                        append("Based on the heated area, this upgrade can reduce heating energy by about ")
                        append("${"%.1f".format(efficiencyUpgrade.percentReduction)}%, saving approximately ")
                        append("${"%.1f".format(efficiencyUpgrade.savedKwh)} kWh/year.")
                    },
                    category = RecommendationCategory.HEATING,
                    priority = RecommendationPriority.MEDIUM,
                    icon = "Heat",
                    estimatedYearlyKwhSaved = efficiencyUpgrade.savedKwh,
                    estimatedYearlyUsdSaved = efficiencyUpgrade.savedCostUsd,
                    estimatedYearlyCo2Saved = efficiencyUpgrade.savedCo2Kg,
                    actionText = efficiencyUpgrade.actionText,
                    standardReference = "Heating Excel - efficiency target"
                )
            )
        }

        return recs
    }

    private data class HeatingEfficiencyUpgrade(
        val systemName: String,
        val currentEfficiency: Double,
        val targetEfficiency: Double,
        val savedKwh: Double,
        val savedCostUsd: Double,
        val savedCo2Kg: Double,
        val percentReduction: Double,
        val actionText: String
    )

    private fun calculateHeatingEfficiencyUpgrade(
        survey: SurveyData,
        report: EnergyReport
    ): HeatingEfficiencyUpgrade? {
        val hvac = survey.hvacInfo ?: return null
        val heatingType = hvac.heatingSystemType

        if (heatingType !in listOf("Electric Heater", "Gas Heater", "Diesel/Fuel Heater")) {
            return null
        }

        val currentEfficiency = resolveHeatingEfficiencyForRecommendation(
            heatingType = heatingType,
            method = hvac.heatingEfficiencyMethod,
            efficiencyPercent = hvac.heatingEfficiencyPercent,
            installationYear = hvac.heatingInstallationYear
        )

        val targetEfficiency = when (heatingType) {
            "Electric Heater" -> 0.85
            "Gas Heater" -> 0.82
            "Diesel/Fuel Heater" -> 0.82
            else -> return null
        }

        if (currentEfficiency >= targetEfficiency) return null

        val heatedArea = hvac.heatedAreaM2.toDoubleOrNull()
        val heatingDemandKwhPerM2Year = 3.0

        val currentInputKwh: Double
        val improvedInputKwh: Double

        if (heatedArea != null && heatedArea > 0.0) {
            currentInputKwh = heatingDemandKwhPerM2Year * heatedArea / currentEfficiency
            improvedInputKwh = heatingDemandKwhPerM2Year * heatedArea / targetEfficiency
        } else {
            if (report.hvacHeating.yearlyKwh <= 0.0) return null

            val usefulHeatingKwh = when (heatingType) {
                "Electric Heater" -> report.hvacHeating.yearlyKwh * currentEfficiency
                "Gas Heater", "Diesel/Fuel Heater" -> report.hvacHeating.yearlyKwh
                else -> return null
            }

            currentInputKwh = usefulHeatingKwh / currentEfficiency
            improvedInputKwh = usefulHeatingKwh / targetEfficiency
        }

        val savedKwh = currentInputKwh - improvedInputKwh
        if (savedKwh <= 0.0) return null

        val costPerKwh = when (heatingType) {
            "Electric Heater" -> EnergyCostCalculator.pricePerKwh(survey.consumptionInfo)
            "Gas Heater" -> LebanonDefaults.GAS_HEATING_COST_USD_PER_KWH
            "Diesel/Fuel Heater" -> LebanonDefaults.DIESEL_HEATING_COST_USD_PER_KWH
            else -> 0.0
        }

        val co2PerKwh = when (heatingType) {
            "Electric Heater" -> electricCo2PerKwh(survey)
            "Gas Heater" -> LebanonDefaults.GAS_HEATING_CO2_KG_PER_KWH
            "Diesel/Fuel Heater" -> LebanonDefaults.DIESEL_HEATING_CO2_KG_PER_KWH
            else -> 0.0
        }

        val systemName = when (heatingType) {
            "Electric Heater" -> "electric heating system"
            "Gas Heater" -> "gas heating system"
            "Diesel/Fuel Heater" -> "diesel/fuel heating system"
            else -> "heating system"
        }

        val actionText = when (heatingType) {
            "Electric Heater" -> "Upgrade to a higher-efficiency electric heating system or heat pump"
            "Gas Heater" -> "Upgrade gas heater to a modern high-efficiency model"
            "Diesel/Fuel Heater" -> "Upgrade diesel/fuel heater to a modern high-efficiency model"
            else -> "Upgrade heating system efficiency"
        }

        return HeatingEfficiencyUpgrade(
            systemName = systemName,
            currentEfficiency = currentEfficiency,
            targetEfficiency = targetEfficiency,
            savedKwh = savedKwh,
            savedCostUsd = savedKwh * costPerKwh,
            savedCo2Kg = savedKwh * co2PerKwh,
            percentReduction = savedKwh / currentInputKwh * 100.0,
            actionText = actionText
        )
    }

    private fun resolveHeatingEfficiencyForRecommendation(
        heatingType: String,
        method: String,
        efficiencyPercent: String,
        installationYear: String
    ): Double {
        return when (method) {
            "I know the efficiency" -> {
                val percent = efficiencyPercent.toSafeDouble()
                if (percent > 0.0) {
                    (percent / 100.0).coerceIn(0.01, 1.0)
                } else {
                    defaultHeatingEfficiencyForRecommendation(heatingType)
                }
            }

            "I know the installation year" -> {
                efficiencyFromHeatingInstallationYearForRecommendation(
                    heatingType = heatingType,
                    installationYear = installationYear
                )
            }

            "I don't know" -> defaultHeatingEfficiencyForRecommendation(heatingType)

            else -> defaultHeatingEfficiencyForRecommendation(heatingType)
        }
    }

    private fun defaultHeatingEfficiencyForRecommendation(heatingType: String): Double {
        return when (heatingType) {
            "Electric Heater" -> 0.75
            "Gas Heater" -> 0.72
            "Diesel/Fuel Heater" -> 0.72
            else -> 1.0
        }
    }

    private fun efficiencyFromHeatingInstallationYearForRecommendation(
        heatingType: String,
        installationYear: String
    ): Double {
        val year = installationYear.trim()

        return when (heatingType) {
            "Electric Heater" -> when (year) {
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
                else -> 0.75
            }

            "Gas Heater", "Diesel/Fuel Heater" -> when (year) {
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
                else -> 0.72
            }

            else -> 1.0
        }
    }


    // ─────────────────────────────────────────────────────────────────────
    // Water Heating
    // ─────────────────────────────────────────────────────────────────────
    private data class SolarWaterHeaterSavingFactors(
        val idSuffix: String,
        val energyKwhPerLiter: Double,
        val co2KgPerLiter: Double,
        val costUsdPerLiter: Double,
        val description: String
    )

    private fun analyzeWaterHeating(
        survey: SurveyData,
        report: EnergyReport
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs
        val house = survey.houseInfo

        val electricWaterHeaters = hvac.waterHeaters.filter { it.type == "Electrical Resistance" }
        val gasWaterHeaters = hvac.waterHeaters.filter { it.type == "Gas Tank" }
        val solarWaterHeaters = hvac.waterHeaters.filter { it.type == "Solar Heater" }

        val hasElectricResistance = electricWaterHeaters.isNotEmpty()
        val hasGasTank = gasWaterHeaters.isNotEmpty()
        val hasSolarWaterHeating = solarWaterHeaters.isNotEmpty()

        if (!hasSolarWaterHeating && (hasElectricResistance || hasGasTank)) {
            val tankSizeLiters = when {
                hasElectricResistance -> electricWaterHeaters.first().tankSizeLiters.toTankSizeLiters()
                hasGasTank -> gasWaterHeaters.first().tankSizeLiters.toTankSizeLiters()
                else -> 0.0
            }

            if (tankSizeLiters > 0.0) {
            val factors = when {
                hasElectricResistance && hasGasTank -> SolarWaterHeaterSavingFactors(
                    idSuffix = "mixed_electric_gas",
                    energyKwhPerLiter = 8.12,
                    co2KgPerLiter = 5.07,
                    costUsdPerLiter = 0.8246,
                    description = "You use both electric resistance and gas-tank water heating. Installing a solar water heater can reduce the annual energy, cost, and CO2 impact of hot-water production."
                )

                hasGasTank -> SolarWaterHeaterSavingFactors(
                    idSuffix = "gas",
                    energyKwhPerLiter = 4.548,
                    co2KgPerLiter = 2.0,
                    costUsdPerLiter = 0.483,
                    description = "You use gas-tank water heating. Installing a solar water heater can reduce gas use and lower yearly hot-water emissions and cost."
                )

                else -> SolarWaterHeaterSavingFactors(
                    idSuffix = "electric",
                    energyKwhPerLiter = 5.59,
                    co2KgPerLiter = 3.82,
                    costUsdPerLiter = 0.55,
                    description = "You use electric resistance water heating. Installing a solar water heater can reduce yearly electricity use, CO2 emissions, and hot-water cost."
                )
            }

            val savedKwh = factors.energyKwhPerLiter * tankSizeLiters
            val savedCo2Kg = factors.co2KgPerLiter * tankSizeLiters
            val savedCostUsd = factors.costUsdPerLiter * tankSizeLiters
            recs.add(
                Recommendation(
                    id = "water_${factors.idSuffix}_to_solar",
                    title = "Switch to a solar water heater",
                    description = buildString {
                        append(factors.description)
                        append(" Based on an installed SWH tank size of ${"%.0f".format(tankSizeLiters)} L, ")
                        append("estimated saving is ${"%.1f".format(savedKwh)} kWh/year, ")
                        append("${"%.1f".format(savedCo2Kg)} kg CO2/year, and ")
                        append("$${"%.1f".format(savedCostUsd)}/year.")
                    },
                    category = RecommendationCategory.WATER_HEATING,
                    priority = RecommendationPriority.HIGH,
                    icon = "☀️",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedCostUsd,
                    estimatedYearlyCo2Saved = savedCo2Kg,
                    actionText = "Install a solar water heater sized around ${"%.0f".format(tankSizeLiters)} L",
                    standardReference = "LEEB 2005 – Renewable Water Heating"
                )
            )
            }
        }

        solarWaterHeaters.forEachIndexed { index, heater ->
            val panelLength = heater.solarPanelLengthMeters.toDoubleOrNull() ?: 0.0
            val panelWidth = heater.solarPanelWidthMeters.toDoubleOrNull() ?: 0.0
            val panelArea = panelLength * panelWidth
            val occupants = house?.numberOfOccupants?.toIntOrNull() ?: 0

            val analysis = SolarWaterHeating.analyze(
                panelAreaM2 = panelArea,
                occupants = occupants,
                backupType = heater.solarBackupType,
                backupHoursPerDay = heater.solarBackupHoursPerDay.toDoubleOrNull() ?: 0.0
            )

            if (analysis != null) {
                when (analysis.recommendation) {
                    SolarWaterHeating.SolarRecommendation.NEED_BACKUP -> {
                        recs.add(
                            Recommendation(
                                id = "solar_need_backup_${index + 1}",
                                title = "Add a backup system to Water Heater ${index + 1}",
                                description = buildString {
                                    append("Water Heater ${index + 1} has a solar panel area of ${"%.2f".format(panelArea)} m². ")
                                    append("It supplies about ${"%.1f".format(analysis.supplyKwhPerDay)} kWh/day, ")
                                    append("while the household needs ${"%.1f".format(analysis.demandKwhPerDay)} kWh/day. ")
                                    append("A backup system is recommended to avoid hot-water shortages.")
                                },
                                category = RecommendationCategory.WATER_HEATING,
                                priority = RecommendationPriority.HIGH,
                                icon = "🔥",
                                estimatedYearlyKwhSaved = 0.0,
                                estimatedYearlyUsdSaved = 0.0,
                                estimatedYearlyCo2Saved = 0.0,
                                actionText = "Install an electric backup heater (~1.5 kW)",
                                standardReference = "LEEB 2005 – Solar Sizing Guidelines"
                            )
                        )
                    }

                    SolarWaterHeating.SolarRecommendation.NEED_AUXILIARY -> {
                        val deficit = analysis.demandKwhPerDay - analysis.totalSupplyKwhPerDay
                        recs.add(
                            Recommendation(
                                id = "solar_need_auxiliary_${index + 1}",
                                title = "Water Heater ${index + 1} is undersized",
                                description = buildString {
                                    append("Water Heater ${index + 1} solar supply is ${"%.1f".format(analysis.supplyKwhPerDay)} kWh/day. ")
                                    append("Backup adds ${"%.1f".format(analysis.backupKwhPerDay)} kWh/day. ")
                                    append("Combined supply is ${"%.1f".format(analysis.totalSupplyKwhPerDay)} kWh/day, ")
                                    append("while household demand is ${"%.1f".format(analysis.demandKwhPerDay)} kWh/day. ")
                                    append("The deficit is ${"%.1f".format(deficit)} kWh/day.")
                                },
                                category = RecommendationCategory.WATER_HEATING,
                                priority = RecommendationPriority.MEDIUM,
                                icon = "⚠️",
                                estimatedYearlyKwhSaved = 0.0,
                                estimatedYearlyUsdSaved = 0.0,
                                estimatedYearlyCo2Saved = 0.0,
                                actionText = "Add an auxiliary heater or expand the solar/backup system",
                                standardReference = "LEEB 2005 – Solar Sizing Guidelines"
                            )
                        )
                    }

                    SolarWaterHeating.SolarRecommendation.NONE -> Unit
                }
            }
        }

        hvac.waterHeaters
            .filter { it.type != "None" && it.type.isNotBlank() && it.tankInsulated == "No" }
            .forEachIndexed { index, _ ->
                val savedKwh = report.waterHeating.yearlyKwh * 0.10
                if (savedKwh > 50) {
                    recs.add(
                        Recommendation(
                            id = "water_insulate_tank_${index + 1}",
                            title = "Insulate Water Heater ${index + 1} tank",
                            description = "An uninsulated hot-water tank loses 10–15% of its heat to the surrounding air. Adding an insulation jacket improves efficiency and reduces standby losses.",
                            category = RecommendationCategory.WATER_HEATING,
                            priority = RecommendationPriority.MEDIUM,
                            icon = "🧥",
                            estimatedYearlyKwhSaved = savedKwh,
                            estimatedYearlyUsdSaved = savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                            estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                            actionText = "Wrap the tank in an insulating jacket",
                            standardReference = "LEEB 2005 – Hot Water Storage"
                        )
                    )
                }
            }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Lighting
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeLighting(
        survey: SurveyData,
        report: EnergyReport
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val light = survey.lightingInfo ?: return recs

        val pricePerKwh = EnergyCostCalculator.pricePerKwh(survey.consumptionInfo)

        var nonLedCount = 0

        for (sample in light.directLampSamples) {
            if (sample.bulbType != "LED") {
                val total = light.numberOfDirectLamps.toIntOrNull() ?: 0
                val types = light.directLampSamples.size

                if (types > 0) {
                    nonLedCount += total / types
                }
            }
        }

        if (light.hasOutdoorLighting) {
            for (lamp in light.outdoorLamps) {
                if (lamp.bulbType != "LED") {
                    nonLedCount += 1
                }
            }
        }

        if (nonLedCount > 0) {
            val ledSavingPercent = 0.41
            val savedKwh = report.lighting.yearlyKwh * ledSavingPercent

            recs.add(
                Recommendation(
                    id = "light_switch_to_led",
                    title = "Replace Non-LED Bulbs with LED",
                    description = buildString {
                        append("You have approximately $nonLedCount non-LED bulbs. ")
                        append("Replacing them with LED bulbs can reduce lighting energy use by about 41%. ")
                        append("Estimated saving: ${"%.1f".format(savedKwh)} kWh/year.")
                    },
                    category = RecommendationCategory.LIGHTING,
                    priority = RecommendationPriority.HIGH,
                    icon = "💡",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedKwh * pricePerKwh,
                    estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                    actionText = "Replace non-LED bulbs with LED bulbs",
                    standardReference = "Lighting Excel - LED replacement saving"
                )
            )
        }

        val houseAreaM2 = survey.houseInfo?.totalAreaM2?.toDoubleOrNull() ?: 0.0
        val installedIndoorLightingWatts = calculateIndoorLightingPowerWatts(survey)
        val lpdLimitWPerM2 = 7.5

        if (houseAreaM2 > 0.0 && installedIndoorLightingWatts > 0.0) {
            val lightingPowerDensity = installedIndoorLightingWatts / houseAreaM2

            if (lightingPowerDensity > lpdLimitWPerM2) {
                val excessReductionPercent =
                    ((lightingPowerDensity - lpdLimitWPerM2) / lightingPowerDensity).coerceIn(0.0, 1.0)

                val savedKwh = report.lighting.yearlyKwh * excessReductionPercent

                recs.add(
                    Recommendation(
                        id = "light_reduce_lpd",
                        title = "Reduce lighting power density",
                        description = buildString {
                            append("Your indoor lighting power density is ")
                            append("${"%.1f".format(lightingPowerDensity)} W/m². ")
                            append("According to LEEB, it should stay below 7.5 W/m². ")
                            append("Reducing unnecessary lighting power or replacing high-wattage lamps can save about ")
                            append("${"%.1f".format(savedKwh)} kWh/year.")
                        },
                        category = RecommendationCategory.LIGHTING,
                        priority = if (lightingPowerDensity >= 10.0) {
                            RecommendationPriority.HIGH
                        } else {
                            RecommendationPriority.MEDIUM
                        },
                        icon = "💡",
                        estimatedYearlyKwhSaved = savedKwh,
                        estimatedYearlyUsdSaved = savedKwh * pricePerKwh,
                        estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                        actionText = "Keep indoor lighting power below 7.5 W/m²",
                        standardReference = "LEEB - Lighting Power Density limit"
                    )
                )
            }
        }

        val hasDirectLighting = light.numberOfDirectLamps.toIntOrNull().orZero() > 0

        val hasAnyLighting =
            hasDirectLighting ||
                    light.hasIndirectLighting ||
                    light.hasOutdoorLighting

        val shouldRecommendSensors = hasAnyLighting

        if (shouldRecommendSensors) {
            val sensorSavingPercent = 0.394
            val savedKwh = report.lighting.yearlyKwh * sensorSavingPercent

            val sensorTypes = mutableListOf<String>()

            if (hasDirectLighting) {
                sensorTypes.add("occupancy or MmWave sensors for indoor rooms")
            }

            if (light.hasOutdoorLighting) {
                sensorTypes.add("outdoor PIR sensors for balcony or exterior lighting")
            }

            if (light.hasIndirectLighting) {
                sensorTypes.add("daylight sensors where natural light is available")
            }

            recs.add(
                Recommendation(
                    id = "light_install_sensors",
                    title = "Install lighting control sensors",
                    description = buildString {
                        append("Lighting controls can reduce unnecessary operating hours, even before replacing bulbs. ")
                        append("Using ${sensorTypes.joinToString(", ")} can reduce lighting consumption by about 39.4%. ")
                        append("Estimated saving: ${"%.1f".format(savedKwh)} kWh/year.")
                    },
                    category = RecommendationCategory.LIGHTING,
                    priority = RecommendationPriority.MEDIUM,
                    icon = "📡",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedKwh * pricePerKwh,
                    estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                    actionText = "Install PIR, MmWave, outdoor PIR, or daylight sensors",
                    standardReference = "Lighting control sensor optimization"
                )
            )
        }

        return recs
    }

    private fun calculateIndoorLightingPowerWatts(survey: SurveyData): Double {
        val light = survey.lightingInfo ?: return 0.0

        val directLampCount = light.numberOfDirectLamps.toIntOrNull() ?: 0
        val directSampleWatts = light.directLampSamples
            .mapNotNull { it.powerWatts.toDoubleOrNull() }
            .filter { it > 0.0 }

        val directLightingWatts =
            if (directLampCount > 0 && directSampleWatts.isNotEmpty()) {
                directSampleWatts.average() * directLampCount
            } else {
                0.0
            }

        val indirectLightingWatts =
            if (light.hasIndirectLighting) {
                light.indirectRooms.sumOf { it.powerWatts.toDoubleOrNull() ?: 0.0 }
            } else {
                0.0
            }

        return directLightingWatts + indirectLightingWatts
    }

    private fun Int?.orZero(): Int {
        return this ?: 0
    }

    // ─────────────────────────────────────────────────────────────────────
    // Per-Appliance Efficiency Label Recommendations
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeApplianceLabels(
        survey: SurveyData,
        report: EnergyReport
    ): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val app = survey.applianceInfo ?: return recs

        for (appliance in app.appliances) {
            if (!appliance.exists) continue

            val (effectiveLabel, wasInferred) = ApplianceEfficiencyTable.resolveLabel(
                applianceName = appliance.name,
                chosenLabel = appliance.efficiencyLabel,
                purchaseYear = appliance.purchaseYear
            )

            if (effectiveLabel.isBlank()) continue

            val upgrade = ApplianceEfficiencyTable.recommendUpgrade(
                applianceName = appliance.name,
                currentLabel = effectiveLabel,
                wasInferredFromYear = wasInferred
            ) ?: continue

            val priority = if (upgrade.currentLabel in listOf("D", "E", "F", "G")) {
                RecommendationPriority.HIGH
            } else {
                RecommendationPriority.MEDIUM
            }

            val targetLabelText =
                if (
                    appliance.name.trim().lowercase() !in listOf("tv", "television", "electric oven", "oven") &&
                    upgrade.currentLabel == "C"
                ) {
                    "B or A"
                } else {
                    upgrade.targetLabel
                }

            val description = buildString {
                append("Your ${appliance.name.lowercase()} is rated Class ${upgrade.currentLabel}")

                if (wasInferred && appliance.purchaseYear.isNotBlank()) {
                    append(" (estimated from purchase year ${appliance.purchaseYear})")
                }

                append(". Recommended upgrade: Class $targetLabelText. ")
                append("Estimated reduction if upgraded to Class ${upgrade.targetLabel}: ")
                append("${"%.1f".format(upgrade.percentReduction)}% ")
                append("(${"%.1f".format(upgrade.savedKwh)} kWh/year saved).")
            }


            recs.add(
                Recommendation(
                    id = "appliance_upgrade_${appliance.name.lowercase().replace(" ", "_")}",
                    title = "Upgrade your ${appliance.name} (Class ${upgrade.currentLabel} → $targetLabelText)",
                    description = description,
                    category = RecommendationCategory.APPLIANCES,
                    priority = priority,
                    icon = iconForAppliance(appliance.name),
                    estimatedYearlyKwhSaved = upgrade.savedKwh,
                    estimatedYearlyUsdSaved = upgrade.savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                    estimatedYearlyCo2Saved = upgrade.savedKwh * electricCo2PerKwh(survey),
                    actionText = "Replace with a Class $targetLabelText ${appliance.name.lowercase()}",
                    standardReference = "LEEB 2005 – Appliance Efficiency"
                )
            )

        }

        return recs
    }

    private fun iconForAppliance(name: String): String = when (name.trim().lowercase()) {
        "fridge", "refrigerator" -> "🧊"
        "washing machine" -> "🧺"
        "dishwasher" -> "🍽️"
        "tv", "television" -> "📺"
        "electric oven", "oven" -> "🔥"
        else -> "🔌"
    }

    // ─────────────────────────────────────────────────────────────────────
    // Renewables
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeRenewables(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val consumption = survey.consumptionInfo ?: return recs

        if (!consumption.usesSolar && report.totalYearlyKwh > 3000) {
            val savedKwh = report.totalYearlyKwh * 0.70
            recs.add(
                Recommendation(
                    id = "ren_install_solar",
                    title = "Install Solar PV System",
                    description = "Lebanon has excellent solar potential (~1800 kWh/m²/year). A 5 kW solar PV system can generate 7000–8000 kWh/year, covering most of your electricity needs with a 4–6 year payback.",
                    category = RecommendationCategory.RENEWABLE,
                    priority = RecommendationPriority.HIGH,
                    icon = "☀️",
                    estimatedYearlyKwhSaved = savedKwh,
                    estimatedYearlyUsdSaved = savedKwh * EnergyCostCalculator.pricePerKwh(survey.consumptionInfo),
                    estimatedYearlyCo2Saved = savedKwh * electricCo2PerKwh(survey),
                    actionText = "Get a solar PV quote (3–5 kW recommended)",
                    standardReference = "LEEB 2025 – Renewable Energy"
                )
            )
        }

        return recs
    }
}
private fun String.toSafeDouble(): Double {
    return this.trim().toDoubleOrNull() ?: 0.0
}

private fun String.toTankSizeLiters(): Double {
    return this
        .replace("L", "", ignoreCase = true)
        .trim()
        .toDoubleOrNull() ?: 0.0
}
