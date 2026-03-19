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

data class LightingSurveyUiState(
    val totalFixtures: String          = "",
    val mainBulbType: String           = "",
    val avgDailyUsageHours: String     = "",
    val mostlyEnergyEfficient: Boolean = false,
    val bulbsLivingRoom: String        = "",
    val bulbsBedrooms: String          = "",
    val bulbsKitchen: String           = "",
    val bulbsBathroom: String          = "",
    val bulbsHallwayOther: String      = "",
    val avgBulbWattage: String         = "",
    val hasOutdoorLighting: Boolean    = false,
    val outdoorLightingHours: String   = "",
    val showErrors: Boolean            = false
)

fun LightingSurveyUiState.isValid(): Boolean {
    if (totalFixtures.isBlank()) return false
    if (mainBulbType.isBlank()) return false
    if (avgDailyUsageHours.isBlank()) return false
    if (bulbsLivingRoom.isBlank()) return false
    if (bulbsBedrooms.isBlank()) return false
    if (hasOutdoorLighting && outdoorLightingHours.isBlank()) return false
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightingSurveyScreen(
    onBackClick: () -> Unit,
    onNextClick: (LightingSurveyUiState) -> Unit,
    onSaveDraft: (LightingSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(LightingSurveyUiState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = SurveyGreenPrimary) } },
                title = {
                    Column {
                        Text("Household Survey", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SurveyGreenDark)
                        Text("Step 3 of 6 – Lighting Systems", fontSize = 12.sp, color = SurveyTextGray)
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
            SurveyStepProgressBar(currentStep = 3, totalSteps = 6)

            SurveySectionCard(title = "💡  General Lighting Overview") {
                SurveyFormTextField(label = "Total Number of Fixtures / Bulbs", value = state.totalFixtures, onValueChange = { state = state.copy(totalFixtures = it) }, placeholder = "e.g. 24", keyboardType = KeyboardType.Number, isError = state.showErrors && state.totalFixtures.isBlank(), errorText = "Required")
                SurveyFormDropdown(label = "Main Bulb Type", options = listOf("LED", "CFL", "Incandescent", "Mixed", "Unknown"), selected = state.mainBulbType, onSelected = { state = state.copy(mainBulbType = it) }, isError = state.showErrors && state.mainBulbType.isBlank(), errorText = "Required")
                SurveyFormTextField(label = "Average Daily Lighting Usage (hours)", value = state.avgDailyUsageHours, onValueChange = { state = state.copy(avgDailyUsageHours = it) }, placeholder = "e.g. 6", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.avgDailyUsageHours.isBlank(), errorText = "Required")
                SurveyFormToggle(label = "Mostly energy-efficient lighting?", description = "e.g. majority of bulbs are LED or CFL", checked = state.mostlyEnergyEfficient, onCheckedChange = { state = state.copy(mostlyEnergyEfficient = it) })
            }

            SurveySectionCard(title = "🏠  Room-Based Lighting Details") {
                SurveyInfoHint(text = "Enter the number of bulbs/fixtures in each room.")
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SurveyFormTextField(modifier = Modifier.weight(1f), label = "Living Room", value = state.bulbsLivingRoom, onValueChange = { state = state.copy(bulbsLivingRoom = it) }, placeholder = "e.g. 6", keyboardType = KeyboardType.Number, isError = state.showErrors && state.bulbsLivingRoom.isBlank(), errorText = "Required")
                    SurveyFormTextField(modifier = Modifier.weight(1f), label = "Bedrooms", value = state.bulbsBedrooms, onValueChange = { state = state.copy(bulbsBedrooms = it) }, placeholder = "e.g. 8", keyboardType = KeyboardType.Number, isError = state.showErrors && state.bulbsBedrooms.isBlank(), errorText = "Required")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SurveyFormTextField(modifier = Modifier.weight(1f), label = "Kitchen", value = state.bulbsKitchen, onValueChange = { state = state.copy(bulbsKitchen = it) }, placeholder = "e.g. 4", keyboardType = KeyboardType.Number)
                    SurveyFormTextField(modifier = Modifier.weight(1f), label = "Bathroom", value = state.bulbsBathroom, onValueChange = { state = state.copy(bulbsBathroom = it) }, placeholder = "e.g. 2", keyboardType = KeyboardType.Number)
                }
                SurveyFormTextField(label = "Hallway / Other Spaces", value = state.bulbsHallwayOther, onValueChange = { state = state.copy(bulbsHallwayOther = it) }, placeholder = "e.g. 3", keyboardType = KeyboardType.Number)
            }

            SurveySectionCard(title = "🔧  Optional Technical Details") {
                SurveyInfoHint(text = "These fields are optional but help improve accuracy.")
                Spacer(Modifier.height(4.dp))
                SurveyFormTextField(label = "Average Bulb Wattage (W) — optional", value = state.avgBulbWattage, onValueChange = { state = state.copy(avgBulbWattage = it) }, placeholder = "e.g. 9 W for LED", keyboardType = KeyboardType.Decimal)
                SurveyFormToggle(label = "Outdoor Lighting Available?", description = "Garden, entrance, balcony, or external lights", checked = state.hasOutdoorLighting, onCheckedChange = { state = state.copy(hasOutdoorLighting = it) })
                if (state.hasOutdoorLighting) {
                    SurveyFormTextField(label = "Outdoor Lighting Daily Usage (hours)", value = state.outdoorLightingHours, onValueChange = { state = state.copy(outdoorLightingHours = it) }, placeholder = "e.g. 5", keyboardType = KeyboardType.Decimal, isError = state.showErrors && state.outdoorLightingHours.isBlank(), errorText = "Required when outdoor lighting is enabled")
                }
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
fun LightingSurveyScreenPreview() {
    MaterialTheme { LightingSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}