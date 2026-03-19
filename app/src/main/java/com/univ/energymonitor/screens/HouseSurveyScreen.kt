package com.univ.energymonitor.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

// ─────────────────────────────────────────────────────────────────────────────
// UI State — Step 1
// ─────────────────────────────────────────────────────────────────────────────
data class HouseSurveyUiState(
    val houseName: String         = "",
    val location: String          = "",
    val houseType: String         = "",
    val floorNumber: String       = "",
    val totalAreaM2: String       = "",
    val numberOfRooms: String     = "",
    val numberOfOccupants: String = "",
    val wallMaterial: String      = "",
    val wallThickness: String     = "",
    val glassType: String         = "",
    val roofExposure: String      = "",
    val insulationLevel: String   = "",
    val showErrors: Boolean       = false
)

fun HouseSurveyUiState.isValid(): Boolean =
    houseName.isNotBlank() && location.isNotBlank() && houseType.isNotBlank() &&
            floorNumber.isNotBlank() && totalAreaM2.isNotBlank() && numberOfRooms.isNotBlank() &&
            numberOfOccupants.isNotBlank() && wallMaterial.isNotBlank() && wallThickness.isNotBlank() &&
            glassType.isNotBlank() && roofExposure.isNotBlank() && insulationLevel.isNotBlank()

// ─────────────────────────────────────────────────────────────────────────────
// Screen — uses shared components from SharedSurveyComponents.kt
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseSurveyScreen(
    onNextClick: (HouseSurveyUiState) -> Unit,
    onSaveDraft: (HouseSurveyUiState) -> Unit
) {
    var state by remember { mutableStateOf(HouseSurveyUiState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Household Survey",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurveyGreenDark        // from SharedSurveyComponents.kt
                        )
                        Text(
                            "Step 1 of 6 – House Information",
                            fontSize = 12.sp,
                            color = SurveyTextGray          // from SharedSurveyComponents.kt
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

            // Progress bar — from SharedSurveyComponents.kt
            SurveyStepProgressBar(currentStep = 1, totalSteps = 6)

            // ── Section 1: General House Information ──────────────────────────
            SurveySectionCard(title = "🏠  General House Information") {

                SurveyFormTextField(
                    label = "House Name / Reference",
                    value = state.houseName,
                    onValueChange = { state = state.copy(houseName = it) },
                    placeholder = "e.g. Apartment – Achrafieh",
                    isError = state.showErrors && state.houseName.isBlank(),
                    errorText = "Required"
                )
                SurveyFormTextField(
                    label = "Location in Lebanon",
                    value = state.location,
                    onValueChange = { state = state.copy(location = it) },
                    placeholder = "e.g. Beirut, Tripoli",
                    isError = state.showErrors && state.location.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "House Type",
                    options = listOf("Apartment", "House"),
                    selected = state.houseType,
                    onSelected = { state = state.copy(houseType = it) },
                    isError = state.showErrors && state.houseType.isBlank(),
                    errorText = "Required"
                )
                SurveyFormTextField(
                    label = "Floor Number",
                    value = state.floorNumber,
                    onValueChange = { state = state.copy(floorNumber = it) },
                    placeholder = "e.g. 3",
                    keyboardType = KeyboardType.Number,
                    isError = state.showErrors && state.floorNumber.isBlank(),
                    errorText = "Required"
                )
                SurveyFormTextField(
                    label = "Total Area (m²)",
                    value = state.totalAreaM2,
                    onValueChange = { state = state.copy(totalAreaM2 = it) },
                    placeholder = "e.g. 120",
                    keyboardType = KeyboardType.Decimal,
                    isError = state.showErrors && state.totalAreaM2.isBlank(),
                    errorText = "Required"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "No. of Rooms",
                        value = state.numberOfRooms,
                        onValueChange = { state = state.copy(numberOfRooms = it) },
                        placeholder = "e.g. 4",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && state.numberOfRooms.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormTextField(
                        modifier = Modifier.weight(1f),
                        label = "No. of Occupants",
                        value = state.numberOfOccupants,
                        onValueChange = { state = state.copy(numberOfOccupants = it) },
                        placeholder = "e.g. 5",
                        keyboardType = KeyboardType.Number,
                        isError = state.showErrors && state.numberOfOccupants.isBlank(),
                        errorText = "Required"
                    )
                }
            }

            // ── Section 2: Building Envelope ──────────────────────────────────
            SurveySectionCard(title = "🧱  Building Envelope") {

                SurveyFormDropdown(
                    label = "Wall Material",
                    options = listOf("Concrete", "Stone", "Brick", "Other"),
                    selected = state.wallMaterial,
                    onSelected = { state = state.copy(wallMaterial = it) },
                    isError = state.showErrors && state.wallMaterial.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Wall Thickness",
                    options = listOf("<15 cm", "15–20 cm", ">20 cm"),
                    selected = state.wallThickness,
                    onSelected = { state = state.copy(wallThickness = it) },
                    isError = state.showErrors && state.wallThickness.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Glass Type",
                    options = listOf("Single glazing", "Double glazing", "Unknown"),
                    selected = state.glassType,
                    onSelected = { state = state.copy(glassType = it) },
                    isError = state.showErrors && state.glassType.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Roof Exposure",
                    options = listOf("Top floor", "Middle floor", "Ground floor"),
                    selected = state.roofExposure,
                    onSelected = { state = state.copy(roofExposure = it) },
                    isError = state.showErrors && state.roofExposure.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Insulation Level",
                    options = listOf("Poor", "Average", "Good"),
                    selected = state.insulationLevel,
                    onSelected = { state = state.copy(insulationLevel = it) },
                    isError = state.showErrors && state.insulationLevel.isBlank(),
                    errorText = "Required"
                )
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SurveyGreenPrimary)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("SAVE DRAFT", fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
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
fun HouseSurveyScreenPreview() {
    MaterialTheme { HouseSurveyScreen(onNextClick = {}, onSaveDraft = {}) }
}