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
import com.univ.energymonitor.ui.state.HvacSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*
import com.univ.energymonitor.domain.engine.EnergyCalculator.estimateCopFromAcAge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HvacSurveyScreen(
    initialState: HvacSurveyUiState = HvacSurveyUiState(),
    buildingAge: String = "",
    onBackClick: () -> Unit,
    onNextClick: (HvacSurveyUiState) -> Unit,
    onSaveDraft: (HvacSurveyUiState) -> Unit
) {
    var state by remember(initialState) { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryGreen)
                    }
                },
                title = {
                    Column {
                        Text("Household Survey", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text("Step 2 of 6 – HVAC & Water Heating", fontSize = 12.sp, color = TextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyStepProgressBar(currentStep = 2, totalSteps = 6)

            // ══════════════════════════════════════════════════════════════
            // COOLING / AC SYSTEMS
            // ══════════════════════════════════════════════════════════════
            SurveySectionCard(title = "❄️  Cooling / AC Systems") {
                SurveyFormTextField(
                    label = "Number of AC Units",
                    value = state.numberOfAcUnits,
                    onValueChange = { state = state.copy(numberOfAcUnits = it).withUpdatedAcCount() },
                    placeholder = "e.g. 3",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && (state.numberOfAcUnits.isBlank()
                            || state.numberOfAcUnits.toIntOrNull()?.let { it !in 0..20 } ?: true),
                    errorText = "Enter 0–20"
                )
                if (state.numberOfAcUnits.toIntOrNull() == 0) {
                    SurveyInfoHint(text = "No AC units — skipping AC details.")
                }
            }

            state.acUnits.forEachIndexed { index, unit ->
                AcUnitCard(
                    title = "🌀  AC Unit ${index + 1}",
                    unit = unit,
                    showErrors = state.showErrors,
                    buildingAge = buildingAge,
                    onUnitChange = { updated ->
                        state = state.copy(acUnits = state.acUnits.toMutableList().also { it[index] = updated })
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════
            // HEATING SYSTEMS
            // ══════════════════════════════════════════════════════════════
            SurveySectionCard(title = "🔥  Heating Systems") {
                SurveyFormDropdown(
                    label = "Heating System Type",
                    options = listOf("AC", "Electric Heater", "Gas Heater", "Diesel/Fuel Heater", "None"),
                    selected = state.heatingSystemType,
                    onSelected = {
                        state = state.copy(
                            heatingSystemType = it,
                            numberOfHeatingAcUnits = "", heatingAcUnits = emptyList(),
                            numberOfHeatingUnits = "", heatingPowerKw = "",
                            heatingDailyUsageHours = "", heatingDaysPerYear = "",
                            heatingGasKgPerYear = "", heatingFuelLitersPerYear = ""
                        )
                    },
                    isError = state.showErrors && state.heatingSystemType.isBlank(),
                    errorText = "Required"
                )

                when (state.heatingSystemType) {
                    "AC" -> {
                        SurveyFormTextField(
                            label = "Number of AC Units Used for Heating",
                            value = state.numberOfHeatingAcUnits,
                            onValueChange = { state = state.copy(numberOfHeatingAcUnits = it).withUpdatedHeatingAcCount() },
                            placeholder = "e.g. 2",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (state.numberOfHeatingAcUnits.isBlank()
                                    || state.numberOfHeatingAcUnits.toIntOrNull()?.let { it !in 1..20 } ?: true),
                            errorText = "Enter 1–20"
                        )
                    }
                    "Electric Heater" -> {
                        SurveyFormTextField(
                            label = "Number of Heating Units",
                            value = state.numberOfHeatingUnits,
                            onValueChange = { state = state.copy(numberOfHeatingUnits = it) },
                            placeholder = "e.g. 2",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (state.numberOfHeatingUnits.isBlank()
                                    || state.numberOfHeatingUnits.toIntOrNull()?.let { it !in 1..20 } ?: true),
                            errorText = "Enter 1–20"
                        )
                        SurveyFormTextField(
                            label = "Heater Power (kW)",
                            value = state.heatingPowerKw,
                            onValueChange = { state = state.copy(heatingPowerKw = it) },
                            placeholder = "e.g. 1.5",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.heatingPowerKw.isBlank()
                                    || state.heatingPowerKw.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid power"
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours per Day",
                                value = state.heatingDailyUsageHours,
                                onValueChange = { state = state.copy(heatingDailyUsageHours = it) },
                                placeholder = "e.g. 6",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (state.heatingDailyUsageHours.isBlank()
                                        || state.heatingDailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "0–24 hrs"
                            )
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Days per Year",
                                value = state.heatingDaysPerYear,
                                onValueChange = { state = state.copy(heatingDaysPerYear = it) },
                                placeholder = "e.g. 90",
                                keyboardType = KeyboardType.Number,
                                isError = state.showErrors && (state.heatingDaysPerYear.isBlank()
                                        || state.heatingDaysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true),
                                errorText = "1–365 days"
                            )
                        }
                    }
                    "Gas Heater" -> {
                        SurveyInfoHint(text = "Gas heating is non-electric. Data collected for total energy analysis.")
                        SurveyFormTextField(
                            label = "Gas Consumption (kg per year)",
                            value = state.heatingGasKgPerYear,
                            onValueChange = { state = state.copy(heatingGasKgPerYear = it) },
                            placeholder = "e.g. 200",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.heatingGasKgPerYear.isBlank()
                                    || state.heatingGasKgPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid amount"
                        )
                    }
                    "Diesel/Fuel Heater" -> {
                        SurveyInfoHint(text = "Fuel heating is non-electric. Data collected for total energy analysis.")
                        SurveyFormTextField(
                            label = "Fuel Consumption (liters per year)",
                            value = state.heatingFuelLitersPerYear,
                            onValueChange = { state = state.copy(heatingFuelLitersPerYear = it) },
                            placeholder = "e.g. 500",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.heatingFuelLitersPerYear.isBlank()
                                    || state.heatingFuelLitersPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid amount"
                        )
                    }
                    "None" -> {
                        SurveyInfoHint(text = "No heating system — skipping heating details.")
                    }
                }
            }

            // Heating AC unit cards
            if (state.heatingSystemType == "AC") {
                state.heatingAcUnits.forEachIndexed { index, unit ->
                    AcUnitCard(
                        title = "🔥  Heating AC ${index + 1}",
                        unit = unit,
                        showErrors = state.showErrors,
                        buildingAge = buildingAge,
                        onUnitChange = { updated ->
                            state = state.copy(heatingAcUnits = state.heatingAcUnits.toMutableList().also { it[index] = updated })
                        }
                    )
                }
            }

            // ══════════════════════════════════════════════════════════════
            // WATER HEATING
            // ══════════════════════════════════════════════════════════════
            SurveySectionCard(title = "🚿  Water Heating") {
                SurveyFormDropdown(
                    label = "Water Heater Type",
                    options = listOf("Electrical Resistance", "Solar Heater", "Gas Tank", "Fuel Heating", "None"),
                    selected = state.waterHeaterType,
                    onSelected = {
                        state = state.copy(
                            waterHeaterType = it,
                            waterTankSizeLiters = "", waterTankInsulated = "",
                            waterHeaterPowerKw = "", waterHeaterDailyHours = "",
                            waterHeaterDaysPerYear = "",
                            solarWaterBackupType = "", solarWaterBackupHoursPerDay = "",
                            gasTankKgPerYear = "", fuelLitersPerYear = ""
                        )
                    },
                    isError = state.showErrors && state.waterHeaterType.isBlank(),
                    errorText = "Required"
                )

                if (state.waterHeaterType != "None" && state.waterHeaterType.isNotBlank()) {
                    SurveyFormDropdown(
                        label = "Tank Size",
                        options = listOf("100 L", "150 L", "200 L", "250 L", "300 L", "350 L", "400 L", "450 L"),
                        selected = state.waterTankSizeLiters,
                        onSelected = { state = state.copy(waterTankSizeLiters = it) },
                        isError = state.showErrors && state.waterTankSizeLiters.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormDropdown(
                        label = "Is the tank insulated?",
                        options = listOf("Yes", "No"),
                        selected = state.waterTankInsulated,
                        onSelected = { state = state.copy(waterTankInsulated = it) },
                        isError = state.showErrors && state.waterTankInsulated.isBlank(),
                        errorText = "Required"
                    )
                }

                when (state.waterHeaterType) {
                    "Electrical Resistance" -> {
                        SurveyFormTextField(
                            label = "Heater Power (kW)",
                            value = state.waterHeaterPowerKw,
                            onValueChange = { state = state.copy(waterHeaterPowerKw = it) },
                            placeholder = "e.g. 1.5",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.waterHeaterPowerKw.isBlank()
                                    || state.waterHeaterPowerKw.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid power"
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours per Day",
                                value = state.waterHeaterDailyHours,
                                onValueChange = { state = state.copy(waterHeaterDailyHours = it) },
                                placeholder = "e.g. 2",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (state.waterHeaterDailyHours.isBlank()
                                        || state.waterHeaterDailyHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "0–24 hrs"
                            )
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Days per Year",
                                value = state.waterHeaterDaysPerYear,
                                onValueChange = { state = state.copy(waterHeaterDaysPerYear = it) },
                                placeholder = "e.g. 365",
                                keyboardType = KeyboardType.Number,
                                isError = state.showErrors && (state.waterHeaterDaysPerYear.isBlank()
                                        || state.waterHeaterDaysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true),
                                errorText = "1–365 days"
                            )
                        }
                    }
                    "Solar Heater" -> {
                        SurveyFormDropdown(
                            label = "Does the solar heater have a backup system?",
                            options = listOf("None", "Electric", "Gas", "Diesel"),
                            selected = state.solarWaterBackupType,
                            onSelected = {
                                state = state.copy(
                                    solarWaterBackupType = it,
                                    solarWaterBackupHoursPerDay = if (it == "None") "" else state.solarWaterBackupHoursPerDay
                                )
                            },
                            isError = state.showErrors && state.solarWaterBackupType.isBlank(),
                            errorText = "Required"
                        )
                        if (state.solarWaterBackupType.isNotBlank() && state.solarWaterBackupType != "None") {
                            SurveyFormTextField(
                                label = "Backup Usage (hours per day)",
                                value = state.solarWaterBackupHoursPerDay,
                                onValueChange = { state = state.copy(solarWaterBackupHoursPerDay = it) },
                                placeholder = "e.g. 1.5",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (state.solarWaterBackupHoursPerDay.isBlank()
                                        || state.solarWaterBackupHoursPerDay.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "Enter 0–24 hours"
                            )
                            SurveyInfoHint(
                                text = when (state.solarWaterBackupType) {
                                    "Electric" -> "Electric backup will be counted in your electricity consumption."
                                    "Gas" -> "Gas backup is non-electric. Data collected for total energy analysis."
                                    "Diesel" -> "Diesel backup is non-electric. Data collected for total energy analysis."
                                    else -> ""
                                }
                            )
                        }
                        if (state.solarWaterBackupType == "None") {
                            SurveyInfoHint(text = "Solar water heaters with no backup are fully non-electric.")
                        }
                    }
                    "Gas Tank" -> {
                        SurveyInfoHint(text = "Gas water heating is non-electric. Data collected for total energy analysis.")
                        SurveyFormTextField(
                            label = "Gas Consumption (kg per year)",
                            value = state.gasTankKgPerYear,
                            onValueChange = { state = state.copy(gasTankKgPerYear = it) },
                            placeholder = "e.g. 150",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.gasTankKgPerYear.isBlank()
                                    || state.gasTankKgPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid amount"
                        )
                    }
                    "Fuel Heating" -> {
                        SurveyInfoHint(text = "Fuel water heating is non-electric. Data collected for total energy analysis.")
                        SurveyFormTextField(
                            label = "Fuel Consumption (liters per year)",
                            value = state.fuelLitersPerYear,
                            onValueChange = { state = state.copy(fuelLitersPerYear = it) },
                            placeholder = "e.g. 300",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (state.fuelLitersPerYear.isBlank()
                                    || state.fuelLitersPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Enter a valid amount"
                        )
                    }
                    "None" -> {
                        SurveyInfoHint(text = "No water heater — skipping water heating details.")
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
                    Text("BACK", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("DRAFT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    Text("NEXT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AcUnitCard(
    title: String,
    unit: com.univ.energymonitor.domain.model.AcUnitInfo,
    showErrors: Boolean,
    buildingAge: String = "",
    onUnitChange: (com.univ.energymonitor.domain.model.AcUnitInfo) -> Unit
) {
    SurveySectionCard(title = title) {
        SurveyFormTextField(
            label = "Room Name",
            value = unit.roomName,
            onValueChange = { onUnitChange(unit.copy(roomName = it)) },
            placeholder = "e.g. Living Room, Bedroom 2",
            isError = showErrors && unit.roomName.isBlank(),
            errorText = "Required"
        )
        SurveyFormTextField(
            label = "Room Size (m²)",
            value = unit.roomSizeM2,
            onValueChange = { onUnitChange(unit.copy(roomSizeM2 = it)) },
            placeholder = "e.g. 20",
            keyboardType = KeyboardType.Decimal,
            isError = showErrors && (unit.roomSizeM2.isBlank()
                    || unit.roomSizeM2.toDoubleOrNull()?.let { it !in 5.0..200.0 } ?: true),
            errorText = "Enter 5–200 m²"
        )
        SurveyFormDropdown(
            label = "AC Type",
            options = listOf("Local (Split/Window)", "Central"),
            selected = unit.acType,
            onSelected = { onUnitChange(unit.copy(acType = it)) },
            isError = showErrors && unit.acType.isBlank(),
            errorText = "Required"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFormTextField(
                modifier = Modifier.weight(2f),
                label = "AC Capacity (optional)",
                value = unit.capacityValue,
                onValueChange = { onUnitChange(unit.copy(capacityValue = it)) },
                placeholder = "e.g. 1.5",
                keyboardType = KeyboardType.Decimal
            )
            SurveyFormDropdown(
                modifier = Modifier.weight(1f),
                label = "Unit",
                options = listOf("kW", "BTU/hr", "Tons"),
                selected = unit.capacityUnit,
                onSelected = { onUnitChange(unit.copy(capacityUnit = it)) }
            )
        }
        if (unit.capacityValue.isBlank()) {
            val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: 0.0
            if (roomSize > 0) {
                val estimatedBtu = when {
                    roomSize <= 22 -> "9,000"
                    roomSize <= 30 -> "12,000"
                    roomSize <= 45 -> "18,000"
                    else -> "24,000"
                }
                SurveyInfoHint(text = "Capacity will be estimated from room size: $estimatedBtu BTU/h")
            }
        }
        SurveyFormDropdown(
            label = "Do you know the COP/EER?",
            options = listOf("I know the COP", "I know the AC year", "I don't know"),
            selected = unit.copMethod,
            onSelected = { onUnitChange(unit.copy(copMethod = it, cop = "", acYear = "")) },
            isError = showErrors && unit.copMethod.isBlank(),
            errorText = "Required"
        )
        if (unit.copMethod == "I know the COP") {
            SurveyFormTextField(
                label = "COP / EER Value",
                value = unit.cop,
                onValueChange = { onUnitChange(unit.copy(cop = it)) },
                placeholder = "e.g. 3.2",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (unit.cop.isBlank()
                        || unit.cop.toDoubleOrNull()?.let { it !in 1.0..8.0 } ?: true),
                errorText = "Enter 1.0–8.0"
            )
        }
        if (unit.copMethod == "I know the AC year") {
            SurveyFormDropdown(
                label = "AC Age Range",
                options = listOf("Before 2000", "2000–2012", "2012–2015", "2015–2020", "After 2020"),
                selected = unit.acYear,
                onSelected = { onUnitChange(unit.copy(acYear = it)) },
                isError = showErrors && unit.acYear.isBlank(),
                errorText = "Required"
            )
            SurveyInfoHint(text = "COP will be estimated: ${estimateCopFromAcAge(unit.acYear)}")
        }
        if (unit.copMethod == "I don't know") {
            SurveyInfoHint(text = "COP will be estimated from building age: ${estimateCopFromAcAge(buildingAge)}")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Hours per Day",
                value = unit.dailyUsageHours,
                onValueChange = { onUnitChange(unit.copy(dailyUsageHours = it)) },
                placeholder = "e.g. 8",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (unit.dailyUsageHours.isBlank()
                        || unit.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                errorText = "0–24 hrs"
            )
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Days per Year",
                value = unit.daysPerYear,
                onValueChange = { onUnitChange(unit.copy(daysPerYear = it)) },
                placeholder = "e.g. 150",
                keyboardType = KeyboardType.Number,
                isError = showErrors && (unit.daysPerYear.isBlank()
                        || unit.daysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true),
                errorText = "1–365 days"
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HvacSurveyScreenPreview() {
    MaterialTheme { HvacSurveyScreen(buildingAge = "2015–2020", onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}