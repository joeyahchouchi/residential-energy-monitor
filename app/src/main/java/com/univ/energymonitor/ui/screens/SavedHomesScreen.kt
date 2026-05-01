package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.data.local.SurveyEntity
import com.univ.energymonitor.ui.navigation.AnalysisType
import com.univ.energymonitor.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedHomesScreen(
    surveys: List<SurveyEntity>,
    onBack: () -> Unit,
    onViewResults: (Long) -> Unit,
    onOpenAnalysis: (Long, AnalysisType) -> Unit,
    onOpenOptimization: (Long) -> Unit,
    onEditSurvey: (Long) -> Unit,
    onDeleteSurvey: (Long) -> Unit
) {
    var surveyToDelete by remember { mutableStateOf<SurveyEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Home Surveys", fontWeight = FontWeight.Bold, color = DarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->
        if (surveys.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No saved surveys found.", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(surveys) { survey ->
                    HomeSurveyCard(
                        survey = survey,
                        onView = { onViewResults(survey.id) },
                        onOpenAnalysis = { type -> onOpenAnalysis(survey.id, type) },
                        onOpenOptimization = { onOpenOptimization(survey.id) },
                        onEdit = { onEditSurvey(survey.id) },
                        onDelete = { surveyToDelete = survey }
                    )
                }
            }
        }

        if (surveyToDelete != null) {
            AlertDialog(
                onDismissRequest = { surveyToDelete = null },
                title = { Text("Delete Survey") },
                text = {
                    Text(
                        "Are you sure you want to delete the survey for '${surveyToDelete?.houseName}'? This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            surveyToDelete?.let { onDeleteSurvey(it.id) }
                            surveyToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { surveyToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeSurveyCard(
    survey: SurveyEntity,
    onView: () -> Unit,
    onOpenAnalysis: (AnalysisType) -> Unit,
    onOpenOptimization: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(survey.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(survey.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(survey.houseName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextGray)
                        Spacer(Modifier.width(4.dp))
                        Text(survey.location, fontSize = 13.sp, color = TextGray)
                    }
                }
                Text(dateStr, fontSize = 12.sp, color = TextGray)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BackgroundGray, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiSmall(modifier = Modifier.weight(1f), label = "kWh/year", value = "%.0f".format(survey.totalYearlyKwh))
                KpiSmall(modifier = Modifier.weight(1f), label = "kg CO₂/year", value = "%.0f".format(survey.totalYearlyCo2Kg))
                KpiSmall(modifier = Modifier.weight(1f), label = "USD/year", value = "$%.0f".format(survey.totalYearlyCostUsd))
            }

            Spacer(Modifier.height(14.dp))

            Text("Project Info", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
            Spacer(Modifier.height(2.dp))
            Text(
                "Tap a section to view graphs and yearly estimates for this home.",
                fontSize = 11.sp,
                color = TextGray,
                lineHeight = 15.sp
            )

            Spacer(Modifier.height(8.dp))

            AnalysisRow(
                icon = Icons.Default.ElectricBolt,
                title = "Electrical Systems",
                subtitle = "Lighting and appliance consumption",
                onClick = { onOpenAnalysis(AnalysisType.ELECTRICAL) }
            )

            AnalysisRow(
                icon = Icons.Default.Thermostat,
                title = "HVAC Analysis",
                subtitle = "Cooling, heating and water heating",
                onClick = { onOpenAnalysis(AnalysisType.HVAC) }
            )

            AnalysisRow(
                icon = Icons.Default.BarChart,
                title = "CO₂ & KPI Reports",
                subtitle = "Energy, cost and emissions summary",
                onClick = { onOpenAnalysis(AnalysisType.KPI) }
            )

            AnalysisRow(
                icon = Icons.Default.AutoFixHigh,
                title = "Save the Planet & Your Wallet",
                subtitle = "Preview upgrades that cut energy, cost, and CO₂",
                containerColor = Color(0xFFE8F5E9),
                iconBackgroundColor = Color(0xFF2E7D32),
                iconTint = Color.White,
                titleColor = Color(0xFF1B5E20),
                arrowTint = Color(0xFF2E7D32),
                onClick = onOpenOptimization
            )


            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onView,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Results")
                }
            }
        }
    }
}

@Composable
private fun AnalysisRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color = Color(0xFFFAFAFA),
    iconBackgroundColor: Color = GreenSurface,
    iconTint: Color = PrimaryGreen,
    titleColor: Color = DarkGreen,
    subtitleColor: Color = TextGray,
    arrowTint: Color = HintGray,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = subtitleColor,
                    lineHeight = 14.sp
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = arrowTint
            )
        }
    }
}

@Composable
private fun KpiSmall(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Column(modifier = modifier) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
        Text(label, fontSize = 10.sp, color = TextGray)
    }
}
