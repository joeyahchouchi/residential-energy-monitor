package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.engine.EnergyCostCalculator
import com.univ.energymonitor.domain.engine.LebanonDefaults
import com.univ.energymonitor.domain.engine.RecommendationEngine
import com.univ.energymonitor.domain.model.*
import com.univ.energymonitor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OptimizationScreen(
    surveyData: SurveyData,
    report: EnergyReport,
    houseName: String,
    onBack: () -> Unit,
    onPreviewOnly: (EnergyReport) -> Unit,
    onApplyToSavedHome: (SurveyData, EnergyReport) -> Unit
) {
    val recommendations = remember(surveyData, report) {
        RecommendationEngine.generate(surveyData, report)
    }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val selectedRecommendations = recommendations.filter { it.id in selectedIds }

    val optimizedSurveyData = remember(selectedIds, surveyData) {
        applySurveyOptimizations(surveyData, selectedRecommendations)
    }

    val optimizedReport = remember(selectedIds, optimizedSurveyData, report) {
        if (selectedRecommendations.isEmpty()) report
        else buildOptimizedReport(report, selectedRecommendations, surveyData)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Want to Optimize?", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text(houseName, fontSize = 12.sp, color = TextGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OptimizedSummaryCard(current = report, optimized = optimizedReport)

            Text(
                "Select upgrades below. You can preview the result only, or apply it if the upgrades were actually done.",
                fontSize = 12.sp,
                color = TextGray,
                lineHeight = 17.sp
            )

            if (recommendations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        "No optimization recommendations available for this home.",
                        modifier = Modifier.padding(18.dp),
                        color = TextGray
                    )
                }
            } else {
                recommendations.forEach { rec ->
                    OptimizationRecommendationCard(
                        recommendation = rec,
                        selected = rec.id in selectedIds,
                        onToggle = {
                            selectedIds = if (rec.id in selectedIds) {
                                selectedIds - rec.id
                            } else {
                                selectedIds + rec.id
                            }
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = { onPreviewOnly(optimizedReport) },
                enabled = selectedRecommendations.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
            ) {
                Text("PREVIEW RESULT ONLY", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onApplyToSavedHome(optimizedSurveyData, optimizedReport) },
                enabled = selectedRecommendations.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("APPLY TO SAVED HOME", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun OptimizedSummaryCard(
    current: EnergyReport,
    optimized: EnergyReport
) {
    val savedKwh = (current.totalYearlyKwh - optimized.totalYearlyKwh).coerceAtLeast(0.0)
    val savedUsd = (current.totalYearlyCostUsd - optimized.totalYearlyCostUsd).coerceAtLeast(0.0)
    val savedCo2 = (current.totalYearlyCo2Kg - optimized.totalYearlyCo2Kg).coerceAtLeast(0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Optimized Result", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                "%.0f kWh/year".format(optimized.totalYearlyKwh),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Potential savings: %.0f kWh/year · $%.0f/year · %.0f kg CO₂/year".format(
                    savedKwh,
                    savedUsd,
                    savedCo2
                ),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun OptimizationRecommendationCard(
    recommendation: Recommendation,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) GreenSurface else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(recommendation.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                Spacer(Modifier.height(4.dp))
                Text(recommendation.description, fontSize = 12.sp, color = TextGray, lineHeight = 17.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Saves about %.0f kWh/year · $%.0f/year · %.0f kg CO₂/year".format(
                        recommendation.estimatedYearlyKwhSaved,
                        recommendation.estimatedYearlyUsdSaved,
                        recommendation.estimatedYearlyCo2Saved
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

private fun applySurveyOptimizations(
    surveyData: SurveyData,
    recommendations: List<Recommendation>
): SurveyData {
    var updated = surveyData

    recommendations.forEach { rec ->
        when (rec.id) {
            "env_wall_insulation" -> {
                updated.houseInfo?.let { house ->
                    updated = updated.copy(
                        houseInfo = house.copy(insulationLevel = "Yes")
                    )
                }
            }

            "env_double_glazing" -> {
                updated.houseInfo?.let { house ->
                    updated = updated.copy(
                        houseInfo = house.copy(glassType = "Double glazing")
                    )
                }
            }

            "water_electric_to_solar",
            "water_gas_to_solar",
            "water_mixed_electric_gas_to_solar" -> {
                updated.hvacInfo?.let { hvac ->
                    val updatedHeaters = hvac.waterHeaters.map { heater ->
                        if (heater.type == "Electrical Resistance" || heater.type == "Gas Tank") {
                            heater.copy(
                                type = "Solar Heater",
                                solarBackupType = "Electric",
                                solarBackupHoursPerDay = "0.5"
                            )
                        } else {
                            heater
                        }
                    }

                    updated = updated.copy(
                        hvacInfo = hvac.copy(
                            waterHeaters = updatedHeaters,
                            numberOfWaterHeaters = updatedHeaters.size.toString()
                        )
                    )
                }
            }

            "light_switch_to_led" -> {
                updated.lightingInfo?.let { lighting ->
                    updated = updated.copy(
                        lightingInfo = lighting.copy(
                            directLampSamples = lighting.directLampSamples.map {
                                it.copy(
                                    bulbType = "LED",
                                    powerWatts = "9"
                                )
                            },
                            outdoorLamps = lighting.outdoorLamps.map {
                                it.copy(
                                    bulbType = "LED",
                                    powerWatts = "9"
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    val appliedIds = (updated.appliedRecommendationIds + recommendations.map { it.id }).distinct()
    val oldReview = updated.reviewInfo

    updated = updated.copy(
        appliedRecommendationIds = appliedIds,
        reviewInfo = ReviewInfo(
            confirmAccuracy = true,
            finalNotes = listOfNotNull(
                oldReview?.finalNotes?.takeIf { it.isNotBlank() },
                "Optimization applied: ${recommendations.joinToString { it.title }}"
            ).joinToString("\n")
        )
    )

    return updated
}


private fun buildOptimizedReport(
    report: EnergyReport,
    recommendations: List<Recommendation>,
    surveyData: SurveyData
): EnergyReport {
    val normalizedCategories = normalizeCategoriesToReportTotal(report)

    var hvacCooling = normalizedCategories[0]
    var hvacHeating = normalizedCategories[1]
    var waterHeating = normalizedCategories[2]
    var lighting = normalizedCategories[3]
    var appliances = normalizedCategories[4]

    var remainingAllowedKwhSaving = report.totalYearlyKwh * 0.75

    recommendations.forEach { rec ->
        if (remainingAllowedKwhSaving <= 0.0) return@forEach

        val requestedKwh = rec.estimatedYearlyKwhSaved.coerceAtLeast(0.0)
        if (requestedKwh <= 0.0) return@forEach

        val allowedKwh = requestedKwh.coerceAtMost(remainingAllowedKwhSaving)
        val allowedRatio = allowedKwh / requestedKwh
        val allowedUsd = rec.estimatedYearlyUsdSaved.coerceAtLeast(0.0) * allowedRatio
        val allowedCo2 = rec.estimatedYearlyCo2Saved.coerceAtLeast(0.0) * allowedRatio

        val beforeTotal = yearlyTotalOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances)

        when (rec.category) {
            RecommendationCategory.COOLING -> {
                hvacCooling = reduceCategory(hvacCooling, allowedKwh, allowedUsd, allowedCo2)
            }

            RecommendationCategory.HEATING -> {
                hvacHeating = reduceCategory(hvacHeating, allowedKwh, allowedUsd, allowedCo2)
            }

            RecommendationCategory.WATER_HEATING -> {
                waterHeating = reduceCategory(waterHeating, allowedKwh, allowedUsd, allowedCo2)
            }

            RecommendationCategory.LIGHTING -> {
                lighting = reduceCategory(lighting, allowedKwh, allowedUsd, allowedCo2)
            }

            RecommendationCategory.APPLIANCES -> {
                appliances = reduceCategory(appliances, allowedKwh, allowedUsd, allowedCo2)
            }

            RecommendationCategory.ENVELOPE -> {
                val hvacTotal = hvacCooling.yearlyKwh + hvacHeating.yearlyKwh

                if (hvacTotal > 0.0) {
                    val reduced = reduceCategoriesProportionally(
                        categories = listOf(hvacCooling, hvacHeating),
                        requestedKwhSaved = allowedKwh,
                        requestedUsdSaved = allowedUsd,
                        requestedCo2Saved = allowedCo2
                    )

                    hvacCooling = reduced[0]
                    hvacHeating = reduced[1]
                } else {
                    val reduced = reduceCategoriesProportionally(
                        categories = listOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances),
                        requestedKwhSaved = allowedKwh,
                        requestedUsdSaved = allowedUsd,
                        requestedCo2Saved = allowedCo2
                    )

                    hvacCooling = reduced[0]
                    hvacHeating = reduced[1]
                    waterHeating = reduced[2]
                    lighting = reduced[3]
                    appliances = reduced[4]
                }
            }

            RecommendationCategory.RENEWABLE,
            RecommendationCategory.BEHAVIOR -> {
                val reduced = reduceCategoriesProportionally(
                    categories = listOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances),
                    requestedKwhSaved = allowedKwh,
                    requestedUsdSaved = allowedUsd,
                    requestedCo2Saved = allowedCo2
                )

                hvacCooling = reduced[0]
                hvacHeating = reduced[1]
                waterHeating = reduced[2]
                lighting = reduced[3]
                appliances = reduced[4]
            }
        }

        val afterTotal = yearlyTotalOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances)
        val actualSavedKwh = (beforeTotal - afterTotal).coerceAtLeast(0.0)
        remainingAllowedKwhSaving = (remainingAllowedKwhSaving - actualSavedKwh).coerceAtLeast(0.0)
    }

    return buildReportFromCategories(
        originalReport = report,
        hvacCooling = hvacCooling,
        hvacHeating = hvacHeating,
        waterHeating = waterHeating,
        lighting = lighting,
        appliances = appliances
    )
}

private fun normalizeCategoriesToReportTotal(report: EnergyReport): List<CategoryResult> {
    val categories = listOf(
        report.hvacCooling,
        report.hvacHeating,
        report.waterHeating,
        report.lighting,
        report.appliances
    )

    val categoryKwhTotal = categories.sumOf { it.yearlyKwh.coerceAtLeast(0.0) }
    val categoryCostTotal = categories.sumOf { it.yearlyCostUsd.coerceAtLeast(0.0) }
    val categoryCo2Total = categories.sumOf { it.yearlyCo2Kg.coerceAtLeast(0.0) }

    val kwhFactor =
        if (categoryKwhTotal > 0.0) report.totalYearlyKwh.coerceAtLeast(0.0) / categoryKwhTotal else 1.0

    val costFactor =
        if (categoryCostTotal > 0.0) report.totalYearlyCostUsd.coerceAtLeast(0.0) / categoryCostTotal else 1.0

    val co2Factor =
        if (categoryCo2Total > 0.0) report.totalYearlyCo2Kg.coerceAtLeast(0.0) / categoryCo2Total else 1.0

    return categories.map { category ->
        scaleCategory(category, kwhFactor, costFactor, co2Factor)
    }
}

private fun scaleCategory(
    category: CategoryResult,
    kwhFactor: Double,
    costFactor: Double,
    co2Factor: Double
): CategoryResult {
    val yearlyKwh = (category.yearlyKwh * kwhFactor).coerceAtLeast(0.0)
    val yearlyCost = (category.yearlyCostUsd * costFactor).coerceAtLeast(0.0)
    val yearlyCo2 = (category.yearlyCo2Kg * co2Factor).coerceAtLeast(0.0)

    return category.copy(
        yearlyKwh = round2(yearlyKwh),
        dailyKwh = round2(yearlyKwh / 365.0),
        yearlyCostUsd = round2(yearlyCost),
        yearlyCo2Kg = round2(yearlyCo2)
    )
}

private fun reduceCategory(
    category: CategoryResult,
    requestedKwhSaved: Double,
    requestedUsdSaved: Double,
    requestedCo2Saved: Double
): CategoryResult {
    val availableKwh = category.yearlyKwh.coerceAtLeast(0.0)
    val savedKwh = requestedKwhSaved.coerceIn(0.0, availableKwh)

    val savedRatio =
        if (requestedKwhSaved > 0.0) savedKwh / requestedKwhSaved else 0.0

    val newYearlyKwh = (category.yearlyKwh - savedKwh).coerceAtLeast(0.0)
    val newYearlyCost = (category.yearlyCostUsd - requestedUsdSaved * savedRatio).coerceAtLeast(0.0)
    val newYearlyCo2 = (category.yearlyCo2Kg - requestedCo2Saved * savedRatio).coerceAtLeast(0.0)

    return category.copy(
        yearlyKwh = round2(newYearlyKwh),
        dailyKwh = round2(newYearlyKwh / 365.0),
        yearlyCostUsd = round2(newYearlyCost),
        yearlyCo2Kg = round2(newYearlyCo2)
    )
}

private fun reduceCategoriesProportionally(
    categories: List<CategoryResult>,
    requestedKwhSaved: Double,
    requestedUsdSaved: Double,
    requestedCo2Saved: Double
): List<CategoryResult> {
    val availableKwh = categories.sumOf { it.yearlyKwh.coerceAtLeast(0.0) }

    if (availableKwh <= 0.0 || requestedKwhSaved <= 0.0) {
        return categories
    }

    val actualKwhSaved = requestedKwhSaved.coerceAtMost(availableKwh)
    val actualRatio = actualKwhSaved / requestedKwhSaved
    val actualUsdSaved = requestedUsdSaved * actualRatio
    val actualCo2Saved = requestedCo2Saved * actualRatio

    return categories.map { category ->
        val share = category.yearlyKwh.coerceAtLeast(0.0) / availableKwh

        reduceCategory(
            category = category,
            requestedKwhSaved = actualKwhSaved * share,
            requestedUsdSaved = actualUsdSaved * share,
            requestedCo2Saved = actualCo2Saved * share
        )
    }
}

private fun buildReportFromCategories(
    originalReport: EnergyReport,
    hvacCooling: CategoryResult,
    hvacHeating: CategoryResult,
    waterHeating: CategoryResult,
    lighting: CategoryResult,
    appliances: CategoryResult
): EnergyReport {
    val totalYearlyKwh = yearlyTotalOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances)
    val totalYearlyCost = listOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances)
        .sumOf { it.yearlyCostUsd }
    val totalYearlyCo2 = listOf(hvacCooling, hvacHeating, waterHeating, lighting, appliances)
        .sumOf { it.yearlyCo2Kg }

    return originalReport.copy(
        hvacCooling = hvacCooling,
        hvacHeating = hvacHeating,
        waterHeating = waterHeating,
        lighting = lighting,
        appliances = appliances,

        totalYearlyKwh = round2(totalYearlyKwh),
        totalDailyKwh = round2(totalYearlyKwh / 365.0),
        totalYearlyCostUsd = round2(totalYearlyCost),
        totalYearlyCo2Kg = round2(totalYearlyCo2),

        avgMonthlyKwh = round2(totalYearlyKwh / 12.0),
        avgMonthlyCostUsd = round2(totalYearlyCost / 12.0),
        avgMonthlyCo2Kg = round2(totalYearlyCo2 / 12.0),

        hvacCoolingPercent = percentOf(hvacCooling.yearlyKwh, totalYearlyKwh),
        hvacHeatingPercent = percentOf(hvacHeating.yearlyKwh, totalYearlyKwh),
        waterHeatingPercent = percentOf(waterHeating.yearlyKwh, totalYearlyKwh),
        lightingPercent = percentOf(lighting.yearlyKwh, totalYearlyKwh),
        appliancesPercent = percentOf(appliances.yearlyKwh, totalYearlyKwh)
    )
}

private fun yearlyTotalOf(vararg categories: CategoryResult): Double {
    return categories.sumOf { it.yearlyKwh.coerceAtLeast(0.0) }
}

private fun percentOf(value: Double, total: Double): Double {
    return if (total > 0.0) {
        round2(value / total * 100.0)
    } else {
        0.0
    }
}

private fun round2(value: Double): Double {
    return Math.round(value * 100.0) / 100.0
}
