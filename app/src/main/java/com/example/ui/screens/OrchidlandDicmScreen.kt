package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.OrchidlandDicmData
import com.example.model.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.ForensicTopBar
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun OrchidlandDicmScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "Orchidland DICM",
                subtitle = "Puna Cyber-Physical Ops & Agentic Mitigation"
            )
        },
        containerColor = palette.canvasBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // DICM Phase Navigation Strip with high-contrast labels
            val selectedIdx = DicmPhaseTab.values().indexOf(uiState.selectedDicmTab).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = selectedIdx,
                containerColor = palette.panelSurface,
                contentColor = palette.advisoryBlue,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedIdx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIdx]),
                            color = palette.advisoryBlue
                        )
                    }
                },
                modifier = Modifier.testTag("dicm_phase_tabs")
            ) {
                DicmPhaseTab.values().forEach { phase ->
                    val selected = uiState.selectedDicmTab == phase
                    Tab(
                        selected = selected,
                        onClick = { viewModel.selectDicmTab(phase) },
                        text = {
                            Text(
                                text = phase.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaFontFamily,
                                color = if (selected) palette.advisoryBlue else palette.textSecondary
                            )
                        },
                        modifier = Modifier.testTag("dicm_tab_${phase.code.lowercase(Locale.US)}")
                    )
                }
            }

            val disableOuterScroll =
                uiState.selectedDicmTab == DicmPhaseTab.AIRSPACE_PTA && uiState.airspaceMapScrollLocked

            LazyColumn(
                userScrollEnabled = !disableOuterScroll,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("dicm_lazy_column")
            ) {
                if (uiState.selectedDicmTab == DicmPhaseTab.DISSECT_IPB) {
                    // Hero Command Telemetry & Visual Profile/Platform Identification Strip on landing view
                    item {
                        OrchidlandCommandHeroCard(
                            uiState = uiState,
                            onFocusSatFeed = { viewModel.focusOrchidlandInSatFeed() },
                            onOpenAirspace = { viewModel.openAirspaceTab() },
                            onSelectProfile = { viewModel.selectAgentProfile(it) },
                            onSelectPlatform = { viewModel.selectPlatformNode(it) },
                            onOpenProfilesTab = { viewModel.selectDicmTab(DicmPhaseTab.PROFILES_PLATFORMS) }
                        )
                    }
                }

                // Selected DICM Phase Content
                item {
                    when (uiState.selectedDicmTab) {
                        DicmPhaseTab.DISSECT_IPB -> DissectAndIpbContent(viewModel, uiState)
                        DicmPhaseTab.PROFILES_PLATFORMS -> ProfilesAndPlatformsContent(viewModel)
                        DicmPhaseTab.CORRELATE_SNA -> CorrelateAndSnaContent(viewModel, uiState)
                        DicmPhaseTab.MITIGATE_ROE -> MitigateAndRoeContent(viewModel, uiState)
                        DicmPhaseTab.AIRSPACE_PTA -> HawaiiAirspaceContent(viewModel, uiState)
                        DicmPhaseTab.HILO_CAM_MONITOR -> HiloDowntownCamContent(viewModel, uiState)
                    }
                }
            }
        }
    }

    // Visual Profile Identification Inspection Dialog
    uiState.selectedAgentProfile?.let { profile ->
        TaskAgentProfileDialog(
            profile = profile,
            onDismiss = { viewModel.selectAgentProfile(null) },
            onJumpToSna = {
                val matchedNode = OrchidlandDicmData.graphNodes.firstOrNull { it.agentProfileId == profile.id }
                if (matchedNode != null) {
                    viewModel.selectGraphNode(matchedNode.id)
                }
                viewModel.selectAgentProfile(null)
                viewModel.selectDicmTab(DicmPhaseTab.CORRELATE_SNA)
            }
        )
    }

    // Extracted Platform Node Inspection Dialog
    uiState.selectedPlatformNode?.let { platform ->
        PlatformNodeDialog(
            platform = platform,
            onDismiss = { viewModel.selectPlatformNode(null) }
        )
    }
}

