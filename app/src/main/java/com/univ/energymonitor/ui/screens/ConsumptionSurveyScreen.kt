package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.ui.state.ConsumptionSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionSurveyScreen(
    initialState: ConsumptionSurveyUiState = ConsumptionSurveyUiState(),
    onBackClick: () -> Unit,
    onNextClick: (ConsumptionSurveyUiState) -> Unit,
    onSaveDraft: (ConsumptionSurveyUiState) -> Unit
) {
    var state by remember(initialState) { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryGreen
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            "Household Survey",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                        Text(
                            "Step 5 of 6 – Electricity Supply Sources",
                            fontSize = 12.sp,
                            color = TextGray
                        )
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
            SurveyStepProgressBar(currentStep = 5, totalSteps = 6)

            // ── EDL Availability ─────────────────────────────────────────
            SurveySectionCard(title = "⚡  EDL Electricity Availability") {
                SurveyFormDropdown(
                    label = "How many hours per day is EDL available in your area?",
                    options = listOf(
                        "0–3 hours", "3–6 hours", "6–9 hours",
                        "9–12 hours", "12–18 hours", "18–24 hours",
                        "24 hours (no cuts)"
                    ),
                    selected = state.edlHoursPerDay,
                    onSelected = { state = state.copy(edlHoursPerDay = it) },
                    isError = state.showErrors && state.edlHoursPerDay.isBlank(),
                    errorText = "Required"
                )
            }

            // ── Electricity Sources ──────────────────────────────────────
            SurveySectionCard(title = "🔌  Electricity Sources") {
                SurveyInfoHint(text = "Select all electricity sources used in your home.")
                Spacer(Modifier.height(8.dp))

                SurveyFormToggle(
                    label = "EDL",
                    description = "Électricité du Liban (public grid)",
                    checked = state.usesEdl,
                    onCheckedChange = {
                        state = state.copy(
                            usesEdl = it,
                            usesNone = if (it) false else state.usesNone,
                            yearlyEdlBillUsd = if (!it) "" else state.yearlyEdlBillUsd,
                            edlPricePerKwhUsd = if (!it) "" else state.edlPricePerKwhUsd
                        )
                    }
                )
                SurveyFormToggle(
                    label = "Private Neighborhood Generator",
                    description = "Subscription to a local diesel generator",
                    checked = state.usesGenerator,
                    onCheckedChange = {
                        state = state.copy(
                            usesGenerator = it,
                            usesNone = if (it) false else state.usesNone,
                            generatorSubscriptionType = if (!it) "" else state.generatorSubscriptionType,
                            yearlyGeneratorBillUsd = if (!it) "" else state.yearlyGeneratorBillUsd,
                            generatorPricePerKwhUsd = if (!it) "" else state.generatorPricePerKwhUsd
                        )
                    }
                )
                SurveyFormToggle(
                    label = "Solar PV System",
                    description = "Rooftop photovoltaic panels",
                    checked = state.usesSolar,
                    onCheckedChange = {
                        state = state.copy(
                            usesSolar = it,
                            usesNone = if (it) false else state.usesNone,
                            solarCapacity = if (!it) "" else state.solarCapacity,
                            solarHasBattery = if (!it) "" else state.solarHasBattery,
                            solarYearlyKwh = if (!it) "" else state.solarYearlyKwh
                        )
                    }
                )
                SurveyFormToggle(
                    label = "UPS / Battery System",
                    description = "Uninterruptible power supply or battery backup",
                    checked = state.usesUps,
                    onCheckedChange = {
                        state = state.copy(
                            usesUps = it,
                            usesNone = if (it) false else state.usesNone
                        )
                    }
                )
                SurveyFormToggle(
                    label = "None",
                    description = "No electricity source available",
                    checked = state.usesNone,
                    onCheckedChange = {
                        state = state.copy(
                            usesNone = it,
                            usesEdl = if (it) false else state.usesEdl,
                            usesGenerator = if (it) false else state.usesGenerator,
                            usesSolar = if (it) false else state.usesSolar,
                            usesUps = if (it) false else state.usesUps,
                            generatorSubscriptionType = if (it) "" else state.generatorSubscriptionType,
                            solarCapacity = if (it) "" else state.solarCapacity,
                            solarHasBattery = if (it) "" else state.solarHasBattery,
                            yearlyEdlBillUsd = if (it) "" else state.yearlyEdlBillUsd,
                            edlPricePerKwhUsd = if (it) "" else state.edlPricePerKwhUsd,
                            yearlyGeneratorBillUsd = if (it) "" else state.yearlyGeneratorBillUsd,
                            generatorPricePerKwhUsd = if (it) "" else state.generatorPricePerKwhUsd,
                            solarYearlyKwh = if (!it) "" else state.solarYearlyKwh
                        )
                    }
                )

                if (state.showErrors && !state.usesEdl && !state.usesGenerator && !state.usesSolar && !state.usesUps && !state.usesNone) {
                    SurveyInfoHint(text = "⚠️ Please select at least one electricity source.")
                }
            }

            // ── EDL Cost Details (conditional) ───────────────────────────
            if (state.usesEdl) {
                SurveySectionCard(title = "⚡  EDL Cost Details") {
                    SurveyInfoHint(text = "Enter your yearly average EDL bill and current price per kWh.")
                    Spacer(Modifier.height(8.dp))

                    SurveyFormTextField(
                        label = "Yearly Average EDL Bill (USD)",
                        value = state.yearlyEdlBillUsd,
                        onValueChange = { state = state.copy(yearlyEdlBillUsd = it) },
                        placeholder = "e.g. 240",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.yearlyEdlBillUsd.isBlank()
                                || state.yearlyEdlBillUsd.toDoubleOrNull()?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid amount"
                    )

                    SurveyFormTextField(
                        label = "EDL Price ($/kWh)",
                        value = state.edlPricePerKwhUsd,
                        onValueChange = { state = state.copy(edlPricePerKwhUsd = it) },
                        placeholder = "e.g. 0.10",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.edlPricePerKwhUsd.isBlank()
                                || state.edlPricePerKwhUsd.toDoubleOrNull()?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid price"
                    )
                }
            }

            // ── Generator Details (conditional) ──────────────────────────
            if (state.usesGenerator) {
                SurveySectionCard(title = "🔋  Generator Subscription") {
                    SurveyFormDropdown(
                        label = "Generator Subscription Type",
                        options = listOf(
                            "Metered (kWh)",
                            "Fixed ampere subscription: 5A",
                            "Fixed ampere subscription: 10A",
                            "Fixed ampere subscription: more than 10A"
                        ),
                        selected = state.generatorSubscriptionType,
                        onSelected = { state = state.copy(generatorSubscriptionType = it) },
                        isError = state.showErrors && state.generatorSubscriptionType.isBlank(),
                        errorText = "Required"
                    )

                    Spacer(Modifier.height(8.dp))
                    SurveyInfoHint(text = "Enter your yearly average generator bill and current price per kWh.")
                    Spacer(Modifier.height(8.dp))

                    SurveyFormTextField(
                        label = "Yearly Average Generator Bill (USD)",
                        value = state.yearlyGeneratorBillUsd,
                        onValueChange = { state = state.copy(yearlyGeneratorBillUsd = it) },
                        placeholder = "e.g. 720",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.yearlyGeneratorBillUsd.isBlank()
                                || state.yearlyGeneratorBillUsd.toDoubleOrNull()?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid amount"
                    )

                    SurveyFormTextField(
                        label = "Generator Price ($/kWh)",
                        value = state.generatorPricePerKwhUsd,
                        onValueChange = { state = state.copy(generatorPricePerKwhUsd = it) },
                        placeholder = "e.g. 0.30",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.generatorPricePerKwhUsd.isBlank()
                                || state.generatorPricePerKwhUsd.toDoubleOrNull()?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid price"
                    )
                }
            }

            // ── Solar Details (conditional) ──────────────────────────────
            if (state.usesSolar) {
                SurveySectionCard(title = "☀️  Solar PV System Details") {
                    SurveyFormDropdown(
                        label = "Solar System Capacity",
                        options = listOf(
                            "Less than 2 kW",
                            "2–5 kW",
                            "5–10 kW",
                            "More than 10 kW"
                        ),
                        selected = state.solarCapacity,
                        onSelected = { state = state.copy(solarCapacity = it) },
                        isError = state.showErrors && state.solarCapacity.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormDropdown(
                        label = "Does your solar system include battery storage?",
                        options = listOf("Yes", "No"),
                        selected = state.solarHasBattery,
                        onSelected = { state = state.copy(solarHasBattery = it) },
                        isError = state.showErrors && state.solarHasBattery.isBlank(),
                        errorText = "Required"
                    )

                    Spacer(Modifier.height(8.dp))

                    SurveyFormTextField(
                        label = "Solar Energy Used Per Year (kWh/year)",
                        value = state.solarYearlyKwh,
                        onValueChange = { state = state.copy(solarYearlyKwh = it) },
                        placeholder = "e.g. 2500",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.solarYearlyKwh.isBlank()
                                || state.solarYearlyKwh.toDoubleOrNull()?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid yearly kWh value"
                    )

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
                        if (state.isValid()) onNextClick(state)
                        else state = state.copy(showErrors = true)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
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
fun ConsumptionSurveyScreenPreview() {
    MaterialTheme { ConsumptionSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}