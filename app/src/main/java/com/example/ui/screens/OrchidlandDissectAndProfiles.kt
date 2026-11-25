package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrchidlandDicmData
import com.example.model.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DissectAndIpbContent(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Inverted MDMP Pipeline
        SectionTitleBanner(
            tag = "PHASE 1 • INVERTED MDMP",
            title = "Adversarial Mission Deconstruction",
            subtitle = "Specified, Implied & Essential Tasks mapped to Lower Puna terrain"
        )

        OrchidlandDicmData.invertedMdmpSteps.forEach { step ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.35f), DarkBorder))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanNeon.copy(alpha = 0.18f))
                        ) {
                            Text(
                                text = "${step.stepNumber}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = step.summary, fontSize = 11.sp, color = Slate400)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate850)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Orchidland: ${step.orchidlandApplication}",
                            fontSize = 11.sp,
                            color = Slate200,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 2. MLCOA vs MDCOA Selector
        SectionTitleBanner(
            tag = "ENEMY COURSES OF ACTION",
            title = "MLCOA vs. MDCOA Projection",
            subtitle = "Toggle baseline phishing funnel vs. storm/eruption off-grid crisis surge"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OrchidlandDicmData.coaScenarios.forEach { coa ->
                val selected = uiState.selectedCoaCode == coa.code
                val accent = if (coa.code == "MDCOA") CrimsonThreat else AmberRadar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) accent else DarkSurface)
                        .border(1.5.dp, if (selected) accent else accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectCoaCode(coa.code) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("coa_toggle_${coa.code.lowercase(Locale.US)}")
                ) {
                    Text(
                        text = if (coa.code == "MLCOA") "MLCOA (Baseline)" else "MDCOA (Crisis Surge)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Slate950 else Slate100
                    )
                }
            }
        }

        val activeCoa = OrchidlandDicmData.coaScenarios.firstOrNull { it.code == uiState.selectedCoaCode }
            ?: OrchidlandDicmData.coaScenarios.first()
        val a11y = LocalAccessibilityAudio.current
        val coaId = "coa_${activeCoa.code.lowercase(Locale.US)}"
        val coaSpeaking = a11y.playbackState.activeSourceId == coaId && a11y.playbackState.isActive

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        visualAudioHighlightBorder(
                            coaSpeaking,
                            a11y.visualAudioEnabled,
                            if (activeCoa.code == "MDCOA") CrimsonThreat else AmberRadar
                        ),
                        if (coaSpeaking) EmeraldVerified else DarkBorder
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${activeCoa.code}: ${activeCoa.title}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeCoa.code == "MDCOA") CrimsonThreat else AmberRadar,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AudioSelectButton(
                        sourceId = coaId,
                        title = "${activeCoa.code} Projection",
                        text = "${activeCoa.code}: ${activeCoa.title}. ${activeCoa.description}. Triggers: ${activeCoa.triggerIndicators.joinToString(". ")}",
                        compact = true
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activeCoa.description,
                    fontSize = 12.sp,
                    color = Slate200,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TRIGGER INDICATORS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(4.dp))
                activeCoa.triggerIndicators.forEach { ind ->
                    Text(
                        text = "• $ind",
                        fontSize = 11.sp,
                        color = Slate300,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        // 3. Center of Gravity (Strategic vs Operational)
        SectionTitleBanner(
            tag = "CENTER OF GRAVITY (COG)",
            title = "Strategic Cyber vs. Operational Ground COG",
            subtitle = "Critical Capabilities (CC), Requirements (CR) & Vulnerabilities (CV)"
        )

        OrchidlandDicmData.cogComparisons.forEach { cog ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(VioletVector.copy(alpha = 0.5f), DarkBorder))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = cog.domain.uppercase(Locale.US),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletVector,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cog.primaryCog,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CogBulletGroup("Critical Capabilities (CC)", cog.criticalCapabilities, CyanNeon)
                    Spacer(modifier = Modifier.height(6.dp))
                    CogBulletGroup("Critical Requirements (CR)", cog.criticalRequirements, AmberRadar)
                    Spacer(modifier = Modifier.height(6.dp))
                    CogBulletGroup("Critical Vulnerabilities (CV)", cog.criticalVulnerabilities, CrimsonThreat)
                }
            }
        }

        // 4. SWOT / PESTLE / BSC OKR Selector
        SectionTitleBanner(
            tag = "STRATEGIC MATRICES",
            title = "SWOT • PESTLE • BSC & OKRs",
            subtitle = "Select analytical lens extracted for Orchidland Estates"
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OrchidlandDicmData.strategicFrameworks.forEach { fw ->
                val selected = uiState.selectedStrategicFramework == fw.frameworkCode
                val fwColor = when (fw.frameworkCode) {
                    "SWOT" -> CyanNeon
                    "PESTLE" -> EmeraldVerified
                    else -> VioletVector
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) fwColor else DarkSurface)
                        .border(1.5.dp, if (selected) fwColor else fwColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.selectStrategicFramework(fw.frameworkCode) }
                        .padding(vertical = 8.dp)
                        .testTag("fw_btn_${fw.frameworkCode.lowercase(Locale.US).replace(" ", "_")}")
                ) {
                    Text(
                        text = fw.frameworkCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Slate950 else Slate100
                    )
                }
            }
        }

        val activeFw = OrchidlandDicmData.strategicFrameworks.firstOrNull {
            it.frameworkCode == uiState.selectedStrategicFramework
        } ?: OrchidlandDicmData.strategicFrameworks.first()
        val fwId = "fw_${activeFw.frameworkCode.lowercase(Locale.US).replace(" ", "_")}"
        val fwSpeaking = a11y.playbackState.activeSourceId == fwId && a11y.playbackState.isActive
        val fwSummary = "${activeFw.title}. " + activeFw.items.joinToString(". ") { "${it.first}: ${it.second}" }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        visualAudioHighlightBorder(fwSpeaking, a11y.visualAudioEnabled, CyanNeon.copy(alpha = 0.4f)),
                        if (fwSpeaking) EmeraldVerified else DarkBorder
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = activeFw.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AudioSelectButton(
                        sourceId = fwId,
                        title = "${activeFw.frameworkCode} Matrix",
                        text = fwSummary,
                        compact = true
                    )
                }
                activeFw.items.forEach { (k, v) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate850)
                            .padding(8.dp)
                    ) {
                        Text(text = k, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberRadar)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = v, fontSize = 11.sp, color = Slate200, lineHeight = 15.sp)
                    }
                }
            }
        }

        // 5. Orchidland Ground IPB 6-Dimension Matrix
        SectionTitleBanner(
            tag = "ORCHIDLAND GROUND IPB",
            title = "6-Dimension Operational Baseline",
            subtitle = "Lower Puna verified terrain, climate, fiber/RF, utilities & airspace"
        )

        OrchidlandDicmData.ipbDimensions.forEach { row ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(DarkBorder, Slate800))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = row.dimension,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = row.verifiedBaseline,
                        fontSize = 11.sp,
                        color = Slate200,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cyber/Ops Effect: ${row.cyberPhysicalImplication}",
                        fontSize = 11.sp,
                        color = EmeraldVerified,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Caveat: ${row.sourceCaveat}",
                        fontSize = 10.sp,
                        color = AmberRadar,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun ProfilesAndPlatformsContent(
    viewModel: ForensicViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Visual Profile Identification (6 Task Agent Archetypes)
        SectionTitleBanner(
            tag = "PHASE 2 • PROFILE IDENTIFICATION",
            title = "Task Agent Visual Signatures (6 Classes)",
            subtitle = "Tap any agent badge to inspect PIN/PIDI, vulnerabilities & interdiction posture"
        )

        OrchidlandDicmData.taskAgentProfiles.forEach { profile ->
            val accentColor = when {
                profile.isHighDegreeTrap -> AmberRadar
                profile.isPrimaryTarget -> CrimsonThreat
                profile.category == TaskAgentCategory.AI_SWARM -> CyanNeon
                else -> VioletVector
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.55f), DarkBorder))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectAgentProfile(profile) }
                    .testTag("agent_profile_${profile.id}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = profile.emblemResId),
                            contentDescription = "${profile.name} identification emblem",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = profile.cryptoId,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = when {
                                        profile.isHighDegreeTrap -> "TRAP NODE"
                                        profile.isPrimaryTarget -> "PRIORITY TARGET"
                                        else -> profile.category.label.uppercase(Locale.US)
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = profile.subtitle,
                                fontSize = 11.sp,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = profile.orchidlandSignature,
                        fontSize = 11.sp,
                        color = Slate200,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Centrality Telemetry Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MiniCentralityBar("DEG", profile.degreeScore, CyanNeon, Modifier.weight(1f))
                        MiniCentralityBar("BTW", profile.betweennessScore, CrimsonThreat, Modifier.weight(1f))
                        MiniCentralityBar("CLS", profile.closenessScore, AmberRadar, Modifier.weight(1f))
                        MiniCentralityBar("EIG", profile.eigenvectorScore, EmeraldVerified, Modifier.weight(1f))
                    }
                }
            }
        }

        // 2. Extracted Platforms & Infrastructure Identification
        SectionTitleBanner(
            tag = "EXTRACTED PLATFORMS & INFRASTRUCTURE",
            title = "FB • Snap • WhatsApp • Proxies • LoRa • Stack",
            subtitle = "Tap any platform card to view Context-Setter takedown & telemetry indicators"
        )

        OrchidlandDicmData.platformNodes.forEach { platform ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.4f), DarkBorder))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectPlatformNode(platform) }
                    .testTag("platform_node_${platform.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = platform.emblemResId),
                        contentDescription = "${platform.name} platform badge",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = platform.categoryBadge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberRadar,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = platform.mitigationStatusLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldVerified
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = platform.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = platform.funnelStage,
                            fontSize = 11.sp,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = platform.orchidlandTelemetry,
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 14.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // 3. Stakeholder Power / Interest & Salience Matrix
        SectionTitleBanner(
            tag = "STAKEHOLDER & SALIENCE MODEL",
            title = "Power / Interest & Legitimacy Grid",
            subtitle = "Separating definitive brokers & institutional Context-Setters from noise traps"
        )

        OrchidlandDicmData.stakeholderQuadrants.forEach { stk ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(DarkBorder, Slate700))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stk.powerLevel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonThreat,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = stk.interestLevel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stk.actorName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100)
                    Text(text = stk.salienceClass, fontSize = 11.sp, color = AmberRadar)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stk.attributes, fontSize = 11.sp, color = Slate300)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Posture: ${stk.posture}", fontSize = 11.sp, color = EmeraldVerified, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun CogBulletGroup(title: String, lines: List<String>, color: Color) {
    Column {
        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        lines.forEach { line ->
            Text(text = "• $line", fontSize = 11.sp, color = Slate300, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun MiniCentralityBar(
    label: String,
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, fontSize = 9.sp, color = Slate400, fontFamily = FontFamily.Monospace)
            Text(
                text = String.format(Locale.US, "%.2f", value),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            color = color,
            trackColor = Slate800,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun SectionTitleBanner(
    tag: String,
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        Text(
            text = tag,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 0.8.sp,
            fontFamily = JetBrainsMonoFontFamily
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Slate100
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Slate300
        )
    }
}
