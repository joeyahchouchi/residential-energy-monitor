package com.univ.energymonitor.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// UI State — Step 5
// ─────────────────────────────────────────────────────────────────────────────
data class ConsumptionSurveyUiState(
    // ── Electricity Bills ─────────────────────────────────────────────────
    val averageMonthlyBill   : String  = "",
    val highestMonthlyBill   : String  = "",
    val billCurrency         : String  = "",
    val hasPreviousBills     : Boolean = false,

    // ── Generator Use ─────────────────────────────────────────────────────
    val usesGenerator        : Boolean = false,
    val generatorAmperage    : String  = "",
    val generatorMonthlyBill : String  = "",
    val generatorBillCurrency: String  = "",
    val generatorHoursPerDay : String  = "",

    // ── Usage Patterns ────────────────────────────────────────────────────
    val peakUsagePeriod      : String  = "",
    val daytimeOccupancy     : String  = "",
    val heavyWeekendUsage    : Boolean = false,
    val frequentAcUse        : Boolean = false,

    // ── Energy Behavior & Solar ───────────────────────────────────────────
    val triesToSaveEnergy         : Boolean = false,
    val turnsOffUnusedAppliances  : Boolean = false,
    val interestedInRecommendations: Boolean = false,
    val hasSolarSystem            : Boolean = false,
    val solarInverterSizeKw       : String  = "",

    // ── Form state ────────────────────────────────────────────────────────
    val showErrors: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// Validation — only truly essential fields are required
// ─────────────────────────────────────────────────────────────────────────────
fun ConsumptionSurveyUiState.isValid(): Boolean {
    if (averageMonthlyBill.isBlank()) return false
    if (billCurrency.isBlank()) return false
    if (peakUsagePeriod.isBlank()) return false
    if (daytimeOccupancy.isBlank()) return false
    // If they use a generator, amperage and hours are required
    if (usesGenerator) {
        if (generatorAmperage.isBlank()) return false
        if (generatorHoursPerDay.isBlank()) return false
    }
    // If they have solar, inverter size is required
    if (hasSolarSystem) {
        if (solarInverterSizeKw.isBlank()) return false
    }
    return true
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionSurveyScreen(
    initialState: ConsumptionSurveyUiState = ConsumptionSurveyUiState(),
    onBackClick : () -> Unit,
    onNextClick : (ConsumptionSurveyUiState) -> Unit,
    onSaveDraft : (ConsumptionSurveyUiState) -> Unit
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
                            "Step 5 of 6 – Energy Bills & Habits",
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
            SurveyStepProgressBar(currentStep = 5, totalSteps = 6)

            // ══════════════════════════════════════════════════════════════════
            // Section 1: Electricity Bills
            // ══════════════════════════════════════════════════════════════════
            SurveySectionCard(title = "💡  Electricity Bills (EDL)") {

                SurveyInfoHint(
                    text = "Enter your EDL electricity bill amounts. " +
                            "This helps estimate baseline consumption."
                )
                Spacer(Modifier.height(4.dp))

                SurveyFormDropdown(
                    label = "Bill Currency",
                    options = listOf("LBP", "USD", "Mixed"),
                    selected = state.billCurrency,
                    onSelected = { state = state.copy(billCurrency = it) },
                    isError = state.showErrors && state.billCurrency.isBlank(),
                    errorText = "Required"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Avg Monthly Bill",
                        value = state.averageMonthlyBill,
                        onValueChange = { state = state.copy(averageMonthlyBill = it) },
                        placeholder = "e.g. 50",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && state.averageMonthlyBill.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Highest Monthly Bill",
                        value = state.highestMonthlyBill,
                        onValueChange = { state = state.copy(highestMonthlyBill = it) },
                        placeholder = "e.g. 80",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                SurveyFormToggle(
                    label = "Has Previous Bills?",
                    description = "Do you have physical or digital past bills available?",
                    checked = state.hasPreviousBills,
                    onCheckedChange = { state = state.copy(hasPreviousBills = it) }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            // Section 2: Generator Use
            // ══════════════════════════════════════════════════════════════════
            SurveySectionCard(title = "🔌  Generator Use") {

                SurveyFormToggle(
                    label = "Uses a Private Generator?",
                    description = "Most Lebanese homes subscribe to a neighborhood generator",
                    checked = state.usesGenerator,
                    onCheckedChange = { state = state.copy(usesGenerator = it) }
                )

                if (state.usesGenerator) {

                    SurveyFormDropdown(
                        label = "Generator Amperage",
                        options = listOf("5 A", "10 A", "15 A", "20 A", "Other"),
                        selected = state.generatorAmperage,
                        onSelected = { state = state.copy(generatorAmperage = it) },
                        isError = state.showErrors && state.generatorAmperage.isBlank(),
                        errorText = "Required"
                    )

                    SurveyFormTextField(
                        label = "Generator Hours per Day",
                        value = state.generatorHoursPerDay,
                        onValueChange = { state = state.copy(generatorHoursPerDay = it) },
                        placeholder = "e.g. 12",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && state.generatorHoursPerDay.isBlank(),
                        errorText = "Required"
                    )

                    SurveyFormDropdown(
                        label = "Generator Bill Currency",
                        options = listOf("LBP", "USD", "Mixed"),
                        selected = state.generatorBillCurrency,
                        onSelected = { state = state.copy(generatorBillCurrency = it) }
                    )

                    SurveyFormTextField(
                        label = "Generator Monthly Bill",
                        value = state.generatorMonthlyBill,
                        onValueChange = { state = state.copy(generatorMonthlyBill = it) },
                        placeholder = "e.g. 100",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                if (!state.usesGenerator) {
                    SurveyInfoHint(text = "No generator — skipping generator details.")
                }
            }

            // ══════════════════════════════════════════════════════════════════
            // Section 3: Usage Patterns
            // ══════════════════════════════════════════════════════════════════
            SurveySectionCard(title = "🕐  Usage Patterns") {

                SurveyInfoHint(
                    text = "Understanding when energy is used helps identify " +
                            "peak loads and saving opportunities."
                )
                Spacer(Modifier.height(4.dp))

                SurveyFormDropdown(
                    label = "Peak Usage Period",
                    options = listOf(
                        "Morning (6 AM – 12 PM)",
                        "Afternoon (12 PM – 6 PM)",
                        "Evening (6 PM – 12 AM)",
                        "Night (12 AM – 6 AM)",
                        "All day"
                    ),
                    selected = state.peakUsagePeriod,
                    onSelected = { state = state.copy(peakUsagePeriod = it) },
                    isError = state.showErrors && state.peakUsagePeriod.isBlank(),
                    errorText = "Required"
                )

                SurveyFormDropdown(
                    label = "Daytime Occupancy",
                    options = listOf(
                        "Empty most of the day",
                        "1–2 people home",
                        "Most family members home",
                        "Fully occupied all day"
                    ),
                    selected = state.daytimeOccupancy,
                    onSelected = { state = state.copy(daytimeOccupancy = it) },
                    isError = state.showErrors && state.daytimeOccupancy.isBlank(),
                    errorText = "Required"
                )

                SurveyFormToggle(
                    label = "Heavy Weekend Usage?",
                    description = "Energy use is noticeably higher on weekends",
                    checked = state.heavyWeekendUsage,
                    onCheckedChange = { state = state.copy(heavyWeekendUsage = it) }
                )

                SurveyFormToggle(
                    label = "Frequent AC Use?",
                    description = "AC runs most of the day during summer months",
                    checked = state.frequentAcUse,
                    onCheckedChange = { state = state.copy(frequentAcUse = it) }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            // Section 4: Energy Behavior & Solar
            // ══════════════════════════════════════════════════════════════════
            SurveySectionCard(title = "☀️  Energy Behavior & Solar") {

                SurveyFormToggle(
                    label = "Tries to Save Energy?",
                    description = "Actively reduces electricity use (e.g. LED bulbs, shorter AC)",
                    checked = state.triesToSaveEnergy,
                    onCheckedChange = { state = state.copy(triesToSaveEnergy = it) }
                )

                SurveyFormToggle(
                    label = "Turns Off Unused Appliances?",
                    description = "Switches off lights, TV, etc. when leaving a room",
                    checked = state.turnsOffUnusedAppliances,
                    onCheckedChange = { state = state.copy(turnsOffUnusedAppliances = it) }
                )

                SurveyFormToggle(
                    label = "Interested in Recommendations?",
                    description = "Would like tips on reducing energy use and costs",
                    checked = state.interestedInRecommendations,
                    onCheckedChange = {
                        state = state.copy(interestedInRecommendations = it)
                    }
                )

                SurveyFormToggle(
                    label = "Has a Solar System?",
                    description = "Rooftop PV panels or solar water heater installed",
                    checked = state.hasSolarSystem,
                    onCheckedChange = { state = state.copy(hasSolarSystem = it) }
                )

                if (state.hasSolarSystem) {
                    SurveyFormTextField(
                        label = "Solar Inverter Size (kW)",
                        value = state.solarInverterSizeKw,
                        onValueChange = { state = state.copy(solarInverterSizeKw = it) },
                        placeholder = "e.g. 5",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && state.solarInverterSizeKw.isBlank(),
                        errorText = "Required when solar is installed"
                    )
                }

                if (!state.hasSolarSystem) {
                    SurveyInfoHint(text = "No solar system — skipping solar details.")
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
                        if (state.isValid()) onNextClick(state)
                        else state = state.copy(showErrors = true)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurveyGreenPrimary
                    )
                ) {
                    Text(
                        "NEXT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConsumptionSurveyScreenPreview() {
    MaterialTheme {
        ConsumptionSurveyScreen(
            onBackClick = {},
            onNextClick = {},
            onSaveDraft = {}
        )
    }
}