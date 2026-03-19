package com.univ.energymonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.*

// ─────────────────────────────────────────────────────────────────────────────
// UI State — Step 6
// ─────────────────────────────────────────────────────────────────────────────
data class ReviewSurveyUiState(
    val confirmAccuracy : Boolean = false,
    val finalNotes      : String  = "",
    val showErrors      : Boolean = false
)

fun ReviewSurveyUiState.isValid(): Boolean {
    return confirmAccuracy
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewSurveyScreen(
    surveyData  : SurveyDataContainer,
    initialState: ReviewSurveyUiState = ReviewSurveyUiState(),
    onBackClick : () -> Unit,
    onEditStep  : (Int) -> Unit,
    onSubmit    : (ReviewSurveyUiState) -> Unit,
    onSaveDraft : (ReviewSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SurveyGreenPrimary
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            "Household Survey",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurveyGreenDark
                        )
                        Text(
                            "Step 6 of 6 – Review & Submit",
                            fontSize = 12.sp,
                            color = SurveyTextGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurveyCardWhite)
            )
        },
        containerColor = SurveyBgGray
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Progress bar ──────────────────────────────────────────────────
            SurveyStepProgressBar(currentStep = 6, totalSteps = 6)

            // ── Header ────────────────────────────────────────────────────────
            SurveyInfoHint(
                text = "Please review all your answers below. " +
                        "Tap \"Edit\" to go back and change any section."
            )

            // ══════════════════════════════════════════════════════════════════
            // Step 1 Summary: House Information
            // ══════════════════════════════════════════════════════════════════
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
                    ReviewRow("Area", "${h.totalAreaM2} m²")
                    ReviewRow("Rooms", h.numberOfRooms)
                    ReviewRow("Occupants", h.numberOfOccupants)
                    ReviewRow("Wall", "${h.wallMaterial} · ${h.wallThickness}")
                    ReviewRow("Glass", h.glassType)
                    ReviewRow("Roof Exposure", h.roofExposure)
                    ReviewRow("Insulation", h.insulationLevel)
                } else {
                    ReviewEmpty()
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Step 2 Summary: HVAC & Water Heating
            // ══════════════════════════════════════════════════════════════════
            ReviewSectionCard(
                title = "❄️  HVAC & Water Heating",
                stepNumber = 2,
                onEditClick = { onEditStep(2) }
            ) {
                val v = surveyData.hvacInfo
                if (v != null) {
                    ReviewRow("AC Units", v.numberOfAcUnits)
                    ReviewRow("AC Type", v.acType)
                    ReviewRow("AC Capacity", "${v.acCapacityKw} kW")
                    ReviewRow("AC Daily Use", "${v.acDailyUsageHours} hrs")
                    ReviewRow("Thermostat", "${v.acThermostatSetpoint} °C")
                    ReviewRow("Inverter AC", if (v.isInverterAc) "Yes" else "No")
                    ReviewRow("Heating", v.heatingSystemType)
                    if (v.heatingSystemType != "None") {
                        ReviewRow("Heating Units", v.numberOfHeatingUnits)
                        ReviewRow("Heating Use", "${v.heatingDailyUsageHours} hrs/day")
                    }
                    ReviewRow("Water Heater", v.waterHeaterType)
                    if (v.waterHeaterType != "None") {
                        ReviewRow("Heater Power", "${v.waterHeaterPowerKw} kW")
                        ReviewRow("Heater Use", "${v.waterHeaterDailyHours} hrs/day")
                        ReviewRow("Tank Size", "${v.waterTankSizeLiters} L")
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Step 3 Summary: Lighting
            // ══════════════════════════════════════════════════════════════════
            ReviewSectionCard(
                title = "💡  Lighting Systems",
                stepNumber = 3,
                onEditClick = { onEditStep(3) }
            ) {
                val l = surveyData.lightingInfo
                if (l != null) {
                    ReviewRow("Total Fixtures", l.totalFixtures)
                    ReviewRow("Main Bulb Type", l.mainBulbType)
                    ReviewRow("Daily Usage", "${l.avgDailyUsageHours} hrs")
                    ReviewRow("Energy-Efficient", if (l.mostlyEnergyEfficient) "Yes" else "No")
                    ReviewRow(
                        "Room Breakdown",
                        "LR: ${l.bulbsLivingRoom} · BR: ${l.bulbsBedrooms} · " +
                                "K: ${l.bulbsKitchen} · Bath: ${l.bulbsBathroom} · " +
                                "Hall: ${l.bulbsHallwayOther}"
                    )
                    if (l.avgBulbWattage.isNotBlank()) {
                        ReviewRow("Avg Wattage", "${l.avgBulbWattage} W")
                    }
                    ReviewRow("Outdoor Lighting", if (l.hasOutdoorLighting) "Yes" else "No")
                } else {
                    ReviewEmpty()
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Step 4 Summary: Appliances
            // ══════════════════════════════════════════════════════════════════
            ReviewSectionCard(
                title = "🔌  Appliances & Loads",
                stepNumber = 4,
                onEditClick = { onEditStep(4) }
            ) {
                val a = surveyData.applianceInfo
                if (a != null) {
                    ReviewRow("Fridges", "${a.refrigeratorsCount} · ${a.refrigeratorHoursPerDay} hrs/day")
                    ReviewRow("Washing", "${a.washingMachinesCount} · ${a.washingMachineUsesPerWeek} uses/wk")
                    ReviewRow("TVs", "${a.tvCount} · ${a.tvHoursPerDay} hrs/day")

                    // Build a list of existing kitchen appliances
                    val kitchen = mutableListOf<String>()
                    if (a.microwaveExists) kitchen.add("Microwave")
                    if (a.ovenExists) kitchen.add("Oven")
                    if (a.dishwasherExists) kitchen.add("Dishwasher")
                    if (a.waterPumpExists) kitchen.add("Water Pump")
                    ReviewRow(
                        "Kitchen & Other",
                        if (kitchen.isNotEmpty()) kitchen.joinToString(", ") else "None"
                    )

                    if (a.computersCount.isNotBlank()) {
                        ReviewRow("Computers", a.computersCount)
                    }
                    if (a.otherAppliancesNotes.isNotBlank()) {
                        ReviewRow("Notes", a.otherAppliancesNotes)
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Step 5 Summary: Energy Bills & Habits
            // ══════════════════════════════════════════════════════════════════
            ReviewSectionCard(
                title = "💡  Energy Bills & Habits",
                stepNumber = 5,
                onEditClick = { onEditStep(5) }
            ) {
                val c = surveyData.consumptionInfo
                if (c != null) {
                    ReviewRow("Avg Monthly Bill", "${c.averageMonthlyBill} ${c.billCurrency}")
                    if (c.highestMonthlyBill.isNotBlank()) {
                        ReviewRow("Highest Bill", "${c.highestMonthlyBill} ${c.billCurrency}")
                    }
                    ReviewRow("Has Past Bills", if (c.hasPreviousBills) "Yes" else "No")

                    ReviewRow("Uses Generator", if (c.usesGenerator) "Yes" else "No")
                    if (c.usesGenerator) {
                        ReviewRow("Generator Amps", c.generatorAmperage)
                        ReviewRow("Generator Hours", "${c.generatorHoursPerDay} hrs/day")
                        if (c.generatorMonthlyBill.isNotBlank()) {
                            ReviewRow(
                                "Generator Bill",
                                "${c.generatorMonthlyBill} ${c.generatorBillCurrency}"
                            )
                        }
                    }

                    ReviewRow("Peak Usage", c.peakUsagePeriod)
                    ReviewRow("Daytime Occupancy", c.daytimeOccupancy)
                    ReviewRow("Heavy Weekend Use", if (c.heavyWeekendUsage) "Yes" else "No")
                    ReviewRow("Frequent AC Use", if (c.frequentAcUse) "Yes" else "No")

                    ReviewRow("Tries to Save", if (c.triesToSaveEnergy) "Yes" else "No")
                    ReviewRow("Turns Off Unused", if (c.turnsOffUnusedAppliances) "Yes" else "No")
                    ReviewRow("Wants Tips", if (c.interestedInRecommendations) "Yes" else "No")
                    ReviewRow("Has Solar", if (c.hasSolarSystem) "Yes" else "No")
                    if (c.hasSolarSystem && c.solarInverterSizeKw.isNotBlank()) {
                        ReviewRow("Solar Inverter", "${c.solarInverterSizeKw} kW")
                    }
                } else {
                    ReviewEmpty()
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Final Notes & Confirmation
            // ══════════════════════════════════════════════════════════════════
            SurveySectionCard(title = "📝  Final Notes & Confirmation") {

                // Optional notes
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Additional Notes (optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SurveyTextDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = state.finalNotes,
                        onValueChange = { state = state.copy(finalNotes = it) },
                        placeholder = {
                            Text(
                                "e.g. house recently renovated, new AC installed…",
                                color = Color(0xFFBDBDBD),
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SurveyGreenPrimary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = SurveyGreenPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = SurveyTextDark
                        )
                    )
                }

                // Confirm accuracy toggle
                SurveyFormToggle(
                    label = "I confirm the data is accurate",
                    description = "You must confirm before submitting the survey",
                    checked = state.confirmAccuracy,
                    onCheckedChange = {
                        state = state.copy(confirmAccuracy = it, showErrors = false)
                    }
                )

                // Error message if not confirmed
                if (state.showErrors && !state.confirmAccuracy) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = SurveyErrorRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Please confirm the data is accurate before submitting.",
                            fontSize = 12.sp,
                            color = SurveyErrorRed
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Navigation Buttons
            // ══════════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SurveyGreenPrimary
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "BACK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SurveyGreenPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Save,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "DRAFT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Button(
                    onClick = {
                        if (state.isValid()) onSubmit(state)
                        else state = state.copy(showErrors = true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE65100)
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "SUBMIT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Review Section Card — like SurveySectionCard but with an Edit button
// ─────────────────────────────────────────────────────────────────────────────
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
        colors = CardDefaults.cardColors(containerColor = SurveyCardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header row: green bar + title + Edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(SurveyGreenPrimary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurveyGreenDark,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Step $stepNumber",
                        tint = SurveyGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Edit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SurveyGreenPrimary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Review Row — single label + value pair
// ─────────────────────────────────────────────────────────────────────────────
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
            color = SurveyTextGray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value.ifBlank { "—" },
            fontSize = 13.sp,
            color = SurveyTextDark,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Review Empty — shown when a step hasn't been completed
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ReviewEmpty() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "This section has not been completed yet. Tap Edit to fill it in.",
            fontSize = 12.sp,
            color = Color(0xFFE65100)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReviewSurveyScreenPreview() {
    MaterialTheme {
        ReviewSurveyScreen(
            surveyData = SurveyDataContainer(),
            onBackClick = {},
            onEditStep = {},
            onSubmit = {},
            onSaveDraft = {}
        )
    }
}