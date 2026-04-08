package com.univ.energymonitor.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.univ.energymonitor.domain.model.CategoryResult
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.GreenSurface
import com.univ.energymonitor.ui.theme.LightDivider
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextDark
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
// ─────────────────────────────────────────────────────────────────────────────
// Colors for each energy category (used in chart and cards)
// ─────────────────────────────────────────────────────────────────────────────
private val CoolingColor   = Color(0xFF2196F3)
private val HeatingColor   = Color(0xFFFF5722)
private val WaterColor     = Color(0xFF00BCD4)
private val LightingColor  = Color(0xFFFFC107)
private val ApplianceColor = Color(0xFF9C27B0)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    report: EnergyReport,
    houseName: String = "Household",
    onBackToDashboard: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Energy Report",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Companion.Bold,
                            color = DarkGreen
                        )
                        Text(
                            houseName,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Companion.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Top Summary Cards (3 KPIs) ───────────────────────────────────
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryKpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "⚡",
                    value = formatNumber(report.totalYearlyKwh),
                    unit = "kWh/yr",
                    label = "Total Energy"
                )
                SummaryKpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "💰",
                    value = "$${formatNumber(report.totalYearlyCostUsd)}",
                    unit = "/year",
                    label = "Est. Cost"
                )
                SummaryKpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "🌿",
                    value = formatNumber(report.totalYearlyCo2Kg),
                    unit = "kg CO₂",
                    label = "Emissions"
                )
            }

            // ── Monthly Averages Card ────────────────────────────────────────
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MonthlyStatItem(
                        value = formatNumber(report.avgMonthlyKwh),
                        unit = "kWh",
                        label = "Monthly Avg"
                    )
                    MonthlyStatItem(
                        value = "$${formatNumber(report.avgMonthlyCostUsd)}",
                        unit = "",
                        label = "Monthly Cost"
                    )
                    MonthlyStatItem(
                        value = formatNumber(report.avgMonthlyCo2Kg),
                        unit = "kg",
                        label = "Monthly CO₂"
                    )
                }
            }

            // ── Energy Breakdown Chart (horizontal bars) ─────────────────────
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.Companion.padding(20.dp)) {

                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        Box(
                            modifier = Modifier.Companion
                                .width(4.dp)
                                .height(20.dp)
                                .background(
                                    PrimaryGreen,
                                    androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(Modifier.Companion.width(10.dp))
                        Text(
                            "📊  Energy Breakdown",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Companion.Bold,
                            color = DarkGreen
                        )
                    }

                    Spacer(Modifier.Companion.height(16.dp))

                    BreakdownBarItem(
                        "HVAC Cooling",
                        report.hvacCoolingPercent,
                        report.hvacCooling.yearlyKwh,
                        CoolingColor
                    )
                    BreakdownBarItem(
                        "HVAC Heating",
                        report.hvacHeatingPercent,
                        report.hvacHeating.yearlyKwh,
                        HeatingColor
                    )
                    BreakdownBarItem(
                        "Water Heating",
                        report.waterHeatingPercent,
                        report.waterHeating.yearlyKwh,
                        WaterColor
                    )
                    BreakdownBarItem(
                        "Lighting",
                        report.lightingPercent,
                        report.lighting.yearlyKwh,
                        LightingColor
                    )
                    BreakdownBarItem(
                        "Appliances",
                        report.appliancesPercent,
                        report.appliances.yearlyKwh,
                        ApplianceColor
                    )
                }
            }

            // ── Category Detail Cards ────────────────────────────────────────
            Text(
                "Category Details",
                fontWeight = FontWeight.Companion.SemiBold,
                fontSize = 15.sp,
                color = DarkGreen
            )

            CategoryDetailCard(
                icon = "❄️",
                result = report.hvacCooling,
                color = CoolingColor,
                detail = "AC cooling over ${LebanonDefaults.COOLING_SEASON_DAYS} days/year"
            )
            CategoryDetailCard(
                icon = "🔥",
                result = report.hvacHeating,
                color = HeatingColor,
                detail = "Heating over ${LebanonDefaults.HEATING_SEASON_DAYS} days/year"
            )
            CategoryDetailCard(
                icon = "🚿",
                result = report.waterHeating,
                color = WaterColor,
                detail = "Water heater year-round"
            )
            CategoryDetailCard(
                icon = "💡",
                result = report.lighting,
                color = LightingColor,
                detail = "All fixtures, all rooms"
            )
            CategoryDetailCard(
                icon = "🔌",
                result = report.appliances,
                color = ApplianceColor,
                detail = "Fridge, TV, washer, kitchen, etc."
            )

            // ── CO₂ Impact Card ──────────────────────────────────────────────
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GreenSurface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.Companion.padding(20.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text("🌍", fontSize = 36.sp)
                    Spacer(Modifier.Companion.height(8.dp))
                    Text(
                        "Environmental Impact",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = DarkGreen
                    )
                    Spacer(Modifier.Companion.height(4.dp))
                    Text(
                        "This household produces an estimated",
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Companion.Center
                    )
                    Spacer(Modifier.Companion.height(8.dp))
                    Text(
                        "${formatNumber(report.totalYearlyCo2Kg)} kg CO₂/year",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = PrimaryGreen
                    )
                    Spacer(Modifier.Companion.height(4.dp))
                    Text(
                        "Based on Lebanon's grid emission factor of ${LebanonDefaults.CO2_KG_PER_KWH} kg CO₂/kWh",
                        fontSize = 11.sp,
                        color = TextGray,
                        textAlign = TextAlign.Companion.Center
                    )
                }
            }

            // ── Assumptions Note ─────────────────────────────────────────────
            SurveyInfoHint(
                text = "Estimates use Lebanon-typical defaults for appliance wattages " +
                        "and seasonal usage. Actual consumption may vary. " +
                        "EDL rate: ${'$'}${LebanonDefaults.EDL_PRICE_PER_KWH_USD}/kWh · " +
                        "CO₂: ${LebanonDefaults.CO2_KG_PER_KWH} kg/kWh."
            )

            // ── Back to Dashboard Button ─────────────────────────────────────
            Button(
                onClick = onBackToDashboard,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Home, null, modifier = Modifier.Companion.size(18.dp))
                Spacer(Modifier.Companion.width(8.dp))
                Text(
                    "BACK TO DASHBOARD",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.Companion.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary KPI Card (top row)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SummaryKpiCard(
    modifier: Modifier = Modifier.Companion,
    icon: String,
    value: String,
    unit: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.Companion.height(4.dp))
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = PrimaryGreen,
                textAlign = TextAlign.Companion.Center
            )
            Text(
                unit,
                fontSize = 10.sp,
                color = TextGray,
                textAlign = TextAlign.Companion.Center
            )
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Companion.Medium,
                color = DarkGreen,
                textAlign = TextAlign.Companion.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Monthly Stat Item (inside green banner)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MonthlyStatItem(
    value: String,
    unit: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            if (unit.isNotBlank()) "$value $unit" else value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Companion.Bold,
            color = Color.Companion.White
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color.Companion.White.copy(alpha = 0.8f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Breakdown Bar Item (horizontal bar chart)
// ─────────────────────────────────────────────────────────────────────────────
@SuppressLint("DefaultLocale")
@Composable
private fun BreakdownBarItem(
    name: String,
    percent: Double,
    yearlyKwh: Double,
    color: Color
) {
    Column(modifier = Modifier.Companion.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                Box(
                    modifier = Modifier.Companion
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.Companion.width(8.dp))
                Text(
                    name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Companion.Medium,
                    color = TextDark
                )
            }
            Text(
                "${formatNumber(yearlyKwh)} kWh · ${String.format("%.1f", percent)}%",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        Spacer(Modifier.Companion.height(4.dp))
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .height(8.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(LightDivider)
        ) {
            Box(
                modifier = Modifier.Companion
                    .fillMaxWidth(fraction = (percent / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Detail Card
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CategoryDetailCard(
    icon: String,
    result: CategoryResult,
    color: Color,
    detail: String
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.Companion.padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Box(
                modifier = Modifier.Companion
                    .size(42.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Spacer(Modifier.Companion.width(14.dp))

            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    result.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Companion.SemiBold,
                    color = DarkGreen
                )
                Text(
                    detail,
                    fontSize = 11.sp,
                    color = TextGray
                )
            }

            Column(horizontalAlignment = Alignment.Companion.End) {
                Text(
                    "${formatNumber(result.yearlyKwh)} kWh",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = color
                )
                Text(
                    "$${formatNumber(result.yearlyCostUsd)}",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    "${formatNumber(result.yearlyCo2Kg)} kg CO₂",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Number formatting helper
// ─────────────────────────────────────────────────────────────────────────────
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
            houseName = "Apartment – Achrafieh",
            onBackToDashboard = {}
        )
    }
}