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

data class HvacSurveyUiState(
    val numberOfAcUnits: String        = "",
    val acType: String                 = "",
    val acCapacityKw: String           = "",
    val acDailyUsageHours: String      = "",
    val acThermostatSetpoint: String   = "",
    val isInverterAc: Boolean          = false,
    val heatingSystemType: String      = "",
    val numberOfHeatingUnits: String   = "",
    val heatingDailyUsageHours: String = "",
    val waterHeaterType: String        = "",
    val waterHeaterPowerKw: String     = "",
    val waterHeaterDailyHours: String  = "",
    val waterTankSizeLiters: String    = "",
    val showErrors: Boolean            = false
)

fun HvacSurveyUiState.isValid(): Boolean {
    if (numberOfAcUnits.isBlank()) return false
    if (acType.isBlank()) return false
    if (acCapacityKw.isBlank()) return false
    if (acDailyUsageHours.isBlank()) return false
    if (acThermostatSetpoint.isBlank()) return false
    if (heatingSystemType.isBlank()) return false
    if (heatingSystemType != "None") {
        if (numberOfHeatingUnits.isBlank()) return false
        if (heatingDailyUsageHours.isBlank()) return false
    }
    if (waterHeaterType.isBlank()) return false
    if (waterHeaterType != "None") {
        if (waterHeaterPowerKw.isBlank()) return false
        if (waterHeaterDailyHours.isBlank()) return false
        if (waterTankSizeLiters.isBlank()) return false
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HvacSurveyScreen(
    onBackClick: () -> Unit,
    onNextClick: (HvacSurveyUiState) -> Unit,
    onSaveDraft: (HvacSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(HvacSurveyUiState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SurveyGreenPrimary) } },
                title = {
                    Column {
                        Text("Household Survey", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SurveyGreenDark)
                        Text("Step 2 of 6 – HVAC & Water Heating", fontSize = 12.sp, color = SurveyTextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurveyCardWhite)
            )
        },
        containerColor = SurveyBgGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyStepProgressBar(currentStep = 2, totalSteps = 6)

            SurveySectionCard(title = "❄️  Cooling / AC Systems") {
                SurveyFormTextField(label = "Number of AC Units", value = state.numberOfAcUnits, onValueChange = { state = state.copy(numberOfAcUnits = it) }, placeholder = "e.g. 3", keyboardType = KeyboardType.Number, isError = state.showErrors && state.numberOfAcUnits.isBlank(), errorText = "Required")
                SurveyFormDropdown(label = "AC Type", options = listOf("Split", "Window", "Central", "Other"), selected = state.acType, onSelected = { state = state.copy(acType = it) }, isError = state.showErrors && state.acType.isBlank(), errorText = "Required")
                SurveyFormTextField(label = "AC Capacity / Power (kW or BTU/hr)", value = state.acCapacityKw, onValueChange = { state = state.copy(acCapacityKw = it) }, placeholder = "e.g. 1.5 kW", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.acCapacityKw.isBlank(), errorText = "Required")
                SurveyFormTextField(label = "Average Daily Usage (hours)", value = state.acDailyUsageHours, onValueChange = { state = state.copy(acDailyUsageHours = it) }, placeholder = "e.g. 8", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.acDailyUsageHours.isBlank(), errorText = "Required")
                SurveyFormTextField(label = "Thermostat Setpoint (°C)", value = state.acThermostatSetpoint, onValueChange = { state = state.copy(acThermostatSetpoint = it) }, placeholder = "e.g. 23", keyboardType = KeyboardType.Number, isError = state.showErrors && state.acThermostatSetpoint.isBlank(), errorText = "Required")
                SurveyFormToggle(label = "Inverter AC?", description = "Inverter ACs are more energy-efficient", checked = state.isInverterAc, onCheckedChange = { state = state.copy(isInverterAc = it) })
            }

            SurveySectionCard(title = "🔥  Heating Systems") {
                SurveyFormDropdown(label = "Heating System Type", options = listOf("Electric heater", "Diesel", "Gas", "None", "Other"), selected = state.heatingSystemType, onSelected = { state = state.copy(heatingSystemType = it) }, isError = state.showErrors && state.heatingSystemType.isBlank(), errorText = "Required")
                if (state.heatingSystemType.isNotBlank() && state.heatingSystemType != "None") {
                    SurveyFormTextField(label = "Number of Heating Units", value = state.numberOfHeatingUnits, onValueChange = { state = state.copy(numberOfHeatingUnits = it) }, placeholder = "e.g. 2", keyboardType = KeyboardType.Number, isError = state.showErrors && state.numberOfHeatingUnits.isBlank(), errorText = "Required")
                    SurveyFormTextField(label = "Average Daily Heating Usage (hours)", value = state.heatingDailyUsageHours, onValueChange = { state = state.copy(heatingDailyUsageHours = it) }, placeholder = "e.g. 4", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.heatingDailyUsageHours.isBlank(), errorText = "Required")
                }
                if (state.heatingSystemType == "None") SurveyInfoHint(text = "No heating system — skipping heating details.")
            }

            SurveySectionCard(title = "🚿  Water Heating") {
                SurveyFormDropdown(label = "Water Heater Type", options = listOf("Electric", "Solar", "Gas", "None", "Other"), selected = state.waterHeaterType, onSelected = { state = state.copy(waterHeaterType = it) }, isError = state.showErrors && state.waterHeaterType.isBlank(), errorText = "Required")
                if (state.waterHeaterType.isNotBlank() && state.waterHeaterType != "None") {
                    SurveyFormTextField(label = "Water Heater Power (kW)", value = state.waterHeaterPowerKw, onValueChange = { state = state.copy(waterHeaterPowerKw = it) }, placeholder = "e.g. 2.0", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.waterHeaterPowerKw.isBlank(), errorText = "Required")
                    SurveyFormTextField(label = "Average Daily Usage (hours)", value = state.waterHeaterDailyHours, onValueChange = { state = state.copy(waterHeaterDailyHours = it) }, placeholder = "e.g. 2", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.waterHeaterDailyHours.isBlank(), errorText = "Required")
                    SurveyFormTextField(label = "Tank Size (liters)", value = state.waterTankSizeLiters, onValueChange = { state = state.copy(waterTankSizeLiters = it) }, placeholder = "e.g. 80", keyboardType = KeyboardType.Number, isError = state.showErrors && state.waterTankSizeLiters.isBlank(), errorText = "Required")
                }
                if (state.waterHeaterType == "None") SurveyInfoHint(text = "No water heater — skipping water heating details.")
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBackClick, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SurveyGreenPrimary)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("BACK", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { onSaveDraft(state) }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = SurveyGreenPrimary)) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("DRAFT", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { if (state.isValid()) onNextClick(state) else state = state.copy(showErrors = true) }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = SurveyGreenPrimary)) {
                    Text("NEXT", fontSize = 13.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
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