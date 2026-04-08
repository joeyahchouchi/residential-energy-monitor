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
import com.univ.energymonitor.domain.model.ApplianceItem
import com.univ.energymonitor.ui.state.ApplianceSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplianceSurveyScreen(
    initialState: ApplianceSurveyUiState = ApplianceSurveyUiState(),
    onBackClick: () -> Unit,
    onNextClick: (ApplianceSurveyUiState) -> Unit,
    onSaveDraft: (ApplianceSurveyUiState) -> Unit
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
                            "Step 4 of 6 – Appliances & Electrical Loads",
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
            SurveyStepProgressBar(currentStep = 4, totalSteps = 6)

            // ── Household Appliances ─────────────────────────────────────
            SurveySectionCard(title = "🔌  Household Appliances") {
                SurveyInfoHint(text = "Toggle ON each appliance that exists in the household, then enter its power and daily usage.")
                Spacer(Modifier.height(8.dp))

                state.appliances.forEachIndexed { index, appliance ->
                    SurveyFormToggle(
                        label = appliance.name,
                        checked = appliance.exists,
                        onCheckedChange = { checked ->
                            state = state.copy(
                                appliances = state.appliances.toMutableList().also { list ->
                                    list[index] = appliance.copy(
                                        exists = checked,
                                        powerWatts = if (!checked) "" else appliance.powerWatts,
                                        dailyUsageHours = if (!checked) "" else appliance.dailyUsageHours
                                    )
                                }
                            )
                        }
                    )

                    if (appliance.exists) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Power (W)",
                                value = appliance.powerWatts,
                                onValueChange = {
                                    state = state.copy(
                                        appliances = state.appliances.toMutableList().also { list ->
                                            list[index] = appliance.copy(powerWatts = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 200",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (appliance.powerWatts.isBlank()
                                        || appliance.powerWatts.toDoubleOrNull()?.let { it <= 0 } ?: true),
                                errorText = "Required"
                            )
                            SurveyFormTextField(
                                modifier = Modifier.weight(1f),
                                label = "Hours/Day",
                                value = appliance.dailyUsageHours,
                                onValueChange = {
                                    state = state.copy(
                                        appliances = state.appliances.toMutableList().also { list ->
                                            list[index] = appliance.copy(dailyUsageHours = it)
                                        }
                                    )
                                },
                                placeholder = "e.g. 8",
                                keyboardType = KeyboardType.Decimal,
                                isError = state.showErrors && (appliance.dailyUsageHours.isBlank()
                                        || appliance.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                                errorText = "0–24 hrs"
                            )
                        }
                    }
                }
            }

            // ── Other / Custom Appliances ────────────────────────────────
            SurveySectionCard(title = "📝  Other Appliances") {
                SurveyInfoHint(text = "Add any appliances not listed above.")
                Spacer(Modifier.height(8.dp))

                state.customAppliances.forEachIndexed { index, custom ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SurveyFormTextField(
                            modifier = Modifier.weight(2f),
                            label = "Name",
                            value = custom.name,
                            onValueChange = {
                                state = state.copy(
                                    customAppliances = state.customAppliances.toMutableList().also { list ->
                                        list[index] = custom.copy(name = it)
                                    }
                                )
                            },
                            placeholder = "e.g. Bread maker",
                            isError = state.showErrors && custom.name.isBlank(),
                            errorText = "Required"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Power (W)",
                            value = custom.powerWatts,
                            onValueChange = {
                                state = state.copy(
                                    customAppliances = state.customAppliances.toMutableList().also { list ->
                                        list[index] = custom.copy(powerWatts = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 800",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (custom.powerWatts.isBlank()
                                    || custom.powerWatts.toDoubleOrNull()?.let { it <= 0 } ?: true),
                            errorText = "Required"
                        )
                        SurveyFormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Hours/Day",
                            value = custom.dailyUsageHours,
                            onValueChange = {
                                state = state.copy(
                                    customAppliances = state.customAppliances.toMutableList().also { list ->
                                        list[index] = custom.copy(dailyUsageHours = it)
                                    }
                                )
                            },
                            placeholder = "e.g. 1",
                            keyboardType = KeyboardType.Decimal,
                            isError = state.showErrors && (custom.dailyUsageHours.isBlank()
                                    || custom.dailyUsageHours.toDoubleOrNull()?.let { it !in 0.0..24.0 } ?: true),
                            errorText = "0–24 hrs"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            state = state.copy(
                                customAppliances = state.customAppliances + ApplianceItem(
                                    name = "",
                                    exists = true
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        Text("+ ADD APPLIANCE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (state.customAppliances.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                state = state.copy(
                                    customAppliances = state.customAppliances.dropLast(1)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("REMOVE LAST", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
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
fun ApplianceSurveyScreenPreview() {
    MaterialTheme { ApplianceSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}