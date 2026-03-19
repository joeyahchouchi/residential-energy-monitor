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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// NOTE: No local color definitions here!
// All colors and shared composables come from SharedSurveyComponents.kt
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────
data class ApplianceSurveyUiState(
    val refrigeratorsCount        : String  = "",
    val refrigeratorHoursPerDay   : String  = "",
    val washingMachinesCount      : String  = "",
    val washingMachineUsesPerWeek : String  = "",
    val tvCount                   : String  = "",
    val tvHoursPerDay             : String  = "",
    val microwaveExists           : Boolean = false,
    val ovenExists                : Boolean = false,
    val dishwasherExists          : Boolean = false,
    val waterPumpExists           : Boolean = false,
    val computersCount            : String  = "",
    val otherAppliancesNotes      : String  = "",
    val showErrors                : Boolean = false
)

fun ApplianceSurveyUiState.isValid(): Boolean {
    if (refrigeratorsCount.isBlank()) return false
    if (refrigeratorHoursPerDay.isBlank()) return false
    if (tvCount.isBlank()) return false
    if (tvHoursPerDay.isBlank()) return false
    return true
}

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplianceSurveyScreen(
    onBackClick : () -> Unit,
    onNextClick : (ApplianceSurveyUiState) -> Unit,
    onSaveDraft : (ApplianceSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(ApplianceSurveyUiState()) }

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
                            "Step 4 of 6 – Appliances & Electrical Loads",
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

            SurveyStepProgressBar(currentStep = 4, totalSteps = 6)

            // ── Section 1: Refrigerator ───────────────────────────────────────
            SurveySectionCard(title = "🧊  Refrigerator") {
                SurveyInfoHint(text = "Most Lebanese homes have 1–2 refrigerators running 24/7.")
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Number of Fridges",
                        value = state.refrigeratorsCount,
                        onValueChange = { state = state.copy(refrigeratorsCount = it) },
                        placeholder = "e.g. 1",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && state.refrigeratorsCount.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Daily Usage (hours)",
                        value = state.refrigeratorHoursPerDay,
                        onValueChange = { state = state.copy(refrigeratorHoursPerDay = it) },
                        placeholder = "e.g. 24",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && state.refrigeratorHoursPerDay.isBlank(),
                        errorText = "Required"
                    )
                }
            }

            // ── Section 2: Washing Machine ────────────────────────────────────
            SurveySectionCard(title = "🫧  Washing Machine") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Number of Machines",
                        value = state.washingMachinesCount,
                        onValueChange = { state = state.copy(washingMachinesCount = it) },
                        placeholder = "e.g. 1",
                        keyboardType = KeyboardType.Number
                    )
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Uses per Week",
                        value = state.washingMachineUsesPerWeek,
                        onValueChange = { state = state.copy(washingMachineUsesPerWeek = it) },
                        placeholder = "e.g. 3",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            // ── Section 3: Television ─────────────────────────────────────────
            SurveySectionCard(title = "📺  Television") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Number of TVs",
                        value = state.tvCount,
                        onValueChange = { state = state.copy(tvCount = it) },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && state.tvCount.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Daily Usage (hours)",
                        value = state.tvHoursPerDay,
                        onValueChange = { state = state.copy(tvHoursPerDay = it) },
                        placeholder = "e.g. 5",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && state.tvHoursPerDay.isBlank(),
                        errorText = "Required"
                    )
                }
            }

            // ── Section 4: Kitchen & Other Appliances ─────────────────────────
            SurveySectionCard(title = "🍳  Kitchen & Other Appliances") {
                SurveyInfoHint(text = "Toggle ON the appliances that exist in this household.")
                Spacer(Modifier.height(8.dp))
                SurveyFormToggle(
                    label = "Microwave",
                    description = "Standard household microwave oven",
                    checked = state.microwaveExists,
                    onCheckedChange = { state = state.copy(microwaveExists = it) }
                )
                SurveyFormToggle(
                    label = "Electric Oven",
                    description = "Built-in or standalone electric oven",
                    checked = state.ovenExists,
                    onCheckedChange = { state = state.copy(ovenExists = it) }
                )
                SurveyFormToggle(
                    label = "Dishwasher",
                    description = "Automatic dishwasher",
                    checked = state.dishwasherExists,
                    onCheckedChange = { state = state.copy(dishwasherExists = it) }
                )
                SurveyFormToggle(
                    label = "Water Pump",
                    description = "Rooftop or basement water pump",
                    checked = state.waterPumpExists,
                    onCheckedChange = { state = state.copy(waterPumpExists = it) }
                )
            }

            // ── Section 5: Computers & Other ──────────────────────────────────
            SurveySectionCard(title = "💻  Computers & Other") {
                SurveyFormTextField(
                    label = "Number of Computers / Laptops",
                    value = state.computersCount,
                    onValueChange = { state = state.copy(computersCount = it) },
                    placeholder = "e.g. 2",
                    keyboardType = KeyboardType.Number
                )
                // Multiline free text field
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Other Appliances (optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SurveyTextDark,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = state.otherAppliancesNotes,
                        onValueChange = { state = state.copy(otherAppliancesNotes = it) },
                        placeholder = {
                            Text(
                                "e.g. electric booster, iron, hair dryer…",
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
            }

            // ── Navigation Buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SurveyGreenPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("BACK", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SurveyGreenPrimary)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("DRAFT", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }
                Button(
                    onClick = {
                        if (state.isValid()) onNextClick(state)
                        else state = state.copy(showErrors = true)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurveyGreenPrimary)
                ) {
                    Text("NEXT", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ApplianceSurveyScreenPreview() {
    MaterialTheme {
        ApplianceSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {})
    }
}