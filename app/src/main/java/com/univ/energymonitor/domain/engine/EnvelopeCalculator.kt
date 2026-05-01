package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.domain.model.WallLayerInfo

object EnvelopeCalculator {

    private const val INTERNAL_SURFACE_RESISTANCE = 0.12
    private const val EXTERNAL_SURFACE_RESISTANCE = 0.04

    private const val WALL_INSULATION_NEW_U_VALUE = 0.743
    private const val DOUBLE_GLAZING_U_VALUE = 2.5

    private const val WALL_SAVING_INDICATOR = 3.25
    private const val GLASS_SAVING_INDICATOR = 7.543

    data class RecommendedEnvelopeUValues(
        val wallU: Double,
        val windowU: Double
    )

    data class EnvelopeEfficiencyResult(
        val actualWallU: Double,
        val recommendedWallU: Double,
        val isWallEfficient: Boolean,
        val actualWindowU: Double,
        val recommendedWindowU: Double,
        val isWindowEfficient: Boolean
    )

    data class EnvelopeSavingResult(
        val title: String,
        val isRecommended: Boolean,
        val actualUValue: Double,
        val improvedUValue: Double,
        val deltaU: Double,
        val surfaceM2: Double,
        val yearlyKwhSaved: Double,
        val yearlyCostSavedUsd: Double,
        val yearlyCo2SavedKg: Double
    )

    fun calculateWindowToWallRatioPercent(
        glassSurfaceM2: String,
        exposedWallSurfaceM2: String
    ): Double {
        val glassSurface = glassSurfaceM2.trim().toDoubleOrNull() ?: return 0.0
        val exposedWallSurface = exposedWallSurfaceM2.trim().toDoubleOrNull() ?: return 0.0

        if (glassSurface < 0.0 || exposedWallSurface <= 0.0) return 0.0

        return round2((glassSurface / exposedWallSurface) * 100.0)
    }

    fun calculateWindowToWallRatioPercentFromSurvey(survey: SurveyData): Double {
        val house = survey.houseInfo ?: return 0.0

        return calculateWindowToWallRatioPercent(
            glassSurfaceM2 = house.glassSurfaceM2,
            exposedWallSurfaceM2 = house.exposedWallSurfaceM2
        )
    }

    fun climateZoneFromLocation(location: String): String {
        return when (location.trim()) {
            "Beirut", "Saida", "Tripoli" -> "Coastal"
            else -> "Coastal"
        }
    }

    fun recommendedUValuesFromSurvey(survey: SurveyData): RecommendedEnvelopeUValues {
        val house = survey.houseInfo ?: return RecommendedEnvelopeUValues(0.0, 0.0)

        val zone = climateZoneFromLocation(house.location)
        val wwrPercent = calculateWindowToWallRatioPercent(
            glassSurfaceM2 = house.glassSurfaceM2,
            exposedWallSurfaceM2 = house.exposedWallSurfaceM2
        )

        return recommendedUValues(zone, wwrPercent)
    }

    fun recommendedUValues(
        zone: String,
        wwrPercent: Double
    ): RecommendedEnvelopeUValues {
        return when (zone.trim()) {
            "Coastal" -> {
                when {
                    wwrPercent <= 35.0 -> RecommendedEnvelopeUValues(1.6, 4.0)
                    else -> RecommendedEnvelopeUValues(1.26, 3.3)
                }
            }

            "Western Mid Mountain" -> {
                when {
                    wwrPercent <= 25.0 -> RecommendedEnvelopeUValues(0.77, 4.0)
                    wwrPercent <= 35.0 -> RecommendedEnvelopeUValues(0.57, 3.3)
                    else -> RecommendedEnvelopeUValues(0.57, 2.6)
                }
            }

            "Inland Plateau" -> {
                when {
                    wwrPercent <= 25.0 -> RecommendedEnvelopeUValues(0.77, 4.0)
                    wwrPercent <= 35.0 -> RecommendedEnvelopeUValues(0.57, 3.3)
                    else -> RecommendedEnvelopeUValues(0.57, 2.6)
                }
            }

            "High Mountain" -> {
                when {
                    wwrPercent <= 15.0 -> RecommendedEnvelopeUValues(0.57, 4.0)
                    wwrPercent <= 25.0 -> RecommendedEnvelopeUValues(0.57, 3.3)
                    wwrPercent <= 35.0 -> RecommendedEnvelopeUValues(0.57, 2.6)
                    else -> RecommendedEnvelopeUValues(0.57, 1.9)
                }
            }

            else -> RecommendedEnvelopeUValues(0.0, 0.0)
        }
    }

