package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppApiCatalog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalMapPalette
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.contrastContentColor

@Composable
fun PolicyUserAgreementDialog(
    alreadyAccepted: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = palette.panelSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, palette.warningAmber),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.86f)
                .testTag("btam_policy_agreement_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Gavel,
                            contentDescription = "User Agreement & Policy Disclaimer",
                            tint = palette.warningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Policy & User Agreement",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGroteskFontFamily,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Non-Affiliation • Non-Endorsement • Use At Own Risk",
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = palette.warningAmber
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btam_policy_dialog_close_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close policy dialog", tint = palette.textPrimary)
                    }
                }

                HorizontalDivider(color = palette.panelBorder)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        color = palette.warningAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = AppApiCatalog.NON_AFFILIATION_HEADER,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = palette.warningAmber
                            )
                            Text(
                                text = AppApiCatalog.NON_AFFILIATION_BODY,
                                fontSize = 12.sp,
                                color = palette.textPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Surface(
                        color = palette.canvasBackground,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.panelBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "USE-AT-YOUR-OWN-RISK & LIABILITY DISCLAIMER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = palette.criticalRed
                            )
                            Text(
                                text = AppApiCatalog.USE_AT_OWN_RISK_BODY,
                                fontSize = 12.sp,
                                color = palette.textPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Surface(
                        color = palette.canvasBackground,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "PRIVACY, NON-PARTISAN & HUMAN GOVERNANCE POLICY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = palette.normalGreen
                            )
                            Text(
                                text = "• Zero political persuasion, voter targeting, or demographic profiling.\n" +
                                    "• Automatic PII scrubbing on unaffiliated community/bystander inputs.\n" +
                                    "• Epistemic labeling strictly separates Verified Public Feeds, User-Generated Info, AI Insights, Forecasts & Speculative Projections.",
                                fontSize = 12.sp,
                                color = palette.textSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.normalGreen,
                        contentColor = contrastContentColor(palette.normalGreen)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("btam_policy_dialog_accept_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (alreadyAccepted) "Agreement Accepted (Close)" else "I Agree & Accept Terms",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Confirms non-affiliation notice & use-at-your-own-risk acknowledgment",
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppApiDirectoryDialog(
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) AppApiCatalog.entries
        else AppApiCatalog.entries.filter {
            it.name.lowercase().contains(q) ||
                it.provider.lowercase().contains(q) ||
                it.appRelevance.lowercase().contains(q) ||
                it.howUsedInApp.lowercase().contains(q)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = palette.panelSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, palette.advisoryBlue),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("app_api_directory_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Api,
                            contentDescription = "Integrated App APIs",
                            tint = palette.advisoryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "All App APIs & Relevance (${AppApiCatalog.entries.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGroteskFontFamily,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Transparent Data Sources • No Agency Endorsement",
                                fontSize = 10.sp,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = palette.advisoryBlue
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("app_api_directory_close_btn")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close API directory", tint = palette.textPrimary)
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Filter APIs (e.g. NASA, FAA, NOAA, OpenSky, 911)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_api_search_input")
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("app_api_directory_list")
                ) {
                    items(filtered, key = { it.id }) { api ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.canvasBackground),
                            border = BorderStroke(1.dp, palette.panelBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_api_card_${api.id}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = api.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.advisoryBlue
                                )
                                Text(
                                    text = "${api.provider}\n${api.endpointOrInterface}",
                                    fontSize = 10.sp,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.textSecondary
                                )
                                Text(
                                    text = "App Relevance: ${api.appRelevance}",
                                    fontSize = 12.sp,
                                    color = palette.textPrimary,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "How Used: ${api.howUsedInApp}",
                                    fontSize = 11.sp,
                                    color = palette.normalGreen,
                                    lineHeight = 15.sp
                                )
                                Text(
                                    text = "Use-At-Own-Risk Note: ${api.disclaimerNote}",
                                    fontSize = 10.sp,
                                    color = palette.warningAmber,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("app_api_directory_done_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Return to Workspace", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "All APIs remain informational only and subject to User Agreement",
                            fontSize = 10.sp,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}
