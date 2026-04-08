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
import com.univ.energymonitor.domain.model.LampInfo
import com.univ.energymonitor.ui.state.LightingSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightingSurveyScreen(
    initialState: LightingSurveyUiState = LightingSurveyUiState(),
    onBackClick: () -> Unit,
    onNextClick: (LightingSurveyUiState) -> Unit,
    onSaveDraft: (LightingSurveyUiState) -> Unit
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
                        Text("Step 3 of 6 – Lighting Systems", fontSize = 12.sp, color = TextGray)
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
            SurveyStepProgressBar(currentStep = 3, totalSteps = 6)

            // ── Indoor Lamp Count ────────────────────────────────────────
            SurveySectionCard(title = "💡  Indoor Lighting") {
                SurveyFormTextField(
                    label = "Number of Indoor Lamps / Fixtures",
                    value = state.numberOfIndoorLamps,
                    onValueChange = {
                        state = state.copy(numberOfIndoorLamps = it).withUpdatedIndoorCount()
                    },
                    placeholder = "e.g. 10",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && (state.numberOfIndoorLamps.isBlank()
                            || state.numberOfIndoorLamps.toIntOrNull()?.let { it !in 1..100 } ?: true),
                    errorText = "Enter 1–100"
                )
            }

            // ── Dynamic card for each indoor lamp ────────────────────────
            state.indoorLamps.forEachIndexed { index, lamp ->
                SurveySectionCard(title = "💡  Lamp ${index + 1}") {

                    SurveyFormTextField(
                        label = "Room / Location",
                        value = lamp.roomName,
                        onValueChange = {
                            state = state.copy(
                                indoorLamps = state.indoorLamps.toMutableList().also { list ->
                                    list[index] = lamp.copy(roomName = it)
                                }
                            )
                        },
                        placeholder = "e.g. Living Room, Kitchen",
                        isError = state.showErrors && lamp.roomName.isBlank(),
                        errorText = "Required"
                    )

                    SurveyFormDropdown(
                        label = "Bulb Type",
                        options = listOf("LED", "CFL", "Incandescent", "Fluorescent", "Halogen", "Other"),
                        selected = lamp.bulbType,
                        onSelected = {
                            state = state.copy(
                                indoorLamps = state.indoorLamps.toMutableList().also { list ->
                                    list[index] = lamp.copy(bulbType = it)
                                }
                            )
                        },
                        isError = state.showErrors && lamp.bulbType.isBlank(),
                        errorText = "Required"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Power (W)",
                            value = lamp.powerWatts,
                            onValueChange = {
                                state = state.copy(
                                    indoorLamps = state.indoorLamps.toMutableList().also { list ->
                                        list[index] = lamp.copy(powerWatts = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 9",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (lamp.powerWatts.isBlank()
                                    || lamp.powerWatts.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Required"
                        )
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Hours/Day",
                            value = lamp.dailyUsageHours,
                            onValueChange = {
                                state = state.copy(
                                    indoorLamps = state.indoorLamps.toMutableList().also { list ->
                                        list[index] = lamp.copy(dailyUsageHours = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 6",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (lamp.dailyUsageHours.isBlank()
                                    || lamp.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                            errorText = "0–24 hrs"
                        )
                    }

                    SurveyFormToggle(
                        label = "Dimmable?",
                        description = "Can this lamp be dimmed",
                        checked = lamp.isDimmable,
                        onCheckedChange = {
                            state = state.copy(
                                indoorLamps = state.indoorLamps.toMutableList().also { list ->
                                    list[index] = lamp.copy(isDimmable = it)
                                }
                            )
                        }
                    )
                }
            }

            // ── Outdoor Lighting Toggle ──────────────────────────────────
            SurveySectionCard(title = "🌙  Outdoor Lighting") {
                SurveyFormToggle(
                    label = "Outdoor Lighting Available?",
                    description = "Garden, entrance, balcony, or external lights",
                    checked = state.hasOutdoorLighting,
                    onCheckedChange = {
                        state = state.copy(
                            hasOutdoorLighting = it,
                            numberOfOutdoorLamps = if (!it) "" else state.numberOfOutdoorLamps,
                            outdoorLamps = if (!it) emptyList() else state.outdoorLamps
                        )
                    }
                )

                if (!state.hasOutdoorLighting) {
                    SurveyInfoHint(text = "No outdoor lighting — skipping outdoor details.")
                }

                if (state.hasOutdoorLighting) {
                    SurveyFormTextField(
                        label = "Number of Outdoor Lamps",
                        value = state.numberOfOutdoorLamps,
                        onValueChange = {
                            state = state.copy(numberOfOutdoorLamps = it).withUpdatedOutdoorCount()
                        },
                        placeholder = "e.g. 3",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.numberOfOutdoorLamps.isBlank()
                                || state.numberOfOutdoorLamps.toIntOrNull()?.let { it !in 1..50 } ?: true),
                        errorText = "Enter 1–50"
                    )
                }
            }

            // ── Dynamic cards for outdoor lamps ──────────────────────────
            if (state.hasOutdoorLighting) {
                state.outdoorLamps.forEachIndexed { index, lamp ->
                    SurveySectionCard(title = "🌙  Outdoor Lamp ${index + 1}") {

                        SurveyFormDropdown(
                            label = "Bulb Type",
                            options = listOf("LED", "CFL", "Incandescent", "Fluorescent", "Halogen", "Solar-powered", "Other"),
                            selected = lamp.bulbType,
                            onSelected = {
                                state = state.copy(
                                    outdoorLamps = state.outdoorLamps.toMutableList().also { list ->
                                        list[index] = lamp.copy(bulbType = it)
                                    }
                                )
                            },
                            isError = state.showErrors && lamp.bulbType.isBlank(),
                            errorText = "Required"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Power (W)",
                                value = lamp.powerWatts,
                                onValueChange = {
                                    state = state.copy(
                                        outdoorLamps = state.outdoorLamps.toMutableList().also { list ->
                                            list[index] = lamp.copy(powerWatts = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 15",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (lamp.powerWatts.isBlank()
                                        || lamp.powerWatts.toDoubleOrNull()?.let { it <= 0 } ?: true),
                                errorText = "Required"
                            )
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours/Day",
                                value = lamp.dailyUsageHours,
                                onValueChange = {
                                    state = state.copy(
                                        outdoorLamps = state.outdoorLamps.toMutableList().also { list ->
                                            list[index] = lamp.copy(dailyUsageHours = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 5",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (lamp.dailyUsageHours.isBlank()
                                        || lamp.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "0–24 hrs"
                            )
                        }

                        SurveyFormToggle(
                            label = "Dimmable?",
                            checked = lamp.isDimmable,
                            onCheckedChange = {
                                state = state.copy(
                                    outdoorLamps = state.outdoorLamps.toMutableList().also { list ->
                                        list[index] = lamp.copy(isDimmable = it)
                                    }
                                )
                            }
                        )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LightingSurveyScreenPreview() {
    MaterialTheme { LightingSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}