    fun conductivityForMaterial(material: String): Double {
        return when (material.trim()) {
            "Pumice" -> 0.5
            "Cement" -> 1.4
            "Hollow Block" -> 0.95
            "Air" -> 0.026
            "Stone Block" -> 1.7
            "Brick" -> 0.72
            else -> 0.0
        }
    }

    fun calculateLayerResistance(layer: WallLayerInfo): Double {
        val thicknessCm = layer.thickness
            .replace("cm", "")
            .trim()
            .toDoubleOrNull()
            ?: return 0.0

        val thicknessMeters = thicknessCm / 100.0
        val conductivity = conductivityForMaterial(layer.material)

        if (thicknessMeters <= 0.0 || conductivity <= 0.0) return 0.0

        return round4(thicknessMeters / conductivity)
    }

    fun calculateWallTotalResistance(wallLayers: List<WallLayerInfo>): Double {
        val materialsResistance = wallLayers.sumOf { layer ->
            calculateLayerResistance(layer)
        }

        return round4(
            INTERNAL_SURFACE_RESISTANCE +
                    EXTERNAL_SURFACE_RESISTANCE +
                    materialsResistance
        )
    }

    fun calculateWallUValue(wallLayers: List<WallLayerInfo>): Double {
        val totalResistance = calculateWallTotalResistance(wallLayers)

        if (totalResistance <= 0.0) return 0.0

        return round3(1.0 / totalResistance)
    }

    fun calculateWallUValueFromSurvey(survey: SurveyData): Double {
        val house = survey.houseInfo ?: return 0.0

        return calculateWallUValue(house.wallLayers)
    }

    fun calculateWindowUValue(glassType: String): Double {
        return when (glassType.trim()) {
            "Single glazing" -> 5.9
            "Double glazing" -> 2.5
            "Triple glazing" -> 1.42
            else -> 0.0
        }
    }

    fun calculateWindowUValueFromSurvey(survey: SurveyData): Double {
        val house = survey.houseInfo ?: return 0.0

        return calculateWindowUValue(house.glassType)
    }

    fun evaluateEnvelopeEfficiencyFromSurvey(
        survey: SurveyData
    ): EnvelopeEfficiencyResult {
        val recommended = recommendedUValuesFromSurvey(survey)

        val actualWallU = calculateWallUValueFromSurvey(survey)
        val actualWindowU = calculateWindowUValueFromSurvey(survey)

        val isWallEfficient =
            actualWallU > 0.0 &&
                    recommended.wallU > 0.0 &&
                    actualWallU <= recommended.wallU

        val isWindowEfficient =
            actualWindowU > 0.0 &&
                    recommended.windowU > 0.0 &&
                    actualWindowU <= recommended.windowU

        return EnvelopeEfficiencyResult(
            actualWallU = actualWallU,
            recommendedWallU = recommended.wallU,
            isWallEfficient = isWallEfficient,
            actualWindowU = actualWindowU,
            recommendedWindowU = recommended.windowU,
            isWindowEfficient = isWindowEfficient
        )
    }

    fun calculateWallInsulationSavingsFromSurvey(
        survey: SurveyData
    ): EnvelopeSavingResult {
        val house = survey.houseInfo ?: return emptySavingResult("Wall insulation")

        val efficiency = evaluateEnvelopeEfficiencyFromSurvey(survey)

        val actualWallU = efficiency.actualWallU
        val deltaU = (actualWallU - WALL_INSULATION_NEW_U_VALUE).coerceAtLeast(0.0)

        val exposedWallSurface = house.exposedWallSurfaceM2
            .trim()
            .toDoubleOrNull()
            ?: 0.0

        val isRecommended =
            actualWallU > 0.0 &&
                    efficiency.recommendedWallU > 0.0 &&
                    actualWallU > efficiency.recommendedWallU &&
                    exposedWallSurface > 0.0 &&
                    deltaU > 0.0

        val yearlyKwhSaved = if (isRecommended) {
            WALL_SAVING_INDICATOR * exposedWallSurface * deltaU
        } else {
            0.0
        }

        val pricePerKwh = calculateWeightedElectricityPriceFromSurvey(survey)

        return EnvelopeSavingResult(
            title = "Wall insulation",
            isRecommended = isRecommended,
            actualUValue = round3(actualWallU),
            improvedUValue = WALL_INSULATION_NEW_U_VALUE,
            deltaU = round3(deltaU),
            surfaceM2 = round2(exposedWallSurface),
            yearlyKwhSaved = round2(yearlyKwhSaved),
            yearlyCostSavedUsd = round2(yearlyKwhSaved * pricePerKwh),
            yearlyCo2SavedKg = round2(
                yearlyKwhSaved *
                        LebanonDefaults.CO2_KG_PER_KWH *
                        calculateEdlAndGeneratorShareFromSurvey(survey)
            )

        )
    }

