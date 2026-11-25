package com.example.ui.screens

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.model.*
import com.example.network.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.FullScreenMapDomain
import com.example.ui.components.AirspaceClassificationFilterPanel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.AudioTextBoxStrip
import com.example.ui.components.ColorCodedActionButton
import com.example.ui.components.LiveNavGpsOfflinePanel
import com.example.ui.components.MapLayerPresetRow
import com.example.ui.components.MapOverlayOptionChip
import com.example.ui.components.MapVisualThemeSelectorRow
import com.example.ui.components.colorForAirspaceClass
import com.example.ui.components.detectMapMultitouchGestures
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun HawaiiAirspaceContent(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    var showPtaIntakeDialog by remember { mutableStateOf(false) }
    val snap = uiState.airspaceSnapshot
    val isLoading = uiState.airspaceLoadState == AirspaceLoadState.Loading

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Live Telemetry Header & High-Contrast Refresh Controls
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.7f), DarkBorder))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("airspace_header_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HAWAIʻI ISLAND AIRSPACE & PTA MONITOR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(
                            text = if (isLoading) "Polling FAA TFRs & NWS CAP..." else "Updated: ${snap.updatedAtHst}",
                            fontSize = 11.sp,
                            color = if (isLoading) AmberRadar else Slate100,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                    onClick = {
                        viewModel.refreshHawaiiAirspace()
                        viewModel.refreshHawaiiWebcams()
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
                    border = BorderStroke(1.5.dp, CyanNeon),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag("airspace_refresh_btn")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Slate950,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Feed", modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AirspaceKpiPill(
                    label = "FAA TFRs",
                    value = "${snap.tfrList.size} HI/ZHN",
                    color = if (snap.tfrList.isNotEmpty()) CrimsonThreat else EmeraldVerified,
                    modifier = Modifier.weight(1f)
                )
                AirspaceKpiPill(
                    label = "WFS POLYS",
                    value = "${snap.tfrPolygons.size}",
                    color = if (snap.tfrPolygons.isNotEmpty()) CrimsonThreat else CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                AirspaceKpiPill(
                    label = "NWS ALERTS",
                    value = "${snap.nwsAlerts.size}",
                    color = if (snap.nwsAlerts.isNotEmpty()) AmberRadar else EmeraldVerified,
                    modifier = Modifier.weight(1f)
                )
                AirspaceKpiPill(
                    label = "LIVE CAMS",
                    value = "${uiState.webcamsSnapshot.allCamsSorted.size}",
                    color = AmberRadar,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // 2. Interactive Hawaiʻi Island Airspace, NASA Satellite, Doppler Radar, Map Scroll & Overlay Controls
    HawaiiAirspaceMapCard(
        tfrPolygons = snap.tfrPolygons,
        selectedSite = uiState.selectedAirspaceSite,
        webcams = uiState.webcamsSnapshot.allCamsSorted,
        dopplerEnabled = uiState.dopplerEnabled,
        activeDopplerLayer = uiState.activeDopplerLayer,
        dopplerOpacity = uiState.dopplerOpacity,
        dopplerRefreshNonce = uiState.dopplerRefreshNonce,
        satEnabled = uiState.airspaceSatEnabled,
        activeSatLayer = uiState.airspaceSatLayer,
        satOpacity = uiState.airspaceSatOpacity,
        satDateIso = uiState.airspaceSatDateIso,
        satRefreshNonce = uiState.airspaceSatRefreshNonce,
        mapZoom = uiState.airspaceMapZoom,
        mapRotationDeg = uiState.mapRotationDeg,
        mapTiltDeg = uiState.mapTiltDeg,
        mapScrollLocked = uiState.airspaceMapScrollLocked,
        mapExpanded = uiState.airspaceMapExpanded,
        showGridAndRoads = uiState.airspaceShowGridAndRoads,
        showSpecialZones = uiState.airspaceShowSpecialZones,
        showTfrs = uiState.airspaceShowTfrs,
        showRadarRings = uiState.airspaceShowRadarRings,
        showSitePins = uiState.airspaceShowSitePins,
        showWebcamPins = uiState.airspaceShowWebcamPins,
        enabledAirspaceClasses = uiState.enabledAirspaceClasses,
        airspaceAltitudeBand = uiState.airspaceAltitudeBand,
        airspaceOpacity = uiState.airspaceOpacity,
        userWaypoints = uiState.userWaypoints,
        gpsTelemetry = uiState.gpsTelemetry,
        adsbEnabled = uiState.adsbEnabled,
        adsbAircraft = uiState.adsbAircraft,
        hdTileResolution = uiState.hdTileResolution,
        onSelectSite = { viewModel.selectAirspaceSite(it) },
        onSelectWebcam = { viewModel.selectWebcamStill(it) },
        onSelectAirspaceZone = { viewModel.selectAirspaceZone(it) },
        onToggleDoppler = { viewModel.toggleDopplerEnabled(it) },
        onSelectDopplerLayer = { viewModel.selectDopplerLayer(it) },
        onChangeDopplerOpacity = { viewModel.setDopplerOpacity(it) },
        onRefreshDoppler = { viewModel.reloadDopplerOverlay() },
        onToggleSat = { viewModel.toggleAirspaceSatEnabled(it) },
        onSelectSatLayer = { viewModel.selectAirspaceSatLayer(it) },
        onChangeSatOpacity = { viewModel.setAirspaceSatOpacity(it) },
        onShiftSatDate = { viewModel.shiftAirspaceSatDateDays(it) },
        onRefreshSat = { viewModel.reloadAirspaceSatOverlay() },
        onOpenFullSatFeed = { site -> viewModel.openAirspaceSiteInFullSatFeed(site) },
        onMapZoomChange = { viewModel.setAirspaceMapZoom(it) },
        onMapTransform = { zoom, deltaRot ->
            viewModel.updateMapTransform(
                zoom = zoom,
                rotationDeg = uiState.mapRotationDeg + deltaRot,
                gestureLatencyMs = 10L
            )
        },
        onResetNorthUp = { viewModel.resetMapToNorthUp() },
        onOpenFullScreen = { viewModel.openFullScreenMap(FullScreenMapDomain.AIRSPACE) },
        onToggleMiniPip = { viewModel.toggleInAppMiniPip() },
        onOpenLegend = { viewModel.toggleMapLegendSheet(true) },
        onCycleSplitMode = {
            viewModel.cycleMapSplitMode()
            viewModel.openFullScreenMap(FullScreenMapDomain.AIRSPACE)
        },
        onToggleMapScrollLock = { viewModel.toggleAirspaceMapScrollLocked() },
        onToggleMapExpanded = { viewModel.toggleAirspaceMapExpanded() },
        onToggleGridAndRoads = { viewModel.toggleAirspaceGridAndRoads() },
        onToggleSpecialZones = { viewModel.toggleAirspaceSpecialZones() },
        onToggleTfrs = { viewModel.toggleAirspaceTfrs() },
        onToggleRadarRings = { viewModel.toggleAirspaceRadarRings() },
        onToggleSitePins = { viewModel.toggleAirspaceSitePins() },
        onToggleWebcamPins = { viewModel.toggleAirspaceWebcamPins() }
    )

    // 2a-1. Visual Accessibility Modes (Day/Night/High-Contrast/Color-Blind) & Presets
    MapVisualThemeSelectorRow(
        selectedMode = uiState.mapVisualThemeMode,
        onSelectMode = { viewModel.setMapVisualThemeMode(it) }
    )

    MapLayerPresetRow(
        activePreset = uiState.activeLayerPreset,
        onSelectPreset = { viewModel.applyMapLayerPreset(it) }
    )

    // 2a-2. Airspace Classification Overlays (A/B/C/D/R/TFR) & Altitude Band Filters
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

    // 2a-3. Real-Time GPS, ADS-B, Dynamic Route Waypoints, Predictive Prefetch & Offline Sectors
    LiveNavGpsOfflinePanel(viewModel = viewModel, uiState = uiState)

    // 2b. Live Hawaiʻi Still Cams (FAA WeatherCams + USGS HVO Volcanoes + UH System)
    HawaiiLiveCamsSection(viewModel = viewModel, uiState = uiState)

    // 3. Active FAA TFRs (Hawaiʻi / ZHN)
    SectionTitleBanner(
        tag = "FAA TFR FEED (tfr.faa.gov)",
        title = "Active FAA TFRs (Hawaiʻi / ZHN)",
        subtitle = "Filtered to HI state & Honolulu Center (ZHN) + WFS GeoJSON polygons"
    )

    if (snap.tfrFeedError != null) {
        AirspaceErrorCard(
            title = "FAA TFR Feed Connection Issue",
            message = snap.tfrFeedError,
            onRetry = { viewModel.refreshHawaiiAirspace() }
        )
    }
    if (snap.tfrFeedError == null && snap.tfrList.isEmpty() && snap.tfrPolygons.isEmpty()) {
        StatusBannerCard(
            badgeText = "CLEAR",
            badgeColor = EmeraldVerified,
            message = "No active FAA TFRs in Hawaiʻi airspace (state=HI / facility=ZHN)."
        )
    } else if (snap.tfrList.isNotEmpty() || snap.tfrPolygons.isNotEmpty()) {
        snap.tfrList.forEach { tfr ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CrimsonThreat.copy(alpha = 0.7f), DarkBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CrimsonThreat)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tfr.type.uppercase(Locale.US),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tfr.notamId,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Facility: ${tfr.facility} | State: ${tfr.state}",
                            fontSize = 11.sp,
                            color = AmberRadar,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        if (tfr.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = tfr.description, fontSize = 12.sp, color = Slate200)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioSelectButton(
                            sourceId = "tfr_${tfr.notamId}",
                            title = "FAA TFR ${tfr.notamId}",
                            text = "${tfr.type} NOTAM ${tfr.notamId}, Facility ${tfr.facility}, State ${tfr.state}. ${tfr.description}",
                            compact = true
                        )
                    }
                }
            }
            snap.tfrPolygons.forEach { poly ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate850),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CrimsonThreat.copy(alpha = 0.6f), DarkBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "WFS POLYGON: ${poly.notamKey}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonThreat,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(text = poly.title, fontSize = 12.sp, color = Slate100)
                        Text(
                            text = "${poly.rings.sumOf { it.size }} boundary vertices plotted on map",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }
        }

        // 4. PTA Military Drills & Convoys (Keyword Extractor)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "USAG HAWAIʻI / PTA ADVISORIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = JetBrainsMonoFontFamily
                )
                Text(
                    text = "PTA Military Drills & Convoys",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
            }
            ColorCodedActionButton(
                label = "Parse Advisory",
                accentColor = AmberRadar,
                icon = Icons.Default.PostAdd,
                filled = true,
                onClick = { showPtaIntakeDialog = true },
                modifier = Modifier.testTag("pta_parse_advisory_btn")
            )
        }

        if (uiState.ptaAdvisories.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(DarkBorder, Slate800))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "No PTA Advisories Parsed Yet",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste a USAG Hawaiʻi / PTA advisory email or Civil Defense bulletin via Parse Advisory to extract live-fire, artillery, mortar, convoy, Saddle Road, helicopter, UAS, R-3103, and Bradshaw indicators.",
                        fontSize = 12.sp,
                        color = Slate300,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            uiState.ptaAdvisories.forEach { adv ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(AmberRadar.copy(alpha = 0.7f), DarkBorder))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pta_advisory_card_${adv.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AmberRadar)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = adv.keywords.joinToString(", "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${adv.dateHst} • ${adv.subject}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = adv.excerpt, fontSize = 12.sp, color = Slate200, lineHeight = 16.sp)
                        if (adv.impactsSaddleRoad || adv.impactsAviationOrUas) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = buildString {
                                    if (adv.impactsSaddleRoad) append("• Saddle Rd / Convoy Transit Impact  ")
                                    if (adv.impactsAviationOrUas) append("• R-3103 / Rotary / UAS Caution")
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonThreat
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioSelectButton(
                            sourceId = "pta_${adv.id}",
                            title = "PTA Advisory ${adv.subject}",
                            text = "${adv.dateHst}, ${adv.subject}. Keywords: ${adv.keywords.joinToString(", ")}. ${adv.excerpt}",
                            compact = true
                        )
                    }
                }
            }
        }

        // 5. NWS / Volcano & Civil CAP Alerts
        SectionTitleBanner(
            tag = "NWS CAP FEED (api.weather.gov)",
            title = "NWS / Volcano & Civil Alerts (HI)",
            subtitle = "Live National Weather Service & HVO civil emergency advisories"
        )

        if (snap.nwsFeedError != null) {
            AirspaceErrorCard(
                title = "NWS Hawaiʻi Feed Connection Issue",
                message = snap.nwsFeedError,
                onRetry = { viewModel.refreshHawaiiAirspace() }
            )
        } else if (snap.nwsAlerts.isEmpty()) {
            StatusBannerCard(
                badgeText = "NORMAL",
                badgeColor = EmeraldVerified,
                message = "No active NWS / Volcano CAP alerts for Hawaiʻi."
            )
        } else {
            snap.nwsAlerts.forEach { alert ->
                val sevColor = when {
                    alert.severity.equals("Extreme", ignoreCase = true) ||
                        alert.severity.equals("Severe", ignoreCase = true) -> CrimsonThreat
                    else -> AmberRadar
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(sevColor.copy(alpha = 0.7f), DarkBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(sevColor)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = alert.severity.uppercase(Locale.US),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate950
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alert.event,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = alert.areaDesc, fontSize = 11.sp, color = CyanNeon)
                        if (alert.headline.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = alert.headline, fontSize = 12.sp, color = Slate200)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioSelectButton(
                            sourceId = "nws_${alert.event.lowercase(Locale.US).replace(" ", "_")}",
                            title = "NWS ${alert.event}",
                            text = "${alert.severity} ${alert.event} for ${alert.areaDesc}. ${alert.headline}",
                            compact = true
                        )
                    }
                }
            }
        }
    }

    if (showPtaIntakeDialog) {
        PtaAdvisoryIntakeDialog(
            onDismiss = { showPtaIntakeDialog = false },
            onParseAndSave = { subject, sender, body ->
                viewModel.parseAndAddPtaAdvisory(subject, sender, body)
                showPtaIntakeDialog = false
            }
        )
    }

    uiState.selectedWebcamStill?.let { cam ->
        WebcamLightboxDialog(
            cam = cam,
            refreshNonce = uiState.webcamRefreshNonce,
            onReload = { viewModel.reloadWebcamStills() },
            onDismiss = { viewModel.selectWebcamStill(null) }
        )
    }
}

