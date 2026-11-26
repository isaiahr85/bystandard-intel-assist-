package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bystander.*
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalMapPalette

@Composable
fun BystanderResourceReferralSection(
    elderMode: Boolean,
    query: String,
    onQueryChange: (String) -> Unit
) {
    val palette = LocalMapPalette.current
    val bodySize = if (elderMode) 14.sp else 12.sp
    val partners = CoordinationPartner.entries.filter {
        query.isBlank() ||
            it.title.contains(query, ignoreCase = true) ||
            it.scope.contains(query, ignoreCase = true)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_resource_referral_section")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = palette.normalGreen)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "5. COMMUNITY RESOURCE REFERRAL DIRECTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.normalGreen
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search support services (e.g. 211, 988, school, HR)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_resource_search_input")
            )
            partners.forEach { partner ->
                Surface(
                    color = palette.canvasBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = partner.title,
                            fontSize = if (elderMode) 15.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.advisoryBlue
                        )
                        Text(
                            text = partner.scope,
                            fontSize = bodySize,
                            color = palette.textPrimary,
                            lineHeight = if (elderMode) 20.sp else 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BystanderBtamEducationSection(elderMode: Boolean) {
    val palette = LocalMapPalette.current
    val bodySize = if (elderMode) 14.sp else 12.sp

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.advisoryBlue),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_btam_education_section")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "6. BEHAVIORAL THREAT ASSESSMENT AND MANAGEMENT (BTAM)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.advisoryBlue
            )
            Text(
                text = if (elderMode) {
                    "Elder-Friendly Summary: Caring teams look at actions—never stereotypes—to connect stressed neighbors with help early."
                } else {
                    "Plain-language guide for town halls, churches, schools, tribal communities, elders, and civic groups."
                },
                fontSize = bodySize,
                color = palette.normalGreen
            )
            BystanderPortalConstants.BTAM_EDUCATION_POINTS.forEach { (headline, detail) ->
                Surface(
                    color = palette.canvasBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(headline, fontWeight = FontWeight.Bold, fontSize = if (elderMode) 15.sp else 13.sp, color = palette.textPrimary)
                        Text(detail, fontSize = bodySize, color = palette.textSecondary, lineHeight = if (elderMode) 20.sp else 16.sp)
                    }
                }
            }

            ClarifyingAnomaliesCard(elderMode = elderMode)
        }
    }
}

@Composable
fun ClarifyingAnomaliesCard(elderMode: Boolean) {
    val palette = LocalMapPalette.current
    val bodySize = if (elderMode) 14.sp else 12.sp
    Surface(
        color = palette.warningAmber.copy(alpha = 0.10f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_clarifying_anomalies_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "CLARIFYING ANOMALIES — SITUATIONS WHERE CLARIFICATION HELPS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.warningAmber
            )
            Text(
                text = BystanderPortalConstants.CLARIFICATION_REASSURANCE,
                fontSize = bodySize,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary,
                lineHeight = if (elderMode) 20.sp else 16.sp
            )
            BystanderPortalConstants.CLARIFYING_ANOMALY_EXAMPLES.forEach { (title, desc) ->
                Text(
                    text = "• $title: $desc",
                    fontSize = bodySize,
                    color = palette.textSecondary,
                    lineHeight = if (elderMode) 20.sp else 16.sp
                )
            }
        }
    }
}

@Composable
fun BystanderNtacLearningCenterSection(
    elderMode: Boolean,
    onMarkReviewed: () -> Unit
) {
    val palette = LocalMapPalette.current
    val uriHandler = LocalUriHandler.current
    val bodySize = if (elderMode) 14.sp else 12.sp

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.advisoryBlue),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_ntac_learning_section")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = palette.advisoryBlue)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "7. U.S. SECRET SERVICE NTAC LEARNING CENTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.advisoryBlue
                )
            }
            Text(
                text = "Independent educational directory linking public U.S. Secret Service NTAC resources (no agency affiliation or endorsement implied).",
                fontSize = 11.sp,
                color = palette.warningAmber
            )

            BystanderPortalConstants.NTAC_PUBLICATIONS.forEach { pub ->
                Surface(
                    color = palette.canvasBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bystander_ntac_pub_${pub.id}")
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(pub.title, fontSize = if (elderMode) 15.sp else 13.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text(pub.publishedNote, fontSize = 10.sp, fontFamily = JetBrainsMonoFontFamily, color = palette.advisoryBlue)
                        Text(
                            text = if (elderMode) pub.elderFriendlyTakeaway else pub.plainSummary,
                            fontSize = bodySize,
                            color = palette.textSecondary,
                            lineHeight = if (elderMode) 20.sp else 16.sp
                        )
                        OutlinedButton(
                            onClick = {
                                onMarkReviewed()
                                runCatching { uriHandler.openUri(pub.url) }
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("bystander_open_ntac_${pub.id}")
                        ) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Open Official NTAC Publication", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(pub.url, fontSize = 9.sp, color = palette.textSecondary, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Surface(
                color = palette.normalGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "FREQUENTLY MISUNDERSTOOD CONCEPTS & INFOGRAPHIC SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = palette.normalGreen
                    )
                    Text(
                        text = "• Myth: 'Threat assessment profiles people.' -> Fact: BTAM strictly evaluates observable actions & communications.\n" +
                            "• Myth: 'Reporting ruins a person's life.' -> Fact: Early community concern reporting connects people with voluntary counseling and crisis support.\n" +
                            "• Infographic Flow: Observe Facts -> Check Context -> Request Clarification -> Connect Resources -> Authorized Review Only If Standards Met.",
                        fontSize = bodySize,
                        color = palette.textPrimary,
                        lineHeight = if (elderMode) 20.sp else 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BystanderTownHallSection(elderMode: Boolean) {
    val palette = LocalMapPalette.current
    val bodySize = if (elderMode) 14.sp else 12.sp
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.panelBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_town_hall_section")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "8. TOWN HALL RESOURCES & COMMUNITY IMPLEMENTATION GUIDE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.advisoryBlue
            )
            Text(
                text = "• Town Hall Prompt: 'How can neighbors speak up early with compassion without turning into neighborhood surveillance?'\n" +
                    "• Churches, Tribal Councils & Elder Hubs: Designate two trusted listeners who pair clarification intake with 211/988 resource warm handoffs.\n" +
                    "• Schools & Workplaces: Pair MTSS/HR wellness teams with clear anti-retaliation and non-profiling policies.\n" +
                    "• Elder-Friendly Reading Mode: Use large text controls in the top bar for printed or spoken community meetings.",
                fontSize = bodySize,
                color = palette.textPrimary,
                lineHeight = if (elderMode) 21.sp else 17.sp
            )
        }
    }
}

@Composable
fun BystanderFaqsSection(elderMode: Boolean) {
    val palette = LocalMapPalette.current
    val bodySize = if (elderMode) 14.sp else 12.sp
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.panelBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_faqs_section")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "9. REPORTING FAQS — PRIVACY, CIVIL LIBERTIES & SAFEGUARDS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.advisoryBlue
            )
            BystanderPortalConstants.REPORTING_FAQS.forEach { faq ->
                Surface(
                    color = palette.canvasBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(faq.question, fontWeight = FontWeight.Bold, fontSize = if (elderMode) 15.sp else 13.sp, color = palette.textPrimary)
                        Text(faq.answer, fontSize = bodySize, color = palette.textSecondary, lineHeight = if (elderMode) 20.sp else 16.sp)
                    }
                }
            }
        }
    }
}
