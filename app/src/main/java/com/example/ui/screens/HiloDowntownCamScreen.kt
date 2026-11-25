package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.HiloCamStatusProbeResult
import com.example.network.HiloCamStatusState
import com.example.network.HiloDowntownCamReference
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.AudioTextBoxStrip
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HiloDowntownCamContent(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    val context = LocalContext.current
    val a11y = LocalAccessibilityAudio.current
    val probeResult: HiloCamStatusProbeResult? =
        (uiState.hiloCamStatusState as? HiloCamStatusState.Checked)?.result
    val isChecking = uiState.hiloCamStatusState == HiloCamStatusState.Checking

    val openExternalUrl: (String) -> Unit = { url ->
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "No browser app available to open $url", Toast.LENGTH_SHORT).show()
        }
    }

    val briefText = HiloDowntownCamReference.buildOperationalBrief(
        probeResult = probeResult,
        userQuery = uiState.hiloCamStatusQuery
    )
    val briefSpeaking =
        a11y.playbackState.activeSourceId == "hilo_cam_brief" && a11y.playbackState.isActive

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hilo_downtown_cam_screen")
    ) {
        // 1. Target Area Header + Spatial Proximity Canvas
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(CyanNeon.copy(alpha = 0.75f), BlueSat.copy(alpha = 0.45f))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hilo_target_sector_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HILO DOWNTOWN CAM MONITOR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(
                            text = HiloDowntownCamReference.TARGET_AREA_TITLE,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = HiloDowntownCamReference.TARGET_STREET_ADDRESS,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberRadar,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.openHiloDowntownInSatFeed() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("hilo_sat_focus_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SatelliteAlt,
                            contentDescription = "Focus Hilo sector in Satellite Feed",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("GIBS Sector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tactical Downtown Hilo Proximity Canvas
                HiloDowntownProximityDiagram(
                    distanceMeters = HiloDowntownCamReference.distanceToPostOfficeMeters()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HiloSectorChip(
                        label = "TARGET BLOCK",
                        value = "154 Waianuenue Ave",
                        accent = AmberRadar,
                        modifier = Modifier.weight(1f)
                    )
                    HiloSectorChip(
                        label = "CAM COORDS",
                        value = "${HiloDowntownCamReference.CAMERA_LAT}, ${HiloDowntownCamReference.CAMERA_LON}",
                        accent = CyanNeon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Primary Public Camera Card (ALWAYS LISTED FIRST)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = BorderStroke(1.5.dp, EmeraldVerified.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hilo_primary_cam_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldVerified.copy(alpha = 0.20f))
                            .border(1.dp, EmeraldVerified, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "#1 PRIMARY PUBLIC CAMERA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldVerified,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Text(
                        text = "~${HiloDowntownCamReference.distanceToPostOfficeMeters()} m to Target",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = HiloDowntownCamReference.PRIMARY_CAMERA_NAME,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Official link listed prominently FIRST
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .border(1.dp, CyanNeon.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "OFFICIAL PUBLIC FEED LINK (LISTED FIRST)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = HiloDowntownCamReference.PRIMARY_OFFICIAL_URL,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        fontFamily = JetBrainsMonoFontFamily,
                        modifier = Modifier.testTag("hilo_official_url_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coordinates approx: ${HiloDowntownCamReference.CAMERA_LAT}, ${HiloDowntownCamReference.CAMERA_LON}",
                        fontSize = 11.sp,
                        color = AmberRadar,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View: ${HiloDowntownCamReference.CAMERA_VIEW_DESCRIPTION}",
                        fontSize = 12.sp,
                        color = Slate200
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bandwidth / Offline Warning & No-Invented-Stream-URL Rule Callout
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberRadar.copy(alpha = 0.14f))
                        .border(1.dp, AmberRadar.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .testTag("hilo_bandwidth_warning_card")
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Bandwidth and offline advisory",
                        tint = AmberRadar,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "FEED RELIABILITY & STREAM POLICY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = HiloDowntownCamReference.BANDWIDTH_OFFLINE_NOTE,
                            fontSize = 12.sp,
                            color = Slate100
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Action Buttons (>=48dp touch targets)
                Button(
                    onClick = { openExternalUrl(HiloDowntownCamReference.PRIMARY_OFFICIAL_URL) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldVerified,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("hilo_open_official_cam_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open Official Webcam Page (tsunami.org)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.checkHiloCamOfficialPageStatus() },
                        enabled = !isChecking,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("hilo_check_status_btn")
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                color = Slate950,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isChecking) "Checking..." else "Check Page Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { openExternalUrl(HiloDowntownCamReference.SEARCH_STATUS_URL) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Slate900,
                            contentColor = AmberRadar
                        ),
                        border = BorderStroke(1.5.dp, AmberRadar),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("hilo_search_status_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Status via Search",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Live Page Reachability Probe Result
                if (probeResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val probeColor = if (probeResult.isReachable) EmeraldVerified else CrimsonThreat
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(probeColor.copy(alpha = 0.14f))
                            .border(1.dp, probeColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .testTag("hilo_status_probe_result")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (probeResult.isReachable) {
                                    "PAGE REACHABLE (HTTP ${probeResult.httpStatusCode ?: 200})"
                                } else {
                                    "PAGE UNREACHABLE / DEGRADED"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = probeColor,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                            Text(
                                text = probeResult.checkedAtHst,
                                fontSize = 10.sp,
                                color = Slate300,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = probeResult.summary,
                            fontSize = 12.sp,
                            color = Slate100
                        )
                    }
                }
            }
        }

        // 3. Operator Query & Structured Status Response Card (with Visual Select-to-Speak)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = BorderStroke(
                width = if (briefSpeaking) 2.dp else 1.dp,
                color = visualAudioHighlightBorder(
                    briefSpeaking,
                    a11y.visualAudioEnabled,
                    DarkBorder
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hilo_status_brief_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DOWNTOWN HILO LIVE VIEW & STATUS BRIEF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(
                            text = "Operator Checklist & Audio Readout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    AudioSelectButton(
                        sourceId = "hilo_cam_brief",
                        title = "Hilo Downtown Cam Status Brief",
                        text = briefText,
                        compact = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.hiloCamStatusQuery,
                    onValueChange = { viewModel.updateHiloCamStatusQuery(it) },
                    label = {
                        Text(
                            text = "Target Observation / Status Note",
                            fontSize = 12.sp,
                            color = CyanNeon
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100,
                        cursorColor = CyanNeon
                    ),
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hilo_cam_query_input")
                )

                if (a11y.visualAudioEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AudioTextBoxStrip(
                        sourceId = "hilo_cam_query",
                        title = "Hilo Observation Note",
                        textToRead = uiState.hiloCamStatusQuery
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ordered compliance checklist matching module rules
                HiloProtocolLine("1", "Primary link first: ${HiloDowntownCamReference.PRIMARY_OFFICIAL_URL}", CyanNeon)
                Spacer(modifier = Modifier.height(5.dp))
                HiloProtocolLine("2", "Availability caveat: Feeds are frequently bandwidth-limited or offline.", AmberRadar)
                Spacer(modifier = Modifier.height(5.dp))
                HiloProtocolLine("3", "Live check: Offer/run status check via search or verify museum page directly.", EmeraldVerified)
                Spacer(modifier = Modifier.height(5.dp))
                HiloProtocolLine("4", "Stream integrity: Never invent a direct stream URL — none are publicly documented.", CrimsonThreat)
            }
        }
    }
}

@Composable
private fun HiloDowntownProximityDiagram(
    distanceMeters: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate950)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "WATERFRONT → WAIANUENUE BLOCK PROXIMITY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate300,
                fontFamily = JetBrainsMonoFontFamily
            )
            Text(
                text = String.format(Locale.US, "~%d m span", distanceMeters),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldVerified,
                fontFamily = JetBrainsMonoFontFamily
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
        ) {
            val w = size.width
            val h = size.height

            // Subtle bay waterfront band on right/top
            drawRect(
                color = BlueSat.copy(alpha = 0.14f),
                topLeft = Offset(w * 0.68f, 0f),
                size = androidx.compose.ui.geometry.Size(w * 0.32f, h)
            )

            // Coordinates for 3 landmarks inside sector schematic
            val fedPos = Offset(w * 0.16f, h * 0.66f)
            val parkPos = Offset(w * 0.42f, h * 0.56f)
            val camPos = Offset(w * 0.72f, h * 0.34f)

            // Dashed sightline / street corridor
            drawLine(
                color = CyanNeon.copy(alpha = 0.75f),
                start = fedPos,
                end = parkPos,
                strokeWidth = 3f
            )
            drawLine(
                color = EmeraldVerified.copy(alpha = 0.85f),
                start = parkPos,
                end = camPos,
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )

            // Camera FoV cone toward Hilo Bay & waterfront edge
            drawCircle(
                color = EmeraldVerified.copy(alpha = 0.18f),
                radius = 22f,
                center = camPos
            )
            drawCircle(
                color = EmeraldVerified,
                radius = 12f,
                center = camPos,
                style = Stroke(width = 2.5f)
            )

            // Dots for 154 Waianuenue Ave, Kalakaua Park, PT Museum Cam
            drawCircle(color = AmberRadar, radius = 8f, center = fedPos)
            drawCircle(color = CyanNeon, radius = 8f, center = parkPos)
            drawCircle(color = EmeraldVerified, radius = 7f, center = camPos)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberRadar))
                Spacer(modifier = Modifier.width(4.dp))
                Text("154 Waianuenue", fontSize = 10.sp, color = Slate200)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanNeon))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kalakaua Park", fontSize = 10.sp, color = Slate200)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldVerified))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PTM Hilo Bay Cam", fontSize = 10.sp, color = Slate200)
            }
        }
    }
}

@Composable
private fun HiloSectorChip(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate850)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            fontFamily = JetBrainsMonoFontFamily
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate100,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = JetBrainsMonoFontFamily
        )
    }
}

@Composable
private fun HiloProtocolLine(
    index: String,
    text: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accent.copy(alpha = 0.2f))
                .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
        ) {
            Text(
                text = index,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                fontFamily = JetBrainsMonoFontFamily
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Slate200,
            modifier = Modifier.weight(1f)
        )
    }
}