@Composable
private fun HawaiiAirspaceMapCard(
    tfrPolygons: List<HawaiiTfrPolygon>,
    selectedSite: HawaiiAirspaceSite,
    webcams: List<HawaiiWebcamStill>,
    dopplerEnabled: Boolean,
    activeDopplerLayer: HawaiiDopplerLayer,
    dopplerOpacity: Float,
    dopplerRefreshNonce: Long,
    satEnabled: Boolean,
    activeSatLayer: NasaGibsLayer,
    satOpacity: Float,
    satDateIso: String,
    satRefreshNonce: Long,
    mapZoom: Float,
    mapRotationDeg: Float,
    mapTiltDeg: Float,
    mapScrollLocked: Boolean,
    mapExpanded: Boolean,
    showGridAndRoads: Boolean,
    showSpecialZones: Boolean,
    showTfrs: Boolean,
    showRadarRings: Boolean,
    showSitePins: Boolean,
    showWebcamPins: Boolean,
    enabledAirspaceClasses: Set<AirspaceClassification>,
    airspaceAltitudeBand: AirspaceAltitudeBand,
    airspaceOpacity: Float,
    userWaypoints: List<UserWaypoint>,
    gpsTelemetry: LiveGpsTelemetry,
    adsbEnabled: Boolean,
    adsbAircraft: List<AdsbAircraftTrack>,
    hdTileResolution: Boolean,
    onSelectSite: (HawaiiAirspaceSite) -> Unit,
    onSelectWebcam: (HawaiiWebcamStill) -> Unit,
    onSelectAirspaceZone: (AirspaceClassificationZone?) -> Unit,
    onToggleDoppler: (Boolean) -> Unit,
    onSelectDopplerLayer: (HawaiiDopplerLayer) -> Unit,
    onChangeDopplerOpacity: (Float) -> Unit,
    onRefreshDoppler: () -> Unit,
    onToggleSat: (Boolean) -> Unit,
    onSelectSatLayer: (NasaGibsLayer) -> Unit,
    onChangeSatOpacity: (Float) -> Unit,
    onShiftSatDate: (Int) -> Unit,
    onRefreshSat: () -> Unit,
    onOpenFullSatFeed: (HawaiiAirspaceSite) -> Unit,
    onMapZoomChange: (Float) -> Unit,
    onMapTransform: (zoom: Float, deltaRotationDeg: Float) -> Unit,
    onResetNorthUp: () -> Unit,
    onOpenFullScreen: () -> Unit,
    onToggleMiniPip: () -> Unit,
    onOpenLegend: () -> Unit,
    onCycleSplitMode: () -> Unit,
    onToggleMapScrollLock: () -> Unit,
    onToggleMapExpanded: () -> Unit,
    onToggleGridAndRoads: () -> Unit,
    onToggleSpecialZones: () -> Unit,
    onToggleTfrs: () -> Unit,
    onToggleRadarRings: () -> Unit,
    onToggleSitePins: () -> Unit,
    onToggleWebcamPins: () -> Unit
) {
    val context = LocalContext.current
    val palette = LocalMapPalette.current
    val minLat = HawaiiAirspaceReference.MAP_MIN_LAT
    val maxLat = HawaiiAirspaceReference.MAP_MAX_LAT
    val minLon = HawaiiAirspaceReference.MAP_MIN_LON
    val maxLon = HawaiiAirspaceReference.MAP_MAX_LON
    val sites = HawaiiAirspaceReference.keySites

    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var dopplerStatus by remember(dopplerEnabled, activeDopplerLayer, dopplerRefreshNonce) { mutableStateOf("SYNC") }
    var satStatus by remember(satEnabled, activeSatLayer, satDateIso, satRefreshNonce) { mutableStateOf("SYNC") }
    var satAutoStepCount by remember(activeSatLayer) { mutableStateOf(0) }

    val currentMapZoom by rememberUpdatedState(mapZoom)
    val currentMapRot by rememberUpdatedState(mapRotationDeg)
    val currentMapScrollLocked by rememberUpdatedState(mapScrollLocked)
    val currentPanOffset by rememberUpdatedState(panOffset)
    val currentViewportSize by rememberUpdatedState(viewportSize)
    val currentSites by rememberUpdatedState(sites)
    val currentWebcams by rememberUpdatedState(webcams)
    val currentEnabledClasses by rememberUpdatedState(enabledAirspaceClasses)
    val currentAltitudeBand by rememberUpdatedState(airspaceAltitudeBand)
    val currentShowSitePins by rememberUpdatedState(showSitePins)
    val currentShowWebcamPins by rememberUpdatedState(showWebcamPins)
    val currentOnMapZoomChange by rememberUpdatedState(onMapZoomChange)
    val currentOnMapTransform by rememberUpdatedState(onMapTransform)
    val currentOnSelectSite by rememberUpdatedState(onSelectSite)
    val currentOnSelectWebcam by rememberUpdatedState(onSelectWebcam)
    val currentOnSelectAirspaceZone by rememberUpdatedState(onSelectAirspaceZone)

    LaunchedEffect(mapZoom, viewportSize) {
        if (mapZoom <= 1.02f) {
            panOffset = Offset.Zero
        } else {
            val maxPanX = ((viewportSize.width * (mapZoom - 1f)) / 2f).coerceAtLeast(0f)
            val maxPanY = ((viewportSize.height * (mapZoom - 1f)) / 2f).coerceAtLeast(0f)
            panOffset = Offset(
                x = panOffset.x.coerceIn(-maxPanX, maxPanX),
                y = panOffset.y.coerceIn(-maxPanY, maxPanY)
            )
        }
    }

    val tileW = if (hdTileResolution) 1440 else 800
    val tileH = if (hdTileResolution) 1080 else 600

    val dopplerUrl = remember(activeDopplerLayer, dopplerRefreshNonce, tileW) {
        HawaiiDopplerUrlBuilder.buildWmsUrl(
            layer = activeDopplerLayer,
            width = tileW,
            height = tileH,
            cacheBustNonce = dopplerRefreshNonce
        )
    }

    val satUrl = remember(activeSatLayer, satDateIso, satRefreshNonce, tileW) {
        NasaGibsUrlBuilder.buildBboxSnapshotUrl(
            south = minLat,
            west = minLon,
            north = maxLat,
            east = maxLon,
            dateIso = satDateIso,
            layer = activeSatLayer,
            width = tileW,
            height = tileH,
            cacheBustNonce = satRefreshNonce
        )
    }

    val labelPaint = remember(palette.textPrimary) {
        val shadowCol = if (palette.textPrimary.luminance() > 0.5f) {
            android.graphics.Color.BLACK
        } else {
            android.graphics.Color.WHITE
        }
        Paint().apply {
            isAntiAlias = true
            color = palette.textPrimary.toArgb()
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            setShadowLayer(4f, 0f, 0f, shadowCol)
        }
    }

    fun project(pt: GeoPoint, w: Float, h: Float): Offset {
        val x = (((pt.lon - minLon) / (maxLon - minLon)).toFloat()).coerceIn(0f, 1f) * w
        val y = ((1.0 - (pt.lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f) * h
        return Offset(x, y)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(palette.advisoryBlue.copy(alpha = 0.7f), palette.panelBorder))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hawaii_airspace_map_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HAWAIʻI ISLAND AIRSPACE, SAT & DOPPLER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.advisoryBlue,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Text(
                        text = buildString {
                            append(if (satEnabled) activeSatLayer.shortLabel else "Sat Off")
                            append(" • ")
                            append(if (dopplerEnabled) activeDopplerLayer.shortLabel else "Doppler Off")
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (dopplerEnabled) palette.classAColor else if (satEnabled) palette.advisoryBlue else palette.textSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // High-contrast NASA GIBS Satellite Base Toggle Pill
                    Box(
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (satEnabled) palette.advisoryBlue else palette.canvasBackground)
                            .border(
                                1.5.dp,
                                if (satEnabled) palette.advisoryBlue else palette.panelBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleSat(!satEnabled) }
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                            .testTag("airspace_sat_toggle_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SatelliteAlt,
                                contentDescription = "Toggle Satellite Layer",
                                tint = if (satEnabled) contrastContentColor(palette.advisoryBlue) else palette.advisoryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (satEnabled) "SAT ON" else "SAT OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (satEnabled) contrastContentColor(palette.advisoryBlue) else palette.textPrimary,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }

                    // High-contrast Doppler Radar Overlay Toggle Pill
                    Box(
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (dopplerEnabled) palette.classAColor else palette.canvasBackground)
                            .border(
                                1.5.dp,
                                if (dopplerEnabled) palette.classAColor else palette.panelBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleDoppler(!dopplerEnabled) }
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                            .testTag("doppler_toggle_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = "Toggle Doppler Radar",
                                tint = if (dopplerEnabled) contrastContentColor(palette.classAColor) else palette.classAColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (dopplerEnabled) "DOPPLER ON" else "DOPPLER OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dopplerEnabled) contrastContentColor(palette.classAColor) else palette.textPrimary,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Map Viewport (3 synchronized geospatial layers transformed together by zoom + pan)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mapExpanded) 370.dp else 270.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.canvasBackground)
                    .border(1.5.dp, if (mapScrollLocked) palette.warningAmber else palette.panelBorder, RoundedCornerShape(10.dp))
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (currentMapZoom > 1.2f) {
                                    currentOnMapZoomChange(1.0f)
                                    panOffset = Offset.Zero
                                } else {
                                    currentOnMapZoomChange(2.4f)
                                }
                            },
                            onTap = { tap ->
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val z = currentMapZoom
                                val pOff = currentPanOffset
                                // Map screen tap back into un-zoomed & un-rotated canvas coordinates
                                val cx = w / 2f
                                val cy = h / 2f
                                val dx = (tap.x - pOff.x - cx) / z
                                val dy = (tap.y - pOff.y - cy) / z
                                val rad = Math.toRadians(-currentMapRot.toDouble())
                                val canvasX = (dx * cos(rad) - dy * sin(rad)).toFloat() + cx
                                val canvasY = (dx * sin(rad) + dy * cos(rad)).toFloat() + cy

                                val nearestSite = if (currentShowSitePins) {
                                    currentSites.minByOrNull { s ->
                                        val p = project(GeoPoint(s.lat, s.lon), w, h)
                                        hypot(canvasX - p.x, canvasY - p.y)
                                    }
                                } else null
                                val siteDist = nearestSite?.let { s ->
                                    val p = project(GeoPoint(s.lat, s.lon), w, h)
                                    hypot(canvasX - p.x, canvasY - p.y)
                                } ?: Float.MAX_VALUE

                                val nearestCam = if (currentShowWebcamPins) {
                                    currentWebcams.filter {
                                        it.latitude in minLat..maxLat && it.longitude in minLon..maxLon
                                    }.minByOrNull { c ->
                                        val p = project(GeoPoint(c.latitude, c.longitude), w, h)
                                        hypot(canvasX - p.x, canvasY - p.y)
                                    }
                                } else null
                                val camDist = nearestCam?.let { c ->
                                    val p = project(GeoPoint(c.latitude, c.longitude), w, h)
                                    hypot(canvasX - p.x, canvasY - p.y)
                                } ?: Float.MAX_VALUE

                                val visibleZones = HawaiiAirspaceClassificationsCatalog.staticZones.filter { zone ->
                                    currentEnabledClasses.contains(zone.classification) &&
                                        currentAltitudeBand.matchesAltitudeRange(zone.floorFtMsl, zone.ceilingFtMsl)
                                }
                                val matchedZone = visibleZones.minByOrNull { zone ->
                                    val p = project(GeoPoint(zone.centerLat, zone.centerLon), w, h)
                                    hypot(canvasX - p.x, canvasY - p.y)
                                }
                                val zoneDist = matchedZone?.let { zone ->
                                    val p = project(GeoPoint(zone.centerLat, zone.centerLon), w, h)
                                    hypot(canvasX - p.x, canvasY - p.y)
                                } ?: Float.MAX_VALUE

                                val threshold = 72f / z
                                if (nearestCam != null && camDist < siteDist && camDist <= threshold) {
                                    currentOnSelectWebcam(nearestCam)
                                } else if (nearestSite != null && siteDist <= threshold) {
                                    currentOnSelectSite(nearestSite)
                                    if (matchedZone != null && zoneDist <= 130f / z) {
                                        currentOnSelectAirspaceZone(matchedZone)
                                    }
                                } else if (matchedZone != null && zoneDist <= 130f / z) {
                                    currentOnSelectAirspaceZone(matchedZone)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectMapMultitouchGestures(
                            enableTilt = false,
                            allowSingleFingerPan = { currentMapScrollLocked || currentMapZoom > 1.02f || currentMapRot != 0f }
                        ) { zoomChange, pan, rotation, _ ->
                            val updatedZoom = (currentMapZoom * zoomChange).coerceIn(1.0f, 8.0f)
                            currentOnMapTransform(updatedZoom, rotation)
                            if (updatedZoom <= 1.02f) {
                                panOffset = Offset.Zero
                            } else {
                                val maxPanX = ((currentViewportSize.width * (updatedZoom - 1f)) / 2f).coerceAtLeast(0f)
                                val maxPanY = ((currentViewportSize.height * (updatedZoom - 1f)) / 2f).coerceAtLeast(0f)
                                panOffset = Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                                )
                            }
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = mapZoom
                            scaleY = mapZoom
                            translationX = panOffset.x
                            translationY = panOffset.y
                            rotationZ = mapRotationDeg
                            rotationX = mapTiltDeg
                        }
                ) {
                    // Layer 1: NASA Worldview GIBS Satellite Tile aligned to Big Island EPSG:4326 map bbox
                    if (satEnabled) {
                        SubcomposeAsyncImage(
                            model = buildFeedImageRequest(
                                context = context,
                                url = satUrl,
                                referer = "https://worldview.earthdata.nasa.gov/"
                            ),
                            contentDescription = "NASA GIBS ${activeSatLayer.fullTitle} satellite overlay for Hawaiʻi Island",
                            contentScale = ContentScale.FillBounds,
                            onState = { state ->
                                when (state) {
                                    is AsyncImagePainter.State.Loading -> satStatus = "SYNC"
                                    is AsyncImagePainter.State.Success -> satStatus = "LIVE"
                                    is AsyncImagePainter.State.Error -> {
                                        satStatus = "ERR"
                                        if (satAutoStepCount < 2 && NasaGibsUrlBuilder.isRecentCompositeWindow(satDateIso)) {
                                            satAutoStepCount += 1
                                            onShiftSatDate(-1)
                                        }
                                    }
                                    AsyncImagePainter.State.Empty -> Unit
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(satOpacity)
                                .testTag("airspace_sat_overlay"),
                            content = {
                                if (painter.state is AsyncImagePainter.State.Success) {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        )
                    }

                    // Layer 1b: Base landmass, coordinate grid & highway corridors
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        if (showGridAndRoads) {
                            for (i in 1..4) {
                                val gx = w * (i / 5f)
                                val gy = h * (i / 5f)
                                drawLine(palette.terrainContour.copy(alpha = 0.45f), Offset(gx, 0f), Offset(gx, h), 1f)
                                drawLine(palette.terrainContour.copy(alpha = 0.45f), Offset(0f, gy), Offset(w, gy), 1f)
                            }
                        }

                        val islandPath = Path().apply {
                            HawaiiAirspaceReference.hawaiiIslandCoastline.forEachIndexed { idx, pt ->
                                val o = project(pt, w, h)
                                if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                            }
                            close()
                        }
                        if (!(satEnabled && satStatus == "LIVE")) {
                            drawPath(islandPath, color = palette.landFill, style = Fill)
                        }
                        drawPath(islandPath, color = palette.landOutline, style = Stroke(width = 2.6f * palette.strokeMultiplier))

                        if (showGridAndRoads) {
                            val saddlePath = Path().apply {
                                HawaiiAirspaceReference.saddleRoadPath.forEachIndexed { idx, pt ->
                                    val o = project(pt, w, h)
                                    if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                }
                            }
                            drawPath(
                                saddlePath,
                                color = palette.textSecondary.copy(alpha = 0.75f),
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                            )
                            val hwy130 = Path().apply {
                                HawaiiAirspaceReference.hwy130Path.forEachIndexed { idx, pt ->
                                    val o = project(pt, w, h)
                                    if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                }
                            }
                            drawPath(hwy130, color = palette.normalGreen.copy(alpha = 0.85f), style = Stroke(width = 2.4f))
                        }
                    }

                    // Layer 2: Live NOAA/NCEP NEXRAD Doppler Radar PNG aligned to EPSG:4326 map bbox
                    if (dopplerEnabled) {
                        SubcomposeAsyncImage(
                            model = buildFeedImageRequest(
                                context = context,
                                url = dopplerUrl,
                                referer = "https://opengeo.ncep.noaa.gov/"
                            ),
                            contentDescription = "Live ${activeDopplerLayer.fullTitle} Doppler radar overlay",
                            contentScale = ContentScale.FillBounds,
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
                                .alpha(dopplerOpacity)
                                .testTag("doppler_wms_overlay"),
                            content = {
                                if (painter.state is AsyncImagePainter.State.Success) {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        )
                    }

                    // Layer 3: Tactical Airspace Polygons, TFRs, NEXRAD range rings & interactive Site Pins
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        if (showSpecialZones) {
                            // Altitude-filtered Class A / B / C / D / Restricted / TFR Airspace Overlays
                            val filteredZones = HawaiiAirspaceClassificationsCatalog.staticZones.filter { zone ->
                                enabledAirspaceClasses.contains(zone.classification) &&
                                    airspaceAltitudeBand.matchesAltitudeRange(zone.floorFtMsl, zone.ceilingFtMsl)
                            }
                            filteredZones.forEach { zone ->
                                val zColor = colorForAirspaceClass(zone.classification, palette)
                                val strokeDash = when (zone.classification) {
                                    AirspaceClassification.CLASS_A -> floatArrayOf(12f, 6f)
                                    AirspaceClassification.CLASS_D -> floatArrayOf(6f, 6f)
                                    AirspaceClassification.RESTRICTED -> floatArrayOf(10f, 6f)
                                    else -> null
                                }
                                if (zone.polygon.isNotEmpty()) {
                                    val path = Path().apply {
                                        zone.polygon.forEachIndexed { idx, pt ->
                                            val o = project(pt, w, h)
                                            if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                        }
                                        close()
                                    }
                                    drawPath(path, color = zColor.copy(alpha = 0.16f * airspaceOpacity), style = Fill)
                                    drawPath(
                                        path,
                                        color = zColor.copy(alpha = airspaceOpacity),
                                        style = Stroke(
                                            width = 2.4f * palette.strokeMultiplier,
                                            pathEffect = strokeDash?.let { PathEffect.dashPathEffect(it) }
                                        )
                                    )
                                } else {
                                    val c = project(GeoPoint(zone.centerLat, zone.centerLon), w, h)
                                    val ringRadiusLatDeg = zone.radiusNm / 60.0
                                    val edge = project(GeoPoint(zone.centerLat + ringRadiusLatDeg, zone.centerLon), w, h)
                                    val radiusPx = (c.y - edge.y).coerceAtLeast(10f)
                                    drawCircle(color = zColor.copy(alpha = 0.14f * airspaceOpacity), radius = radiusPx, center = c)
                                    drawCircle(
                                        color = zColor.copy(alpha = airspaceOpacity),
                                        radius = radiusPx,
                                        center = c,
                                        style = Stroke(
                                            width = 2.0f * palette.strokeMultiplier,
                                            pathEffect = strokeDash?.let { PathEffect.dashPathEffect(it) }
                                        )
                                    )
                                }
                            }
                        }

                        // Active Route & User Waypoints
                        if (userWaypoints.isNotEmpty()) {
                            val pts = listOf(GeoPoint(gpsTelemetry.latitude, gpsTelemetry.longitude)) +
                                userWaypoints.map { GeoPoint(it.lat, it.lon) }
                            val routePath = Path().apply {
                                pts.forEachIndexed { i, pt ->
                                    val o = project(pt, w, h)
                                    if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                }
                            }
                            drawPath(
                                routePath,
                                color = palette.routeLineColor,
                                style = Stroke(width = 2.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 5f)))
                            )
                            userWaypoints.forEach { wp ->
                                val wpPos = project(GeoPoint(wp.lat, wp.lon), w, h)
                                drawCircle(palette.routeLineColor, radius = 5.5f, center = wpPos)
                            }
                        }

                        // Live GPS Position Indicator
                        val gpsPos = project(GeoPoint(gpsTelemetry.latitude, gpsTelemetry.longitude), w, h)
                        drawCircle(palette.normalGreen.copy(alpha = 0.25f), radius = 14f, center = gpsPos)
                        drawCircle(palette.normalGreen, radius = 5.5f, center = gpsPos)

                        // Live ADS-B Aircraft Targets
                        if (adsbEnabled) {
                            adsbAircraft.forEach { ac ->
                                val p = project(GeoPoint(ac.latitude, ac.longitude), w, h)
                                drawCircle(palette.adsbTargetColor, radius = 5f, center = p)
                            }
                        }

                        // NEXRAD WSR-88D Doppler sweep range rings
                        if (dopplerEnabled && showRadarRings) {
                            val ringSpecDeg = 0.45
                            listOf(
                                GeoPoint(19.0947, -155.5689), // PHWA
                                GeoPoint(20.1253, -155.7781)  // PHKM
                            ).forEach { radarPt ->
                                val c = project(radarPt, w, h)
                                val edge = project(GeoPoint(radarPt.lat + ringSpecDeg, radarPt.lon), w, h)
                                val rPx = (c.y - edge.y).coerceAtLeast(18f)
                                drawCircle(
                                    color = palette.classAColor.copy(alpha = 0.45f),
                                    radius = rPx,
                                    center = c,
                                    style = Stroke(width = 1.6f * palette.strokeMultiplier, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                                )
                            }
                        }

                        // Live FAA WFS TFR GeoJSON polygons
                        if (showTfrs && enabledAirspaceClasses.contains(AirspaceClassification.TFR)) {
                            tfrPolygons.forEach { tfrPoly ->
                                tfrPoly.rings.forEach { ring ->
                                    val tfrPath = Path().apply {
                                        ring.forEachIndexed { idx, pt ->
                                            val o = project(pt, w, h)
                                            if (idx == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
                                        }
                                        close()
                                    }
                                    drawPath(tfrPath, color = palette.criticalRed.copy(alpha = 0.36f * airspaceOpacity), style = Fill)
                                    drawPath(tfrPath, color = palette.criticalRed, style = Stroke(width = 2.6f * palette.strokeMultiplier))
                                }
                            }
                        }

                        // Key site pins + labels
                        if (showSitePins) {
                            sites.forEach { site ->
                                val p = project(GeoPoint(site.lat, site.lon), w, h)
                                val isSel = selectedSite.id == site.id
                                val pinColor = when (site.id) {
                                    "site_r3103", "site_phsf" -> palette.warningAmber
                                    "site_hvo" -> palette.criticalRed
                                    "site_orchidland" -> palette.normalGreen
                                    "site_phwa", "site_phkm" -> palette.classAColor
                                    else -> palette.advisoryBlue
                                }
                                if (isSel) {
                                    drawCircle(pinColor.copy(alpha = 0.38f), radius = 16f, center = p)
                                }
                                drawCircle(pinColor, radius = if (isSel) 9.5f else 7f, center = p)
                                drawCircle(palette.textPrimary, radius = if (isSel) 9.5f else 7f, center = p, style = Stroke(2f))

                                // Draw concise site tag (skip PHSF when overlapping R-3103 unless zoomed or selected)
                                if (site.id != "site_phsf" || mapZoom >= 1.4f || isSel) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        site.shortCode,
                                        (p.x + 12f).coerceAtMost(w - 86f),
                                        (p.y + 6f).coerceIn(24f, h - 10f),
                                        labelPaint
                                    )
                                }
                            }
                        }
                        // Live Hawaiʻi Island Webcam Markers (USGS HVO, UH Mauna Kea, FAA WeatherCams)
                        if (showWebcamPins) {
                            webcams.forEach { cam ->
                                if (cam.latitude in minLat..maxLat && cam.longitude in minLon..maxLon) {
                                    val p = project(GeoPoint(cam.latitude, cam.longitude), w, h)
                                    val camColor = when (cam.source) {
                                        WebcamFeedSource.FAA_WEATHERCAM -> palette.advisoryBlue
                                        WebcamFeedSource.USGS_VOLCANO -> if (cam.isThermal) palette.criticalRed else palette.warningAmber
                                        WebcamFeedSource.UH_SYSTEM -> palette.normalGreen
                                    }
                                    drawRect(
                                        color = camColor,
                                        topLeft = Offset(p.x - 5f, p.y - 5f),
                                        size = androidx.compose.ui.geometry.Size(10f, 10f)
                                    )
                                    drawRect(
                                        color = palette.canvasBackground,
                                        topLeft = Offset(p.x - 5f, p.y - 5f),
                                        size = androidx.compose.ui.geometry.Size(10f, 10f),
                                        style = Stroke(1.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // HUD: Top-left Satellite + NOAA Radar Status (with Error Retry!) + Zoom Level Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    if (satEnabled) {
                        val (satColor, satText) = when (satStatus) {
                            "LIVE" -> palette.advisoryBlue to "SAT • ${activeSatLayer.shortLabel} • LIVE"
                            "ERR" -> palette.criticalRed to "SAT ERR • TAP RETRY"
                            else -> palette.advisoryBlue to "SAT • ${activeSatLayer.shortLabel} • SYNC"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.canvasBackground.copy(alpha = 0.92f))
                                .border(1.dp, satColor, RoundedCornerShape(6.dp))
                                .clickable { onRefreshSat() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("airspace_sat_status_badge")
                        ) {
                            Text(
                                text = satText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = satColor,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                    if (dopplerEnabled) {
                        val (pillColor, pillText) = when (dopplerStatus) {
                            "LIVE" -> palette.classAColor to "NOAA • ${activeDopplerLayer.shortLabel} • LIVE"
                            "ERR" -> palette.criticalRed to "WMS ERR • TAP RETRY"
                            else -> palette.warningAmber to "NOAA • ${activeDopplerLayer.shortLabel} • SYNC"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.canvasBackground.copy(alpha = 0.92f))
                                .border(1.dp, pillColor, RoundedCornerShape(6.dp))
                                .clickable { onRefreshDoppler() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("doppler_status_badge")
                        ) {
                            Text(
                                text = pillText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = pillColor,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.canvasBackground.copy(alpha = 0.90f))
                            .border(1.dp, palette.warningAmber, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("airspace_zoom_readout")
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fx • %s", mapZoom, if (mapScrollLocked) "PAN ON" else "SCROLL"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.warningAmber,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }

                // Floating Right-Side Map Fullscreen, Zoom, North-Up & Expand Controls
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    AirspaceMapControlSquare(
                        icon = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen Airspace Map",
                        accentColor = EmeraldVerified,
                        onClick = onOpenFullScreen,
                        modifier = Modifier.testTag("airspace_fullscreen_btn")
                    )
                    AirspaceMapControlSquare(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In Hawaiʻi Map",
                        accentColor = CyanNeon,
                        onClick = { onMapZoomChange((mapZoom + 0.5f).coerceAtMost(8.0f)) },
                        modifier = Modifier.testTag("airspace_zoom_in_btn")
                    )
                    AirspaceMapControlSquare(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out Hawaiʻi Map",
                        accentColor = CyanNeon,
                        onClick = { onMapZoomChange((mapZoom - 0.5f).coerceAtLeast(1.0f)) },
                        modifier = Modifier.testTag("airspace_zoom_out_btn")
                    )
                    AirspaceMapControlSquare(
                        icon = if (mapExpanded) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = if (mapExpanded) "Compact Map Height" else "Expand Map Height",
                        accentColor = EmeraldVerified,
                        onClick = onToggleMapExpanded,
                        modifier = Modifier.testTag("airspace_expand_map_btn")
                    )
                    AirspaceMapControlSquare(
                        icon = Icons.Default.Explore,
                        contentDescription = "North-Up Orientation Reset",
                        accentColor = AmberRadar,
                        onClick = {
                            onResetNorthUp()
                            onMapZoomChange(1.0f)
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.testTag("airspace_north_up_btn")
                    )
                    if (mapZoom > 1.05f || panOffset != Offset.Zero) {
                        AirspaceMapControlSquare(
                            icon = Icons.Default.CenterFocusStrong,
                            contentDescription = "Reset Map Zoom",
                            accentColor = AmberRadar,
                            onClick = {
                                onMapZoomChange(1.0f)
                                panOffset = Offset.Zero
                            },
                            modifier = Modifier.testTag("airspace_zoom_reset_btn")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Map Scroll & Overlay Options Strip
            Text(
                text = "MAP SCROLL & OVERLAY OPTIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate300,
                fontFamily = JetBrainsMonoFontFamily
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("airspace_overlay_options_row")
            ) {
                MapOverlayOptionChip(
                    label = if (mapScrollLocked) "Map Pan ON" else "Page Scroll",
                    active = mapScrollLocked,
                    accentColor = AmberRadar,
                    icon = if (mapScrollLocked) Icons.Default.Lock else Icons.Default.PanTool,
                    onClick = onToggleMapScrollLock,
                    modifier = Modifier.testTag("airspace_scroll_lock_btn")
                )
                MapOverlayOptionChip(
                    label = "Satellite",
                    active = satEnabled,
                    accentColor = CyanNeon,
                    icon = Icons.Default.SatelliteAlt,
                    onClick = { onToggleSat(!satEnabled) },
                    modifier = Modifier.testTag("overlay_opt_satellite")
                )
                MapOverlayOptionChip(
                    label = "Grid & Rds",
                    active = showGridAndRoads,
                    accentColor = EmeraldVerified,
                    onClick = onToggleGridAndRoads,
                    modifier = Modifier.testTag("overlay_opt_grid_roads")
                )
                MapOverlayOptionChip(
                    label = "R-3103/PHTO",
                    active = showSpecialZones,
                    accentColor = BlueSat,
                    onClick = onToggleSpecialZones,
                    modifier = Modifier.testTag("overlay_opt_special_zones")
                )
                MapOverlayOptionChip(
                    label = "FAA TFRs",
                    active = showTfrs,
                    accentColor = CrimsonThreat,
                    onClick = onToggleTfrs,
                    modifier = Modifier.testTag("overlay_opt_tfrs")
                )
                MapOverlayOptionChip(
                    label = "Radar Rings",
                    active = showRadarRings,
                    accentColor = VioletVector,
                    onClick = onToggleRadarRings,
                    modifier = Modifier.testTag("overlay_opt_radar_rings")
                )
                MapOverlayOptionChip(
                    label = "Site Pins",
                    active = showSitePins,
                    accentColor = CyanNeon,
                    onClick = onToggleSitePins,
                    modifier = Modifier.testTag("overlay_opt_site_pins")
                )
                MapOverlayOptionChip(
                    label = "Live Cams",
                    active = showWebcamPins,
                    accentColor = AmberRadar,
                    onClick = onToggleWebcamPins,
                    modifier = Modifier.testTag("overlay_opt_webcams")
                )
                MapOverlayOptionChip(
                    label = "Split View",
                    active = false,
                    accentColor = EmeraldVerified,
                    icon = Icons.Default.Splitscreen,
                    onClick = onCycleSplitMode,
                    modifier = Modifier.testTag("airspace_split_mode_btn")
                )
                MapOverlayOptionChip(
                    label = "Mini PiP",
                    active = false,
                    accentColor = BlueSat,
                    icon = Icons.Default.PictureInPictureAlt,
                    onClick = onToggleMiniPip,
                    modifier = Modifier.testTag("airspace_pip_btn")
                )
                MapOverlayOptionChip(
                    label = "Legend",
                    active = false,
                    accentColor = AmberRadar,
                    icon = Icons.Default.Map,
                    onClick = onOpenLegend,
                    modifier = Modifier.testTag("airspace_legend_btn")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Satellite Options Row (NASA Worldview GIBS Spectral Layers + Temporal Step + Opacity)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SATELLITE OPTIONS (NASA GIBS • $satDateIso)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = JetBrainsMonoFontFamily,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(-1 to "-1d", 1 to "+1d").forEach { (delta, lbl) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate850)
                                .border(1.dp, CyanNeon.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .clickable { onShiftSatDate(delta) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("airspace_sat_date_${delta}")
                        ) {
                            Text(
                                text = lbl,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanNeon.copy(alpha = 0.2f))
                            .border(1.dp, CyanNeon, RoundedCornerShape(6.dp))
                            .clickable { onOpenFullSatFeed(selectedSite) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("airspace_open_full_sat_btn")
                    ) {
                        Text(
                            text = "Full Sat →",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("airspace_sat_layers_row")
            ) {
                NasaGibsLayer.values().forEach { layer ->
                    val isSelected = satEnabled && activeSatLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectSatLayer(layer) },
                        label = {
                            Text(
                                text = layer.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanNeon,
                            selectedLabelColor = Slate950,
                            containerColor = Slate850,
                            labelColor = Slate200
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CyanNeon.copy(alpha = 0.5f),
                            selectedBorderColor = CyanNeon
                        ),
                        modifier = Modifier.testTag("airspace_sat_layer_${layer.id}")
                    )
                }
            }

            if (satEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SAT OPACITY ${(satOpacity * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Slider(
                        value = satOpacity,
                        onValueChange = onChangeSatOpacity,
                        valueRange = 0.25f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanNeon,
                            activeTrackColor = CyanNeon,
                            inactiveTrackColor = Slate800
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .testTag("airspace_sat_opacity_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Doppler Product Selector Row (MRMS + PHWA + PHKM)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                HawaiiDopplerLayer.values().forEach { layer ->
                    val isSelected = dopplerEnabled && activeDopplerLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDopplerLayer(layer) },
                        label = {
                            Text(
                                text = layer.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VioletVector,
                            selectedLabelColor = Slate950,
                            containerColor = Slate850,
                            labelColor = Slate200
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = VioletVector.copy(alpha = 0.5f),
                            selectedBorderColor = VioletVector
                        ),
                        modifier = Modifier.testTag("doppler_layer_${layer.id}")
                    )
                }
            }

            if (dopplerEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "OPACITY ${(dopplerOpacity * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletVector,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Slider(
                        value = dopplerOpacity,
                        onValueChange = onChangeDopplerOpacity,
                        valueRange = 0.25f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = VioletVector,
                            activeTrackColor = VioletVector,
                            inactiveTrackColor = Slate800
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .testTag("doppler_opacity_slider")
                    )
                }

                // Doppler Color Scale Bar (dBZ vs Wind Velocity)
                DopplerScaleStrip(isVelocity = activeDopplerLayer.isVelocityProduct)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeDopplerLayer.operationalNote,
                    fontSize = 11.sp,
                    color = Slate200
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item { MapLegendBadge("R-3103", AmberRadar) }
                item { MapLegendBadge("PHTO 4.3NM", BlueSat) }
                item { MapLegendBadge("TFRs", CrimsonThreat) }
                item { MapLegendBadge("NEXRAD", VioletVector) }
                item { MapLegendBadge("Orchidland", EmeraldVerified) }
                item { MapLegendBadge("Live Cams", AmberRadar) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Site Chips cleanly chunked into rows of 3 (9 sites including PHWA & PHKM)
            sites.chunked(3).forEachIndexed { rowIdx, rowSites ->
                if (rowIdx > 0) Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowSites.forEach { site ->
                        SiteQuickChip(site, selectedSite.id == site.id, Modifier.weight(1f)) { onSelectSite(site) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Site Detail Callout
            val distNm = HawaiiAirspaceReference.distanceNauticalMiles(
                HawaiiAirspaceReference.ORCHIDLAND_LAT,
                HawaiiAirspaceReference.ORCHIDLAND_LON,
                selectedSite.lat,
                selectedSite.lon
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("selected_airspace_site_box")
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedSite.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f NM to Orchidland", distNm),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldVerified,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Text(
                        text = String.format(Locale.US, "%s • %.4f°, %.4f°", selectedSite.type, selectedSite.lat, selectedSite.lon),
                        fontSize = 10.sp,
                        color = AmberRadar,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = selectedSite.airspaceNote, fontSize = 12.sp, color = Slate200)
                    Spacer(modifier = Modifier.height(6.dp))
                    AudioSelectButton(
                        sourceId = "airspace_site",
                        title = selectedSite.name,
                        text = "${selectedSite.name}, ${selectedSite.type}, ${String.format(Locale.US, "%.1f", distNm)} nautical miles from Orchidland. ${selectedSite.airspaceNote}",
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun AirspaceMapControlSquare(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.canvasBackground.copy(alpha = 0.90f))
            .border(1.5.dp, accentColor, RoundedCornerShape(8.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DopplerScaleStrip(isVelocity: Boolean) {
    val stops = if (isVelocity) {
        listOf(
            Color(0xFF00E676) to "-60kt In",
            Color(0xFF81C784) to "-20kt",
            Color(0xFF64748B) to "0",
            Color(0xFFFF8A65) to "+20kt",
            Color(0xFFEF4444) to "+60kt Out"
        )
    } else {
        listOf(
            Color(0xFF38BDF8) to "15 dBZ",
            Color(0xFF22C55E) to "30 dBZ",
            Color(0xFFFACC15) to "45 dBZ",
            Color(0xFFEF4444) to "55 dBZ",
            Color(0xFFD946EF) to "65+ dBZ"
        )
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doppler_color_scale")
    ) {
        stops.forEach { (color, lbl) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Text(
                    text = lbl,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate200,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
        }
    }
}

@Composable
private fun SiteQuickChip(
    site: HawaiiAirspaceSite,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalMapPalette.current
    val accentColor = when (site.id) {
        "site_r3103", "site_phsf" -> palette.warningAmber
        "site_hvo" -> palette.criticalRed
        "site_orchidland" -> palette.normalGreen
        "site_phwa", "site_phkm" -> palette.classAColor
        else -> palette.advisoryBlue
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accentColor else palette.canvasBackground)
            .border(1.5.dp, if (selected) accentColor else accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 6.dp)
            .testTag("airspace_site_chip_${site.id}")
    ) {
        Text(
            text = site.shortCode,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) contrastContentColor(accentColor) else palette.textPrimary,
            fontFamily = JetBrainsMonoFontFamily,
            maxLines = 1
        )
    }
}

@Composable
private fun AirspaceKpiPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Text(text = label, fontSize = 9.sp, color = Slate300, fontFamily = JetBrainsMonoFontFamily)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = JetBrainsMonoFontFamily)
    }
}

@Composable
private fun MapLegendBadge(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Slate200)
    }
}

@Composable
private fun StatusBannerCard(
    badgeText: String,
    badgeColor: Color,
    message: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(badgeColor.copy(alpha = 0.65f), DarkBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate950
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = message, fontSize = 12.sp, color = Slate100)
        }
    }
}

@Composable
private fun AirspaceErrorCard(
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CrimsonThreat.copy(alpha = 0.7f), DarkBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(Icons.Default.CloudOff, contentDescription = null, tint = CrimsonThreat, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate100)
                Text(text = message, fontSize = 11.sp, color = Slate300)
            }
            ColorCodedActionButton(
                label = "Retry",
                accentColor = CyanNeon,
                filled = true,
                onClick = onRetry
            )
        }
    }
}

@Composable
private fun PtaAdvisoryIntakeDialog(
    onDismiss: () -> Unit,
    onParseAndSave: (String, String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("usag.hawaii.comrel@army.mil") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Parse PTA / Military Advisory",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Advisory Subject", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sender,
                    onValueChange = { sender = it },
                    label = { Text("Sender / Source", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Bulletin Text (live-fire, convoy, R-3103, Saddle Rd, UAS...)", fontSize = 11.sp) },
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                AudioTextBoxStrip(
                    sourceId = "pta_advisory_body_input",
                    title = "PTA Bulletin Text",
                    textToRead = body
                )
            }
        },
        confirmButton = {
            ColorCodedActionButton(
                label = "Extract & Log",
                accentColor = CyanNeon,
                filled = true,
                enabled = body.isNotBlank() || subject.isNotBlank(),
                onClick = { onParseAndSave(subject, sender, body) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate200, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
