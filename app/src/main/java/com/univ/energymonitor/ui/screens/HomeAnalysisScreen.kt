package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.model.CategoryResult
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.ui.navigation.AnalysisType
import com.univ.energymonitor.ui.theme.*

private data class ChartSlice(
    val name: String,
    val result: CategoryResult,
    val color: Color
)

private val CoolingColor = Color(0xFF2196F3)
private val HeatingColor = Color(0xFFFF5722)
private val WaterColor = Color(0xFF00BCD4)
private val LightingColor = Color(0xFFFFC107)
private val ApplianceColor = Color(0xFF9C27B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAnalysisScreen(
    analysisType: AnalysisType,
    report: EnergyReport,
    surveyData: SurveyData,
    houseName: String,
    onBack: () -> Unit,
    onViewFullResults: () -> Unit
) {
    val title = when (analysisType) {
        AnalysisType.ELECTRICAL -> "Electrical Systems"
        AnalysisType.HVAC -> "HVAC Analysis"
        AnalysisType.KPI -> "CO₂ & KPI Reports"
    }

    val subtitle = when (analysisType) {
        AnalysisType.ELECTRICAL -> "Lighting and appliance breakdown"
        AnalysisType.HVAC -> "Cooling, heating and water heating breakdown"
        AnalysisType.KPI -> "Energy, cost and emissions summary"
    }

    val slices = when (analysisType) {
        AnalysisType.ELECTRICAL -> listOf(
            ChartSlice("Lighting", report.lighting, LightingColor),
            ChartSlice("Appliances", report.appliances, ApplianceColor)
        )

        AnalysisType.HVAC -> listOf(
            ChartSlice("Cooling", report.hvacCooling, CoolingColor),
            ChartSlice("Heating", report.hvacHeating, HeatingColor),
            ChartSlice("Water Heating", report.waterHeating, WaterColor)
        )

        AnalysisType.KPI -> listOf(
            ChartSlice("Cooling", report.hvacCooling, CoolingColor),
            ChartSlice("Heating", report.hvacHeating, HeatingColor),
            ChartSlice("Water Heating", report.waterHeating, WaterColor),
            ChartSlice("Lighting", report.lighting, LightingColor),
            ChartSlice("Appliances", report.appliances, ApplianceColor)
        )
    }.filter { it.result.yearlyKwh > 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
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
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = title,
                subtitle = subtitle,
                report = report,
                analysisType = analysisType
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Yearly Energy Breakdown",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        "The chart shows how this home's yearly energy use is divided between the selected categories. Larger slices mean higher energy consumption.",
                        fontSize = 11.sp,
                        color = TextGray,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    if (slices.isEmpty()) {
                        Text("No energy data available for this section.", color = TextGray, fontSize = 13.sp)
                    } else {
                        EnergyPieChart(
                            slices = slices,
                            modifier = Modifier.size(210.dp)
                        )

                        Spacer(Modifier.height(18.dp))

                        slices.forEach { slice ->
                            LegendRow(
                                name = slice.name,
                                color = slice.color,
                                yearlyKwh = slice.result.yearlyKwh,
                                totalKwh = slices.sumOf { it.result.yearlyKwh }
                            )
                        }
                    }
                }
            }

            DetailValuesCard(analysisType = analysisType, report = report)

            Button(
                onClick = onViewFullResults,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("VIEW FULL RESULTS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EnergyPieChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.result.yearlyKwh }

    Canvas(modifier = modifier) {
        var startAngle = -90f

        slices.forEach { slice ->
            val sweepAngle = ((slice.result.yearlyKwh / total) * 360.0).toFloat()

            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )

            startAngle += sweepAngle
        }

        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.28f
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    subtitle: String,
    report: EnergyReport,
    analysisType: AnalysisType
) {
    val mainValue = when (analysisType) {
        AnalysisType.ELECTRICAL -> report.lighting.yearlyKwh + report.appliances.yearlyKwh
        AnalysisType.HVAC -> report.hvacCooling.yearlyKwh + report.hvacHeating.yearlyKwh + report.waterHeating.yearlyKwh
        AnalysisType.KPI -> report.totalYearlyKwh
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            
            if (analysisType == AnalysisType.KPI) {
                Text(
                    "%.0f kWh/year".format(report.totalYearlyKwh),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$%.0f/year · %.0f kg CO₂/year".format(
                        report.totalYearlyCostUsd,
                        report.totalYearlyCo2Kg
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    "%.0f kWh/year".format(mainValue),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    name: String,
    color: Color,
    yearlyKwh: Double,
    totalKwh: Double
) {
    val percent = if (totalKwh > 0) yearlyKwh / totalKwh * 100 else 0.0

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(12.dp).clip(CircleShape).background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(name, modifier = Modifier.weight(1f), fontSize = 13.sp, color = DarkGreen)
        Text(
            "%.0f kWh/year · %.1f%%".format(yearlyKwh, percent),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray
        )
    }
}

@Composable
private fun DetailValuesCard(
    analysisType: AnalysisType,
    report: EnergyReport
) {
    val rows = when (analysisType) {
        AnalysisType.ELECTRICAL -> listOf(
            Triple("Lighting", report.lighting.yearlyKwh, report.lighting.yearlyCo2Kg),
            Triple("Appliances", report.appliances.yearlyKwh, report.appliances.yearlyCo2Kg)
        )

        AnalysisType.HVAC -> listOf(
            Triple("Cooling", report.hvacCooling.yearlyKwh, report.hvacCooling.yearlyCo2Kg),
            Triple("Heating", report.hvacHeating.yearlyKwh, report.hvacHeating.yearlyCo2Kg),
            Triple("Water Heating", report.waterHeating.yearlyKwh, report.waterHeating.yearlyCo2Kg)
        )

        AnalysisType.KPI -> listOf(
            Triple("Total Energy", report.totalYearlyKwh, report.totalYearlyCo2Kg),
            Triple("Monthly Average", report.avgMonthlyKwh, report.avgMonthlyCo2Kg),
            Triple("Estimated Cost", report.totalYearlyCostUsd, 0.0)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Values", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(10.dp))

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(row.first, fontSize = 13.sp, color = TextGray)

                    val valueText = if (analysisType == AnalysisType.KPI && row.first == "Estimated Cost") {
                        "$%.0f/year".format(row.second)
                    } else if (analysisType == AnalysisType.KPI && row.first == "Monthly Average") {
                        "%.0f kWh/month".format(row.second)
                    } else {
                        "%.0f kWh/year".format(row.second)
                    }

                    Text(valueText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
            }
        }
    }
}
