package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civic.*
import com.example.ui.ForensicViewModel
import com.example.ui.components.ForensicTopBar
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun CivicIntelligenceScreen(
    viewModel: ForensicViewModel,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    val civic by viewModel.civicState.collectAsState()
    val coordinator = viewModel.civicCoordinator

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .testTag("civic_intelligence_screen")
    ) {
        ForensicTopBar(
            title = "Civic AI & Policy",
            subtitle = "${civic.activeMilestoneVersion.versionTag} • Non-Partisan Public Resource Engine"
        )

        // Sub-section selector pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.panelSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            CivicWorkspaceSection.entries.forEach { sec ->
                val selected = civic.activeSection == sec
                FilterChip(
                    selected = selected,
                    onClick = { coordinator.selectSection(sec) },
                    label = { Text(sec.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("civic_sec_${sec.name.lowercase()}")
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("civic_lazy_list")
        ) {
            // Always show mandatory Epistemic Separation Legend Banner
            item(key = "epistemic_legend") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.dp, palette.advisoryBlue.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("civic_epistemic_legend")
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "EPISTEMIC SEPARATION CONTRACT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.advisoryBlue
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            EpistemicTier.entries.forEach { tier ->
                                EpistemicBadgeChip(tier = tier)
                            }
                        }
                    }
                }
            }

            when (civic.activeSection) {
                CivicWorkspaceSection.AI_AND_VERSION -> {
                    item(key = "version_roadmap") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_version_roadmap_card")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "TIMELINE-BASED VERSIONING SYSTEM (v1.0 – v10.0)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    PlatformMilestoneVersion.entries.forEach { ver ->
                                        FilterChip(
                                            selected = civic.activeMilestoneVersion == ver,
                                            onClick = { coordinator.activateMilestoneVersion(ver) },
                                            label = { Text(ver.versionTag, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("civic_version_${ver.name.lowercase()}")
                                        )
                                    }
                                }
                                val v = civic.activeMilestoneVersion
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${v.versionTag}: ${v.title} (${v.horizonLabel})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EpistemicBadgeChip(tier = v.epistemicTier)
                                }
                                Text("Datasets: ${v.datasetsActivated.joinToString(", ")}", fontSize = 11.sp, color = palette.textSecondary)
                                Text("AI Models: ${v.aiModelsActivated.joinToString(", ")}", fontSize = 11.sp, color = palette.textSecondary)
                                Text("Reporting: ${v.reportingSystems.joinToString(", ")}", fontSize = 11.sp, color = palette.textSecondary)
                                Text("Engagement: ${v.engagementFeatures.joinToString(", ")}", fontSize = 11.sp, color = palette.normalGreen)
                            }
                        }
                    }

                    item(key = "cultural_engine") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_cultural_card")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "CULTURAL SENSITIVITY & ANTI-BIAS ENGINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.normalGreen
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Indigenous & Regional Respect Framing (${civic.culturalProfile.geographicRegion})",
                                        fontSize = 12.sp,
                                        color = palette.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = civic.culturalProfile.indigenousProtocolRespect,
                                        onCheckedChange = { coordinator.toggleCulturalIndigenousContext(it) },
                                        modifier = Modifier.testTag("civic_indigenous_switch")
                                    )
                                }
                                Text(
                                    text = civic.culturalProfile.communityNormSummary,
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                                OutlinedTextField(
                                    value = civic.culturalAuditInput,
                                    onValueChange = { coordinator.updateCulturalAuditInput(it) },
                                    label = { Text("Test Non-Partisan / Anti-Bias Guardrail") },
                                    placeholder = { Text("Enter draft civic copy to audit") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("civic_cultural_audit_input")
                                )
                                civic.latestBiasAudit?.let { audit ->
                                    Text(
                                        text = audit.guardReason,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (audit.passed) palette.normalGreen else palette.criticalRed,
                                        modifier = Modifier.testTag("civic_cultural_audit_result")
                                    )
                                }
                            }
                        }
                    }

                    item(key = "federated_bandit_header") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.classAColor.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ON-DEVICE RL + FEDERATED DP SYNC",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = palette.classAColor
                                        )
                                        Text(
                                            text = "Edge weight updates: ${civic.currentWeights.updatesCount} • Opt-in DP gradient sharing (zero raw PII)",
                                            fontSize = 11.sp,
                                            color = palette.textSecondary
                                        )
                                    }
                                    Switch(
                                        checked = civic.federatedSyncOptIn,
                                        onCheckedChange = { coordinator.toggleFederatedOptInAndExport(it) },
                                        modifier = Modifier.testTag("civic_federated_switch")
                                    )
                                }
                                civic.latestFederatedDelta?.let { delta ->
                                    Text(
                                        text = "Exported ${delta.epochId} • ε=${delta.epsilonBudget} • L2=${String.format(Locale.US, "%.3f", delta.clippedL2Norm)} • RawData=${delta.containsRawUserData}",
                                        fontSize = 10.sp,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = palette.normalGreen,
                                        modifier = Modifier.testTag("civic_federated_delta_text")
                                    )
                                }
                            }
                        }
                    }

                    items(civic.activeRecommendations.take(4), key = { it.id }) { rec ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_rec_card_${rec.topic.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = rec.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EpistemicBadgeChip(tier = rec.epistemicTier)
                                }
                                Text(rec.plainLanguageSummary, fontSize = 12.sp, color = palette.textSecondary)
                                Text(
                                    text = "Explainability (${(rec.score * 100).toInt()}%): ${rec.attributionBreakdown.joinToString(" • ")}",
                                    fontSize = 10.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { coordinator.recordRecommendationFeedback(rec.topic, +0.20f) },
                                        modifier = Modifier.testTag("civic_rec_helpful_${rec.topic.id}")
                                    ) {
                                        Text("+Relevant", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { coordinator.recordRecommendationFeedback(rec.topic, -0.15f) },
                                        modifier = Modifier.testTag("civic_rec_less_${rec.topic.id}")
                                    ) {
                                        Text("Less Like This", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                CivicWorkspaceSection.POLICY_AND_CYCLES -> {
                    item(key = "election_cycle_trigger") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.5.dp, palette.warningAmber),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "ELECTION-CYCLE & ADMINISTRATION ADAPTATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.warningAmber
                                )
                                Text(
                                    text = "Tracks administration transitions and newly declared public priorities strictly for informational relevance — never voter steering or partisan advocacy.",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary
                                )
                                Button(
                                    onClick = { coordinator.simulateAdministrationTransition() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("civic_detect_transition_btn")
                                ) {
                                    Text("Detect Transition & Recalculate Policy Relevance", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(civic.cycleComparisons, key = { it.id }) { comp ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.advisoryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_cycle_comp_${comp.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(comp.cyclesCompared, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = comp.forecastBasis)
                                }
                                Text(comp.policyContinuityNote, fontSize = 11.sp, color = palette.normalGreen)
                                Text(comp.programChangesNote, fontSize = 11.sp, color = palette.textPrimary)
                                Text(comp.serviceImpactForecast, fontSize = 11.sp, color = palette.warningAmber)
                                Text(comp.budgetTrendSummary, fontSize = 11.sp, color = palette.advisoryBlue)
                            }
                        }
                    }

                    items(civic.policies, key = { it.id }) { pol ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_policy_${pol.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(pol.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = pol.epistemicTier)
                                }
                                Text(
                                    text = "${pol.jurisdiction} • Effective ${pol.effectiveDateIso} • ${pol.status} • Relevance ${(pol.relevanceScore * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                                Text(pol.informationalSummary, fontSize = 12.sp, color = palette.textSecondary)
                                Text(
                                    text = "Revisions (${pol.revisions.size}): ${pol.revisions.lastOrNull()?.summary.orEmpty()}",
                                    fontSize = 11.sp,
                                    color = palette.normalGreen
                                )
                            }
                        }
                    }
                }

                CivicWorkspaceSection.ISSUES_AND_REPORTS -> {
                    item(key = "issue_subscriptions") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "SOCIAL ISSUE INTELLIGENCE SUBSCRIPTIONS (10 TOPICS)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    SocialIssueTopic.entries.forEach { topic ->
                                        FilterChip(
                                            selected = civic.subscribedTopics.contains(topic),
                                            onClick = { coordinator.toggleTopicSubscription(topic) },
                                            label = { Text(topic.title, fontSize = 11.sp) },
                                            modifier = Modifier.testTag("civic_sub_${topic.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "community_report_composer") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "MODERATED COMMUNITY OBSERVATION & RESOURCE GAP REPORT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.normalGreen
                                )
                                OutlinedTextField(
                                    value = civic.reportDraftTitle,
                                    onValueChange = { coordinator.updateCommunityReportDraft(title = it) },
                                    label = { Text("Issue / Service Gap Title") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("civic_report_title_input")
                                )
                                OutlinedTextField(
                                    value = civic.reportDraftDetail,
                                    onValueChange = { coordinator.updateCommunityReportDraft(detail = it) },
                                    label = { Text("Observable Community Need (PII auto-scrubbed)") },
                                    minLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("civic_report_detail_input")
                                )
                                OutlinedTextField(
                                    value = civic.reportDraftLocation,
                                    onValueChange = { coordinator.updateCommunityReportDraft(location = it) },
                                    label = { Text("General Corridor / Neighborhood") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("civic_report_loc_input")
                                )
                                Button(
                                    onClick = { coordinator.submitCommunityReport() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("civic_submit_report_btn")
                                ) {
                                    Text("Submit for Moderation & Human Validation", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(civic.communityReports, key = { it.id }) { rep ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_report_card_${rep.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(rep.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = rep.epistemicTier)
                                }
                                Text(rep.observableDetail, fontSize = 12.sp, color = palette.textSecondary)
                                Text(
                                    text = "${rep.locationLabel} • Rep ${(rep.reporterReputation * 100).toInt()}% • ${rep.moderationStatus.label}" +
                                        if (rep.piiRedacted) " • PII Scrubbed" else "",
                                    fontSize = 10.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.warningAmber
                                )
                                if (rep.moderationStatus == ModerationStatus.PENDING_HUMAN_REVIEW) {
                                    OutlinedButton(
                                        onClick = { coordinator.humanVerifyCommunityReport(rep.id) },
                                        modifier = Modifier.testTag("civic_verify_report_${rep.id}")
                                    ) {
                                        Text("Human Validate Report", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    items(civic.resources, key = { it.id }) { res ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(res.organizationName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = res.epistemicTier)
                                }
                                Text(res.serviceSummary, fontSize = 12.sp, color = palette.textSecondary)
                                Text("Access: ${res.accessChannel} • Verified ${res.verifiedDateIso}", fontSize = 11.sp, color = palette.normalGreen)
                            }
                        }
                    }
                }

                CivicWorkspaceSection.FIELD_AND_EVOLUTION -> {
                    items(civic.fieldCases, key = { it.id }) { fc ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.advisoryBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_field_case_${fc.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(fc.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = fc.epistemicTier)
                                }
                                Text("${fc.assignedRole} • ${fc.taskStatus}", fontSize = 11.sp, color = palette.advisoryBlue)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Consent-Based Site Location Log", fontSize = 12.sp, color = palette.textPrimary)
                                    Switch(
                                        checked = fc.locationLoggingConsentGranted,
                                        onCheckedChange = { coordinator.toggleFieldLocationConsent(fc.id) },
                                        modifier = Modifier.testTag("civic_field_consent_switch")
                                    )
                                }
                                Text(fc.consentBoundaryNote, fontSize = 11.sp, color = palette.normalGreen)
                            }
                        }
                    }

                    item(key = "automated_updates") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "AUTOMATED UPDATE SCHEDULER & OPTIMIZATION REPORTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    UpdateCadence.entries.forEach { cad ->
                                        OutlinedButton(
                                            onClick = { coordinator.runUpdateCadence(cad) },
                                            modifier = Modifier.testTag("civic_run_cadence_${cad.id}")
                                        ) {
                                            Text(cad.label, fontSize = 11.sp)
                                        }
                                    }
                                }
                                civic.optimizationReports.firstOrNull()?.let { latest ->
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Retention ${(latest.retentionRateEstimate * 100).toInt()}% • A11y ${(latest.accessibilityAdoptionRate * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.normalGreen
                                        )
                                        EpistemicBadgeChip(tier = latest.epistemicTier)
                                    }
                                    Text(latest.summary, fontSize = 11.sp, color = palette.textSecondary)
                                }
                            }
                        }
                    }

                    items(civic.improvementProposals, key = { it.id }) { prop ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_proposal_${prop.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Self-Refinement: ${prop.area}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary, modifier = Modifier.weight(1f))
                                    EpistemicBadgeChip(tier = prop.epistemicTier)
                                }
                                Text(prop.recommendation, fontSize = 12.sp, color = palette.textPrimary)
                                Text("Telemetry: ${prop.measuredSignal}", fontSize = 11.sp, color = palette.textSecondary)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { coordinator.reviewImprovementProposal(prop.id, true) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.normalGreen,
                                            contentColor = contrastContentColor(palette.normalGreen)
                                        ),
                                        modifier = Modifier.testTag("civic_approve_prop_${prop.id}")
                                    ) {
                                        Text(if (prop.humanApproved == true) "Human Approved" else "Approve Change", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { coordinator.reviewImprovementProposal(prop.id, false) },
                                        modifier = Modifier.testTag("civic_reject_prop_${prop.id}")
                                    ) {
                                        Text(if (prop.humanApproved == false) "Rejected" else "Hold", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    item(key = "speculative_projection_card") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.classAColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("5–10 YEAR STRATEGIC HORIZON", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMonoFontFamily, color = palette.classAColor)
                                    EpistemicBadgeChip(tier = EpistemicTier.SPECULATIVE_PROJECTION)
                                }
                                Text(civic.speculativeHorizonNote, fontSize = 12.sp, color = palette.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EpistemicBadgeChip(tier: EpistemicTier) {
    val palette = LocalMapPalette.current
    val accent: Color = when (tier) {
        EpistemicTier.VERIFIED_DATA -> palette.normalGreen
        EpistemicTier.USER_GENERATED -> palette.warningAmber
        EpistemicTier.AI_GENERATED_INSIGHT -> palette.advisoryBlue
        EpistemicTier.FORECAST -> palette.classBColor
        EpistemicTier.SPECULATIVE_PROJECTION -> palette.classAColor
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = tier.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
