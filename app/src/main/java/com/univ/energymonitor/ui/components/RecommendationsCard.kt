package com.univ.energymonitor.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.domain.model.Recommendation
import com.univ.energymonitor.domain.model.RecommendationPriority
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextDark
import com.univ.energymonitor.ui.theme.TextGray

private val HighPriorityColor = Color(0xFFE53935)
private val MediumPriorityColor = Color(0xFFFF9800)
private val LowPriorityColor = Color(0xFF4CAF50)

@Composable
fun RecommendationsSection(recommendations: List<Recommendation>) {
    if (recommendations.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(PrimaryGreen, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "💡  Recommendations",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "${recommendations.size} personalized actions to reduce your energy consumption",
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(Modifier.height(16.dp))

            recommendations.forEach { rec ->
                RecommendationCard(rec)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun RecommendationCard(rec: Recommendation) {
    var expanded by remember { mutableStateOf(false) }

    val priorityColor = when (rec.priority) {
        RecommendationPriority.HIGH -> HighPriorityColor
        RecommendationPriority.MEDIUM -> MediumPriorityColor
        RecommendationPriority.LOW -> LowPriorityColor
    }

    val priorityLabel = when (rec.priority) {
        RecommendationPriority.HIGH -> "HIGH PRIORITY"
        RecommendationPriority.MEDIUM -> "MEDIUM"
        RecommendationPriority.LOW -> "LOW"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Header row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(priorityColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(rec.icon, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(priorityColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                priorityLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        rec.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }

                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextGray
                )
            }

            // Savings row
            if (rec.estimatedYearlyUsdSaved > 0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SavingsChip(
                        icon = "⚡",
                        value = "${rec.estimatedYearlyKwhSaved.toInt()} kWh",
                        modifier = Modifier.weight(1f)
                    )
                    SavingsChip(
                        icon = "💰",
                        value = "$${String.format("%.0f", rec.estimatedYearlyUsdSaved)}",
                        modifier = Modifier.weight(1f)
                    )
                    SavingsChip(
                        icon = "🌿",
                        value = "${rec.estimatedYearlyCo2Saved.toInt()} kg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        rec.description,
                        fontSize = 12.sp,
                        color = TextGray,
                        lineHeight = 18.sp
                    )
                    if (rec.actionText.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGreen.copy(alpha = 0.1f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", fontSize = 14.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                rec.actionText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarkGreen
                            )
                        }
                    }
                    if (rec.standardReference.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "📘 ${rec.standardReference}",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsChip(icon: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryGreen.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 11.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkGreen
        )
    }
}