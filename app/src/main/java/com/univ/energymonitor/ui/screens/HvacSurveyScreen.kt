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
import com.univ.energymonitor.domain.engine.EnergyCalculator.estimateCopFromYear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HvacSurveyScreen(
    initialState: HvacSurveyUiState = HvacSurveyUiState(),
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
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
                            "Step 2 of 6 – HVAC & Water Heating",
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
            SurveyStepProgressBar(currentStep = 2, totalSteps = 6)

            // ── AC Units Count ───────────────────────────────────────────
            SurveySectionCard(title = "❄️  Cooling / AC Systems") {
                SurveyFormTextField(
                    label = "Number of AC Units",
                    value = state.numberOfAcUnits,
                    onValueChange = {
                        state = state.copy(numberOfAcUnits = it).withUpdatedAcCount()
                    },
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

            // ── Dynamic card for each AC unit ────────────────────────────
            state.acUnits.forEachIndexed { index, unit ->
                SurveySectionCard(title = "🌀  AC Unit ${index + 1}") {

                    SurveyFormTextField(
                        label = "Room Name",
                        value = unit.roomName,
                        onValueChange = {
                            state = state.copy(
                                acUnits = state.acUnits.toMutableList().also { list ->
                                    list[index] = unit.copy(roomName = it)
                                }
                            )
                        },
                        placeholder = "e.g. Living Room, Bedroom 2",
                        isError = state.showErrors && unit.roomName.isBlank(),
                        errorText = "Required"
                    )

                    SurveyFormTextField(
                        label = "Room Size (m²)",
                        value = unit.roomSizeM2,
                        onValueChange = {
                            state = state.copy(
                                acUnits = state.acUnits.toMutableList().also { list ->
                                    list[index] = unit.copy(roomSizeM2 = it)
                                }
                            )
                        },
                        placeholder = "e.g. 20",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (unit.roomSizeM2.isBlank()
                                || unit.roomSizeM2.toDoubleOrNull()
                            ?.let { it !in 5.0..200.0 } ?: true),
                        errorText = "Enter 5–200 m²"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SurveyFormTextField(
                            modifier = Modifier.weight(2f),
                            label = "AC Capacity",
                            value = unit.capacityValue,
                            onValueChange = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(capacityValue = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 1.5",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (unit.capacityValue.isBlank()
                                    || unit.capacityValue.toDoubleOrNull()
                                ?.let { it <= 0 } ?: true),
                            errorText = "Required"
                        )
                        SurveyFormDropdown(
                            modifier = Modifier.weight(1f),
                            label = "Unit",
                            options = listOf("kW", "BTU/hr", "Tons"),
                            selected = unit.capacityUnit,
                            onSelected = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(capacityUnit = it)
                                    }
                                )
                            }
                        )
                    }

                    // ── COP section ──────────────────────────────────────
                    SurveyFormToggle(
                        label = "Do you know the COP/EER?",
                        description = "Found on the AC sticker or specs sheet",
                        checked = unit.knowsCop,
                        onCheckedChange = {
                            state = state.copy(
                                acUnits = state.acUnits.toMutableList().also { list ->
                                    list[index] = unit.copy(knowsCop = it)
                                }
                            )
                        }
                    )

                    if (unit.knowsCop) {
                        SurveyFormTextField(
                            label = "COP / EER Value",
                            value = unit.cop,
                            onValueChange = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(cop = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 3.2",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (unit.cop.isBlank()
                                    || unit.cop.toDoubleOrNull()
                                ?.let { it !in 1.0..8.0 } ?: true),
                            errorText = "Enter 1.0–8.0"
                        )
                    } else {
                        SurveyFormDropdown(
                            label = "Year of AC Purchase",
                            options = listOf(
                                "2000–2012", "2012–2015",
                                "2015–2020", "After 2020"
                            ),
                            selected = unit.acYear,
                            onSelected = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(acYear = it)
                                    }
                                )
                            },
                            isError = state.showErrors && unit.acYear.isBlank(),
                            errorText = "Required"
                        )
                        SurveyInfoHint(
                            text = "COP will be estimated: ${estimateCopFromYear(unit.acYear)}"
                        )
                    }

                    // ── Usage hours and days ─────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Usage Hours per Day",
                            value = unit.dailyUsageHours,
                            onValueChange = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(dailyUsageHours = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 8",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (unit.dailyUsageHours.isBlank()
                                    || unit.dailyUsageHours.toDoubleOrNull()
                                ?.let { it !in 0.0..24.0 } ?: true),
                            errorText = "0–24 hrs"
                        )
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Usage Days per Year",
                            value = unit.daysPerYear,
                            onValueChange = {
                                state = state.copy(
                                    acUnits = state.acUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(daysPerYear = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 150",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (unit.daysPerYear.isBlank()
                                    || unit.daysPerYear.toIntOrNull()
                                ?.let { it !in 1..365 } ?: true),
                            errorText = "1–365 days"
                        )
                    }
                }
            }

            // ── Heating Systems ──────────────────────────────────────────
            // ── Heating Systems ──────────────────────────────────────────
            SurveySectionCard(title = "🔥  Heating Systems") {
                SurveyFormDropdown(
                    label = "Heating System Type",
                    options = listOf("AC", "Electric heater", "Diesel", "Gas", "None", "Other"),
                    selected = state.heatingSystemType,
                    onSelected = { state = state.copy(heatingSystemType = it, numberOfHeatingAcUnits = "", heatingAcUnits = emptyList()) },
                    isError = state.showErrors && state.heatingSystemType.isBlank(),
                    errorText = "Required"
                )

                if (state.heatingSystemType == "AC") {
                    SurveyFormTextField(
                        label = "Number of AC Units Used for Heating",
                        value = state.numberOfHeatingAcUnits,
                        onValueChange = {
                            state = state.copy(numberOfHeatingAcUnits = it).withUpdatedHeatingAcCount()
                        },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.numberOfHeatingAcUnits.isBlank()
                                || state.numberOfHeatingAcUnits.toIntOrNull()?.let { it !in 1..20 } ?: true),
                        errorText = "Enter 1–20"
                    )
                }

                if (state.heatingSystemType.isNotBlank() && state.heatingSystemType != "None" && state.heatingSystemType != "AC") {
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
                        label = "Average Daily Heating Usage (hours)",
                        value = state.heatingDailyUsageHours,
                        onValueChange = { state = state.copy(heatingDailyUsageHours = it) },
                        placeholder = "e.g. 4",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.heatingDailyUsageHours.isBlank()
                                || state.heatingDailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                        errorText = "Enter 0–24 hours"
                    )
                }

                if (state.heatingSystemType == "None") {
                    SurveyInfoHint(text = "No heating system — skipping heating details.")
                }
            }

            // ── Dynamic cards for heating AC units ───────────────────────
            if (state.heatingSystemType == "AC") {
                state.heatingAcUnits.forEachIndexed { index, unit ->
                    SurveySectionCard(title = "🔥  Heating AC ${index + 1}") {

                        SurveyFormTextField(
                            label = "Room Name",
                            value = unit.roomName,
                            onValueChange = {
                                state = state.copy(
                                    heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(roomName = it)
                                    }
                                )
                            },
                            placeholder = "e.g. Living Room, Bedroom 2",
                            isError = state.showErrors && unit.roomName.isBlank(),
                            errorText = "Required"
                        )

                        SurveyFormTextField(
                            label = "Room Size (m²)",
                            value = unit.roomSizeM2,
                            onValueChange = {
                                state = state.copy(
                                    heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(roomSizeM2 = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 20",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (unit.roomSizeM2.isBlank()
                                    || unit.roomSizeM2.toDoubleOrNull()?.let { it !in 5.0..200.0 } ?: true),
                            errorText = "Enter 5–200 m²"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(2f),
                                label = "AC Capacity",
                                value = unit.capacityValue,
                                onValueChange = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(capacityValue = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 1.5",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (unit.capacityValue.isBlank()
                                        || unit.capacityValue.toDoubleOrNull()?.let { it <= 0 } ?: true),
                                errorText = "Required"
                            )
                            SurveyFormDropdown(
                                modifier = Modifier.weight(1f),
                                label = "Unit",
                                options = listOf("kW", "BTU/hr", "Tons"),
                                selected = unit.capacityUnit,
                                onSelected = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(capacityUnit = it)
                                        }
                                    )
                                }
                            )
                        }

                        SurveyFormToggle(
                            label = "Do you know the COP/EER?",
                            description = "Found on the AC sticker or specs sheet",
                            checked = unit.knowsCop,
                            onCheckedChange = {
                                state = state.copy(
                                    heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                        list[index] = unit.copy(knowsCop = it)
                                    }
                                )
                            }
                        )

                        if (unit.knowsCop) {
                            SurveyFormTextField(
                                label = "COP / EER Value",
                                value = unit.cop,
                                onValueChange = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(cop = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 3.2",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (unit.cop.isBlank()
                                        || unit.cop.toDoubleOrNull()?.let { it !in 1.0..8.0 } ?: true),
                                errorText = "Enter 1.0–8.0"
                            )
                        } else {
                            SurveyFormDropdown(
                                label = "Year of AC Purchase",
                                options = listOf("2000–2012", "2012–2015", "2015–2020", "After 2020"),
                                selected = unit.acYear,
                                onSelected = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(acYear = it)
                                        }
                                    )
                                },
                                isError = state.showErrors && unit.acYear.isBlank(),
                                errorText = "Required"
                            )
                            SurveyInfoHint(
                                text = "COP will be estimated: ${estimateCopFromYear(unit.acYear)}"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours per Day",
                                value = unit.dailyUsageHours,
                                onValueChange = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(dailyUsageHours = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 6",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (unit.dailyUsageHours.isBlank()
                                        || unit.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "0–24 hrs"
                            )
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Days per Year",
                                value = unit.daysPerYear,
                                onValueChange = {
                                    state = state.copy(
                                        heatingAcUnits = state.heatingAcUnits.toMutableList().also { list ->
                                            list[index] = unit.copy(daysPerYear = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 90",
                                keyboardType = KeyboardType.Number,
                                isError = state.showErrors && (unit.daysPerYear.isBlank()
                                        || unit.daysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true),
                                errorText = "1–365 days"
                            )
                        }
                    }
                }
            }
            // ── Water Heating ────────────────────────────────────────────
            SurveySectionCard(title = "🚿  Water Heating") {
                SurveyFormDropdown(
                    label = "Water Heater Type",
                    options = listOf("Electric", "Solar", "Gas", "None", "Other"),
                    selected = state.waterHeaterType,
                    onSelected = { state = state.copy(waterHeaterType = it) },
                    isError = state.showErrors && state.waterHeaterType.isBlank(),
                    errorText = "Required"
                )
                if (state.waterHeaterType.isNotBlank() && state.waterHeaterType != "None") {
                    SurveyFormTextField(
                        label = "Water Heater Power (kW)",
                        value = state.waterHeaterPowerKw,
                        onValueChange = { state = state.copy(waterHeaterPowerKw = it) },
                        placeholder = "e.g. 2.0",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.waterHeaterPowerKw.isBlank()
                                || state.waterHeaterPowerKw.toDoubleOrNull()
                            ?.let { it <= 0 } ?: true),
                        errorText = "Enter a valid power value"
                    )
                    SurveyFormTextField(
                        label = "Average Daily Usage (hours)",
                        value = state.waterHeaterDailyHours,
                        onValueChange = { state = state.copy(waterHeaterDailyHours = it) },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Decimal,
                        isError = state.showErrors && (state.waterHeaterDailyHours.isBlank()
                                || state.waterHeaterDailyHours.toDoubleOrNull()
                            ?.let { it !in 0.0..24.0 } ?: true),
                        errorText = "Enter 0–24 hours"
                    )
                    SurveyFormTextField(
                        label = "Tank Size (liters)",
                        value = state.waterTankSizeLiters,
                        onValueChange = { state = state.copy(waterTankSizeLiters = it) },
                        placeholder = "e.g. 80",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.waterTankSizeLiters.isBlank()
                                || state.waterTankSizeLiters.toIntOrNull()
                            ?.let { it !in 1..500 } ?: true),
                        errorText = "Enter 1–500 liters"
                    )
                }
                if (state.waterHeaterType == "None") {
                    SurveyInfoHint(text = "No water heater — skipping water heating details.")
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
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "BACK",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
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
                        fontWeight = FontWeight.Bold
                    )
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
                    Text(
                        "NEXT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
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
fun HvacSurveyScreenPreview() {
    MaterialTheme { HvacSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}