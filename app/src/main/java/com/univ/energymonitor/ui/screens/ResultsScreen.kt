package com.univ.energymonitor.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.engine.LebanonDefaults
import com.univ.energymonitor.domain.engine.RecommendationEngine
import com.univ.energymonitor.domain.model.CategoryResult
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.ui.components.*
import com.univ.energymonitor.ui.theme.*
import androidx.compose.foundation.clickable
import com.univ.energymonitor.domain.engine.CategoryBreakdownCalculator
import com.univ.energymonitor.domain.engine.BreakdownItem



private val CoolingColor = Color(0xFF2196F3)
private val HeatingColor = Color(0xFFFF5722)
private val WaterColor = Color(0xFF00BCD4)
private val LightingColor = Color(0xFFFFC107)
private val ApplianceColor = Color(0xFF9C27B0)

private data class EfficiencyRating(
    val grade: String,
    val label: String,
    val color: Color,
    val kwhPerM2: Double
)

private fun calculateRating(report: EnergyReport, areaM2: Double): EfficiencyRating {
    if (areaM2 <= 0) return EfficiencyRating("—", "Unknown", Color.Gray, 0.0)
    val kwhPerM2 = report.totalYearlyKwh / areaM2
    return when {
        kwhPerM2 < 30 -> EfficiencyRating("A+++", "Excellent", Color(0xFF1B5E20), kwhPerM2)
        kwhPerM2 < 50 -> EfficiencyRating("A++", "Very Good", Color(0xFF2E7D32), kwhPerM2)
        kwhPerM2 < 80 -> EfficiencyRating("A+", "Good", Color(0xFF388E3C), kwhPerM2)
        kwhPerM2 < 120 -> EfficiencyRating("A", "Average", Color(0xFF689F38), kwhPerM2)
        kwhPerM2 < 180 -> EfficiencyRating("B", "Below Average", Color(0xFFFBC02D), kwhPerM2)
        kwhPerM2 < 250 -> EfficiencyRating("C", "Poor", Color(0xFFF57C00), kwhPerM2)
        else -> EfficiencyRating("D", "Very Poor", Color(0xFFD32F2F), kwhPerM2)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    report: EnergyReport,
    surveyData: SurveyData,
    houseName: String = "Household",
    onBackToDashboard: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📊 Overview", "📈 Details", "💡 Tips", "🌍 Impact")

    val areaM2 = surveyData.houseInfo?.totalAreaM2?.toDoubleOrNull() ?: 0.0
    val rating = remember(report, areaM2) { calculateRating(report, areaM2) }
    val recommendations = remember(report, surveyData) {
        RecommendationEngine.generate(surveyData, report)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Energy Report", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text(houseName, fontSize = 12.sp, color = TextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HeroCard(report, rating)

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryGreen,
                edgePadding = 8.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) PrimaryGreen else TextGray
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTab(report)
                    1 -> DetailsTab(report, surveyData)
                    2 -> TipsTab(recommendations)
                    3 -> ImpactTab(report, rating)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Home, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("BACK TO DASHBOARD", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun HeroCard(report: EnergyReport, rating: EfficiencyRating) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Total Yearly Consumption",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${formatNumber(report.totalYearlyKwh)} kWh",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HeroStat("💰", "$${formatNumber(report.totalYearlyCostUsd)}", "per year")
                HeroStat("🌿", "${formatNumber(report.totalYearlyCo2Kg)}", "kg CO₂")
                HeroStat("📅", "${formatNumber(report.totalDailyKwh)}", "kWh/day")
            }

            if (rating.grade != "—") {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(rating.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            rating.grade,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Efficiency: ${rating.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Text(
                            "${String.format("%.1f", rating.kwhPerM2)} kWh/m²/year",
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun OverviewTab(report: EnergyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Monthly Averages", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthlyStatItem(formatNumber(report.avgMonthlyKwh), "kWh", "Energy")
                MonthlyStatItem("$${formatNumber(report.avgMonthlyCostUsd)}", "", "Cost")
                MonthlyStatItem(formatNumber(report.avgMonthlyCo2Kg), "kg", "CO₂")
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📊  Energy Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(16.dp))

            BreakdownBarItem("HVAC Cooling", report.hvacCoolingPercent, report.hvacCooling.yearlyKwh, CoolingColor)
            BreakdownBarItem("HVAC Heating", report.hvacHeatingPercent, report.hvacHeating.yearlyKwh, HeatingColor)
            BreakdownBarItem("Water Heating", report.waterHeatingPercent, report.waterHeating.yearlyKwh, WaterColor)
            BreakdownBarItem("Lighting", report.lightingPercent, report.lighting.yearlyKwh, LightingColor)
            BreakdownBarItem("Appliances", report.appliancesPercent, report.appliances.yearlyKwh, ApplianceColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsTab(report: EnergyReport, surveyData: SurveyData) {
    var selectedTitle by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf(Color.Gray) }
    var selectedTotal by remember { mutableStateOf<CategoryResult?>(null) }
    var selectedItems by remember { mutableStateOf(emptyList<BreakdownItem>()) }

    fun openBreakdown(
        title: String,
        color: Color,
        total: CategoryResult,
        items: List<BreakdownItem>
    ) {
        selectedTitle = title
        selectedColor = color
        selectedTotal = total
        selectedItems = items
    }

    Text(
        "Category Breakdown",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = DarkGreen
    )

    CategoryDetailCard(
        icon = "❄️",
        result = report.hvacCooling,
        color = CoolingColor,
        detail = "Tap to see AC unit breakdown",
        onClick = {
            openBreakdown(
                title = "HVAC Cooling",
                color = CoolingColor,
                total = report.hvacCooling,
                items = CategoryBreakdownCalculator.coolingItems(surveyData)
            )
        }
    )

    CategoryDetailCard(
        icon = "🔥",
        result = report.hvacHeating,
        color = HeatingColor,
        detail = "Tap to see heating breakdown",
        onClick = {
            openBreakdown(
                title = "HVAC Heating",
                color = HeatingColor,
                total = report.hvacHeating,
                items = CategoryBreakdownCalculator.heatingItems(surveyData)
            )
        }
    )

    CategoryDetailCard(
        icon = "🚿",
        result = report.waterHeating,
        color = WaterColor,
        detail = "Tap to see water heater breakdown",
        onClick = {
            openBreakdown(
                title = "Water Heating",
                color = WaterColor,
                total = report.waterHeating,
                items = CategoryBreakdownCalculator.waterHeatingItems(surveyData)
            )
        }
    )

    CategoryDetailCard(
        icon = "💡",
        result = report.lighting,
        color = LightingColor,
        detail = "Tap to see lighting breakdown",
        onClick = {
            openBreakdown(
                title = "Lighting",
                color = LightingColor,
                total = report.lighting,
                items = CategoryBreakdownCalculator.lightingItems(surveyData)
            )
        }
    )

    CategoryDetailCard(
        icon = "🔌",
        result = report.appliances,
        color = ApplianceColor,
        detail = "Tap to see appliance breakdown",
        onClick = {
            openBreakdown(
                title = "Appliances",
                color = ApplianceColor,
                total = report.appliances,
                items = CategoryBreakdownCalculator.applianceItems(surveyData)
            )
        }
    )

    val edlPrice = surveyData.consumptionInfo?.edlPricePerKwhUsd?.toDoubleOrNull()
        ?: 0.0

    SurveyInfoHint(
        text = "Estimates use Lebanon-typical defaults for appliance wattages " +
                "and seasonal usage. Actual consumption may vary. " +
                "EDL rate: ${'$'}${String.format("%.2f", edlPrice)}/kWh · " +
                "CO₂: ${LebanonDefaults.CO2_KG_PER_KWH} kg/kWh."
    )

    if (selectedTitle != null && selectedTotal != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedTitle = null
                selectedTotal = null
                selectedItems = emptyList()
            },
            containerColor = Color.White
        ) {
            CategoryBreakdownSheet(
                title = selectedTitle ?: "",
                color = selectedColor,
                total = selectedTotal!!,
                items = selectedItems
            )
        }
    }
}


@Composable
private fun TipsTab(recommendations: List<com.univ.energymonitor.domain.model.Recommendation>) {
    if (recommendations.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GreenSurface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your home is already efficient!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "No major improvements recommended at this time.",
                    fontSize = 12.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        RecommendationsSection(recommendations = recommendations)
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ImpactTab(report: EnergyReport, rating: EfficiencyRating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GreenSurface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌍", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text("Environmental Impact", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(8.dp))
            Text(
                "${formatNumber(report.totalYearlyCo2Kg)} kg CO₂/year",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Based on Lebanon's grid emission factor of ${LebanonDefaults.CO2_KG_PER_KWH} kg CO₂/kWh",
                fontSize = 11.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )
        }
    }

    val treesNeeded = (report.totalYearlyCo2Kg / 22.0).toInt()
    val carKm = (report.totalYearlyCo2Kg / 0.12).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Equivalent to...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(12.dp))
            EquivalenceRow("🌳", "$treesNeeded trees", "needed to offset yearly CO₂")
            Spacer(Modifier.height(8.dp))
            EquivalenceRow("🚗", "${formatNumber(carKm.toDouble())} km", "driven by an average car")
        }
    }

    if (rating.grade != "—") {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Efficiency Rating", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(rating.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(rating.grade, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(rating.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text(
                            "${String.format("%.1f", rating.kwhPerM2)} kWh/m²/year",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                        Text("Based on LEEB 2025 standards", fontSize = 10.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun EquivalenceRow(icon: String, value: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Text(description, fontSize = 11.sp, color = TextGray)
        }
    }
}

@Composable
private fun MonthlyStatItem(value: String, unit: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            if (unit.isNotBlank()) "$value $unit" else value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
        Text(label, fontSize = 11.sp, color = TextGray)
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun BreakdownBarItem(name: String, percent: Double, yearlyKwh: Double, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(8.dp))
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
            }
            Text(
                "${formatNumber(yearlyKwh)} kWh · ${String.format("%.1f", percent)}%",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LightDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percent / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun CategoryDetailCard(
    icon: String,
    result: CategoryResult,
    color: Color,
    detail: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkGreen
                )
                Text(detail, fontSize = 11.sp, color = TextGray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${formatNumber(result.yearlyKwh)} kWh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text("$${formatNumber(result.yearlyCostUsd)}", fontSize = 12.sp, color = TextGray)
                Text("${formatNumber(result.yearlyCo2Kg)} kg CO₂", fontSize = 11.sp, color = TextGray)
            }
        }
    }
}
@Composable
private fun CategoryBreakdownSheet(
    title: String,
    color: Color,
    total: CategoryResult,
    items: List<BreakdownItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(28.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Text(
                    "${formatNumber(total.yearlyKwh)} kWh/year · $${formatNumber(total.yearlyCostUsd)} · ${formatNumber(total.yearlyCo2Kg)} kg CO₂",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }

        if (items.isEmpty()) {
            Text(
                "No detailed contributors available for this category.",
                fontSize = 13.sp,
                color = TextGray
            )
        } else {
            val maxKwh = items.maxOfOrNull { it.yearlyKwh } ?: 0.0

            items.forEach { item ->
                BreakdownBar(
                    item = item,
                    maxKwh = maxKwh,
                    totalKwh = total.yearlyKwh,
                    color = color
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BreakdownBar(
    item: BreakdownItem,
    maxKwh: Double,
    totalKwh: Double,
    color: Color
) {
    val barFraction = if (maxKwh > 0.0) {
        (item.yearlyKwh / maxKwh).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val percentOfCategory = if (totalKwh > 0.0) {
        item.yearlyKwh / totalKwh * 100.0
    } else {
        0.0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    item.detail,
                    fontSize = 11.sp,
                    color = TextGray
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${formatNumber(item.yearlyKwh)} kWh",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    "${String.format("%.1f", percentOfCategory)}%",
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(LightDivider)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble() && value < 100000) {
        value.toLong().toString()
    } else if (value < 10) {
        String.format("%.2f", value)
    } else if (value < 1000) {
        String.format("%.1f", value)
    } else {
        String.format("%,.0f", value)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultsScreenPreview() {
    MaterialTheme {
        ResultsScreen(
            report = EnergyReport(
                hvacCooling = CategoryResult("HVAC cooling", 10.73, 3918.0, 391.8, 1959.0),
                hvacHeating = CategoryResult("HVAC heating", 0.74, 270.0, 27.0, 135.0),
                waterHeating = CategoryResult("Water heating", 3.0, 1095.0, 109.5, 547.5),
                lighting = CategoryResult("Lighting", 1.58, 577.8, 57.78, 288.9),
                appliances = CategoryResult("Appliances", 6.12, 2233.8, 223.38, 1116.9),
                totalDailyKwh = 22.17,
                totalYearlyKwh = 8094.6,
                totalYearlyCostUsd = 809.46,
                totalYearlyCo2Kg = 4047.3,
                avgMonthlyKwh = 674.55,
                avgMonthlyCostUsd = 67.46,
                avgMonthlyCo2Kg = 337.28,
                hvacCoolingPercent = 48.4,
                hvacHeatingPercent = 3.34,
                waterHeatingPercent = 13.53,
                lightingPercent = 7.14,
                appliancesPercent = 27.6
            ),
            surveyData = SurveyData(),
            houseName = "Apartment – Achrafieh",
            onBackToDashboard = {}
        )
    }
}
