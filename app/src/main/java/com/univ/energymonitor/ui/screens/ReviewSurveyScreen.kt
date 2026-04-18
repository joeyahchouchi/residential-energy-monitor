package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.model.SurveyData
import com.univ.energymonitor.ui.state.ReviewSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.ErrorRed
import com.univ.energymonitor.ui.theme.ErrorSurface
import com.univ.energymonitor.ui.theme.HintGray
import com.univ.energymonitor.ui.theme.LightDivider
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextDark
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.theme.WarningOrange
import com.univ.energymonitor.ui.theme.WarningSurface
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSurveyScreen(
    surveyData: SurveyData,
    initialState: ReviewSurveyUiState = ReviewSurveyUiState(),
    onBackClick: () -> Unit,
    onEditStep: (Int) -> Unit,
    onSubmit: (ReviewSurveyUiState) -> Unit,
    onSaveDraft: (ReviewSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                title = {
                    Column {
                        Text("Household Survey", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text("Step 6 of 6 – Review & Submit", fontSize = 12.sp, color = TextGray)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            SurveyStepProgressBar(currentStep = 6, totalSteps = 6)

            SurveyInfoHint(
                text = "Please review all your answers below. Tap \"Edit\" to go back and change any section."
            )

            // ── Step 1 Summary: House Information ────────────────────────
            ReviewSectionCard(
                title = "🏠  House Information",
                stepNumber = 1,
                onEditClick = { onEditStep(1) }
            ) {
                val h = surveyData.houseInfo
                if (h != null) {
                    ReviewRow("House Name", h.houseName)
                    ReviewRow("Location", h.location)
                    ReviewRow("Type", h.houseType)
                    ReviewRow("Floor", h.floorNumber)
                    ReviewRow("Building Age", h.buildingAge)
                    ReviewRow("Area", "${h.totalAreaM2} m²")
                    ReviewRow("Rooms", h.numberOfRooms)
                    ReviewRow("Occupants", h.numberOfOccupants)
                    ReviewRow("External Wall", "${h.wallMaterial} · ${h.wallThickness}")
                    ReviewRow("Interior Wall", h.interiorWallMaterial)
                    ReviewRow("Glass", h.glassType)
                    ReviewRow("Roof Exposure", h.roofExposure)
                    ReviewRow("Insulation", h.insulationLevel)
                } else {
                    ReviewEmpty()
                }
            }

            // ── Step 2 Summary: HVAC & Water Heating ─────────────────────
            ReviewSectionCard(
                title = "❄️  HVAC & Water Heating",
                stepNumber = 2,
                onEditClick = { onEditStep(2) }
            ) {
                val v = surveyData.hvacInfo
                if (v != null) {
                    // Cooling
                    ReviewRow("AC Units", v.numberOfAcUnits)
                    v.acUnits.forEachIndexed { index, unit ->
                        ReviewRow("AC ${index + 1} Room", unit.roomName)
                        ReviewRow("AC ${index + 1} Room Size", "${unit.roomSizeM2} m²")
                        ReviewRow("AC ${index + 1} Type", unit.acType)
                        if (unit.capacityValue.isNotBlank()) {
                            ReviewRow("AC ${index + 1} Capacity", "${unit.capacityValue} ${unit.capacityUnit}")
                        }
                        ReviewRow("AC ${index + 1} COP Method", unit.copMethod)
                        if (unit.copMethod == "I know the COP") {
                            ReviewRow("AC ${index + 1} COP", unit.cop)
                        }
                        if (unit.copMethod == "I know the AC year") {
                            ReviewRow("AC ${index + 1} AC Age", unit.acYear)
                        }
                        ReviewRow("AC ${index + 1} Hours/Day", "${unit.dailyUsageHours} hrs")
                        ReviewRow("AC ${index + 1} Days/Year", "${unit.daysPerYear} days")
                    }

                    // Heating
                    ReviewRow("Heating Type", v.heatingSystemType)
                    when (v.heatingSystemType) {
                        "AC" -> {
                            ReviewRow("Heating AC Units", v.numberOfHeatingAcUnits)
                            v.heatingAcUnits.forEachIndexed { index, unit ->
                                ReviewRow("Heat AC ${index + 1} Room", unit.roomName)
                                ReviewRow("Heat AC ${index + 1} Size", "${unit.roomSizeM2} m²")
                                ReviewRow("Heat AC ${index + 1} Type", unit.acType)
                                if (unit.capacityValue.isNotBlank()) {
                                    ReviewRow("Heat AC ${index + 1} Cap.", "${unit.capacityValue} ${unit.capacityUnit}")
                                }
                                ReviewRow("Heat AC ${index + 1} COP", unit.copMethod)
                                ReviewRow("Heat AC ${index + 1} Hrs/Day", "${unit.dailyUsageHours} hrs")
                                ReviewRow("Heat AC ${index + 1} Days/Yr", "${unit.daysPerYear} days")
                            }
                        }
                        "Electric Heater" -> {
                            ReviewRow("Heating Units", v.numberOfHeatingUnits)
                            ReviewRow("Heater Power", "${v.heatingPowerKw} kW")
                            ReviewRow("Hours/Day", "${v.heatingDailyUsageHours} hrs")
                            ReviewRow("Days/Year", "${v.heatingDaysPerYear} days")
                        }
                        "Gas Heater" -> {
                            ReviewRow("Gas Usage", "${v.heatingGasKgPerYear} kg/year")
                        }
                        "Diesel/Fuel Heater" -> {
                            ReviewRow("Fuel Usage", "${v.heatingFuelLitersPerYear} L/year")
                        }
                    }

                    // Water Heating
                    ReviewRow("Water Heater", v.waterHeaterType)
                    if (v.waterHeaterType != "None" && v.waterHeaterType.isNotBlank()) {
                        ReviewRow("Tank Size", v.waterTankSizeLiters)
                        ReviewRow("Insulated", v.waterTankInsulated)
                    }
                    when (v.waterHeaterType) {
                        "Electrical Resistance" -> {
                            ReviewRow("Heater Power", "${v.waterHeaterPowerKw} kW")
                            ReviewRow("Hours/Day", "${v.waterHeaterDailyHours} hrs")
                            ReviewRow("Days/Year", "${v.waterHeaterDaysPerYear} days")
                        }
                        "Solar Heater" -> {
                            ReviewRow("Backup Type", v.solarWaterBackupType)
                            if (v.solarWaterBackupType != "None" && v.solarWaterBackupType.isNotBlank()) {
                                ReviewRow("Backup Usage", "${v.solarWaterBackupHoursPerDay} hrs/day")
                            }
                        }
                        "Gas Tank" -> {
                            ReviewRow("Gas Usage", "${v.gasTankKgPerYear} kg/year")
                        }
                        "Fuel Heating" -> {
                            ReviewRow("Fuel Usage", "${v.fuelLitersPerYear} L/year")
                        }
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ── Step 3 Summary: Lighting ─────────────────────────────────
            ReviewSectionCard(
                title = "💡  Lighting Systems",
                stepNumber = 3,
                onEditClick = { onEditStep(3) }
            ) {
                val l = surveyData.lightingInfo
                if (l != null) {
                    ReviewRow("Direct Lamps", l.numberOfDirectLamps)
                    if ((l.numberOfDirectLamps.toIntOrNull() ?: 0) > 0) {
                        ReviewRow("Direct Types", l.numberOfDirectTypes)
                        l.directLampSamples.forEachIndexed { i, lamp ->
                            ReviewRow(
                                "Direct ${i + 1}",
                                "${lamp.roomName} · ${lamp.bulbType} · ${lamp.powerWatts}W · ${lamp.dailyUsageHours}h${if (lamp.isDimmable) " · Dim" else ""}"
                            )
                        }
                    }
                    ReviewRow("Indirect Lighting", if (l.hasIndirectLighting) "Yes (${l.numberOfIndirectRooms} rooms)" else "No")
                    if (l.hasIndirectLighting) {
                        l.indirectRooms.forEachIndexed { i, room ->
                            ReviewRow(
                                "Indirect ${i + 1}",
                                "${room.roomName} · ${room.lengthMeters}m · ${room.powerWatts}W · ${room.dailyUsageHours}h"
                            )
                        }
                    }
                    ReviewRow("Outdoor Lighting", if (l.hasOutdoorLighting) "Yes (${l.numberOfOutdoorLamps})" else "No")
                    if (l.hasOutdoorLighting) {
                        l.outdoorLamps.forEachIndexed { i, lamp ->
                            ReviewRow("Outdoor ${i + 1}", "${lamp.powerWatts}W · ${lamp.dailyUsageHours}h")
                        }
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ── Step 4 Summary: Appliances ───────────────────────────────
            ReviewSectionCard(
                title = "🔌  Appliances & Loads",
                stepNumber = 4,
                onEditClick = { onEditStep(4) }
            ) {
                val a = surveyData.applianceInfo
                if (a != null) {
                    val active = a.appliances.filter { it.exists }
                    if (active.isEmpty() && a.customAppliances.isEmpty()) {
                        ReviewRow("Appliances", "None selected")
                    } else {
                        active.forEach { appliance ->
                            ReviewRow(
                                appliance.name,
                                "${appliance.powerWatts}W · ${appliance.dailyUsageHours} hrs/day"
                            )
                        }
                        a.customAppliances.forEach { custom ->
                            ReviewRow(
                                "${custom.name} (custom)",
                                "${custom.powerWatts}W · ${custom.dailyUsageHours} hrs/day"
                            )
                        }
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ── Step 5 Summary: Electricity Supply Sources ───────────────
            ReviewSectionCard(
                title = "⚡  Electricity Supply Sources",
                stepNumber = 5,
                onEditClick = { onEditStep(5) }
            ) {
                val c = surveyData.consumptionInfo
                if (c != null) {
                    ReviewRow("EDL Hours/Day", c.edlHoursPerDay)

                    val sources = mutableListOf<String>()
                    if (c.usesEdl) sources.add("EDL")
                    if (c.usesGenerator) sources.add("Generator")
                    if (c.usesSolar) sources.add("Solar PV")
                    if (c.usesUps) sources.add("UPS/Battery")
                    if (c.usesNone) sources.add("None")
                    ReviewRow("Sources", sources.joinToString(", "))

                    if (c.usesGenerator) {
                        ReviewRow("Generator Type", c.generatorSubscriptionType)
                    }
                    if (c.usesSolar) {
                        ReviewRow("Solar Capacity", c.solarCapacity)
                        ReviewRow("Battery Storage", c.solarHasBattery)
                    }
                    if (c.usesEdl && c.monthlyEdlBill.isNotBlank()) {
                        ReviewRow("Monthly EDL Bill", "$${c.monthlyEdlBill}")
                    }
                    if (c.usesGenerator && c.monthlyGeneratorBill.isNotBlank()) {
                        ReviewRow("Monthly Generator Bill", "$${c.monthlyGeneratorBill}")
                    }
                    if (c.usesSolar && c.solarSystemCost.isNotBlank()) {
                        ReviewRow("Solar Installation Cost", "$${c.solarSystemCost}")
                    }
                } else {
                    ReviewEmpty()
                }
            }
            // ── Final Notes & Confirmation ───────────────────────────────
            SurveySectionCard(title = "📝  Final Notes & Confirmation") {

                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Additional Notes (optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = state.finalNotes,
                        onValueChange = { state = state.copy(finalNotes = it) },
                        placeholder = {
                            Text("e.g. house recently renovated, new AC installed…", color = HintGray, fontSize = 13.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = LightDivider,
                            cursorColor = PrimaryGreen
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextDark)
                    )
                }

                SurveyFormToggle(
                    label = "I confirm the data is accurate",
                    description = "You must confirm before submitting the survey",
                    checked = state.confirmAccuracy,
                    onCheckedChange = {
                        state = state.copy(confirmAccuracy = it, showErrors = false)
                    }
                )

                if (state.showErrors && !state.confirmAccuracy) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorSurface, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Please confirm the data is accurate before submitting.", fontSize = 12.sp, color = ErrorRed)
                    }
                }
            }

            // ── Navigation Buttons ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("BACK", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("DRAFT", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
                Button(
                    onClick = {
                        if (state.isValid()) onSubmit(state)
                        else state = state.copy(showErrors = true)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                ) {
                    Text("SUBMIT", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReviewSectionCard(
    title: String,
    stepNumber: Int,
    onEditClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(PrimaryGreen, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreen, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Step $stepNumber", tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreen)
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value.ifBlank { "—" },
            fontSize = 13.sp,
            color = TextDark,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun ReviewEmpty() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarningSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("This section has not been completed yet. Tap Edit to fill it in.", fontSize = 12.sp, color = WarningOrange)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReviewSurveyScreenPreview() {
    MaterialTheme {
        ReviewSurveyScreen(
            surveyData = SurveyData(),
            onBackClick = {},
            onEditStep = {},
            onSubmit = {},
            onSaveDraft = {}
        )
    }
}