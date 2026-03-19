package com.univ.energymonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand colors (same as LoginScreen) ────────────────────────────────────────
private val GreenDark    = Color(0xFF1B5E20)
private val GreenPrimary = Color(0xFF2E7D32)
private val GreenLight   = Color(0xFF81C784)
private val BgGray       = Color(0xFFF5F5F5)
private val CardWhite    = Color.White
private val TextGray     = Color(0xFF757575)
private val DividerGray  = Color(0xFFE0E0E0)

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(GreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("⚡", fontSize = 16.sp) }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Lebanon Energy Monitor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        },
        containerColor = BgGray
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Welcome banner ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Welcome back,",
                            color = GreenLight,
                            fontSize = 13.sp
                        )
                        Text(
                            username.replaceFirstChar { it.uppercase() },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Ready to log energy data?",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 28.sp)
                    }
                }
            }

            // ── KPI cards row ─────────────────────────────────────────────────
            Text(
                "Overview",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = GreenDark
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    icon = "📋",
                    label = "Surveys",
                    value = "0",
                    sub = "submitted"
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    icon = "⚡",
                    label = "Avg kWh",
                    value = "—",
                    sub = "per month"
                )
                KpiCard(
                    modifier = Modifier.weight(1f),
                    icon = "🌿",
                    label = "CO₂",
                    value = "—",
                    sub = "kg saved"
                )
            }

            // ── Start Survey CTA ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏡", fontSize = 44.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Start a Household Survey",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Collect energy consumption data for a\nresidential unit in Lebanon.",
                        fontSize = 13.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onStartSurvey,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "START SURVEY",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // ── Quick info cards ──────────────────────────────────────────────
            Text(
                "Project Info",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = GreenDark
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

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── KPI Card ───────────────────────────────────────────────────────────────────
@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    sub: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = GreenDark)
            Text(sub, fontSize = 10.sp, color = TextGray, textAlign = TextAlign.Center)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenDark)
                Text(subtitle, fontSize = 12.sp, color = TextGray)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
        }
    }}
