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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.univ.energymonitor.ui.state.HouseSurveyUiState
import com.univ.energymonitor.ui.state.isValid
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
import androidx.compose.runtime.*
import androidx.compose.material3.IconButton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseSurveyScreen(
    initialState: HouseSurveyUiState = HouseSurveyUiState(),
    onBackClick: () -> Unit,
    onNextClick: (HouseSurveyUiState) -> Unit,
    onSaveDraft: (HouseSurveyUiState) -> Unit
) {
    var state by remember(initialState) { mutableStateOf(initialState) }

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
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
                            fontWeight = FontWeight.Companion.Bold,
                            color = DarkGreen
                        )
                        Text("Step 1 of 6 – House Information", fontSize = 12.sp, color = TextGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Companion.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        Column(
            modifier = Modifier.Companion.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurveyStepProgressBar(currentStep = 1, totalSteps = 6)

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
                    options = listOf("Apartment", "House", "Studio", "Villa", "Other"),
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
                    keyboardType = KeyboardType.Companion.Number,
                    isError = state.showErrors && state.floorNumber.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Building Age",
                    options = listOf(
                        "before 2000",
                        "2000–2012" ,
                        "2012–2015" ,
                        "2015–2020" ,
                         "After 2020"
                    ),
                    selected = state.buildingAge,
                    onSelected = { state = state.copy(buildingAge = it) },
                    isError = state.showErrors && state.buildingAge.isBlank(),
                    errorText = "Required"
                )
                SurveyFormTextField(
                    label = "Total Area (m²)",
                    value = state.totalAreaM2,
                    onValueChange = { state = state.copy(totalAreaM2 = it) },
                    placeholder = "e.g. 120",
                    keyboardType = KeyboardType.Companion.Decimal,
                    isError = state.showErrors && state.totalAreaM2.isBlank(),
                    errorText = "Required"
                )
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SurveyFormTextField(
                        modifier = Modifier.Companion.weight(1f),
                        label = "No. of Rooms",
                        value = state.numberOfRooms,
                        onValueChange = { state = state.copy(numberOfRooms = it) },
                        placeholder = "e.g. 4",
                        keyboardType = KeyboardType.Companion.Number,
                        isError = state.showErrors && state.numberOfRooms.isBlank(),
                        errorText = "Required"
                    )
                    SurveyFormTextField(
                        modifier = Modifier.Companion.weight(1f),
                        label = "No. of Occupants",
                        value = state.numberOfOccupants,
                        onValueChange = { state = state.copy(numberOfOccupants = it) },
                        placeholder = "e.g. 5",
                        keyboardType = KeyboardType.Companion.Number,
                        isError = state.showErrors && state.numberOfOccupants.isBlank(),
                        errorText = "Required"
                    )
                }
            }

            SurveySectionCard(title = "🧱  Building Envelope") {
                SurveyFormDropdown(
                    label = "External Wall Material",
                    options = listOf("Cement", "Pumice", "Don't know/Other"),
                    selected = state.wallMaterial,
                    onSelected = {
                        state = state.copy(
                            wallMaterial = it,
                            wallThickness = ""
                        )
                    },
                    isError = state.showErrors && state.wallMaterial.isBlank(),
                    errorText = "Required"
                )

                SurveyFormDropdown(
                    label = "Interior Wall Material",
                    options = listOf("Gypsum Board", "Concrete Block", "Plaster on Brick", "Don't know/Other"),
                    selected = state.interiorWallMaterial,
                    onSelected = { state = state.copy(interiorWallMaterial = it) },
                    isError = state.showErrors && state.interiorWallMaterial.isBlank(),
                    errorText = "Required"
                )
                SurveyInfoHint(text = "💡 If you're unsure about the interior wall, most Lebanese apartments use plaster on brick or concrete block.")

                SurveyFormDropdown(
                    label = "Wall Thickness",
                    options = when (state.wallMaterial) {
                        "Cement" -> listOf("1 cm", "1.5 cm", "2 cm")
                        "Pumice" -> listOf("15 cm", "20 cm", "25 cm")
                        else -> listOf("15 cm", "20 cm", "25 cm", "1 cm", "1.5 cm", "2 cm")
                    },
                    selected = state.wallThickness,
                    onSelected = { state = state.copy(wallThickness = it) },
                    isError = state.showErrors && state.wallThickness.isBlank(),
                    errorText = "Required"
                )
                SurveyInfoHint(text = "💡 If unsure, check the depth of a window frame or door frame — it usually matches the wall thickness.")

                SurveyFormDropdown(
                    label = "Glass Type",
                    options = listOf("Single glazing", "Double glazing", "Triple glazing", "Unknown"),
                    selected = state.glassType,
                    onSelected = { state = state.copy(glassType = it) },
                    isError = state.showErrors && state.glassType.isBlank(),
                    errorText = "Required"
                )
                SurveyInfoHint(text = "💡 If unsure, check the depth of a window frame or door frame — single glazing is thin (~4mm), double glazing is thicker (~20mm) with a visible gap between panes.")

                SurveyFormDropdown(
                    label = "Roof Exposure",
                    options = listOf("Apartment", "Roof Top", "brick-roofed house"),
                    selected = state.roofExposure,
                    onSelected = { state = state.copy(roofExposure = it) },
                    isError = state.showErrors && state.roofExposure.isBlank(),
                    errorText = "Required"
                )
                SurveyFormDropdown(
                    label = "Has Insulation?",
                    options = listOf("Yes", "No"),
                    selected = state.insulationLevel,
                    onSelected = { state = state.copy(insulationLevel = it) },
                    isError = state.showErrors && state.insulationLevel.isBlank(),
                    errorText = "Required"
                )
            }

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onSaveDraft(state) },
                    modifier = Modifier.Companion.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.Companion.size(18.dp))
                    Spacer(Modifier.Companion.width(6.dp))
                    Text(
                        "SAVE DRAFT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Button(
                    onClick = {
                        if (state.isValid()) onNextClick(state) else state =
                            state.copy(showErrors = true)
                    },
                    modifier = Modifier.Companion.weight(1f).height(52.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(
                        "NEXT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.Companion.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.Companion.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.Companion.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HouseSurveyScreenPreview() {
    MaterialTheme { HouseSurveyScreen(onBackClick = {}, onNextClick = {}, onSaveDraft = {}) }
}