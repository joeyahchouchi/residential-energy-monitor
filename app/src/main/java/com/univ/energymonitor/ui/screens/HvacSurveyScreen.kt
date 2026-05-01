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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.engine.EnergyCalculator.estimateCopFromAcAge
import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.WaterHeaterInfo
import com.univ.energymonitor.ui.components.SurveyFormDropdown
import com.univ.energymonitor.ui.components.SurveyFormTextField
import com.univ.energymonitor.ui.components.SurveyInfoHint
import com.univ.energymonitor.ui.components.SurveySectionCard
import com.univ.energymonitor.ui.components.SurveyStepProgressBar
import com.univ.energymonitor.ui.state.HvacSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.state.withUpdatedAcCount
import com.univ.energymonitor.ui.state.withUpdatedHeatingAcCount
import com.univ.energymonitor.ui.state.withUpdatedWaterHeaterCount
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray

private val HeatingEfficiencyMethodOptions = listOf(
    "I know the efficiency",
    "I know the installation year",
    "I don't know"
)

private val HeatingInstallationYearOptions = listOf(
    "2021–2026",
    "2020",
    "2019",
    "2018",
    "2017",
    "2016",
    "2015",
    "2014",
    "2013",
    "2012",
    "2011",
    "2010",
    "Before 2010"
)

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyStepProgressBar(currentStep = 2, totalSteps = 6)

            SurveySectionCard(title = "❄️  Cooling / AC Systems") {
                SurveyFormTextField(
                    label = "Number of AC Units",
                    value = state.numberOfAcUnits,
                    onValueChange = { state = state.copy(numberOfAcUnits = it).withUpdatedAcCount() },
                    placeholder = "e.g. 3",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && (
                            state.numberOfAcUnits.isBlank() ||
                                    state.numberOfAcUnits.toIntOrNull()?.let { it !in 0..20 } ?: true
                            ),
                    errorText = "Enter 0–20"
                )

                if (state.numberOfAcUnits.toIntOrNull() == 0) {
                    SurveyInfoHint(text = "No AC units — skipping AC details.")
                }
            }

            state.acUnits.forEachIndexed { index, unit ->
                AcUnitCard(
                    title = "AC Unit ${index + 1}",
                    unit = unit,
                    showErrors = state.showErrors,
                    buildingAge = buildingAge,
                    onUnitChange = { updated ->
                        state = state.copy(
                            acUnits = state.acUnits.toMutableList().also { it[index] = updated }
                        )
                    }
                )
            }

            SurveySectionCard(title = "🔥  Heating Systems") {
                SurveyFormDropdown(
                    label = "Heating System Type",
                    options = listOf("AC", "Electric Heater", "Gas Heater", "Diesel/Fuel Heater", "None"),
                    selected = state.heatingSystemType,
                    onSelected = {
                        state = state.copy(
                            heatingSystemType = it,
                            heatedAreaM2 = "",
                            numberOfHeatingAcUnits = "",
                            heatingAcUnits = emptyList(),
                            numberOfHeatingUnits = "",
                            heatingPowerKw = "",
                            heatingDailyUsageHours = "",
                            heatingDaysPerYear = "",
                            heatingGasKgPerYear = "",
                            heatingFuelLitersPerYear = "",
                            heatingEfficiencyMethod = "",
                            heatingEfficiencyPercent = "",
                            heatingInstallationYear = ""
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
                            onValueChange = {
                                state = state.copy(numberOfHeatingAcUnits = it).withUpdatedHeatingAcCount()
                            },
                            placeholder = "e.g. 2",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (
                                    state.numberOfHeatingAcUnits.isBlank() ||
                                            state.numberOfHeatingAcUnits.toIntOrNull()?.let { it !in 1..20 } ?: true
                                    ),
                            errorText = "Enter 1–20"
                        )
                    }

                    "Electric Heater" -> {
                        HeatedAreaField(
                            value = state.heatedAreaM2,
                            showErrors = state.showErrors,
                            onValueChange = { state = state.copy(heatedAreaM2 = it) }
                        )

                        SurveyFormTextField(
                            label = "Number of Heating Units",
                            value = state.numberOfHeatingUnits,
                            onValueChange = { state = state.copy(numberOfHeatingUnits = it) },
                            placeholder = "e.g. 2",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (
                                    state.numberOfHeatingUnits.isBlank() ||
                                            state.numberOfHeatingUnits.toIntOrNull()?.let { it !in 1..20 } ?: true
                                    ),
                            errorText = "Enter 1–20"
                        )

                        SurveyFormTextField(
                            label = "Heater Power (kW)",
                            value = state.heatingPowerKw,
                            onValueChange = { state = state.copy(heatingPowerKw = it) },
                            placeholder = "e.g. 1.5",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (
                                    state.heatingPowerKw.isBlank() ||
                                            state.heatingPowerKw.toDoubleOrNull()?.let { it <= 0 } ?: true
                                    ),
                            errorText = "Enter a valid power"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours per Day",
                                value = state.heatingDailyUsageHours,
                                onValueChange = { state = state.copy(heatingDailyUsageHours = it) },
                                placeholder = "e.g. 6",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (
                                        state.heatingDailyUsageHours.isBlank() ||
                                                state.heatingDailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true
                                        ),
                                errorText = "0–24 hrs"
                            )

                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Days per Year",
                                value = state.heatingDaysPerYear,
                                onValueChange = { state = state.copy(heatingDaysPerYear = it) },
                                placeholder = "e.g. 90",
                                keyboardType = KeyboardType.Number,
                                isError = state.showErrors && (
                                        state.heatingDaysPerYear.isBlank() ||
                                                state.heatingDaysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true
                                        ),
                                errorText = "1–365 days"
                            )
                        }

                        HeatingEfficiencyFields(
                            method = state.heatingEfficiencyMethod,
                            efficiencyPercent = state.heatingEfficiencyPercent,
                            installationYear = state.heatingInstallationYear,
                            showErrors = state.showErrors,
                            onMethodChange = {
                                state = state.copy(
                                    heatingEfficiencyMethod = it,
                                    heatingEfficiencyPercent = "",
                                    heatingInstallationYear = ""
                                )
                            },
                            onEfficiencyChange = { state = state.copy(heatingEfficiencyPercent = it) },
                            onYearChange = { state = state.copy(heatingInstallationYear = it) }
                        )
                    }

                    "Gas Heater" -> {
                        SurveyInfoHint(text = "Gas heating is non-electric. Data collected for total energy analysis.")

                        HeatedAreaField(
                            value = state.heatedAreaM2,
                            showErrors = state.showErrors,
                            onValueChange = { state = state.copy(heatedAreaM2 = it) }
                        )

                        SurveyFormTextField(
                            label = "Gas Consumption (kg per year)",
                            value = state.heatingGasKgPerYear,
                            onValueChange = { state = state.copy(heatingGasKgPerYear = it) },
                            placeholder = "e.g. 200",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (
                                    state.heatingGasKgPerYear.isBlank() ||
                                            state.heatingGasKgPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true
                                    ),
                            errorText = "Enter a valid amount"
                        )

                        HeatingEfficiencyFields(
                            method = state.heatingEfficiencyMethod,
                            efficiencyPercent = state.heatingEfficiencyPercent,
                            installationYear = state.heatingInstallationYear,
                            showErrors = state.showErrors,
                            onMethodChange = {
                                state = state.copy(
                                    heatingEfficiencyMethod = it,
                                    heatingEfficiencyPercent = "",
                                    heatingInstallationYear = ""
                                )
                            },
                            onEfficiencyChange = { state = state.copy(heatingEfficiencyPercent = it) },
                            onYearChange = { state = state.copy(heatingInstallationYear = it) }
                        )
                    }

                    "Diesel/Fuel Heater" -> {
                        SurveyInfoHint(text = "Fuel heating is non-electric. One diesel/fuel tank is considered 20 L.")

                        HeatedAreaField(
                            value = state.heatedAreaM2,
                            showErrors = state.showErrors,
                            onValueChange = { state = state.copy(heatedAreaM2 = it) }
                        )

                        SurveyFormTextField(
                            label = "Number of Diesel/Fuel Tanks per Year",
                            value = state.heatingFuelLitersPerYear,
                            onValueChange = { state = state.copy(heatingFuelLitersPerYear = it) },
                            placeholder = "e.g. 10",
                            keyboardType = KeyboardType.Number,
                            isError = state.showErrors && (
                                    state.heatingFuelLitersPerYear.isBlank() ||
                                            state.heatingFuelLitersPerYear.toDoubleOrNull()?.let { it <= 0 } ?: true
                                    ),
                            errorText = "Enter a valid number of tanks"
                        )

                        HeatingEfficiencyFields(
                            method = state.heatingEfficiencyMethod,
                            efficiencyPercent = state.heatingEfficiencyPercent,
                            installationYear = state.heatingInstallationYear,
                            showErrors = state.showErrors,
                            onMethodChange = {
                                state = state.copy(
                                    heatingEfficiencyMethod = it,
                                    heatingEfficiencyPercent = "",
                                    heatingInstallationYear = ""
                                )
                            },
                            onEfficiencyChange = { state = state.copy(heatingEfficiencyPercent = it) },
                            onYearChange = { state = state.copy(heatingInstallationYear = it) }
                        )
                    }

                    "None" -> {
                        SurveyInfoHint(text = "No heating system — skipping heating details.")
                    }
                }
            }

            if (state.heatingSystemType == "AC") {
                state.heatingAcUnits.forEachIndexed { index, unit ->
                    AcUnitCard(
                        title = "Heating AC ${index + 1}",
                        unit = unit,
                        showErrors = state.showErrors,
                        buildingAge = buildingAge,
                        onUnitChange = { updated ->
                            state = state.copy(
                                heatingAcUnits = state.heatingAcUnits.toMutableList().also { it[index] = updated }
                            )
                        }
                    )
                }
            }

            SurveySectionCard(title = "🚿  Water Heating") {
                SurveyFormTextField(
                    label = "Number of Water Heaters",
                    value = state.numberOfWaterHeaters,
                    onValueChange = {
                        state = state.copy(numberOfWaterHeaters = it).withUpdatedWaterHeaterCount()
                    },
                    placeholder = "e.g. 2",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && (
                            state.numberOfWaterHeaters.isBlank() ||
                                    state.numberOfWaterHeaters.toIntOrNull()?.let { it !in 0..10 } ?: true
                            ),
                    errorText = "Enter 0–10"
                )

                if (state.numberOfWaterHeaters.toIntOrNull() == 0) {
                    SurveyInfoHint(text = "No water heaters — skipping water heating details.")
                }
            }

            state.waterHeaters.forEachIndexed { index, heater ->
                WaterHeaterCard(
                    title = "Water Heater ${index + 1}",
                    heater = heater,
                    showErrors = state.showErrors,
                    onHeaterChange = { updated ->
                        state = state.copy(
                            waterHeaters = state.waterHeaters.toMutableList().also { it[index] = updated }
                        )
                    }
                )
            }

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
private fun HeatedAreaField(
    value: String,
    showErrors: Boolean,
    onValueChange: (String) -> Unit
) {
    SurveyFormTextField(
        label = "Heated Area Covered (m2)",
        value = value,
        onValueChange = onValueChange,
        placeholder = "e.g. 25",
        keyboardType = KeyboardType.Decimal,
        isError = showErrors && (
                value.isBlank() ||
                        value.toDoubleOrNull()?.let { it !in 1.0..1000.0 } ?: true
                ),
        errorText = "Enter 1–1000 m2"
    )
}

