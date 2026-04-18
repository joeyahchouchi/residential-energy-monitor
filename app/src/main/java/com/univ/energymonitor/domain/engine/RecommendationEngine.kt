package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.Recommendation
import com.univ.energymonitor.domain.model.RecommendationCategory
import com.univ.energymonitor.domain.model.RecommendationPriority
import com.univ.energymonitor.domain.model.SurveyData

object RecommendationEngine {

    fun generate(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()

        recommendations.addAll(analyzeEnvelope(survey))
        recommendations.addAll(analyzeCooling(survey, report))
        recommendations.addAll(analyzeHeating(survey, report))
        recommendations.addAll(analyzeWaterHeating(survey, report))
        recommendations.addAll(analyzeLighting(survey, report))
        recommendations.addAll(analyzeAppliances(survey, report))
        recommendations.addAll(analyzeRenewables(survey, report))

        // Sort by priority then by savings
        return recommendations.sortedWith(
            compareBy<Recommendation> { it.priority.ordinal }
                .thenByDescending { it.estimatedYearlyUsdSaved }
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // Building Envelope
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeEnvelope(survey: SurveyData): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val house = survey.houseInfo ?: return recs

        // Insulation check
        if (house.insulationLevel == "No") {
            recs.add(
                Recommendation(
                    id = "env_insulation",
                    title = "Add Thermal Insulation",
                    description = "Your home has no insulation. Adding proper wall and roof insulation can reduce heating and cooling loads by 25–40%, significantly lowering energy bills and improving comfort.",
                    category = RecommendationCategory.ENVELOPE,
                    priority = RecommendationPriority.HIGH,
                    icon = "🏠",
                    estimatedYearlyKwhSaved = 1500.0,
                    estimatedYearlyUsdSaved = 150.0,
                    estimatedYearlyCo2Saved = 750.0,
                    actionText = "Install insulation on exterior walls and roof",
                    standardReference = "LEEB 2025 – Building Envelope"
                )
            )
        }

        // Single glazing check
        if (house.glassType == "Single glazing") {
            recs.add(
                Recommendation(
                    id = "env_glazing",
                    title = "Upgrade to Double Glazing",
                    description = "Single-pane windows lose significant heat in winter and allow heat gain in summer. Double or triple glazing can reduce HVAC energy use by 15–25%.",
                    category = RecommendationCategory.ENVELOPE,
                    priority = RecommendationPriority.HIGH,
                    icon = "🪟",
                    estimatedYearlyKwhSaved = 800.0,
                    estimatedYearlyUsdSaved = 80.0,
                    estimatedYearlyCo2Saved = 400.0,
                    actionText = "Replace windows with double or triple glazing",
                    standardReference = "LEEB 2025 – Fenestration"
                )
            )
        }

        // Old building
        if (house.buildingAge == "Before 2000" || house.buildingAge == "2000–2012") {
            recs.add(
                Recommendation(
                    id = "env_old_building",
                    title = "Consider Building Envelope Audit",
                    description = "Older buildings often have poor insulation, air leaks, and inefficient windows. A professional energy audit can identify the most cost-effective upgrades for your specific building.",
                    category = RecommendationCategory.ENVELOPE,
                    priority = RecommendationPriority.MEDIUM,
                    icon = "🔍",
                    actionText = "Schedule a professional energy audit",
                    standardReference = "ISO 50001 – Energy Management"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Cooling
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeCooling(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs

        // High cooling percentage
        if (report.hvacCoolingPercent > 40) {
            recs.add(
                Recommendation(
                    id = "cool_high_usage",
                    title = "Reduce AC Usage",
                    description = "Cooling accounts for ${report.hvacCoolingPercent.toInt()}% of your total electricity use. Setting your AC to 24–26°C (instead of lower) can save 20–30% on cooling costs.",
                    category = RecommendationCategory.COOLING,
                    priority = RecommendationPriority.HIGH,
                    icon = "❄️",
                    estimatedYearlyKwhSaved = report.hvacCooling.yearlyKwh * 0.25,
                    estimatedYearlyUsdSaved = report.hvacCooling.yearlyCostUsd * 0.25,
                    estimatedYearlyCo2Saved = report.hvacCooling.yearlyCo2Kg * 0.25,
                    actionText = "Set AC temperature to 24–26°C",
                    standardReference = "ASHRAE 55 – Thermal Comfort"
                )
            )
        }

        // Old or inefficient AC units
        var oldAcCount = 0
        for (unit in hvac.acUnits) {
            val isOld = when (unit.copMethod) {
                "I know the COP" -> (unit.cop.toDoubleOrNull() ?: 3.0) < 3.0
                "I know the AC year" -> unit.acYear == "Before 2000" || unit.acYear == "2000–2012"
                else -> survey.houseInfo?.buildingAge == "Before 2000" ||
                        survey.houseInfo?.buildingAge == "2000–2012"
            }
            if (isOld) oldAcCount++
        }

        if (oldAcCount > 0) {
            recs.add(
                Recommendation(
                    id = "cool_old_ac",
                    title = "Upgrade to Inverter AC",
                    description = "You have $oldAcCount AC unit(s) that appear to be old or low-efficiency. Modern inverter ACs with COP ≥ 4.0 can reduce cooling electricity by 30–50%.",
                    category = RecommendationCategory.COOLING,
                    priority = RecommendationPriority.HIGH,
                    icon = "🌀",
                    estimatedYearlyKwhSaved = report.hvacCooling.yearlyKwh * 0.40,
                    estimatedYearlyUsdSaved = report.hvacCooling.yearlyCostUsd * 0.40,
                    estimatedYearlyCo2Saved = report.hvacCooling.yearlyCo2Kg * 0.40,
                    actionText = "Replace old AC units with inverter models (COP ≥ 4.0)",
                    standardReference = "IEC 60335-2-40"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Heating
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeHeating(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs

        if (hvac.heatingSystemType == "Electric Heater") {
            recs.add(
                Recommendation(
                    id = "heat_electric_to_ac",
                    title = "Switch to Heat Pump (AC Heating)",
                    description = "Electric resistance heaters convert 1 kWh of electricity into 1 kWh of heat. A heat pump (AC in heating mode) delivers 3–4 kWh of heat per kWh of electricity, saving up to 70%.",
                    category = RecommendationCategory.HEATING,
                    priority = RecommendationPriority.HIGH,
                    icon = "🔥",
                    estimatedYearlyKwhSaved = report.hvacHeating.yearlyKwh * 0.65,
                    estimatedYearlyUsdSaved = report.hvacHeating.yearlyCostUsd * 0.65,
                    estimatedYearlyCo2Saved = report.hvacHeating.yearlyCo2Kg * 0.65,
                    actionText = "Use AC in heating mode instead of electric heaters",
                    standardReference = "ASHRAE 90.1"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Water Heating
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeWaterHeating(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val hvac = survey.hvacInfo ?: return recs

        if (hvac.waterHeaterType == "Electrical Resistance") {
            recs.add(
                Recommendation(
                    id = "water_electric_to_solar",
                    title = "Install Solar Water Heater",
                    description = "Electrical resistance water heaters are among the highest energy consumers in a home. A solar water heater can provide 70–90% of your hot water needs for free, with a payback period of 3–5 years in Lebanon's sunny climate.",
                    category = RecommendationCategory.WATER_HEATING,
                    priority = RecommendationPriority.HIGH,
                    icon = "☀️",
                    estimatedYearlyKwhSaved = report.waterHeating.yearlyKwh * 0.80,
                    estimatedYearlyUsdSaved = report.waterHeating.yearlyCostUsd * 0.80,
                    estimatedYearlyCo2Saved = report.waterHeating.yearlyCo2Kg * 0.80,
                    actionText = "Install a 200–300L solar water heater",
                    standardReference = "LEEB 2025 – Renewable Energy"
                )
            )
        }

        if (hvac.waterTankInsulated == "No" && hvac.waterHeaterType != "None" && hvac.waterHeaterType.isNotBlank()) {
            recs.add(
                Recommendation(
                    id = "water_insulate_tank",
                    title = "Insulate Your Water Tank",
                    description = "An un-insulated water tank loses heat constantly, wasting energy. Adding a tank jacket or insulation reduces standby losses by 25–45%.",
                    category = RecommendationCategory.WATER_HEATING,
                    priority = RecommendationPriority.MEDIUM,
                    icon = "🛢️",
                    estimatedYearlyKwhSaved = report.waterHeating.yearlyKwh * 0.15,
                    estimatedYearlyUsdSaved = report.waterHeating.yearlyCostUsd * 0.15,
                    estimatedYearlyCo2Saved = report.waterHeating.yearlyCo2Kg * 0.15,
                    actionText = "Install tank insulation jacket",
                    standardReference = "ISO 50001"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Lighting
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeLighting(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val light = survey.lightingInfo ?: return recs

        // Count non-LED lamps
        var nonLedCount = 0
        for (sample in light.directLampSamples) {
            if (sample.bulbType != "LED") {
                val total = light.numberOfDirectLamps.toIntOrNull() ?: 0
                val types = light.directLampSamples.size
                if (types > 0) nonLedCount += total / types
            }
        }

        if (nonLedCount > 0) {
            recs.add(
                Recommendation(
                    id = "light_switch_to_led",
                    title = "Replace Non-LED Bulbs with LED",
                    description = "You have approximately $nonLedCount non-LED bulbs. LEDs use 80–90% less electricity than incandescent and last 15× longer. This is the fastest payback upgrade in any home.",
                    category = RecommendationCategory.LIGHTING,
                    priority = RecommendationPriority.HIGH,
                    icon = "💡",
                    estimatedYearlyKwhSaved = report.lighting.yearlyKwh * 0.75,
                    estimatedYearlyUsdSaved = report.lighting.yearlyCostUsd * 0.75,
                    estimatedYearlyCo2Saved = report.lighting.yearlyCo2Kg * 0.75,
                    actionText = "Replace all incandescent/halogen/CFL bulbs with LED",
                    standardReference = "LEEB 2025 – Lighting Efficiency"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Appliances
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeAppliances(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        if (report.appliancesPercent > 30) {
            recs.add(
                Recommendation(
                    id = "app_high_usage",
                    title = "Review Appliance Efficiency",
                    description = "Appliances account for ${report.appliancesPercent.toInt()}% of your electricity. When replacing appliances, choose A++ or A+++ energy-rated models. Also consider unplugging devices on standby.",
                    category = RecommendationCategory.APPLIANCES,
                    priority = RecommendationPriority.MEDIUM,
                    icon = "🔌",
                    estimatedYearlyKwhSaved = report.appliances.yearlyKwh * 0.15,
                    estimatedYearlyUsdSaved = report.appliances.yearlyCostUsd * 0.15,
                    estimatedYearlyCo2Saved = report.appliances.yearlyCo2Kg * 0.15,
                    actionText = "Replace old appliances with A++ rated models",
                    standardReference = "IEC 62087"
                )
            )
        }

        return recs
    }

    // ─────────────────────────────────────────────────────────────────────
    // Renewables
    // ─────────────────────────────────────────────────────────────────────
    private fun analyzeRenewables(survey: SurveyData, report: EnergyReport): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()
        val consumption = survey.consumptionInfo ?: return recs

        // No solar yet
        if (!consumption.usesSolar && report.totalYearlyKwh > 3000) {
            recs.add(
                Recommendation(
                    id = "ren_install_solar",
                    title = "Install Solar PV System",
                    description = "Lebanon has excellent solar potential (~1800 kWh/m²/year). A 5 kW solar PV system can generate 7000–8000 kWh/year, covering most of your electricity needs with a 4–6 year payback.",
                    category = RecommendationCategory.RENEWABLE,
                    priority = RecommendationPriority.HIGH,
                    icon = "☀️",
                    estimatedYearlyKwhSaved = report.totalYearlyKwh * 0.70,
                    estimatedYearlyUsdSaved = report.totalYearlyCostUsd * 0.70,
                    estimatedYearlyCo2Saved = report.totalYearlyCo2Kg * 0.70,
                    actionText = "Get a solar PV quote (3–5 kW recommended)",
                    standardReference = "LEEB 2025 – Renewable Energy"
                )
            )
        }

        return recs
    }
}