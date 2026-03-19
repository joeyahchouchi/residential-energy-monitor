package com.univ.energymonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// SharedSurveyComponents.kt
// All reusable composables used across survey steps live here.
// Steps 1–6 all import from this file — no duplicates anywhere.
// ─────────────────────────────────────────────────────────────────────────────

// Brand colors — single source of truth for the whole survey
val SurveyGreenDark    = Color(0xFF1B5E20)
val SurveyGreenPrimary = Color(0xFF2E7D32)
val SurveyBgGray       = Color(0xFFF5F5F5)
val SurveyCardWhite    = Color.White
val SurveyTextGray     = Color(0xFF757575)
val SurveyTextDark     = Color(0xFF212121)
val SurveyErrorRed     = Color(0xFFD32F2F)

// ─────────────────────────────────────────────────────────────────────────────
// Progress Bar — shows current step and % complete
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyStepProgressBar(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Step $currentStep of $totalSteps",
                fontSize = 12.sp,
                color = SurveyGreenPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${((currentStep.toFloat() / totalSteps) * 100).toInt()}% complete",
                fontSize = 12.sp,
                color = SurveyTextGray
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = SurveyGreenPrimary,
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Card — white rounded card with green left accent bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveySectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurveyCardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(SurveyGreenPrimary, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurveyGreenDark
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text Field — labeled input with error state and numeric keyboard support
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyFormTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorText: String = ""
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) SurveyErrorRed else SurveyTextDark,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFBDBDBD), fontSize = 13.sp) },
            isError = isError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SurveyGreenPrimary,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = SurveyErrorRed,
                cursorColor = SurveyGreenPrimary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = SurveyTextDark)
        )
        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                color = SurveyErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dropdown — labeled dropdown with error state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyFormDropdown(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    isError: Boolean = false,
    errorText: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) SurveyErrorRed else SurveyTextDark,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select an option", color = Color(0xFFBDBDBD), fontSize = 13.sp) },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isError) SurveyErrorRed else SurveyGreenPrimary
                        )
                    }
                },
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SurveyGreenPrimary,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    errorBorderColor = SurveyErrorRed,
                    cursorColor = SurveyGreenPrimary
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = SurveyTextDark)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurveyCardWhite)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                fontSize = 14.sp,
                                color = if (option == selected) SurveyGreenPrimary else SurveyTextDark,
                                fontWeight = if (option == selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                color = SurveyErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toggle — green switch with label and optional description
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyFormToggle(
    label: String,
    description: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) Color(0xFFE8F5E9) else Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SurveyTextDark)
                if (description.isNotBlank()) {
                    Text(
                        description,
                        fontSize = 11.sp,
                        color = SurveyTextGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SurveyCardWhite,
                    checkedTrackColor = SurveyGreenPrimary,
                    uncheckedThumbColor = SurveyCardWhite,
                    uncheckedTrackColor = Color(0xFFBDBDBD)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Info Hint — green info box shown as tips or conditional messages
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyInfoHint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ℹ️", fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = SurveyGreenDark)
    }
}