@Composable
private fun HeatingEfficiencyFields(
    method: String,
    efficiencyPercent: String,
    installationYear: String,
    showErrors: Boolean,
    onMethodChange: (String) -> Unit,
    onEfficiencyChange: (String) -> Unit,
    onYearChange: (String) -> Unit
) {
    SurveyFormDropdown(
        label = "Do you know the heating system efficiency?",
        options = HeatingEfficiencyMethodOptions,
        selected = method,
        onSelected = onMethodChange,
        isError = showErrors && method.isBlank(),
        errorText = "Required"
    )

    when (method) {
        "I know the efficiency" -> {
            SurveyFormTextField(
                label = "Heating Efficiency (%)",
                value = efficiencyPercent,
                onValueChange = onEfficiencyChange,
                placeholder = "e.g. 82",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (
                        efficiencyPercent.isBlank() ||
                                efficiencyPercent.toDoubleOrNull()?.let { it !in 1.0..100.0 } ?: true
                        ),
                errorText = "Enter 1–100%"
            )
        }

        "I know the installation year" -> {
            SurveyFormDropdown(
                label = "Installation / Model Year",
                options = HeatingInstallationYearOptions,
                selected = installationYear,
                onSelected = onYearChange,
                isError = showErrors && installationYear.isBlank(),
                errorText = "Required"
            )
        }

        "I don't know" -> {
            SurveyInfoHint(
                text = "Efficiency will be estimated using the default old-system value from the heating Excel."
            )
        }
    }
}

