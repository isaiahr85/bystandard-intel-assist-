package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tools.BallisticMetrologyEngine
import com.example.tools.ErrorLevelAnalysisEngine
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.LabTool
import com.example.ui.NavigationTab
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.ForensicTopBar
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun ForensicLabScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "Forensic Lab",
                subtitle = "Multimodal Verification & Computational Toolkits"
            )
        },
        containerColor = Slate950,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Lab Tool Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = uiState.activeLabTool.ordinal,
                containerColor = Slate950,
                contentColor = CyanNeon,
                edgePadding = 16.dp,
                divider = { HorizontalDivider(color = DarkBorder) },
                modifier = Modifier.fillMaxWidth()
            ) {
                LabTool.values().forEach { tool ->
                    Tab(
                        selected = uiState.activeLabTool == tool,
                        onClick = { viewModel.selectLabTool(tool) },
                        text = {
                            Text(
                                text = tool.label,
                                fontSize = 12.sp,
                                fontWeight = if (uiState.activeLabTool == tool) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("lab_tab_${tool.name.lowercase()}")
                    )
                }
            }

            // Tool Content Body
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    when (uiState.activeLabTool) {
                        LabTool.CHRONOLOCATION -> ChronolocationTool(viewModel, uiState)
                        LabTool.ELA_INSPECTOR -> ElaInspectorTool(viewModel, uiState)
                        LabTool.BALLISTICS_SCOPE -> BallisticScopeTool(viewModel, uiState)
                        LabTool.GEOINT_SENSOR -> GeointSensorTool(viewModel, uiState)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. CHRONOLOCATION & SHADOW GEOMETRY TOOL
// -------------------------------------------------------------
@Composable
fun ChronolocationTool(viewModel: ForensicViewModel, uiState: ForensicUiState) {
    val result = uiState.chronoResult

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "SOLAR EPHEMERIS & SHADOW TRIANGULATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 1.sp
        )

        Text(
            text = "Calculate incident capture timestamp from object height and cast shadow length. Solves solar elevation angle θ = arctan(h/s) and cross-references astronomical ephemeris.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        // Interactive Canvas Diagram of Shadow Angle
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Real-time Trigonometric Geometry",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate100
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AmberRadar.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "θ = ${result.sunElevationDeg}°",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate850)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val groundY = h * 0.78f
                        val poleX = w * 0.35f

                        // Draw ground plane line
                        drawLine(
                            color = Slate600,
                            start = Offset(0f, groundY),
                            end = Offset(w, groundY),
                            strokeWidth = 3f
                        )

                        // Calculate visual scaling
                        val poleH = (uiState.chronoObjectHeight.toFloat() * 5f).coerceIn(30f, groundY - 20f)
                        val poleTop = groundY - poleH

                        // Draw vertical object pole
                        drawLine(
                            color = CyanNeon,
                            start = Offset(poleX, groundY),
                            end = Offset(poleX, poleTop),
                            strokeWidth = 6f
                        )

                        // Draw shadow on ground (towards right)
                        val shadowVisualLength = (uiState.chronoShadowLength.toFloat() * 3.5f).coerceIn(20f, w - poleX - 20f)
                        val shadowTipX = poleX + shadowVisualLength

                        drawLine(
                            color = AmberRadar,
                            start = Offset(poleX, groundY),
                            end = Offset(shadowTipX, groundY),
                            strokeWidth = 6f
                        )

                        // Draw hypotenuse sun beam line
                        drawLine(
                            color = Slate300.copy(alpha = 0.65f),
                            start = Offset(shadowTipX, groundY),
                            end = Offset(poleX, poleTop),
                            strokeWidth = 2f
                        )

                        // Sun position extrapolated along hypotenuse backwards
                        val sunDist = 50f
                        val dirX = poleX - shadowTipX
                        val dirY = poleTop - groundY
                        val len = sqrt(dirX * dirX + dirY * dirY).coerceAtLeast(1f)
                        val sunX = poleX + (dirX / len) * sunDist
                        val sunY = poleTop + (dirY / len) * sunDist

                        drawCircle(
                            color = AmberRadar,
                            radius = 12f,
                            center = Offset(sunX.coerceIn(15f, w - 15f), sunY.coerceIn(15f, groundY - 15f))
                        )
                    }
                }
            }
        }

        // Sliders Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Height Slider
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Object Height: ${uiState.chronoObjectHeight} m", fontSize = 12.sp, color = Slate200)
                    Text("Building / Pole", fontSize = 11.sp, color = Slate400)
                }
                Slider(
                    value = uiState.chronoObjectHeight.toFloat(),
                    onValueChange = {
                        viewModel.updateChronolocation(height = (it * 10).roundToInt() / 10.0)
                    },
                    valueRange = 1f..40f,
                    colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon),
                    modifier = Modifier.testTag("chrono_height_slider")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Shadow Length Slider
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Shadow Length: ${uiState.chronoShadowLength} m", fontSize = 12.sp, color = Slate200)
                    Text("Cast Ground Extent", fontSize = 11.sp, color = Slate400)
                }
                Slider(
                    value = uiState.chronoShadowLength.toFloat(),
                    onValueChange = {
                        viewModel.updateChronolocation(shadow = (it * 10).roundToInt() / 10.0)
                    },
                    valueRange = 1f..60f,
                    colors = SliderDefaults.colors(thumbColor = AmberRadar, activeTrackColor = AmberRadar),
                    modifier = Modifier.testTag("chrono_shadow_slider")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Latitude
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Latitude: ${uiState.chronoLatitude}° N", fontSize = 12.sp, color = Slate200)
                    Text("Sector 4 Waterfront", fontSize = 11.sp, color = Slate400)
                }
                Slider(
                    value = uiState.chronoLatitude.toFloat(),
                    onValueChange = {
                        viewModel.updateChronolocation(lat = (it * 100).roundToInt() / 100.0)
                    },
                    valueRange = 10f..70f,
                    colors = SliderDefaults.colors(thumbColor = Slate200, activeTrackColor = Slate400),
                    modifier = Modifier.testTag("chrono_lat_slider")
                )
            }
        }

        // Calculation Result Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.5f), DarkBorder))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOLAR TIME ESTIMATION (ASTRONOMICAL FIT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Morning Window (Primary):", fontSize = 11.sp, color = Slate400)
                        Text(
                            text = result.morningEstimateUtc,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldVerified,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Afternoon Mirror:", fontSize = 11.sp, color = Slate400)
                        Text(
                            text = result.afternoonEstimateUtc,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate200,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Shadow-to-Height Ratio:", fontSize = 12.sp, color = Slate400)
                    Text("${result.shadowRatio} : 1.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solar Zenith Angle:", fontSize = 12.sp, color = Slate400)
                    Text("${result.sunZenithDeg}°", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Astronomical Confidence:", fontSize = 12.sp, color = Slate400)
                    Text("${result.solarTimeConfidence}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                }
                Spacer(modifier = Modifier.height(10.dp))
                AudioSelectButton(
                    sourceId = "lab_chrono",
                    title = "Chronolocation Result",
                    text = "Solar elevation ${result.sunElevationDeg} degrees. Morning window ${result.morningEstimateUtc}, afternoon mirror ${result.afternoonEstimateUtc}. Astronomical confidence ${result.solarTimeConfidence} percent.",
                    compact = true
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. ERROR LEVEL ANALYSIS (ELA) & TAMPERING INSPECTOR
// -------------------------------------------------------------
@Composable
fun ElaInspectorTool(viewModel: ForensicViewModel, uiState: ForensicUiState) {
    val assessment = uiState.elaAssessment

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "DIGITAL MEDIA ERROR LEVEL ANALYSIS & CONTAINER AUDIT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 1.sp
        )

        Text(
            text = "Inspect high-frequency 8x8 DCT compression discrepancies, container atom signatures (ftyp/moov/mdat), and C2PA cryptographic provenance manifests to identify manipulated media.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        // Preset selector buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple("PRISTINE_CAMERA", "Pristine", EmeraldVerified),
                Triple("SPLICED_MUNITION", "Spliced", CrimsonThreat),
                Triple("STRIPPED_EXIF", "Stripped EXIF", AmberRadar)
            ).forEach { (key, label, accent) ->
                val isSelected = uiState.selectedElaPreset == key
                OutlinedButton(
                    onClick = { viewModel.selectElaPreset(key) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) accent else Slate850,
                        contentColor = if (isSelected) Slate950 else Slate100
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            if (isSelected) listOf(accent, accent) else listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0.5f))
                        )
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // Simulated ELA Heatmap Canvas
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Simulated 8x8 DCT Error Heatmap",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate100
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (assessment.isTamperingSuspected) CrimsonThreat.copy(alpha = 0.2f) else EmeraldVerified.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (assessment.isTamperingSuspected) "TAMPERING FLAGGED (${assessment.anomalyScorePercent}%)" else "UNIFORM NOISE FLOOR (${assessment.anomalyScorePercent}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (assessment.isTamperingSuspected) CrimsonThreat else EmeraldVerified
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw background noise matrix
                        val cols = 20
                        val rows = 8
                        val cellW = w / cols
                        val cellH = h / rows

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val noise = ((c * 7 + r * 13) % 10) / 10f
                                val cellColor = if (uiState.selectedElaPreset == "SPLICED_MUNITION" && c in 9..14 && r in 2..5) {
                                    // Spliced bounding box: high-energy hot magenta & amber
                                    if ((c + r) % 2 == 0) CrimsonThreat else AmberRadar
                                } else {
                                    // Pristine or uniform noise
                                    Color(0xFF0F1E36).copy(alpha = 0.5f + noise * 0.4f)
                                }

                                drawRect(
                                    color = cellColor,
                                    topLeft = Offset(c * cellW, r * cellH),
                                    size = Size(cellW - 1f, cellH - 1f)
                                )
                            }
                        }

                        // If spliced, draw highlight bounding reticle
                        if (uiState.selectedElaPreset == "SPLICED_MUNITION") {
                            drawRect(
                                color = CrimsonThreat,
                                topLeft = Offset(9 * cellW, 2 * cellH),
                                size = Size(6 * cellW, 4 * cellH),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }
            }
        }

        // Assessment Details Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DCT QUANTIZATION & NOISE FINDINGS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(text = "Compression: ${assessment.globalCompressionRatio}", fontSize = 12.sp, color = Slate200)
                Text(text = "Uniformity: ${assessment.quantizationUniformity}", fontSize = 12.sp, color = Slate400)

                assessment.flaggedRegions.forEach { region ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("• ", color = if (assessment.isTamperingSuspected) CrimsonThreat else EmeraldVerified, fontSize = 14.sp)
                        Text(text = region, fontSize = 11.sp, color = Slate200)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "CONTAINER ATOMS & C2PA CRYPTOGRAPHIC AUDIT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberRadar,
                    letterSpacing = 1.sp
                )
                Text(text = "Container Atoms: ${assessment.containerAnalysis.atomStructure}", fontSize = 11.sp, color = Slate200, fontFamily = FontFamily.Monospace)
                Text(text = "Encoder: ${assessment.containerAnalysis.encoderSignature}", fontSize = 11.sp, color = Slate400)
                Text(text = "C2PA Provenance: ${assessment.c2paAudit.signerIdentity} (${assessment.c2paAudit.certValidity})", fontSize = 11.sp, color = if (assessment.c2paAudit.isManifestPresent) EmeraldVerified else Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                AudioSelectButton(
                    sourceId = "lab_ela",
                    title = "ELA Integrity Assessment",
                    text = "ELA anomaly score ${assessment.anomalyScorePercent} percent, tampering suspected ${assessment.isTamperingSuspected}. ${assessment.globalCompressionRatio}. ${assessment.quantizationUniformity}. Findings: ${assessment.flaggedRegions.joinToString(". ")}",
                    compact = true
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. BALLISTIC SPLIT-SCOPE (COMPARISON MICROSCOPE)
// -------------------------------------------------------------
@Composable
fun BallisticScopeTool(viewModel: ForensicViewModel, uiState: ForensicUiState) {
    val comparison = uiState.ballisticComparison
    val (waveA, waveB) = remember(comparison.isPositiveMatch) {
        BallisticMetrologyEngine.generateWaveformPoints(comparison.isPositiveMatch)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "CONFOCAL 3D SURFACE TOPOGRAPHY & TOOLMARK SPLIT-SCOPE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 1.sp
        )

        Text(
            text = "Simulates optical comparison microscope matching crime scene cartridge casing striations against the NIST NBTRD and NIBIN correlation database.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        // Casing Selector Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    viewModel.updateBallisticComparison(
                        casingA = "Glock 19 Gen 5 (9x19mm)",
                        casingB = "Glock 19 Gen 5 (9x19mm)",
                        slider = uiState.ballisticSplitSlider
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (comparison.isPositiveMatch) EmeraldVerified else Slate850,
                    contentColor = if (comparison.isPositiveMatch) Slate950 else Slate100
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        if (comparison.isPositiveMatch) listOf(EmeraldVerified, EmeraldVerified)
                        else listOf(EmeraldVerified.copy(alpha = 0.5f), EmeraldVerified.copy(alpha = 0.5f))
                    )
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
            ) {
                Text("Match Pair (Glock 19)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.updateBallisticComparison(
                        casingA = "Glock 19 Gen 5 (9x19mm)",
                        casingB = "Colt M4A1 (5.56mm)",
                        slider = uiState.ballisticSplitSlider
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!comparison.isPositiveMatch) CrimsonThreat else Slate850,
                    contentColor = if (!comparison.isPositiveMatch) Slate950 else Slate100
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        if (!comparison.isPositiveMatch) listOf(CrimsonThreat, CrimsonThreat)
                        else listOf(CrimsonThreat.copy(alpha = 0.5f), CrimsonThreat.copy(alpha = 0.5f))
                    )
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
            ) {
                Text("Discordant Non-Match", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Interactive Split-View Comparison Canvas with Draggable Slider
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Optical Split Divider (Drag Left/Right)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Slate100
                    )
                    Text(
                        text = "Score: ${comparison.correlationScorePercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comparison.isPositiveMatch) EmeraldVerified else CrimsonThreat
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val currentSlider by rememberUpdatedState(uiState.ballisticSplitSlider)
                val currentCasingA by rememberUpdatedState(uiState.ballisticCasingA)
                val currentCasingB by rememberUpdatedState(uiState.ballisticCasingB)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newSlider = (currentSlider + (dragAmount.x / size.width.toFloat())).coerceIn(0.1f, 0.9f)
                                viewModel.updateBallisticComparison(
                                    casingA = currentCasingA,
                                    casingB = currentCasingB,
                                    slider = newSlider
                                )
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val splitX = w * uiState.ballisticSplitSlider

                        // Left Field: Casing A (Crime Scene)
                        for (i in 0 until 40) {
                            val x = i * (splitX / 40f)
                            val intensity = (sin(i * 0.6f) * 0.4f + 0.5f)
                            drawLine(
                                color = CyanNeon.copy(alpha = intensity * 0.8f),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = (splitX / 40f) * 0.7f
                            )
                        }

                        // Right Field: Casing B (Reference)
                        val rightWidth = w - splitX
                        for (i in 0 until 40) {
                            val x = splitX + i * (rightWidth / 40f)
                            val intensity = if (comparison.isPositiveMatch) {
                                (sin(i * 0.6f) * 0.4f + 0.5f)
                            } else {
                                (cos(i * 1.1f) * 0.4f + 0.5f)
                            }
                            drawLine(
                                color = AmberRadar.copy(alpha = intensity * 0.8f),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = (rightWidth / 40f) * 0.7f
                            )
                        }

                        // Split line
                        drawLine(
                            color = Color.White,
                            start = Offset(splitX, 0f),
                            end = Offset(splitX, h),
                            strokeWidth = 3f
                        )

                        // Split handle circle
                        drawCircle(
                            color = Color.White,
                            radius = 8f,
                            center = Offset(splitX, h / 2f)
                        )
                    }

                    // Field labels
                    Text(
                        text = "CASE EVIDENCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                    Text(
                        text = "NIST REFERENCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberRadar,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
            }
        }

        // 3D Roughness Waveform Plot Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "3D STRIATION CROSS-SECTION (X3P PROFILER)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate950)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f
                        val step = w / waveA.size.toFloat()

                        for (i in 0 until waveA.size - 1) {
                            drawLine(
                                color = CyanNeon,
                                start = Offset(i * step, midY + waveA[i] * 30f),
                                end = Offset((i + 1) * step, midY + waveA[i + 1] * 30f),
                                strokeWidth = 2.5f
                            )
                            drawLine(
                                color = AmberRadar.copy(alpha = 0.8f),
                                start = Offset(i * step, midY + waveB[i] * 30f),
                                end = Offset((i + 1) * step, midY + waveB[i + 1] * 30f),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = comparison.conclusion,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (comparison.isPositiveMatch) EmeraldVerified else CrimsonThreat
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Congruent Cells (CMC):", fontSize = 11.sp, color = Slate400)
                    Text("${comparison.congruentMatchingCells} / ${comparison.totalCellsEvaluated}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Roughness Delta (Ra):", fontSize = 11.sp, color = Slate400)
                    Text("${comparison.meanRoughnessRaDeltaUm} µm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate100)
                }
                Spacer(modifier = Modifier.height(10.dp))
                AudioSelectButton(
                    sourceId = "lab_ballistics",
                    title = "Ballistic Comparison Report",
                    text = "Correlation score ${comparison.correlationScorePercent} percent. Congruent matching cells ${comparison.congruentMatchingCells} of ${comparison.totalCellsEvaluated}. ${comparison.conclusion}",
                    compact = true
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 4. GEOINT & SAR SENSOR ANALYZER
// -------------------------------------------------------------
@Composable
fun GeointSensorTool(viewModel: ForensicViewModel, uiState: ForensicUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "RADAR & MULTISPECTRAL REMOTE SENSING MATRIX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            letterSpacing = 1.sp
        )

        Text(
            text = "Comparative evaluation of Synthetic Aperture Radar (SAR), high-resolution optical, and short-wave infrared (SWIR) sensors for conflict reconnaissance and structural damage mapping.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate400
        )

        Button(
            onClick = { viewModel.selectTab(NavigationTab.SAT_FEED) },
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.SatelliteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Live NASA EONET & GIBS Satellite Feed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        uiState.geointSensors.forEach { sensor ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sensor.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate100
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BlueSat.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sensor.resolutionMeters,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlueSat
                            )
                        }
                    }

                    Text(
                        text = sensor.constellation,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanNeon
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sensor.bestUseCases,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate850)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Spectral Bands: ${sensor.primarySpectralBands}",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Access Model: ${sensor.accessModel}",
                                fontSize = 11.sp,
                                color = EmeraldVerified
                            )
                        }
                    }
                }
            }
        }
    }
}
