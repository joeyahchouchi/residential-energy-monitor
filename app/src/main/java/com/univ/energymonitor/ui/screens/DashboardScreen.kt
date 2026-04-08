package com.univ.energymonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.energymonitor.ui.theme.BackgroundGray
import com.univ.energymonitor.ui.theme.DarkGreen
import com.univ.energymonitor.ui.theme.GreenSurface
import com.univ.energymonitor.ui.theme.HintGray
import com.univ.energymonitor.ui.theme.LightGreen
import com.univ.energymonitor.ui.theme.PrimaryGreen
import com.univ.energymonitor.ui.theme.TextGray
import com.univ.energymonitor.ui.components.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    onStartSurvey: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                        Box(
                            modifier = Modifier.Companion
                                .size(32.dp)
                                .background(PrimaryGreen, CircleShape),
                            contentAlignment = Alignment.Companion.Center
                        ) { Text("⚡", fontSize = 16.sp) }
                        Spacer(Modifier.Companion.width(10.dp))
                        Text(
                            "Lebanon Energy Monitor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Companion.Bold,
                            color = DarkGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = PrimaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Companion.White)
            )
        },
        containerColor = BackgroundGray
    ) { innerPadding ->

        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Welcome banner ────────────────────────────────────────────────
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.Companion.padding(20.dp),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Column(modifier = Modifier.Companion.weight(1f)) {
                        Text(
                            "Welcome back,",
                            color = LightGreen,
                            fontSize = 13.sp
                        )
                        Text(
                            username.replaceFirstChar { it.uppercase() },
                            color = Color.Companion.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Companion.Bold
                        )
                        Spacer(Modifier.Companion.height(4.dp))
                        Text(
                            "Ready to log energy data?",
                            color = Color.Companion.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier.Companion
                            .size(60.dp)
                            .background(Color.Companion.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        Text("🏠", fontSize = 28.sp)
                    }
                }
            }

            // ── KPI cards row ─────────────────────────────────────────────────
            Text(
                "Overview",
                fontWeight = FontWeight.Companion.SemiBold,
                fontSize = 15.sp,
                color = DarkGreen
            )
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "📋",
                    label = "Surveys",
                    value = "0",
                    sub = "submitted"
                )
                KpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "⚡",
                    label = "Avg kWh",
                    value = "—",
                    sub = "per month"
                )
                KpiCard(
                    modifier = Modifier.Companion.weight(1f),
                    icon = "🌿",
                    label = "CO₂",
                    value = "—",
                    sub = "kg saved"
                )
            }

            // ── Start Survey CTA ──────────────────────────────────────────────
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.Companion.padding(24.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text("🏡", fontSize = 44.sp, textAlign = TextAlign.Companion.Center)
                    Spacer(Modifier.Companion.height(12.dp))
                    Text(
                        "Start a Household Survey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = DarkGreen,
                        textAlign = TextAlign.Companion.Center
                    )
                    Spacer(Modifier.Companion.height(6.dp))
                    Text(
                        "Collect energy consumption data for a\nresidential unit in Lebanon.",
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Companion.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.Companion.height(20.dp))
                    Button(
                        onClick = onStartSurvey,
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Companion.White
                        )
                        Spacer(Modifier.Companion.width(8.dp))
                        Text(
                            "START SURVEY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Companion.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ── Quick info cards ──────────────────────────────────────────────
            Text(
                "Project Info",
                fontWeight = FontWeight.Companion.SemiBold,
                fontSize = 15.sp,
                color = DarkGreen
            )

            InfoRow(
                icon = Icons.Default.ElectricBolt,
                title = "Electrical Systems",
                subtitle = "Appliance usage & consumption per unit"
            )
            InfoRow(
                icon = Icons.Default.Thermostat,
                title = "HVAC Analysis",
                subtitle = "Heating, cooling & thermal behaviour"
            )
            InfoRow(
                icon = Icons.Default.BarChart,
                title = "CO₂ & KPI Reports",
                subtitle = "Savings estimation & M&E dashboards"
            )

            Spacer(Modifier.Companion.height(8.dp))
        }
    }
}

// ── KPI Card ───────────────────────────────────────────────────────────────────
@Composable
private fun KpiCard(
    modifier: Modifier = Modifier.Companion,
    icon: String,
    label: String,
    value: String,
    sub: String
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.Companion.height(4.dp))
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = PrimaryGreen
            )
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Companion.Medium,
                color = DarkGreen
            )
            Text(sub, fontSize = 10.sp, color = TextGray, textAlign = TextAlign.Companion.Center)
        }
    }
}

// ── Info Row ───────────────────────────────────────────────────────────────────
@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Companion.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.Companion.padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Box(
                modifier = Modifier.Companion
                    .size(42.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                    .background(GreenSurface),
                contentAlignment = Alignment.Companion.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.Companion.size(22.dp)
                )
            }
            Spacer(Modifier.Companion.width(14.dp))
            Column {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Companion.SemiBold,
                    color = DarkGreen
                )
                Text(subtitle, fontSize = 12.sp, color = TextGray)
            }
            Spacer(Modifier.Companion.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HintGray)
        }
    }
}