@Composable
private fun WaterHeaterCard(
    title: String,
    heater: WaterHeaterInfo,
    showErrors: Boolean,
    onHeaterChange: (WaterHeaterInfo) -> Unit
) {
    SurveySectionCard(title = title) {
        SurveyFormDropdown(
            label = "Water Heater Type",
            options = listOf("Electrical Resistance", "Solar Heater", "Gas Tank", "None"),
            selected = heater.type,
            onSelected = {
                onHeaterChange(
                    heater.copy(
                        type = it,
                        tankSizeLiters = "",
                        tankInsulated = "",
                        powerKw = "",
                        dailyHours = "",
                        daysPerYear = "",
                        solarBackupType = "",
                        solarBackupHoursPerDay = "",
                        solarPanelLengthMeters = "",
                        solarPanelWidthMeters = "",
                        gasTankCountPerYear = "",
                        gasTankCostUsd = ""
                    )
                )
            },
            isError = showErrors && heater.type.isBlank(),
            errorText = "Required"
        )

        if (heater.type != "None" && heater.type.isNotBlank()) {
            SurveyFormDropdown(
                label = "Tank Size",
                options = listOf("100 L", "150 L", "200 L", "250 L", "300 L", "350 L", "400 L", "450 L"),
                selected = heater.tankSizeLiters,
                onSelected = { onHeaterChange(heater.copy(tankSizeLiters = it)) },
                isError = showErrors && heater.tankSizeLiters.isBlank(),
                errorText = "Required"
            )

            SurveyFormDropdown(
                label = "Is the tank insulated?",
                options = listOf("Yes", "No"),
                selected = heater.tankInsulated,
                onSelected = { onHeaterChange(heater.copy(tankInsulated = it)) },
                isError = showErrors && heater.tankInsulated.isBlank(),
                errorText = "Required"
            )
        }

        when (heater.type) {
            "Electrical Resistance" -> {
                SurveyFormTextField(
                    label = "Heater Power (kW)",
                    value = heater.powerKw,
                    onValueChange = { onHeaterChange(heater.copy(powerKw = it)) },
                    placeholder = "e.g. 1.5",
                    keyboardType = KeyboardType.Decimal,
                    isError = showErrors && (
                            heater.powerKw.isBlank() ||
                                    heater.powerKw.toDoubleOrNull()?.let { it <= 0 } ?: true
                            ),
                    errorText = "Enter a valid power"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Hours per Day",
                        value = heater.dailyHours,
                        onValueChange = { onHeaterChange(heater.copy(dailyHours = it)) },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Decimal,
                        isError = showErrors && (
                                heater.dailyHours.isBlank() ||
                                        heater.dailyHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true
                                ),
                        errorText = "0–24 hrs"
                    )

                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "Days per Year",
                        value = heater.daysPerYear,
                        onValueChange = { onHeaterChange(heater.copy(daysPerYear = it)) },
                        placeholder = "e.g. 365",
                        keyboardType = KeyboardType.Number,
                        isError = showErrors && (
                                heater.daysPerYear.isBlank() ||
                                        heater.daysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true
                                ),
                        errorText = "1–365 days"
                    )
                }
            }

            "Solar Heater" -> {
                SurveyFormTextField(
                    label = "Total Panel Length (m)",
                    value = heater.solarPanelLengthMeters,
                    onValueChange = { onHeaterChange(heater.copy(solarPanelLengthMeters = it)) },
                    placeholder = "e.g. 2.0",
                    keyboardType = KeyboardType.Decimal,
                    isError = showErrors && (
                            heater.solarPanelLengthMeters.isBlank() ||
                                    heater.solarPanelLengthMeters.toDoubleOrNull()?.let { it <= 0 } ?: true
                            ),
                    errorText = "Enter a valid length"
                )

                SurveyFormTextField(
                    label = "Total Panel Width (m)",
                    value = heater.solarPanelWidthMeters,
                    onValueChange = { onHeaterChange(heater.copy(solarPanelWidthMeters = it)) },
                    placeholder = "e.g. 1.2",
                    keyboardType = KeyboardType.Decimal,
                    isError = showErrors && (
                            heater.solarPanelWidthMeters.isBlank() ||
                                    heater.solarPanelWidthMeters.toDoubleOrNull()?.let { it <= 0 } ?: true
                            ),
                    errorText = "Enter a valid width"
                )

                SurveyFormDropdown(
                    label = "Does the solar heater have a backup system?",
                    options = listOf("None", "Electric", "Gas", "Diesel"),
                    selected = heater.solarBackupType,
                    onSelected = {
                        onHeaterChange(
                            heater.copy(
                                solarBackupType = it,
                                solarBackupHoursPerDay = if (it == "None") "" else heater.solarBackupHoursPerDay
                            )
                        )
                    },
                    isError = showErrors && heater.solarBackupType.isBlank(),
                    errorText = "Required"
                )

                if (heater.solarBackupType.isNotBlank() && heater.solarBackupType != "None") {
                    SurveyFormTextField(
                        label = "Backup Usage (hours per day)",
                        value = heater.solarBackupHoursPerDay,
                        onValueChange = { onHeaterChange(heater.copy(solarBackupHoursPerDay = it)) },
                        placeholder = "e.g. 1.5",
                        keyboardType = KeyboardType.Decimal,
                        isError = showErrors && (
                                heater.solarBackupHoursPerDay.isBlank() ||
                                        heater.solarBackupHoursPerDay.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true
                                ),
                        errorText = "Enter 0–24 hours"
                    )
                }
            }

            "Gas Tank" -> {
                SurveyFormTextField(
                    label = "Number of Gas Tanks per Year",
                    value = heater.gasTankCountPerYear,
                    onValueChange = { onHeaterChange(heater.copy(gasTankCountPerYear = it)) },
                    placeholder = "e.g. 15",
                    keyboardType = KeyboardType.Number,
                    isError = showErrors && (
                            heater.gasTankCountPerYear.isBlank() ||
                                    heater.gasTankCountPerYear.toIntOrNull()?.let { it <= 0 } ?: true
                            ),
                    errorText = "Enter a valid number of tanks"
                )

                SurveyFormTextField(
                    label = "Gas Tank Cost (USD)",
                    value = heater.gasTankCostUsd,
                    onValueChange = { onHeaterChange(heater.copy(gasTankCostUsd = it)) },
                    placeholder = "e.g. 11.54",
                    keyboardType = KeyboardType.Decimal,
                    isError = showErrors && (
                            heater.gasTankCostUsd.isBlank() ||
                                    heater.gasTankCostUsd.toDoubleOrNull()?.let { it <= 0 } ?: true
                            ),
                    errorText = "Enter a valid cost"
                )
            }

            "None" -> {
                SurveyInfoHint(text = "No water heater details needed.")
            }
        }
    }
}

