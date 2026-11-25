package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.network.GeoPoint
import com.example.network.HawaiiAirspaceReference
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Unified multi-touch gesture handler for embedded and full-screen map viewports:
 * - Supports 2-finger pinch-to-zoom, 2-finger rotation, 2-finger pan, and parallel vertical 2-finger tilt
 *   at ANY zoom level (including 1.0x) without waiting for page scroll lock.
 * - Preserves parent LazyColumn vertical scrolling for 1-finger swipes when [allowSingleFingerPan] is false.
 */
suspend fun PointerInputScope.detectMapMultitouchGestures(
    enableTilt: Boolean = false,
    isSingleFingerTiltEnabled: () -> Boolean = { false },
    allowSingleFingerPan: () -> Boolean = { true },
    onTransform: (zoomChange: Float, panChange: Offset, rotationChangeDeg: Float, tiltDeltaDeg: Float) -> Unit
) {
    awaitEachGesture {
        var zoom = 1f
        var rotation = 0f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break

            val isMultiTouch = pressed.size >= 2
            val allowOneFinger = allowSingleFingerPan()
            if (!isMultiTouch && !allowOneFinger) {
                // Leave single-finger vertical/horizontal drags unconsumed so LazyColumn scrolls smoothly
                continue
            }

            val zoomChange = if (isMultiTouch) event.calculateZoom() else 1f
            val rotationChange = if (isMultiTouch) event.calculateRotation() else 0f
            val panChange = event.calculatePan()

            if (!pastTouchSlop) {
                zoom *= zoomChange
                rotation += rotationChange
                pan += panChange

                val centroidSize = event.calculateCentroid(useCurrent = false).let {
                    if (it == Offset.Unspecified) 0f else 1f
                }
                val zoomMotion = abs(1 - zoom) * if (isMultiTouch) 120f * centroidSize else 0f
                val rotationMotion = abs(rotation * PI.toFloat() * 120f / 180f)
                val panMotion = pan.getDistance()

                if (zoomMotion > touchSlop || rotationMotion > touchSlop || panMotion > touchSlop) {
                    pastTouchSlop = true
                }
            }

            if (pastTouchSlop) {
                var emittedPan = panChange
                var tiltDelta = 0f

                if (enableTilt && isMultiTouch) {
                    val verticalParallelTilt =
                        abs(panChange.y) > 2.5f &&
                            abs(panChange.y) > abs(panChange.x) * 1.35f &&
                            abs(zoomChange - 1f) < 0.035f &&
                            abs(rotationChange) < 1.8f
                    if (verticalParallelTilt) {
                        tiltDelta = -panChange.y * 0.18f
                        emittedPan = Offset.Zero
                    }
                } else if (enableTilt && isSingleFingerTiltEnabled() && !isMultiTouch && abs(panChange.y) > abs(panChange.x)) {
                    tiltDelta = -panChange.y * 0.15f
                    emittedPan = Offset.Zero
                }

                if (zoomChange != 1f || rotationChange != 0f || emittedPan != Offset.Zero || tiltDelta != 0f) {
                    onTransform(zoomChange, emittedPan, rotationChange, tiltDelta)
                }
                if (isMultiTouch || allowOneFinger) {
                    pressed.forEach { it.consume() }
                }
            }
        } while (pressed.isNotEmpty())
    }
}

fun colorForAirspaceClass(cls: AirspaceClassification, palette: MapColorPalette): Color = when (cls) {
    AirspaceClassification.CLASS_A -> palette.classAColor
    AirspaceClassification.CLASS_B -> palette.classBColor
    AirspaceClassification.CLASS_C -> palette.classCColor
    AirspaceClassification.CLASS_D -> palette.classDColor
    AirspaceClassification.RESTRICTED -> palette.restrictedColor
    AirspaceClassification.TFR -> palette.tfrColor
}