    fun calculateGlassSavingsFromSurvey(
        survey: SurveyData
    ): EnvelopeSavingResult {
        val house = survey.houseInfo ?: return emptySavingResult("Double glazing")

        val efficiency = evaluateEnvelopeEfficiencyFromSurvey(survey)

        val actualWindowU = efficiency.actualWindowU
        val deltaU = (actualWindowU - DOUBLE_GLAZING_U_VALUE).coerceAtLeast(0.0)

        val glassSurface = house.glassSurfaceM2
            .trim()
            .toDoubleOrNull()
            ?: 0.0

        val isRecommended =
            actualWindowU > 0.0 &&
                    efficiency.recommendedWindowU > 0.0 &&
                    actualWindowU > efficiency.recommendedWindowU &&
                    glassSurface > 0.0 &&
                    deltaU > 0.0

        val yearlyKwhSaved = if (isRecommended) {
            GLASS_SAVING_INDICATOR * glassSurface * deltaU
        } else {
            0.0
        }

        val pricePerKwh = calculateWeightedElectricityPriceFromSurvey(survey)

        return EnvelopeSavingResult(
            title = "Double glazing",
            isRecommended = isRecommended,
            actualUValue = round3(actualWindowU),
            improvedUValue = DOUBLE_GLAZING_U_VALUE,
            deltaU = round3(deltaU),
            surfaceM2 = round2(glassSurface),
            yearlyKwhSaved = round2(yearlyKwhSaved),
            yearlyCostSavedUsd = round2(yearlyKwhSaved * pricePerKwh),
            yearlyCo2SavedKg = round2(
                yearlyKwhSaved *
                        LebanonDefaults.CO2_KG_PER_KWH *
                        calculateEdlAndGeneratorShareFromSurvey(survey)
            )

        )
    }

    fun calculateEnvelopeSavingsFromSurvey(
        survey: SurveyData
    ): List<EnvelopeSavingResult> {
        return listOf(
            calculateWallInsulationSavingsFromSurvey(survey),
            calculateGlassSavingsFromSurvey(survey)
        )
    }

    private fun calculateWeightedElectricityPriceFromSurvey(
        survey: SurveyData
    ): Double {
        val consumption = survey.consumptionInfo ?: return 0.0

        val edlPrice = consumption.edlPricePerKwhUsd
            .trim()
            .toDoubleOrNull()
            ?: 0.0

        val generatorPrice = consumption.generatorPricePerKwhUsd
            .trim()
            .toDoubleOrNull()
            ?: 0.0

        val edlHours = consumption.edlHoursPerDay
            .trim()
            .toDoubleOrNull()
            ?.coerceIn(0.0, 24.0)
            ?: 0.0

        val generatorHours = if (consumption.usesGenerator) {
            24.0 - edlHours
        } else {
            0.0
        }

        val totalHours = edlHours + generatorHours

        if (totalHours <= 0.0) return when {
            edlPrice > 0.0 -> edlPrice
            generatorPrice > 0.0 -> generatorPrice
            else -> 0.0
        }

        val edlShare = edlHours / totalHours
        val generatorShare = generatorHours / totalHours

        return round4(
            edlShare * edlPrice +
                    generatorShare * generatorPrice
        )
    }

    private fun emptySavingResult(title: String): EnvelopeSavingResult {
        return EnvelopeSavingResult(
            title = title,
            isRecommended = false,
            actualUValue = 0.0,
            improvedUValue = 0.0,
            deltaU = 0.0,
            surfaceM2 = 0.0,
            yearlyKwhSaved = 0.0,
            yearlyCostSavedUsd = 0.0,
            yearlyCo2SavedKg = 0.0
        )
    }

    private fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }

    private fun round3(value: Double): Double {
        return Math.round(value * 1000.0) / 1000.0
    }

    private fun round4(value: Double): Double {
        return Math.round(value * 10000.0) / 10000.0
    }
}
private fun calculateEdlAndGeneratorShareFromSurvey(
    survey: SurveyData
): Double {
    val consumption = survey.consumptionInfo ?: return 1.0

    val edlHours = consumption.edlHoursPerDay
        .trim()
        .toDoubleOrNull()
        ?.coerceIn(0.0, 24.0)
        ?: 0.0

    val generatorHours = if (consumption.usesGenerator) {
        24.0 - edlHours
    } else {
        0.0
    }

    val totalHours = edlHours + generatorHours

    if (totalHours <= 0.0) return 1.0

    val edlShare = if (consumption.usesEdl) {
        edlHours / totalHours
    } else {
        0.0
    }

    val generatorShare = if (consumption.usesGenerator) {
        generatorHours / totalHours
    } else {
        0.0
    }

    return (edlShare + generatorShare).coerceIn(0.0, 1.0)
}