@Composable
private fun OrchidlandCommandHeroCard(
    uiState: ForensicUiState,
    onFocusSatFeed: () -> Unit,
    onOpenAirspace: () -> Unit,
    onSelectProfile: (TaskAgentProfile) -> Unit,
    onSelectPlatform: (PlatformNodeProfile) -> Unit,
    onOpenProfilesTab: () -> Unit
) {
    val report = uiState.snaNetworkReport
    val a11y = LocalAccessibilityAudio.current
    val heroSpeaking = a11y.playbackState.activeSourceId == "orchidland_hero" && a11y.playbackState.isActive
    val heroSummaryText = buildString {
        append("Orchidland Estates Lower Puna command overview. ")
        append("Sector area 9.71 square miles, bush lots 49 to 52 percent. ")
        append("Social Network Analysis fragmentation ${String.format(Locale.US, "%.0f", report.fragmentationPercent)} percent. ")
        append("Rule of engagement compliance checks ${uiState.checkedGuardrailIds.size} of 5 verified. ")
        append(report.tacticalAssessment)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    visualAudioHighlightBorder(heroSpeaking, a11y.visualAudioEnabled, CyanNeon.copy(alpha = 0.6f)),
                    if (heroSpeaking) EmeraldVerified else VioletVector.copy(alpha = 0.4f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("orchidland_hero_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ORCHIDLAND ESTATES • LOWER PUNA, HI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Text(
                        text = "19.5614° N, 155.0131° W • ZIP 96749 / 96760",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberRadar,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onOpenAirspace,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberRadar,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("orchidland_airspace_btn")
                    ) {
                        Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TFR/PTA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onFocusSatFeed,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .testTag("orchidland_sat_feed_btn")
                    ) {
                        Icon(Icons.Default.SatelliteAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GIBS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Operational Stats Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DashboardStatChip("SECTOR", "9.71 sq mi", Slate200, Modifier.weight(1f))
                DashboardStatChip("BUSH LOTS", "49–52%", AmberRadar, Modifier.weight(1f))
                DashboardStatChip("SNA FRAG", String.format(Locale.US, "%.0f%%", report.fragmentationPercent), EmeraldVerified, Modifier.weight(1f))
                DashboardStatChip("ROE CHECK", "${uiState.checkedGuardrailIds.size}/5", CyanNeon, Modifier.weight(1f))
            }

            if (a11y.visualAudioEnabled || heroSpeaking) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SECTOR COMMAND BRIEFING AUDIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate300,
                        fontFamily = FontFamily.Monospace
                    )
                    AudioSelectButton(
                        sourceId = "orchidland_hero",
                        title = "Orchidland Command Brief",
                        text = heroSummaryText,
                        compact = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile Identification Visual Strip
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "PROFILE IDENTIFICATION (6 AGENTS)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "View All →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenProfilesTab)
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("hero_view_all_profiles_btn")
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OrchidlandDicmData.taskAgentProfiles, key = { it.id }) { profile ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(74.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(
                                1.dp,
                                if (profile.isPrimaryTarget) CrimsonThreat.copy(alpha = 0.6f)
                                else if (profile.isHighDegreeTrap) AmberRadar.copy(alpha = 0.6f)
                                else DarkBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectProfile(profile) }
                            .padding(6.dp)
                            .testTag("hero_profile_${profile.id}")
                    ) {
                        Image(
                            painter = painterResource(id = profile.emblemResId),
                            contentDescription = "${profile.cryptoId} visual profile",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.cryptoId,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile.name,
                            fontSize = 9.sp,
                            color = Slate200,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Extracted Platforms Visual Strip
            Text(
                text = "PLATFORM IDENTIFICATION (DIGITAL & RF)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate300,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(OrchidlandDicmData.platformNodes, key = { it.id }) { platform ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(74.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectPlatform(platform) }
                            .padding(6.dp)
                            .testTag("hero_platform_${platform.id}")
                    ) {
                        Image(
                            painter = painterResource(id = platform.emblemResId),
                            contentDescription = "${platform.name} platform imagery",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = platform.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardStatChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate850)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Text(text = label, fontSize = 8.sp, color = Slate400, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TaskAgentProfileDialog(
    profile: TaskAgentProfile,
    onDismiss: () -> Unit,
    onJumpToSna: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(CyanNeon, DarkBorder))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = profile.emblemResId),
                        contentDescription = "${profile.name} emblem",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.cryptoId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = profile.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(text = profile.snaRole, fontSize = 11.sp, color = AmberRadar)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = profile.coreCharacteristics, fontSize = 12.sp, color = Slate200)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "TYPICAL TASKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                Text(text = profile.typicalTasks, fontSize = 11.sp, color = Slate300)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "PIN / UTILITY MODEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VioletVector)
                Text(text = profile.pinOrUtilityProfile, fontSize = 11.sp, color = Slate300)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "PRIMARY VULNERABILITIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonThreat)
                profile.primaryVulnerabilities.forEach { v ->
                    Text(text = "• $v", fontSize = 11.sp, color = Slate200)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Interdiction: ${profile.interdictionAction}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldVerified
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                AudioSelectButton(
                    sourceId = "profile_dialog_${profile.id}",
                    title = "${profile.cryptoId} ${profile.name}",
                    text = "${profile.cryptoId} ${profile.name}, ${profile.snaRole}. ${profile.coreCharacteristics}. Typical tasks: ${profile.typicalTasks}. Interdiction: ${profile.interdictionAction}",
                    compact = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Close", color = Slate300)
                    }
                    Button(
                        onClick = onJumpToSna,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Inspect in SNA", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformNodeDialog(
    platform: PlatformNodeProfile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AmberRadar, DarkBorder))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("platform_node_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = platform.emblemResId),
                        contentDescription = "${platform.name} emblem",
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = platform.categoryBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = platform.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(text = platform.funnelStage, fontSize = 11.sp, color = CyanNeon)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "ORCHIDLAND EMPIRICAL BASELINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                Text(text = platform.orchidlandTelemetry, fontSize = 11.sp, color = Slate200)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ASSOCIATED TASK AGENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VioletVector)
                Text(text = platform.agentIds.joinToString(" • "), fontSize = 11.sp, color = Slate100, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "EXTRACTED INDICATORS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberRadar)
                platform.extractionIndicators.forEach { ind ->
                    Text(text = "• $ind", fontSize = 11.sp, color = Slate300)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Context-Setter Action: ${platform.contextSetterMechanism}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldVerified
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                AudioSelectButton(
                    sourceId = "platform_dialog_${platform.id}",
                    title = platform.name,
                    text = "${platform.name}, ${platform.funnelStage}. ${platform.orchidlandTelemetry}. Context-Setter action: ${platform.contextSetterMechanism}",
                    compact = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
