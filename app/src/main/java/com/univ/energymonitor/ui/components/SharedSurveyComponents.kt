package com.univ.energymonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.ErrorRed
import com.univ.energymonitor.ui.theme.GreenSurface
import com.univ.energymonitor.ui.theme.HintGray
import com.univ.energymonitor.ui.theme.LightDivider
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextDark
import com.univ.energymonitor.ui.theme.TextGray
import androidx.compose.runtime.*
// ─────────────────────────────────────────────────────────────────────────────
// Progress Bar — shows current step and % complete
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyStepProgressBar(currentStep: Int, totalSteps: Int) {
    Column(modifier = Modifier.Companion.fillMaxWidth()) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Step $currentStep of $totalSteps",
                fontSize = 12.sp,
                color = PrimaryGreen,
                fontWeight = FontWeight.Companion.SemiBold
            )
            Text(
                "${((currentStep.toFloat() / totalSteps) * 100).toInt()}% complete",
                fontSize = 12.sp,
                color = TextGray
            )
        }
        Spacer(Modifier.Companion.height(6.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier.Companion.fillMaxWidth().height(6.dp),
            color = PrimaryGreen,
            trackColor = LightDivider
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
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.Companion.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                Box(
                    modifier = Modifier.Companion
                        .width(4.dp)
                        .height(20.dp)
                        .background(
                            PrimaryGreen,
                            androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                        )
                )
                Spacer(Modifier.Companion.width(10.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = DarkGreen
                )
            }
            Spacer(Modifier.Companion.height(16.dp))
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Text Field — labeled input with error state and numeric keyboard support
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyFormTextField(
    modifier: Modifier = Modifier.Companion,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Companion.Text,
    isError: Boolean = false,
    errorText: String = ""
) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Companion.Medium,
            color = if (isError) ErrorRed else TextDark,
            modifier = Modifier.Companion.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = HintGray, fontSize = 13.sp) },
            isError = isError,
            singleLine = true,
            modifier = Modifier.Companion.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = LightDivider,
                errorBorderColor = ErrorRed,
                cursorColor = PrimaryGreen
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextDark)
        )
        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.Companion.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dropdown — labeled dropdown with error state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurveyFormDropdown(
    modifier: Modifier = Modifier.Companion,
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
            fontWeight = FontWeight.Companion.Medium,
            color = if (isError) ErrorRed else TextDark,
            modifier = Modifier.Companion.padding(bottom = 4.dp)
        )
        Box {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select an option", color = HintGray, fontSize = 13.sp) },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isError) ErrorRed else PrimaryGreen
                        )
                    }
                },
                isError = isError,
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = LightDivider,
                    errorBorderColor = ErrorRed,
                    cursorColor = PrimaryGreen
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextDark)
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.Companion.background(Color.Companion.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                option,
                                fontSize = 14.sp,
                                color = if (option == selected) PrimaryGreen else TextDark,
                                fontWeight = if (option == selected) FontWeight.Companion.SemiBold else FontWeight.Companion.Normal
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
                color = ErrorRed,
                fontSize = 11.sp,
                modifier = Modifier.Companion.padding(start = 4.dp, top = 2.dp)
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
        modifier = Modifier.Companion.fillMaxWidth().padding(bottom = 12.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) GreenSurface else Color(0xFFFAFAFA)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Companion.Medium,
                    color = TextDark
                )
                if (description.isNotBlank()) {
                    Text(
                        description,
                        fontSize = 11.sp,
                        color = TextGray,
                        modifier = Modifier.Companion.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Companion.White,
                    checkedTrackColor = PrimaryGreen,
                    uncheckedThumbColor = Color.Companion.White,
                    uncheckedTrackColor = HintGray
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
        modifier = Modifier.Companion
            .fillMaxWidth()
            .background(GreenSurface, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text("ℹ️", fontSize = 14.sp)
        Spacer(Modifier.Companion.width(8.dp))
        Text(text, fontSize = 12.sp, color = DarkGreen)
    }
}