@Composable
private fun AcUnitCard(
    title: String,
    unit: AcUnitInfo,
    showErrors: Boolean,
    buildingAge: String = "",
    onUnitChange: (AcUnitInfo) -> Unit
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
            isError = showErrors && (
                    unit.roomSizeM2.isBlank() ||
                            unit.roomSizeM2.toDoubleOrNull()?.let { it !in 5.0..200.0 } ?: true
                    ),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                isError = showErrors && (
                        unit.cop.isBlank() ||
                                unit.cop.toDoubleOrNull()?.let { it !in 1.0..8.0 } ?: true
                        ),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Hours per Day",
                value = unit.dailyUsageHours,
                onValueChange = { onUnitChange(unit.copy(dailyUsageHours = it)) },
                placeholder = "e.g. 8",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (
                        unit.dailyUsageHours.isBlank() ||
                                unit.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true
                        ),
                errorText = "0–24 hrs"
            )

            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Days per Year",
                value = unit.daysPerYear,
                onValueChange = { onUnitChange(unit.copy(daysPerYear = it)) },
                placeholder = "e.g. 150",
                keyboardType = KeyboardType.Number,
                isError = showErrors && (
                        unit.daysPerYear.isBlank() ||
                                unit.daysPerYear.toIntOrNull()?.let { it !in 1..365 } ?: true
                        ),
                errorText = "1–365 days"
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HvacSurveyScreenPreview() {
    MaterialTheme {
        HvacSurveyScreen(
            buildingAge = "2015–2020",
            onBackClick = {},
            onNextClick = {},
            onSaveDraft = {}
        )
    }
}