@Composable
fun MapVisualThemeSelectorRow(
    selectedMode: MapVisualThemeMode,
    onSelectMode: (MapVisualThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "VISUAL ACCESSIBILITY MODE (WCAG • SUNLIGHT • CVD)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = palette.advisoryBlue,
            fontFamily = JetBrainsMonoFontFamily
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("map_visual_mode_row")
        ) {
            MapVisualThemeMode.entries.forEach { mode ->
                val active = selectedMode == mode
                val accent = when (mode) {
                    MapVisualThemeMode.NIGHT -> CyanNeon
                    MapVisualThemeMode.DAY -> palette.advisoryBlue
                    MapVisualThemeMode.HIGH_CONTRAST -> palette.warningAmber
                    MapVisualThemeMode.COLOR_BLIND -> palette.normalGreen
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) accent else palette.panelSurface)
                        .border(1.5.dp, if (active) accent else accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .clickable { onSelectMode(mode) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("map_theme_${mode.id}")
                ) {
                    Text(
                        text = mode.shortLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) contrastContentColor(accent) else palette.textPrimary,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun MapLayerPresetRow(
    activePreset: MapLayerPreset?,
    onSelectPreset: (MapLayerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "QUICK LAYER PRESETS (INSTANT SWITCH <500MS)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = palette.normalGreen,
            fontFamily = JetBrainsMonoFontFamily
        )
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("map_presets_row")
        ) {
            MapLayerPreset.entries.forEach { preset ->
                val selected = activePreset == preset
                FilterChip(
                    selected = selected,
                    onClick = { onSelectPreset(preset) },
                    label = {
                        Text(
                            text = preset.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.normalGreen,
                        selectedLabelColor = contrastContentColor(palette.normalGreen),
                        containerColor = palette.panelSurface,
                        labelColor = palette.textPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = palette.normalGreen.copy(alpha = 0.5f),
                        selectedBorderColor = palette.normalGreen
                    ),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag("map_preset_${preset.id}")
                )
            }
        }
    }
}

@Composable
fun AirspaceClassificationFilterPanel(
    enabledClasses: Set<AirspaceClassification>,
    altitudeBand: AirspaceAltitudeBand,
    airspaceOpacity: Float,
    selectedZone: AirspaceClassificationZone?,
    onToggleClass: (AirspaceClassification) -> Unit,
    onSelectAltitudeBand: (AirspaceAltitudeBand) -> Unit,
    onChangeOpacity: (Float) -> Unit,
    onDismissZone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.panelSurface)
            .border(1.5.dp, palette.panelBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("airspace_classification_panel")
    ) {
        Text(
            text = "AIRSPACE CLASSIFICATION & ALTITUDE FILTER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = palette.warningAmber,
            fontFamily = JetBrainsMonoFontFamily
        )

        // Class A / B / C / D / Restricted / TFR interactive toggle chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("airspace_class_chips_row")
        ) {
            AirspaceClassification.entries.forEach { cls ->
                val active = enabledClasses.contains(cls)
                val accent = colorForAirspaceClass(cls, palette)
                Box(
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) accent else palette.canvasBackground)
                        .border(1.5.dp, accent, RoundedCornerShape(8.dp))
                        .clickable { onToggleClass(cls) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("airspace_cls_${cls.id}")
                ) {
                    Text(
                        text = "${cls.symbolGlyph} ${cls.code}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) contrastContentColor(accent) else palette.textPrimary,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }
        }

        // Altitude Band Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("airspace_altitude_row")
        ) {
            AirspaceAltitudeBand.entries.forEach { band ->
                val selected = altitudeBand == band
                FilterChip(
                    selected = selected,
                    onClick = { onSelectAltitudeBand(band) },
                    label = {
                        Text(
                            text = band.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.advisoryBlue,
                        selectedLabelColor = contrastContentColor(palette.advisoryBlue),
                        containerColor = palette.canvasBackground,
                        labelColor = palette.textPrimary
                    ),
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .testTag("altitude_band_${band.id}")
                )
            }
        }

        // Airspace Overlay Opacity Slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "AIRSPACE OPACITY ${(airspaceOpacity * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary,
                fontFamily = JetBrainsMonoFontFamily
            )
            Slider(
                value = airspaceOpacity,
                onValueChange = onChangeOpacity,
                valueRange = 0.25f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = palette.warningAmber,
                    activeTrackColor = palette.warningAmber,
                    inactiveTrackColor = palette.panelBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .testTag("airspace_opacity_slider")
            )
        }

        // Interactive Selected Airspace Zone Detail Card
        if (selectedZone != null) {
            val zColor = colorForAirspaceClass(selectedZone.classification, palette)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(zColor.copy(alpha = 0.14f))
                    .border(1.5.dp, zColor, RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .testTag("selected_airspace_zone_detail")
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${selectedZone.classification.symbolGlyph} ${selectedZone.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissZone,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close airspace detail",
                            tint = palette.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${selectedZone.classification.code} • ${selectedZone.altitudeDisplay} • ${selectedZone.controllingAgency}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = zColor,
                    fontFamily = JetBrainsMonoFontFamily
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = selectedZone.operationalDetail,
                    fontSize = 12.sp,
                    color = palette.textPrimary
                )
            }
        }
    }
}

@Composable
fun LiveNavGpsOfflinePanel(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    var newWpLabel by remember { mutableStateOf("") }
    var showAddWpDialog by remember { mutableStateOf(false) }

    val locPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.any { it }
        viewModel.onLocationPermissionResult(granted)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.5.dp, palette.panelBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_nav_gps_offline_panel")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "REAL-TIME GPS • ADS-B • ROUTES • OFFLINE CACHE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.advisoryBlue,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Text(
                        text = if (uiState.isGpsLiveEnabled) {
                            "GPS LIVE (${uiState.gpsTelemetry.providerName})"
                        } else {
                            "Navigation & Sensor Readiness"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                // Semantic status indicator (Green/Blue/Amber/Red)
                val statusColor = if (uiState.isGpsLiveEnabled) palette.normalGreen else palette.advisoryBlue
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (uiState.isGpsLiveEnabled) "FIX <1s" else "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }

            // GPS Telemetry Strip + Route Summary
            val gps = uiState.gpsTelemetry
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TelemetryBox(
                    label = "POSITION",
                    value = String.format(Locale.US, "%.4f°, %.4f°", gps.latitude, gps.longitude),
                    accent = palette.advisoryBlue,
                    modifier = Modifier.weight(1f)
                )
                TelemetryBox(
                    label = "HDG / SPD / ALT",
                    value = "${gps.headingDeg}° • ${gps.speedKts.roundToInt()}kt • ${gps.altitudeFt}ft",
                    accent = palette.normalGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!uiState.gpsStatusNote.isNullOrBlank()) {
                val noteColor = if (uiState.isGpsLiveEnabled) palette.normalGreen else palette.warningAmber
                Text(
                    text = uiState.gpsStatusNote,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = noteColor,
                    fontFamily = JetBrainsMonoFontFamily,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(noteColor.copy(alpha = 0.12f))
                        .border(1.dp, noteColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("gps_status_note_text")
                )
            }

            // Action Buttons: Live GPS, ADS-B Traffic, Add Waypoint, Full-Screen Split
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val gpsBtnColor = if (uiState.isGpsLiveEnabled) palette.normalGreen else palette.advisoryBlue
                Button(
                    onClick = {
                        if (uiState.isGpsLiveEnabled) {
                            viewModel.stopLiveGpsTracking()
                        } else if (uiState.gpsPermissionGranted) {
                            viewModel.startLiveGpsTracking()
                        } else {
                            locPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gpsBtnColor,
                        contentColor = contrastContentColor(gpsBtnColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("nav_gps_toggle_btn")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isGpsLiveEnabled) "GPS Active" else "Enable GPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { viewModel.toggleAdsbTraffic() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.adsbEnabled) palette.warningAmber else palette.canvasBackground,
                        contentColor = if (uiState.adsbEnabled) contrastContentColor(palette.warningAmber) else palette.textPrimary
                    ),
                    border = BorderStroke(1.5.dp, palette.warningAmber),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("nav_adsb_toggle_btn")
                ) {
                    Icon(Icons.Default.AirplanemodeActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.adsbEnabled) "ADS-B (${uiState.adsbAircraft.size})" else "ADS-B Live",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = { showAddWpDialog = true },
                    border = BorderStroke(1.5.dp, palette.normalGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("nav_add_waypoint_btn")
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = palette.normalGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+Waypoint",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.normalGreen,
                        maxLines = 1
                    )
                }
            }

            // Dynamic Route Recalculation Summary & Waypoint Pills
            val route = uiState.routeSummary
            if (route.legs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.canvasBackground)
                        .border(1.dp, palette.normalGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .testTag("dynamic_route_summary_box")
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "ACTIVE ROUTE: %d legs • %.1f NM • ETE %.0f min • Next: %s (%03d° / %.1f NM)",
                            route.legs.size,
                            route.totalDistanceNm,
                            route.totalEteMinutes,
                            route.nextWaypointLabel ?: "-",
                            route.nextBearingDeg ?: 0,
                            route.nextDistanceNm ?: 0.0
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.normalGreen,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        uiState.userWaypoints.forEach { wp ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(palette.panelSurface)
                                    .border(1.dp, palette.panelBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${wp.label} (${wp.altitudeFt}ft)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.textPrimary,
                                    fontFamily = JetBrainsMonoFontFamily
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove waypoint ${wp.label}",
                                    tint = palette.criticalRed,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { viewModel.removeUserWaypoint(wp.id) }
                                )
                            }
                        }
                    }
                }
            }

            // ADS-B Status Readout
            if (uiState.adsbEnabled) {
                val adsbNote = when (val st = uiState.adsbStatus) {
                    AdsbFeedStatus.Idle -> "ADS-B armed — tap to poll OpenSky Hawaiʻi sector."
                    AdsbFeedStatus.Loading -> "Polling OpenSky Network ADS-B state vectors..."
                    is AdsbFeedStatus.Live -> "OpenSky Live (${st.updatedAtHst}): ${st.count} aircraft tracked in Hawaiʻi bounds."
                    is AdsbFeedStatus.Unavailable -> "ADS-B Unavailable: ${st.message}"
                }
                Text(
                    text = adsbNote,
                    fontSize = 11.sp,
                    color = if (uiState.adsbStatus is AdsbFeedStatus.Unavailable) palette.warningAmber else palette.textSecondary,
                    fontFamily = JetBrainsMonoFontFamily,
                    modifier = Modifier.testTag("adsb_status_text")
                )
            }

            // Offline Storage, Predictive Prefetch & Performance Telemetry
            Text(
                text = "OFFLINE SECTORS & PREDICTIVE TILE CACHE (${uiState.tileCacheSizeKb} KB)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.warningAmber,
                fontFamily = JetBrainsMonoFontFamily
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Button(
                    onClick = { viewModel.prefetchPredictiveMapTiles() },
                    enabled = !uiState.isPrefetchingTiles,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.advisoryBlue,
                        contentColor = contrastContentColor(palette.advisoryBlue)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .testTag("predictive_prefetch_btn")
                ) {
                    Text(
                        text = if (uiState.isPrefetchingTiles) "Caching..." else "Predictive Prefetch",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HawaiiOfflineSectorCatalog.sectors.forEach { sector ->
                    val cached = uiState.offlineSectorsCached.contains(sector.id)
                    OutlinedButton(
                        onClick = { viewModel.cacheOfflineSector(sector) },
                        border = BorderStroke(1.2.dp, if (cached) palette.normalGreen else palette.panelBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("offline_sector_${sector.id}")
                    ) {
                        Icon(
                            imageVector = if (cached) Icons.Default.CloudDone else Icons.Default.DownloadForOffline,
                            contentDescription = null,
                            tint = if (cached) palette.normalGreen else palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sector.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cached) palette.normalGreen else palette.textPrimary
                        )
                    }
                }
            }

            // Performance Latency + 4K HD + Multi-Display + Modular API Registry Strip
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.canvasBackground)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LATENCY: Gesture ${uiState.mapPerfMetrics.lastGestureLatencyMs}ms • Layer ${uiState.mapPerfMetrics.lastLayerSwitchMs}ms • Ext Displays ${uiState.externalDisplayCount}",
                    fontSize = 10.sp,
                    color = palette.textSecondary,
                    fontFamily = JetBrainsMonoFontFamily,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (uiState.hdTileResolution) palette.normalGreen.copy(alpha = 0.2f) else palette.panelSurface)
                        .border(1.dp, palette.normalGreen, RoundedCornerShape(6.dp))
                        .clickable { viewModel.toggleHdTileResolution() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                        .testTag("hd_tile_res_toggle")
                ) {
                    Text(
                        text = uiState.mapPerfMetrics.tileResolutionLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.normalGreen,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }

            // Scalable API-Driven External Overlay Registry
            Text(
                text = "API-DRIVEN LAYER REGISTRY (3RD-PARTY / AR TERRAIN READY)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textSecondary,
                fontFamily = JetBrainsMonoFontFamily
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                uiState.registeredExternalLayers.forEach { layer ->
                    FilterChip(
                        selected = layer.enabled,
                        onClick = { viewModel.toggleRegisteredExternalLayer(layer.id) },
                        label = {
                            Text(
                                text = "${layer.category}: ${layer.title.substringBefore(" ")}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("ext_layer_${layer.id}")
                    )
                }
            }
        }
    }

    if (showAddWpDialog) {
        AlertDialog(
            onDismissRequest = { showAddWpDialog = false },
            containerColor = palette.panelSurface,
            title = {
                Text(
                    text = "Add Navigation Waypoint",
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Plots a persistent waypoint at current map/GPS target (${String.format(Locale.US, "%.4f°, %.4f°", uiState.selectedAirspaceSite.lat, uiState.selectedAirspaceSite.lon)}) and recalculates active route legs.",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                    OutlinedTextField(
                        value = newWpLabel,
                        onValueChange = { newWpLabel = it },
                        label = { Text("Waypoint Ident (e.g. KILAUEA-N)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_waypoint_label_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addUserWaypoint(
                            label = newWpLabel.ifBlank { uiState.selectedAirspaceSite.shortCode },
                            lat = uiState.selectedAirspaceSite.lat,
                            lon = uiState.selectedAirspaceSite.lon,
                            altitudeFt = 2500
                        )
                        newWpLabel = ""
                        showAddWpDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_waypoint_btn")
                ) {
                    Text("Save Waypoint")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TelemetryBox(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(palette.canvasBackground)
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
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
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = JetBrainsMonoFontFamily
        )
    }
}

@Composable
fun ContextSensitiveMapLegendDialog(
    uiState: ForensicUiState,
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
            border = BorderStroke(1.5.dp, palette.panelBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("context_map_legend_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CONTEXT-SENSITIVE MAP KEY (${uiState.mapVisualThemeMode.shortLabel})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close legend", tint = palette.textPrimary)
                    }
                }
                Text(
                    text = "Semantic Alert Hierarchy",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary
                )
                LegendRow("CRITICAL / TFR ACTIVE", "Prohibited / TFR / Volcanic hazard zone", palette.criticalRed)
                LegendRow("WARNING / RESTRICTED", "R-3103 PTA military range & weather caution", palette.warningAmber)
                LegendRow("ADVISORY / CLASS B-D", "Towered Class B/C/D shelves & satellite info", palette.advisoryBlue)
                LegendRow("NORMAL / ACTIVE ROUTE", "Verified clear corridors & GPS flight route", palette.normalGreen)

                HorizontalDivider(color = palette.panelBorder)
                Text(
                    text = "Active Airspace Classifications (${uiState.airspaceAltitudeBand.label})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary
                )
                uiState.enabledAirspaceClasses.forEach { cls ->
                    LegendRow(
                        badge = "${cls.symbolGlyph} ${cls.code}",
                        desc = cls.label,
                        color = colorForAirspaceClass(cls, palette)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendRow(badge: String, desc: String, color: Color) {
    val palette = LocalMapPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(color)
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = contrastContentColor(color),
                fontFamily = JetBrainsMonoFontFamily
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = desc,
            fontSize = 11.sp,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickToolbarConfigDialog(
    selectedTools: List<QuickToolbarTool>,
    onToggleTool: (QuickToolbarTool) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
            border = BorderStroke(1.5.dp, palette.panelBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_toolbar_config_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "CONFIGURE FLOATING QUICK TOOLBAR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.advisoryBlue,
                    fontFamily = JetBrainsMonoFontFamily
                )
                QuickToolbarTool.entries.forEach { tool ->
                    val checked = selectedTools.contains(tool)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleTool(tool) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggleTool(tool) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tool.shortLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

/**
 * Floating draggable In-App Picture-in-Picture mini-map overlay so operators
 * can keep live radar, route, and airspace awareness visible across all app tabs.
 */
@Composable
fun InAppPipMiniMapOverlay(
    uiState: ForensicUiState,
    onExpandFullScreen: () -> Unit,
    onClosePip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    val density = LocalDensity.current
    val pipWidth = 188.dp
    val pipHeight = 142.dp
    var dragOffset by remember { mutableStateOf(Offset(16f, 90f)) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxX = with(density) { (maxWidth - pipWidth).toPx().coerceAtLeast(0f) }
        val maxY = with(density) { (maxHeight - pipHeight).toPx().coerceAtLeast(0f) }
        val currentMaxX by rememberUpdatedState(maxX)
        val currentMaxY by rememberUpdatedState(maxY)

        LaunchedEffect(maxX, maxY) {
            dragOffset = Offset(
                dragOffset.x.coerceIn(0f, maxX),
                dragOffset.y.coerceIn(0f, maxY)
            )
        }

        Box(
            modifier = modifier
                .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                .width(pipWidth)
                .height(pipHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.canvasBackground)
                .border(2.dp, palette.advisoryBlue, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        dragOffset = Offset(
                            (dragOffset.x + dragAmount.x).coerceIn(0f, currentMaxX),
                            (dragOffset.y + dragAmount.y).coerceIn(0f, currentMaxY)
                        )
                    }
                }
                .testTag("pip_mini_map_overlay")
        ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minLat = HawaiiAirspaceReference.MAP_MIN_LAT
            val maxLat = HawaiiAirspaceReference.MAP_MAX_LAT
            val minLon = HawaiiAirspaceReference.MAP_MIN_LON
            val maxLon = HawaiiAirspaceReference.MAP_MAX_LON

            fun proj(pt: GeoPoint): Offset {
                val x = (((pt.lon - minLon) / (maxLon - minLon)).toFloat()).coerceIn(0f, 1f) * w
                val y = ((1.0 - (pt.lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f) * h
                return Offset(x, y)
            }

            val islandPath = Path().apply {
                HawaiiAirspaceReference.hawaiiIslandCoastline.forEachIndexed { i, p ->
                    val o = proj(p)
                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                }
                close()
            }
            drawPath(islandPath, color = palette.landFill)
            drawPath(islandPath, color = palette.landOutline, style = Stroke(2f))

            // Route line across waypoints
            if (uiState.userWaypoints.size >= 2) {
                val routePath = Path().apply {
                    uiState.userWaypoints.forEachIndexed { idx, wp ->
                        val o = proj(GeoPoint(wp.lat, wp.lon))
                        if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                    }
                }
                drawPath(
                    routePath,
                    color = palette.routeLineColor,
                    style = Stroke(2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)))
                )
            }

            // GPS / primary marker
            val gpsPt = proj(GeoPoint(uiState.gpsTelemetry.latitude, uiState.gpsTelemetry.longitude))
            drawCircle(palette.normalGreen, radius = 5f, center = gpsPt)
        }

        // Top mini header with Expand + Close buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.panelSurface.copy(alpha = 0.88f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "PiP MAP • ${String.format(Locale.US, "%.1fx", uiState.airspaceMapZoom)}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = palette.advisoryBlue,
                fontFamily = JetBrainsMonoFontFamily
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Expand PiP to Full Screen",
                    tint = palette.normalGreen,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onExpandFullScreen() }
                        .testTag("pip_expand_btn")
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close PiP Mini Map",
                    tint = palette.criticalRed,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onClosePip() }
                        .testTag("pip_close_btn")
                )
            }
        }
        }
    }
}
