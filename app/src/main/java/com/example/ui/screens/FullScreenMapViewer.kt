package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint
import android.graphics.Typeface
import android.view.Window
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.model.*
import com.example.network.*
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.FullScreenMapDomain
import com.example.ui.components.AirspaceClassificationFilterPanel
import com.example.ui.components.MapLayerPresetRow
import com.example.ui.components.MapVisualThemeSelectorRow
import com.example.ui.components.colorForAirspaceClass
import com.example.ui.components.detectMapMultitouchGestures
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private tailrec fun Context.findActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.findActivityWindow()
    else -> null
}

@Composable
fun FullScreenMapViewer(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    onRequestSystemPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val palette = LocalMapPalette.current
    val density = LocalDensity.current

    var isDrawerOpen by remember { mutableStateOf(false) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isTiltModeGesture by remember { mutableStateOf(false) }

    val animatedRotation by animateFloatAsState(
        targetValue = uiState.mapRotationDeg,
        animationSpec = spring(stiffness = 380f),
        label = "map_rotation"
    )
    val animatedTilt by animateFloatAsState(
        targetValue = uiState.mapTiltDeg,
        animationSpec = spring(stiffness = 380f),
        label = "map_tilt"
    )

    // Hide system navigation bar while keeping transparent edge-to-edge status bar so
    // Android's OS-level "Viewing full screen / Got it" cling never covers the HUD or traps input.
    DisposableEffect(view, context, palette.mode) {
        val window = context.findActivityWindow()
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        runCatching {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.isAppearanceLightStatusBars = (palette.mode == MapVisualThemeMode.DAY)
            controller?.hide(WindowInsetsCompat.Type.navigationBars())
        }
        onDispose {
            runCatching {
                controller?.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler {
        when {
            isDrawerOpen -> isDrawerOpen = false
            uiState.selectedAirspaceZone != null -> viewModel.selectAirspaceZone(null)
            else -> viewModel.closeFullScreenMap()
        }
    }

    val isSatCenterMode = uiState.fullScreenMapDomain == FullScreenMapDomain.SATELLITE &&
        uiState.mapSplitMode == MapSplitMode.SINGLE

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .testTag("fullscreen_map_viewer")
    ) {
        val isWideLayout = maxWidth >= 600.dp
        val showHawaiiVectors = !isSatCenterMode ||
            AspectRatioCalibrationMath.isWithinHawaiiBounds(
                uiState.customGibsTarget.latitude,
                uiState.customGibsTarget.longitude
            )

        // 1. Primary / Split-Screen Immersive Map Canvas Area (100% display coverage)
        if (uiState.mapSplitMode == MapSplitMode.SINGLE) {
            InteractiveMapViewportPane(
                paneTitle = if (isSatCenterMode) "SATELLITE TARGET • ${uiState.activeGibsLayer.shortLabel}" else "UNIFIED TACTICAL AIRSPACE & RADAR",
                uiState = uiState,
                viewModel = viewModel,
                showSatellite = if (isSatCenterMode) true else uiState.airspaceSatEnabled,
                showDoppler = if (isSatCenterMode) false else uiState.dopplerEnabled,
                showAirspace = !isSatCenterMode,
                showTerrain = uiState.terrainLayerEnabled,
                showRoutes = showHawaiiVectors,
                showAdsb = uiState.adsbEnabled && showHawaiiVectors,
                satUrlOverride = if (isSatCenterMode) {
                    NasaGibsUrlBuilder.buildSnapshotUrl(
                        latitude = uiState.customGibsTarget.latitude,
                        longitude = uiState.customGibsTarget.longitude,
                        dateIso = uiState.customGibsTarget.dateIso,
                        layer = uiState.activeGibsLayer,
                        spanDegrees = uiState.gibsSpanDegrees,
                        width = if (uiState.hdTileResolution) 1600 else 960,
                        height = if (uiState.hdTileResolution) 1200 else 620,
                        cacheBustNonce = uiState.gibsRefreshNonce
                    )
                } else null,
                zoom = if (isSatCenterMode) uiState.satVisualZoom else uiState.airspaceMapZoom,
                rotationDeg = animatedRotation,
                tiltDeg = animatedTilt,
                panOffset = panOffset,
                tiltGestureMode = isTiltModeGesture,
                cameraDistPx = 14f * density.density,
                bottomHudClearanceDp = 76.dp,
                onPanChange = { panOffset = it },
                onTransform = { newZoom, deltaRot, deltaTilt, latency ->
                    if (isSatCenterMode) {
                        viewModel.setSatVisualZoom(newZoom)
                    } else {
                        viewModel.updateMapTransform(
                            zoom = newZoom,
                            rotationDeg = uiState.mapRotationDeg + deltaRot,
                            tiltDeg = uiState.mapTiltDeg + deltaTilt,
                            gestureLatencyMs = latency
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Synchronized Split-Screen Multi-Map Layout (Side-by-side on tablet/landscape, stacked on portrait)
            val splitMode = uiState.mapSplitMode
            val containerModifier = Modifier
                .fillMaxSize()
                .testTag("split_map_container_${splitMode.id}")

            val leftContent: @Composable (Modifier) -> Unit = { mod ->
                InteractiveMapViewportPane(
                    paneTitle = splitMode.leftPaneTitle,
                    uiState = uiState,
                    viewModel = viewModel,
                    showSatellite = splitMode == MapSplitMode.SAT_AIRSPACE || splitMode == MapSplitMode.WEATHER_ROUTES,
                    showDoppler = splitMode == MapSplitMode.WEATHER_ROUTES,
                    showAirspace = false,
                    showTerrain = splitMode == MapSplitMode.TERRAIN_NAV,
                    showRoutes = false,
                    showAdsb = false,
                    satUrlOverride = null,
                    zoom = uiState.airspaceMapZoom,
                    rotationDeg = animatedRotation,
                    tiltDeg = animatedTilt,
                    panOffset = panOffset,
                    tiltGestureMode = isTiltModeGesture,
                    cameraDistPx = 14f * density.density,
                    bottomHudClearanceDp = if (isWideLayout) 76.dp else 10.dp,
                    onPanChange = { panOffset = it },
                    onTransform = { newZoom, deltaRot, deltaTilt, latency ->
                        viewModel.updateMapTransform(
                            zoom = newZoom,
                            rotationDeg = uiState.mapRotationDeg + deltaRot,
                            tiltDeg = uiState.mapTiltDeg + deltaTilt,
                            gestureLatencyMs = latency
                        )
                    },
                    modifier = mod.testTag("split_pane_left")
                )
            }

            val rightContent: @Composable (Modifier) -> Unit = { mod ->
                InteractiveMapViewportPane(
                    paneTitle = splitMode.rightPaneTitle,
                    uiState = uiState,
                    viewModel = viewModel,
                    showSatellite = false,
                    showDoppler = splitMode == MapSplitMode.SAT_AIRSPACE && uiState.dopplerEnabled,
                    showAirspace = splitMode == MapSplitMode.SAT_AIRSPACE || splitMode == MapSplitMode.WEATHER_ROUTES,
                    showTerrain = false,
                    showRoutes = splitMode == MapSplitMode.TERRAIN_NAV || splitMode == MapSplitMode.WEATHER_ROUTES,
                    showAdsb = splitMode == MapSplitMode.WEATHER_ROUTES && uiState.adsbEnabled,
                    satUrlOverride = null,
                    zoom = uiState.airspaceMapZoom,
                    rotationDeg = animatedRotation,
                    tiltDeg = animatedTilt,
                    panOffset = panOffset,
                    tiltGestureMode = isTiltModeGesture,
                    cameraDistPx = 14f * density.density,
                    bottomHudClearanceDp = 76.dp,
                    onPanChange = { panOffset = it },
                    onTransform = { newZoom, deltaRot, deltaTilt, latency ->
                        viewModel.updateMapTransform(
                            zoom = newZoom,
                            rotationDeg = uiState.mapRotationDeg + deltaRot,
                            tiltDeg = uiState.mapTiltDeg + deltaTilt,
                            gestureLatencyMs = latency
                        )
                    },
                    modifier = mod.testTag("split_pane_right")
                )
            }

            if (isWideLayout) {
                Row(modifier = containerModifier) {
                    leftContent(Modifier.weight(1f).fillMaxHeight())
                    Box(Modifier.width(2.dp).fillMaxHeight().background(palette.panelBorder))
                    rightContent(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                Column(modifier = containerModifier) {
                    leftContent(Modifier.weight(1f).fillMaxWidth())
                    Box(Modifier.height(2.dp).fillMaxWidth().background(palette.panelBorder))
                    rightContent(Modifier.weight(1f).fillMaxWidth())
                }
            }
        }

        // 2. Top Edge-to-Edge Safe Telemetry HUD & Exit/Split/Drawer Controls
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Left: Compass Rose + Zoom / Heading / Tilt / Latency Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.panelSurface.copy(alpha = 0.88f))
                    .border(1.5.dp, palette.panelBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("fullscreen_hud_telemetry")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(palette.canvasBackground)
                        .border(1.dp, palette.advisoryBlue, CircleShape)
                        .clickable {
                            viewModel.resetMapToNorthUp()
                            panOffset = Offset.Zero
                        }
                        .testTag("fullscreen_compass_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Compass North-Up Reset",
                        tint = palette.criticalRed,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(-animatedRotation)
                    )
                }
                Column {
                    val activeZoom = if (isSatCenterMode) uiState.satVisualZoom else uiState.airspaceMapZoom
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.1fx • HDG %03d° • TILT %02d°",
                            activeZoom,
                            uiState.mapRotationDeg.toInt(),
                            uiState.mapTiltDeg.toInt()
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Text(
                        text = "${uiState.satAspectCalibrationMode.shortLabel} ${String.format(Locale.US, "%.2fx", uiState.satZoomCalibrationFactor)} • ${uiState.mapPerfMetrics.lastGestureLatencyMs}ms • ${uiState.mapSplitMode.label}",
                        fontSize = 10.sp,
                        color = palette.advisoryBlue,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }

            // Right: Layer Drawer Toggle, System PiP, Exit Full-Screen
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FloatingHudIconButton(
                    icon = if (isDrawerOpen) Icons.Default.Close else Icons.Default.Layers,
                    contentDescription = "Toggle Layer Controls Drawer",
                    accent = palette.advisoryBlue,
                    onClick = { isDrawerOpen = !isDrawerOpen },
                    modifier = Modifier.testTag("fullscreen_drawer_btn")
                )
                FloatingHudIconButton(
                    icon = Icons.Default.PictureInPictureAlt,
                    contentDescription = "Enter Picture-in-Picture",
                    accent = palette.warningAmber,
                    onClick = onRequestSystemPip,
                    modifier = Modifier.testTag("fullscreen_system_pip_btn")
                )
                FloatingHudIconButton(
                    icon = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Full Screen",
                    accent = palette.criticalRed,
                    onClick = { viewModel.closeFullScreenMap() },
                    modifier = Modifier.testTag("fullscreen_exit_btn")
                )
            }
        }

        // 3. Right Floating Vertical Control Rail (Zoom, Tilt Gesture Toggle, North-Up, 3D Pitch +/-)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .safeDrawingPadding()
                .padding(end = 10.dp)
        ) {
            FloatingHudIconButton(
                icon = Icons.Default.Add,
                contentDescription = "Zoom In",
                accent = palette.advisoryBlue,
                onClick = {
                    if (isSatCenterMode) viewModel.setSatVisualZoom(uiState.satVisualZoom + 0.5f)
                    else viewModel.setAirspaceMapZoom(uiState.airspaceMapZoom + 0.5f)
                },
                modifier = Modifier.testTag("fullscreen_zoom_in_btn")
            )
            FloatingHudIconButton(
                icon = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                accent = palette.advisoryBlue,
                onClick = {
                    if (isSatCenterMode) viewModel.setSatVisualZoom(uiState.satVisualZoom - 0.5f)
                    else viewModel.setAirspaceMapZoom(uiState.airspaceMapZoom - 0.5f)
                },
                modifier = Modifier.testTag("fullscreen_zoom_out_btn")
            )
            FloatingHudIconButton(
                icon = Icons.Default.Explore,
                contentDescription = "Reset to North-Up",
                accent = palette.normalGreen,
                onClick = {
                    viewModel.resetMapToNorthUp()
                    panOffset = Offset.Zero
                },
                modifier = Modifier.testTag("fullscreen_north_up_btn")
            )
            FloatingHudIconButton(
                icon = Icons.Default.Terrain,
                contentDescription = "Toggle 2-Finger Tilt Mode",
                accent = if (isTiltModeGesture) palette.warningAmber else palette.textSecondary,
                filled = isTiltModeGesture,
                onClick = { isTiltModeGesture = !isTiltModeGesture },
                modifier = Modifier.testTag("fullscreen_tilt_toggle_btn")
            )
            FloatingHudIconButton(
                icon = Icons.Default.AspectRatio,
                contentDescription = "Cycle Fixed-Aspect Zoom Calibration",
                accent = palette.classAColor,
                filled = uiState.satAspectCalibrationMode != SatAspectCalibrationMode.FIT,
                onClick = {
                    val modes = SatAspectCalibrationMode.entries
                    val next = modes[(uiState.satAspectCalibrationMode.ordinal + 1) % modes.size]
                    viewModel.setSatAspectCalibrationMode(next)
                    panOffset = Offset.Zero
                },
                modifier = Modifier.testTag("fullscreen_calibration_btn")
            )
        }

        // 4. Bottom Configurable Floating Quick-Access Toolbar + Airspace Detail Card
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            uiState.selectedAirspaceZone?.let { zone ->
                val zColor = colorForAirspaceClass(zone.classification, palette)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.panelSurface.copy(alpha = 0.94f))
                        .border(1.5.dp, zColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("fullscreen_airspace_detail")
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${zone.classification.symbolGlyph} ${zone.name} (${zone.classification.code})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "${zone.altitudeDisplay} • ${zone.controllingAgency}",
                            fontSize = 10.sp,
                            color = zColor,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(
                            text = zone.operationalDetail,
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.selectAirspaceZone(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss detail", tint = palette.textPrimary)
                    }
                }
            }

            // Configurable Quick-Access Floating Toolbar with pinned always-visible Exit button
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.panelSurface.copy(alpha = 0.92f))
                    .border(1.5.dp, palette.panelBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("fullscreen_quick_toolbar")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.criticalRed)
                        .clickable { viewModel.closeFullScreenMap() }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("fullscreen_bottom_exit_btn")
                ) {
                    Text(
                        text = "Exit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contrastContentColor(palette.criticalRed),
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    uiState.quickToolbarTools.forEach { tool ->
                        val (isActive, accent) = when (tool) {
                            QuickToolbarTool.FULLSCREEN -> true to palette.normalGreen
                            QuickToolbarTool.NORTH_UP -> (uiState.mapRotationDeg == 0f && uiState.mapTiltDeg == 0f) to palette.normalGreen
                            QuickToolbarTool.SPLIT_VIEW -> (uiState.mapSplitMode != MapSplitMode.SINGLE) to palette.advisoryBlue
                            QuickToolbarTool.GPS_TRACK -> uiState.isGpsLiveEnabled to palette.normalGreen
                            QuickToolbarTool.ADSB_TRAFFIC -> uiState.adsbEnabled to palette.warningAmber
                            QuickToolbarTool.ADD_WAYPOINT -> false to palette.normalGreen
                            QuickToolbarTool.PIP_MODE -> uiState.isInAppMiniPipActive to palette.advisoryBlue
                            QuickToolbarTool.LEGEND -> uiState.showMapLegendSheet to palette.warningAmber
                            QuickToolbarTool.VISUAL_MODE -> true to palette.advisoryBlue
                        }
                        Box(
                            modifier = Modifier
                                .heightIn(min = 38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isActive) accent.copy(alpha = 0.22f) else palette.canvasBackground)
                                .border(1.dp, accent, RoundedCornerShape(8.dp))
                                .clickable {
                                    when (tool) {
                                        QuickToolbarTool.FULLSCREEN -> viewModel.closeFullScreenMap()
                                        QuickToolbarTool.NORTH_UP -> {
                                            viewModel.resetMapToNorthUp()
                                            panOffset = Offset.Zero
                                        }
                                        QuickToolbarTool.SPLIT_VIEW -> viewModel.cycleMapSplitMode()
                                        QuickToolbarTool.GPS_TRACK -> {
                                            if (uiState.isGpsLiveEnabled) viewModel.stopLiveGpsTracking()
                                            else viewModel.startLiveGpsTracking()
                                        }
                                        QuickToolbarTool.ADSB_TRAFFIC -> viewModel.toggleAdsbTraffic()
                                        QuickToolbarTool.ADD_WAYPOINT -> {
                                            viewModel.addUserWaypoint(
                                                label = "WP-${uiState.userWaypoints.size + 1}",
                                                lat = uiState.gpsTelemetry.latitude,
                                                lon = uiState.gpsTelemetry.longitude,
                                                altitudeFt = 2500
                                            )
                                        }
                                        QuickToolbarTool.PIP_MODE -> viewModel.toggleInAppMiniPip(true)
                                        QuickToolbarTool.LEGEND -> viewModel.toggleMapLegendSheet(true)
                                        QuickToolbarTool.VISUAL_MODE -> viewModel.cycleMapVisualThemeMode()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("quick_tool_${tool.id}")
                        ) {
                            Text(
                                text = when (tool) {
                                    QuickToolbarTool.VISUAL_MODE -> uiState.mapVisualThemeMode.shortLabel
                                    QuickToolbarTool.FULLSCREEN -> "Exit Full"
                                    else -> tool.shortLabel
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { viewModel.toggleQuickToolbarConfig(true) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("fullscreen_config_toolbar_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure quick toolbar",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 5. Collapsible Side Control Drawer (Layers, Split Mode, Tilt Slider, Presets, Airspace Filter, Visual Mode)
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .safeDrawingPadding()
                .padding(top = 58.dp, bottom = 70.dp, end = 8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = palette.panelSurface.copy(alpha = 0.96f)),
                border = BorderStroke(1.5.dp, palette.panelBorder),
                modifier = Modifier
                    .widthIn(max = if (isWideLayout) 390.dp else 320.dp)
                    .fillMaxHeight()
                    .testTag("fullscreen_control_drawer")
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "SPLIT-SCREEN & LAYER WORKSPACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.advisoryBlue,
                        fontFamily = JetBrainsMonoFontFamily
                    )

                    // Split-Screen View Picker
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        MapSplitMode.entries.forEach { mode ->
                            val sel = uiState.mapSplitMode == mode
                            FilterChip(
                                selected = sel,
                                onClick = { viewModel.setMapSplitMode(mode) },
                                label = { Text(mode.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("drawer_split_${mode.id}")
                            )
                        }
                    }

                    // Fixed-Aspect-Ratio Satellite & Map Zoom Calibration (Zero Stretch)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ASPECT-LOCKED ZOOM CALIBRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.classAColor,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        TextButton(
                            onClick = {
                                viewModel.resetSatCalibration()
                                panOffset = Offset.Zero
                            },
                            modifier = Modifier.testTag("calibration_reset_btn")
                        ) {
                            Text("Reset", fontSize = 11.sp)
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        SatAspectCalibrationMode.entries.forEach { calMode ->
                            FilterChip(
                                selected = uiState.satAspectCalibrationMode == calMode,
                                onClick = {
                                    viewModel.setSatAspectCalibrationMode(calMode)
                                    panOffset = Offset.Zero
                                },
                                label = { Text(calMode.shortLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("calibration_mode_${calMode.id}")
                            )
                        }
                    }
                    Text(
                        text = "OPTICAL CALIBRATION SCALE (${String.format(Locale.US, "%.2fx", uiState.satZoomCalibrationFactor)} • 1:1 LOCKED)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Slider(
                        value = uiState.satZoomCalibrationFactor,
                        onValueChange = { viewModel.setSatZoomCalibrationFactor(it) },
                        valueRange = 0.75f..2.50f,
                        modifier = Modifier.testTag("calibration_factor_slider")
                    )

                    // 3D Terrain Tilt & Rotation Sliders (complements multi-touch gestures)
                    Text(
                        text = "3D PITCH TILT (${uiState.mapTiltDeg.toInt()}°) & BEARING (${uiState.mapRotationDeg.toInt()}°)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.warningAmber,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Slider(
                        value = uiState.mapTiltDeg,
                        onValueChange = { viewModel.updateMapTransform(tiltDeg = it) },
                        valueRange = 0f..55f,
                        modifier = Modifier.testTag("drawer_tilt_slider")
                    )

                    MapVisualThemeSelectorRow(
                        selectedMode = uiState.mapVisualThemeMode,
                        onSelectMode = { viewModel.setMapVisualThemeMode(it) }
                    )

                    MapLayerPresetRow(
                        activePreset = uiState.activeLayerPreset,
                        onSelectPreset = { viewModel.applyMapLayerPreset(it) }
                    )

                    // Fast Layer Toggles (zero reload)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = uiState.airspaceSatEnabled,
                            onClick = { viewModel.toggleAirspaceSatEnabled() },
                            label = { Text("Satellite", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.dopplerEnabled,
                            onClick = { viewModel.toggleDopplerEnabled(!uiState.dopplerEnabled) },
                            label = { Text("Radar", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = uiState.terrainLayerEnabled,
                            onClick = { viewModel.toggleTerrainLayer() },
                            label = { Text("Terrain", fontSize = 11.sp) }
                        )
                    }

                    AirspaceClassificationFilterPanel(
                        enabledClasses = uiState.enabledAirspaceClasses,
                        altitudeBand = uiState.airspaceAltitudeBand,
                        airspaceOpacity = uiState.airspaceOpacity,
                        selectedZone = uiState.selectedAirspaceZone,
                        onToggleClass = { viewModel.toggleAirspaceClassification(it) },
                        onSelectAltitudeBand = { viewModel.setAirspaceAltitudeBand(it) },
                        onChangeOpacity = { viewModel.setAirspaceOpacity(it) },
                        onDismissZone = { viewModel.selectAirspaceZone(null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveMapViewportPane(
    paneTitle: String,
    uiState: ForensicUiState,
    viewModel: ForensicViewModel,
    showSatellite: Boolean,
    showDoppler: Boolean,
    showAirspace: Boolean,
    showTerrain: Boolean,
    showRoutes: Boolean,
    showAdsb: Boolean,
    satUrlOverride: String?,
    zoom: Float,
    rotationDeg: Float,
    tiltDeg: Float,
    panOffset: Offset,
    tiltGestureMode: Boolean,
    cameraDistPx: Float,
    bottomHudClearanceDp: Dp = 74.dp,
    onPanChange: (Offset) -> Unit,
    onTransform: (newZoom: Float, deltaRot: Float, deltaTilt: Float, latencyMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalMapPalette.current
    val density = LocalDensity.current
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    val minLat = HawaiiAirspaceReference.MAP_MIN_LAT
    val maxLat = HawaiiAirspaceReference.MAP_MAX_LAT
    val minLon = HawaiiAirspaceReference.MAP_MIN_LON
    val maxLon = HawaiiAirspaceReference.MAP_MAX_LON

    val currentZoom by rememberUpdatedState(zoom)
    val currentPan by rememberUpdatedState(panOffset)
    val currentTiltMode by rememberUpdatedState(tiltGestureMode)
    val currentEnabledClasses by rememberUpdatedState(uiState.enabledAirspaceClasses)
    val currentAltBand by rememberUpdatedState(uiState.airspaceAltitudeBand)
    val currentRotDeg by rememberUpdatedState(rotationDeg)

    val tileW = if (uiState.hdTileResolution) 1600 else 800
    val tileH = if (uiState.hdTileResolution) 1200 else 600
    val targetStageAspect = tileW.toFloat() / tileH.toFloat()
    val viewportW = viewportSize.width.toFloat().coerceAtLeast(1f)
    val viewportH = viewportSize.height.toFloat().coerceAtLeast(1f)
    val (stageW, stageH) = if (viewportSize.width > 0 && viewportSize.height > 0) {
        AspectRatioCalibrationMath.computeFixedAspectStagePx(viewportW, viewportH, targetStageAspect)
    } else {
        Pair(viewportW, viewportH)
    }
    val calibrationScale = AspectRatioCalibrationMath.computeUniformCalibrationScale(
        viewportWidthPx = viewportW,
        viewportHeightPx = viewportH,
        stageWidthPx = stageW,
        stageHeightPx = stageH,
        mode = uiState.satAspectCalibrationMode,
        calibrationMultiplier = uiState.satZoomCalibrationFactor
    )
    val effectiveScale = (zoom * calibrationScale).coerceIn(0.75f, 10.0f)
    val currentEffectiveScale by rememberUpdatedState(effectiveScale)
    val currentStageW by rememberUpdatedState(stageW)
    val currentStageH by rememberUpdatedState(stageH)
    val currentCalibrationScale by rememberUpdatedState(calibrationScale)

    val isHawaiiTarget = satUrlOverride == null ||
        AspectRatioCalibrationMath.isWithinHawaiiBounds(
            uiState.customGibsTarget.latitude,
            uiState.customGibsTarget.longitude
        )

    val activeSatDate = if (satUrlOverride != null) uiState.customGibsTarget.dateIso else uiState.airspaceSatDateIso
    val activeSatLayer = if (satUrlOverride != null) uiState.activeGibsLayer else uiState.airspaceSatLayer

    var satStatus by remember(showSatellite, satUrlOverride, uiState.airspaceSatLayer, uiState.airspaceSatDateIso, uiState.airspaceSatRefreshNonce, uiState.gibsRefreshNonce) {
        mutableStateOf("SYNC")
    }
    var dopplerStatus by remember(showDoppler, uiState.activeDopplerLayer, uiState.dopplerRefreshNonce) {
        mutableStateOf("SYNC")
    }
    var satAutoStepCount by remember(satUrlOverride != null, activeSatLayer.id) { mutableStateOf(0) }

    LaunchedEffect(effectiveScale, viewportSize, stageW, stageH) {
        val maxPanX = ((stageW * effectiveScale - viewportW) / 2f).coerceAtLeast(0f)
        val maxPanY = ((stageH * effectiveScale - viewportH) / 2f).coerceAtLeast(0f)
        if (maxPanX <= 1f && maxPanY <= 1f) {
            onPanChange(Offset.Zero)
        } else {
            onPanChange(
                Offset(
                    x = panOffset.x.coerceIn(-maxPanX, maxPanX),
                    y = panOffset.y.coerceIn(-maxPanY, maxPanY)
                )
            )
        }
    }

    val satUrl = satUrlOverride ?: remember(uiState.airspaceSatLayer, uiState.airspaceSatDateIso, uiState.airspaceSatRefreshNonce, tileW) {
        NasaGibsUrlBuilder.buildBboxSnapshotUrl(
            south = minLat,
            west = minLon,
            north = maxLat,
            east = maxLon,
            dateIso = uiState.airspaceSatDateIso,
            layer = uiState.airspaceSatLayer,
            width = tileW,
            height = tileH,
            cacheBustNonce = uiState.airspaceSatRefreshNonce
        )
    }

    val dopplerUrl = remember(uiState.activeDopplerLayer, uiState.dopplerRefreshNonce, tileW) {
        HawaiiDopplerUrlBuilder.buildWmsUrl(
            layer = uiState.activeDopplerLayer,
            width = tileW,
            height = tileH,
            cacheBustNonce = uiState.dopplerRefreshNonce
        )
    }

    val labelPaint = remember(palette.textPrimary) {
        Paint().apply {
            isAntiAlias = true
            color = palette.textPrimary.toArgb()
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    fun project(pt: GeoPoint, w: Float, h: Float): Offset {
        val x = (((pt.lon - minLon) / (maxLon - minLon)).toFloat()).coerceIn(0f, 1f) * w
        val y = ((1.0 - (pt.lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f) * h
        return Offset(x, y)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(palette.canvasBackground)
            .clip(RoundedCornerShape(0.dp))
            .onSizeChanged { viewportSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        val nextZoom = if (currentZoom > 1.3f) 1.0f else 2.5f
                        if (nextZoom == 1.0f) onPanChange(Offset.Zero)
                        onTransform(nextZoom, 0f, 0f, 10L)
                    },
                    onTap = { tap ->
                        if (!showAirspace) return@detectTapGestures
                        val vw = size.width.toFloat().coerceAtLeast(1f)
                        val vh = size.height.toFloat().coerceAtLeast(1f)
                        val sw = currentStageW.coerceAtLeast(1f)
                        val sh = currentStageH.coerceAtLeast(1f)
                        val cx = vw / 2f
                        val cy = vh / 2f
                        val dx = (tap.x - currentPan.x - cx) / currentEffectiveScale
                        val dy = (tap.y - currentPan.y - cy) / currentEffectiveScale
                        val rad = Math.toRadians(-currentRotDeg.toDouble())
                        val unrotX = (dx * cos(rad) - dy * sin(rad)).toFloat() + (sw / 2f)
                        val unrotY = (dx * sin(rad) + dy * cos(rad)).toFloat() + (sh / 2f)

                        val visibleZones = HawaiiAirspaceClassificationsCatalog.staticZones.filter { z ->
                            currentEnabledClasses.contains(z.classification) &&
                                currentAltBand.matchesAltitudeRange(z.floorFtMsl, z.ceilingFtMsl)
                        }
                        val matchedZone = visibleZones.minByOrNull { z ->
                            val center = project(GeoPoint(z.centerLat, z.centerLon), sw, sh)
                            hypot(unrotX - center.x, unrotY - center.y)
                        }?.let { z ->
                            val center = project(GeoPoint(z.centerLat, z.centerLon), sw, sh)
                            val distPx = hypot(unrotX - center.x, unrotY - center.y)
                            if (distPx <= 140f / currentEffectiveScale) z else null
                        }
                        viewModel.selectAirspaceZone(matchedZone)
                    }
                )
            }
            .pointerInput(Unit) {
                detectMapMultitouchGestures(
                    enableTilt = true,
                    isSingleFingerTiltEnabled = { currentTiltMode },
                    allowSingleFingerPan = { true }
                ) { zoomFactor, pan, rotationChange, tiltDelta ->
                    val t0 = System.currentTimeMillis()
                    val nextZoom = (currentZoom * zoomFactor).coerceIn(1.0f, 8.0f)
                    val nextEffective = (nextZoom * currentCalibrationScale).coerceIn(0.75f, 10.0f)
                    val vw = viewportSize.width.toFloat().coerceAtLeast(1f)
                    val vh = viewportSize.height.toFloat().coerceAtLeast(1f)
                    val maxPanX = ((currentStageW * nextEffective - vw) / 2f).coerceAtLeast(0f)
                    val maxPanY = ((currentStageH * nextEffective - vh) / 2f).coerceAtLeast(0f)
                    if (maxPanX <= 1f && maxPanY <= 1f) {
                        onPanChange(Offset.Zero)
                    } else if (pan != Offset.Zero) {
                        onPanChange(
                            Offset(
                                x = (currentPan.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                y = (currentPan.y + pan.y).coerceIn(-maxPanY, maxPanY)
                            )
                        )
                    }
                    onTransform(
                        nextZoom,
                        rotationChange,
                        tiltDelta,
                        (System.currentTimeMillis() - t0).coerceAtLeast(4L)
                    )
                }
            }
    ) {
        val stageModifier = if (viewportSize.width > 0 && viewportSize.height > 0) {
            Modifier.size(
                width = with(density) { stageW.toDp() },
                height = with(density) { stageH.toDp() }
            )
        } else {
            Modifier.fillMaxSize()
        }

        Box(
            modifier = stageModifier
                .graphicsLayer {
                    scaleX = effectiveScale
                    scaleY = effectiveScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                    rotationZ = rotationDeg
                    rotationX = tiltDeg
                    cameraDistance = cameraDistPx
                }
                .testTag("fullscreen_aspect_stage")
        ) {
            if (showSatellite) {
                SubcomposeAsyncImage(
                    model = buildFeedImageRequest(
                        context = context,
                        url = satUrl,
                        referer = "https://worldview.earthdata.nasa.gov/"
                    ),
                    contentDescription = "Full-screen NASA GIBS Satellite Layer",
                    contentScale = ContentScale.Fit,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Loading -> satStatus = "SYNC"
                            is AsyncImagePainter.State.Success -> satStatus = "LIVE"
                            is AsyncImagePainter.State.Error -> {
                                satStatus = "ERR"
                                if (satAutoStepCount < 2 && NasaGibsUrlBuilder.isRecentCompositeWindow(activeSatDate)) {
                                    satAutoStepCount += 1
                                    if (satUrlOverride != null) viewModel.shiftGibsDateDays(-1)
                                    else viewModel.shiftAirspaceSatDateDays(-1)
                                }
                            }
                            AsyncImagePainter.State.Empty -> Unit
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(uiState.airspaceSatOpacity),
                    content = {
                        if (painter.state is AsyncImagePainter.State.Success) {
                            SubcomposeAsyncImageContent()
                        }
                    }
                )
            }

            if (showDoppler) {
                SubcomposeAsyncImage(
                    model = buildFeedImageRequest(
                        context = context,
                        url = dopplerUrl,
                        referer = "https://opengeo.ncep.noaa.gov/"
                    ),
                    contentDescription = "Full-screen NOAA NEXRAD Weather Layer",
                    contentScale = ContentScale.Fit,
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Loading -> dopplerStatus = "SYNC"
                            is AsyncImagePainter.State.Success -> dopplerStatus = "LIVE"
                            is AsyncImagePainter.State.Error -> dopplerStatus = "ERR"
                            AsyncImagePainter.State.Empty -> Unit
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(uiState.dopplerOpacity),
                    content = {
                        if (painter.state is AsyncImagePainter.State.Success) {
                            SubcomposeAsyncImageContent()
                        }
                    }
                )
            }

            // Hardware-accelerated Vector Coastline, Terrain Contours, Airspace Classes A/B/C/D/R/TFR, Routes & ADS-B
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val sm = palette.strokeMultiplier

                // Terrain Elevation Contours & Lat/Lon Mesh
                if (showTerrain) {
                    for (i in 1..7) {
                        val gx = w * (i / 8f)
                        val gy = h * (i / 8f)
                        drawLine(palette.terrainContour.copy(alpha = 0.4f), Offset(gx, 0f), Offset(gx, h), 1f)
                        drawLine(palette.terrainContour.copy(alpha = 0.4f), Offset(0f, gy), Offset(w, gy), 1f)
                    }
                    if (isHawaiiTarget) {
                        // Concentric elevation contours around Mauna Kea & Mauna Loa peaks
                        listOf(
                            GeoPoint(19.8207, -155.4681) to listOf(18f, 34f, 52f),
                            GeoPoint(19.4795, -155.6025) to listOf(20f, 38f, 58f)
                        ).forEach { (summit, radii) ->
                            val c = project(summit, w, h)
                            radii.forEach { r ->
                                drawCircle(
                                    color = palette.terrainContour.copy(alpha = uiState.terrainOpacity),
                                    radius = r,
                                    center = c,
                                    style = Stroke(1.6f * sm, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
                                )
                            }
                        }
                    }
                }

                if (!isHawaiiTarget) {
                    // Global EONET/GIBS target reticle + framing border (suppresses unrelated Hawaiʻi coastline/routes)
                    val cx = w / 2f
                    val cy = h / 2f
                    drawCircle(
                        color = palette.advisoryBlue.copy(alpha = 0.85f),
                        radius = 28f,
                        center = Offset(cx, cy),
                        style = Stroke(2.0f * sm, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                    )
                    drawLine(palette.advisoryBlue.copy(alpha = 0.8f), Offset(cx - 42f, cy), Offset(cx + 42f, cy), 1.6f * sm)
                    drawLine(palette.advisoryBlue.copy(alpha = 0.8f), Offset(cx, cy - 42f), Offset(cx, cy + 42f), 1.6f * sm)
                } else {
                    // Island landmass & outline
                    val islandPath = Path().apply {
                        HawaiiAirspaceReference.hawaiiIslandCoastline.forEachIndexed { idx, pt ->
                            val o = project(pt, w, h)
                            if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                        }
                        close()
                    }
                    if (!(showSatellite && satStatus == "LIVE")) {
                        drawPath(islandPath, color = palette.landFill)
                    }
                    drawPath(islandPath, color = palette.landOutline, style = Stroke(width = 2.8f * sm))
                }

                // Airspace Classification Overlays (Class A / B / C / D / Restricted / TFR) filtered by Altitude Band
                if (showAirspace && isHawaiiTarget) {
                    val zones = HawaiiAirspaceClassificationsCatalog.staticZones.filter { z ->
                        uiState.enabledAirspaceClasses.contains(z.classification) &&
                            uiState.airspaceAltitudeBand.matchesAltitudeRange(z.floorFtMsl, z.ceilingFtMsl)
                    }
                    zones.forEach { zone ->
                        val zColor = colorForAirspaceClass(zone.classification, palette)
                        val alphaFill = (0.18f * uiState.airspaceOpacity).coerceIn(0.05f, 0.45f)
                        val dashPattern = when (zone.classification) {
                            AirspaceClassification.CLASS_A -> floatArrayOf(14f, 6f)
                            AirspaceClassification.CLASS_B -> null
                            AirspaceClassification.CLASS_C -> floatArrayOf(10f, 5f, 3f, 5f)
                            AirspaceClassification.CLASS_D -> floatArrayOf(6f, 6f)
                            AirspaceClassification.RESTRICTED -> floatArrayOf(12f, 4f)
                            AirspaceClassification.TFR -> null
                        }
                        if (zone.polygon.isNotEmpty()) {
                            val polyPath = Path().apply {
                                zone.polygon.forEachIndexed { i, pt ->
                                    val o = project(pt, w, h)
                                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                }
                                close()
                            }
                            drawPath(polyPath, color = zColor.copy(alpha = alphaFill))
                            drawPath(
                                polyPath,
                                color = zColor.copy(alpha = uiState.airspaceOpacity),
                                style = Stroke(
                                    width = 2.8f * sm,
                                    pathEffect = dashPattern?.let { PathEffect.dashPathEffect(it) }
                                )
                            )
                        } else {
                            val c = project(GeoPoint(zone.centerLat, zone.centerLon), w, h)
                            val degLat = zone.radiusNm / 60.0
                            val edge = project(GeoPoint(zone.centerLat + degLat, zone.centerLon), w, h)
                            val rPx = (c.y - edge.y).coerceAtLeast(14f)
                            drawCircle(color = zColor.copy(alpha = alphaFill), radius = rPx, center = c)
                            drawCircle(
                                color = zColor.copy(alpha = uiState.airspaceOpacity),
                                radius = rPx,
                                center = c,
                                style = Stroke(
                                    width = 2.4f * sm,
                                    pathEffect = dashPattern?.let { PathEffect.dashPathEffect(it) }
                                )
                            )
                        }
                    }

                    // Live FAA WFS TFR polygons
                    if (uiState.enabledAirspaceClasses.contains(AirspaceClassification.TFR)) {
                        uiState.airspaceSnapshot.tfrPolygons.forEach { tfrPoly ->
                            tfrPoly.rings.forEach { ring ->
                                val tfrPath = Path().apply {
                                    ring.forEachIndexed { idx, pt ->
                                        val o = project(pt, w, h)
                                        if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                    }
                                    close()
                                }
                                drawPath(tfrPath, color = palette.criticalRed.copy(alpha = 0.32f * uiState.airspaceOpacity))
                                drawPath(tfrPath, color = palette.criticalRed, style = Stroke(3.0f * sm))
                            }
                        }
                    }
                }

                // Dynamic Flight / Navigation Routes & User Waypoints
                if (showRoutes && uiState.userWaypoints.isNotEmpty()) {
                    val routePoints = listOf(GeoPoint(uiState.gpsTelemetry.latitude, uiState.gpsTelemetry.longitude)) +
                        uiState.userWaypoints.map { GeoPoint(it.lat, it.lon) }
                    val routePath = Path().apply {
                        routePoints.forEachIndexed { idx, pt ->
                            val o = project(pt, w, h)
                            if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                        }
                    }
                    drawPath(
                        routePath,
                        color = palette.routeLineColor,
                        style = Stroke(width = 3.2f * sm, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 7f)))
                    )
                    uiState.userWaypoints.forEach { wp ->
                        val p = project(GeoPoint(wp.lat, wp.lon), w, h)
                        drawCircle(palette.routeLineColor, radius = 8f, center = p)
                        drawCircle(palette.canvasBackground, radius = 4f, center = p)
                        drawContext.canvas.nativeCanvas.drawText(
                            wp.label,
                            (p.x + 12f).coerceAtMost(w - 70f),
                            (p.y - 8f).coerceAtLeast(22f),
                            labelPaint
                        )
                    }
                }

                // Live GPS Position Marker (only in Hawaiʻi geospatial view)
                if (isHawaiiTarget) {
                    val gpsPos = project(GeoPoint(uiState.gpsTelemetry.latitude, uiState.gpsTelemetry.longitude), w, h)
                    drawCircle(palette.normalGreen.copy(alpha = 0.25f), radius = 18f, center = gpsPos)
                    drawCircle(palette.normalGreen, radius = 7f, center = gpsPos)
                    drawCircle(palette.textPrimary, radius = 7f, center = gpsPos, style = Stroke(2f))
                }

                // Live ADS-B Aircraft Vectors
                if (showAdsb) {
                    uiState.adsbAircraft.forEach { ac ->
                        val p = project(GeoPoint(ac.latitude, ac.longitude), w, h)
                        drawCircle(palette.adsbTargetColor, radius = 6f, center = p)
                        drawContext.canvas.nativeCanvas.drawText(
                            "${ac.callsign} ${ac.altitudeFt / 100}",
                            (p.x + 10f).coerceAtMost(w - 90f),
                            (p.y + 5f).coerceIn(24f, h - 10f),
                            labelPaint
                        )
                    }
                }
            }
        }

        // Split Pane Label Chip + Live Feed Status / Retry Badges
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = bottomHudClearanceDp)
        ) {
            if (showSatellite || showDoppler) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (showSatellite) {
                        val (sColor, sText) = when (satStatus) {
                            "LIVE" -> palette.normalGreen to "SAT LIVE"
                            "ERR" -> palette.criticalRed to "SAT ERR • RETRY"
                            else -> palette.advisoryBlue to "SAT SYNC"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.canvasBackground.copy(alpha = 0.88f))
                                .border(1.dp, sColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    if (satUrlOverride != null) viewModel.reloadGibsTile()
                                    else viewModel.reloadAirspaceSatOverlay()
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("fullscreen_sat_status_pill")
                        ) {
                            Text(
                                text = sText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = sColor,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                    if (showDoppler) {
                        val (dColor, dText) = when (dopplerStatus) {
                            "LIVE" -> palette.classAColor to "NOAA LIVE"
                            "ERR" -> palette.criticalRed to "NOAA ERR • RETRY"
                            else -> palette.warningAmber to "NOAA SYNC"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.canvasBackground.copy(alpha = 0.88f))
                                .border(1.dp, dColor, RoundedCornerShape(6.dp))
                                .clickable { viewModel.reloadDopplerOverlay() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("fullscreen_doppler_status_pill")
                        ) {
                            Text(
                                text = dText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dColor,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.panelSurface.copy(alpha = 0.85f))
                    .border(1.dp, palette.panelBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = paneTitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
        }
    }
}

@Composable
private fun FloatingHudIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    val palette = LocalMapPalette.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp) // Accessible >=48dp touch target
            .clip(RoundedCornerShape(12.dp))
            .background(if (filled) accent else palette.panelSurface.copy(alpha = 0.90f))
            .border(1.5.dp, accent, RoundedCornerShape(12.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) contrastContentColor(accent) else accent,
            modifier = Modifier.size(22.dp)
        )
    }
}
