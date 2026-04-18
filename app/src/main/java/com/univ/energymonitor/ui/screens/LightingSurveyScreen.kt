package com.univ.energymonitor.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.model.IndirectLampInfo
import com.univ.energymonitor.domain.model.LampInfo
import com.univ.energymonitor.ui.state.LightingSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.*
import com.univ.energymonitor.ui.components.*

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PrimaryGreen)
                    }
                },
                title = {
                    Column {
                        Text("Household Survey", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                        Text("Step 3 of 6 – Lighting Systems", fontSize = 12.sp, color = TextGray)
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
            SurveyStepProgressBar(currentStep = 3, totalSteps = 6)

            // ── Direct Lighting ──────────────────────────────────────────
            SurveySectionCard(title = "💡  Direct Lighting") {
                SurveyInfoHint(text = "Direct = ceiling lamps, spotlights, downlights pointing down.")
                Spacer(Modifier.height(8.dp))

                SurveyFormTextField(
                    label = "Number of Direct Lamps",
                    value = state.numberOfDirectLamps,
                    onValueChange = { state = state.copy(numberOfDirectLamps = it) },
                    placeholder = "e.g. 15",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && state.numberOfDirectLamps.toIntOrNull()?.let { it < 0 } ?: true,
                    errorText = "Required"
                )

                if ((state.numberOfDirectLamps.toIntOrNull() ?: 0) > 0) {
                    SurveyFormTextField(
                        label = "How many DIFFERENT types of direct lamps?",
                        value = state.numberOfDirectTypes,
                        onValueChange = { state = state.copy(numberOfDirectTypes = it).withUpdatedDirectTypes() },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.numberOfDirectTypes.toIntOrNull() ?: 0) !in 1..10,
                        errorText = "Enter 1–10"
                    )
                }
            }

            state.directLampSamples.forEachIndexed { index, lamp ->
                val totalDirect = state.numberOfDirectLamps.toIntOrNull() ?: 0
                val typesCount = state.directLampSamples.size
                val countPerType = if (typesCount > 0) totalDirect / typesCount else 0
                DirectLampCard(
                    title = "🔆  Direct Type ${index + 1}",
                    lamp = lamp,
                    showErrors = state.showErrors,
                    totalCount = countPerType,
                    isMultipleTypes = typesCount > 1,
                    onChange = { updated ->
                        state = state.copy(directLampSamples = state.directLampSamples.toMutableList().also { it[index] = updated })
                    }
                )
            }

            // ── Indirect Lighting ────────────────────────────────────────
            SurveySectionCard(title = "✨  Indirect Lighting") {
                SurveyInfoHint(text = "Indirect = LED strips, cove lighting, wall washers, ambient lighting.")
                Spacer(Modifier.height(8.dp))

                SurveyFormToggle(
                    label = "Do you have indirect lighting?",
                    checked = state.hasIndirectLighting,
                    onCheckedChange = {
                        state = state.copy(
                            hasIndirectLighting = it,
                            numberOfIndirectRooms = if (!it) "" else state.numberOfIndirectRooms,
                            indirectRooms = if (!it) emptyList() else state.indirectRooms
                        )
                    }
                )

                if (state.hasIndirectLighting) {
                    SurveyFormTextField(
                        label = "In how many rooms do you have indirect lighting?",
                        value = state.numberOfIndirectRooms,
                        onValueChange = { state = state.copy(numberOfIndirectRooms = it).withUpdatedIndirectRooms() },
                        placeholder = "e.g. 2",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.numberOfIndirectRooms.toIntOrNull() ?: 0) !in 1..20,
                        errorText = "Enter 1–20"
                    )
                }
            }

            // Indirect lamp cards per room
            if (state.hasIndirectLighting) {
                state.indirectRooms.forEachIndexed { index, room ->
                    IndirectRoomCard(
                        title = "✨  Indirect Room ${index + 1}",
                        room = room,
                        showErrors = state.showErrors,
                        onChange = { updated ->
                            state = state.copy(
                                indirectRooms = state.indirectRooms.toMutableList().also { it[index] = updated }
                            )
                        }
                    )
                }
            }

            // ── Outdoor Lighting ─────────────────────────────────────────
            SurveySectionCard(title = "🌙  Outdoor Lighting") {
                SurveyFormToggle(
                    label = "Outdoor Lighting Available?",
                    description = "Garden, entrance, balcony, external lights",
                    checked = state.hasOutdoorLighting,
                    onCheckedChange = {
                        state = state.copy(
                            hasOutdoorLighting = it,
                            numberOfOutdoorLamps = if (!it) "" else state.numberOfOutdoorLamps,
                            outdoorLamps = if (!it) emptyList() else state.outdoorLamps
                        )
                    }
                )
                if (state.hasOutdoorLighting) {
                    SurveyFormTextField(
                        label = "Number of Outdoor Lamps",
                        value = state.numberOfOutdoorLamps,
                        onValueChange = { state = state.copy(numberOfOutdoorLamps = it).withUpdatedOutdoorCount() },
                        placeholder = "e.g. 3",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && (state.numberOfOutdoorLamps.toIntOrNull() ?: 0) !in 1..50,
                        errorText = "Enter 1–50"
                    )
                }
            }

            if (state.hasOutdoorLighting) {
                state.outdoorLamps.forEachIndexed { index, lamp ->
                    OutdoorLampCard(
                        title = "🌙  Outdoor Lamp ${index + 1}",
                        lamp = lamp,
                        showErrors = state.showErrors,
                        onChange = { updated ->
                            state = state.copy(outdoorLamps = state.outdoorLamps.toMutableList().also { it[index] = updated })
                        }
                    )
                }
            }

            // ── Navigation Buttons ───────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun DirectLampCard(
    title: String,
    lamp: LampInfo,
    showErrors: Boolean,
    totalCount: Int,
    isMultipleTypes: Boolean,
    onChange: (LampInfo) -> Unit
) {
    SurveySectionCard(title = title) {
        if (totalCount > 1) {
            if (isMultipleTypes) {
                SurveyInfoHint(
                    text = "ℹ Please take the AVERAGE SIZE lamp of this type and fill in its info ."
                )
            } else {
                SurveyInfoHint(
                    text = "ℹ Enter the info for ONE lamp only — we'll automatically multiply by $totalCount lamps of this type."
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        SurveyFormTextField(
            label = "Room / Location",
            value = lamp.roomName,
            onValueChange = { onChange(lamp.copy(roomName = it)) },
            placeholder = "e.g. Living Room",
            isError = showErrors && lamp.roomName.isBlank(),
            errorText = "Required"
        )
        SurveyFormDropdown(
            label = "Bulb Type",
            options = listOf("LED", "CFL", "Incandescent", "Fluorescent", "Halogen", "Other"),
            selected = lamp.bulbType,
            onSelected = { onChange(lamp.copy(bulbType = it)) },
            isError = showErrors && lamp.bulbType.isBlank(),
            errorText = "Required"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Power (W)",
                value = lamp.powerWatts,
                onValueChange = { onChange(lamp.copy(powerWatts = it)) },
                placeholder = "e.g. 9",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (lamp.powerWatts.toDoubleOrNull() ?: 0.0) <= 0,
                errorText = "Required"
            )
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Hours/Day",
                value = lamp.dailyUsageHours,
                onValueChange = { onChange(lamp.copy(dailyUsageHours = it)) },
                placeholder = "e.g. 6",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (lamp.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                errorText = "0–24"
            )
        }
        SurveyFormToggle(
            label = "Dimmable?",
            checked = lamp.isDimmable,
            onCheckedChange = { onChange(lamp.copy(isDimmable = it)) }
        )
    }
}

@Composable
private fun IndirectRoomCard(
    title: String,
    room: IndirectLampInfo,
    showErrors: Boolean,
    onChange: (IndirectLampInfo) -> Unit
) {
    SurveySectionCard(title = title) {
        SurveyFormTextField(
            label = "Room / Location",
            value = room.roomName,
            onValueChange = { onChange(room.copy(roomName = it)) },
            placeholder = "e.g. Living Room",
            isError = showErrors && room.roomName.isBlank(),
            errorText = "Required"
        )
        SurveyFormTextField(
            label = "Length (meters)",
            value = room.lengthMeters,
            onValueChange = { onChange(room.copy(lengthMeters = it)) },
            placeholder = "e.g. 5",
            keyboardType = KeyboardType.Decimal,
            isError = showErrors && (room.lengthMeters.toDoubleOrNull() ?: 0.0) <= 0,
            errorText = "Enter a valid length"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Power (W)",
                value = room.powerWatts,
                onValueChange = { onChange(room.copy(powerWatts = it)) },
                placeholder = "e.g. 40",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (room.powerWatts.toDoubleOrNull() ?: 0.0) <= 0,
                errorText = "Required"
            )
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Hours/Day",
                value = room.dailyUsageHours,
                onValueChange = { onChange(room.copy(dailyUsageHours = it)) },
                placeholder = "e.g. 4",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (room.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                errorText = "0–24"
            )
        }
    }
}

@Composable
private fun OutdoorLampCard(
    title: String,
    lamp: LampInfo,
    showErrors: Boolean,
    onChange: (LampInfo) -> Unit
) {
    SurveySectionCard(title = title) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Power (W)",
                value = lamp.powerWatts,
                onValueChange = { onChange(lamp.copy(powerWatts = it)) },
                placeholder = "e.g. 15",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (lamp.powerWatts.toDoubleOrNull() ?: 0.0) <= 0,
                errorText = "Required"
            )
            SurveyFormTextField(
                modifier = Modifier.weight(1f),
                label = "Hours/Day",
                value = lamp.dailyUsageHours,
                onValueChange = { onChange(lamp.copy(dailyUsageHours = it)) },
                placeholder = "e.g. 5",
                keyboardType = KeyboardType.Decimal,
                isError = showErrors && (lamp.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                errorText = "0–24"
            )
        }
    }
}