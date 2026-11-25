package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.model.MapSplitMode
import com.example.model.MapVisualThemeMode
import com.example.network.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.FullScreenMapDomain
import com.example.ui.NasaFeedLoadState
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.ColorCodedActionButton
import com.example.ui.components.ForensicTopBar
import com.example.ui.components.MapOverlayOptionChip
import com.example.ui.components.detectMapMultitouchGestures
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun NasaSatFeedScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    var showCustomCoordDialog by remember { mutableStateOf(false) }

    val target = uiState.customGibsTarget
    val snapshotUrl = remember(
        target.latitude,
        target.longitude,
        target.dateIso,
        uiState.activeGibsLayer,
        uiState.gibsSpanDegrees,
        uiState.gibsRefreshNonce
    ) {
        NasaGibsUrlBuilder.buildSnapshotUrl(
            latitude = target.latitude,
            longitude = target.longitude,
            dateIso = target.dateIso,
            layer = uiState.activeGibsLayer,
            spanDegrees = uiState.gibsSpanDegrees,
            cacheBustNonce = uiState.gibsRefreshNonce
        )
    }

    val palette = LocalMapPalette.current

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "NASA Sat Feed",
                subtitle = "EOSDIS EONET v3 Telemetry & Worldview GIBS Imagery"
            )
        },
        containerColor = palette.canvasBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            userScrollEnabled = !uiState.satScrollLocked,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. LIVE NASA GIBS SATELLITE IMAGERY VIEWPORT WITH ZOOM, PAN & OVERLAYS
            item {
                GibsImageryCard(
                    targetLabel = target.label,
                    latitude = target.latitude,
                    longitude = target.longitude,
                    dateIso = target.dateIso,
                    snapshotUrl = snapshotUrl,
                    activeLayer = uiState.activeGibsLayer,
                    spanDegrees = uiState.gibsSpanDegrees,
                    visualZoom = uiState.satVisualZoom,
                    scrollLocked = uiState.satScrollLocked,
                    showReticle = uiState.satShowReticle,
                    showCoordGrid = uiState.satShowCoordGrid,
                    showHud = uiState.satShowHud,
                    selectedEvent = uiState.selectedNasaEvent,
                    onSelectLayer = { viewModel.selectGibsLayer(it) },
                    onSelectSpan = { viewModel.setGibsSpanDegrees(it) },
                    onVisualZoomChange = { viewModel.setSatVisualZoom(it) },
                    onToggleScrollLock = { viewModel.toggleSatScrollLocked() },
                    onToggleReticle = { viewModel.toggleSatReticle() },
                    onToggleCoordGrid = { viewModel.toggleSatCoordGrid() },
                    onToggleHud = { viewModel.toggleSatHud() },
                    onReloadTile = { viewModel.reloadGibsTile() },
                    onShiftDate = { viewModel.shiftGibsDateDays(it) },
                    onOpenCustomCoords = { showCustomCoordDialog = true },
                    onFocusOrchidland = { viewModel.focusOrchidlandInSatFeed() },
                    onOpenHawaiiCams = { viewModel.openAirspaceTab() },
                    onIngestCase = { viewModel.ingestActiveGibsTargetToCasebook() },
                    onSendToAiIntel = { viewModel.sendSatTargetToAiIntel() },
                    onOpenFullScreen = { viewModel.openFullScreenMap(FullScreenMapDomain.SATELLITE) },
                    onOpenSplitSatAirspace = {
                        viewModel.setMapSplitMode(MapSplitMode.SAT_AIRSPACE)
                        viewModel.openFullScreenMap(FullScreenMapDomain.SATELLITE)
                    },
                    visualThemeMode = uiState.mapVisualThemeMode,
                    onCycleVisualTheme = { viewModel.cycleMapVisualThemeMode() }
                )
            }

            // 2. EONET FEED HEADER & COLOR-CODED CONTROLS
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE NASA EONET v3 EVENT STREAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = CyanNeon,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Real-time Earth Observatory satellite anomalies",
                            fontSize = 12.sp,
                            color = Slate300
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.nasaIncludeClosed,
                            onClick = { viewModel.toggleNasaIncludeClosed(!uiState.nasaIncludeClosed) },
                            label = {
                                Text(
                                    text = if (uiState.nasaIncludeClosed) "All Events" else "Active Only",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberRadar,
                                selectedLabelColor = Slate950,
                                containerColor = Slate850,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.nasaIncludeClosed,
                                borderColor = AmberRadar.copy(alpha = 0.6f),
                                selectedBorderColor = AmberRadar
                            ),
                            modifier = Modifier.testTag("nasa_status_toggle")
                        )
                        IconButton(
                            onClick = { viewModel.refreshNasaFeed() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanNeon.copy(alpha = 0.18f))
                                .border(1.5.dp, CyanNeon, RoundedCornerShape(10.dp))
                                .testTag("nasa_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh NASA Feed",
                                tint = CyanNeon
                            )
                        }
                    }
                }
            }

            // 3. SEARCH BAR & HIGH-CONTRAST CATEGORY FILTERS
            item {
                OutlinedTextField(
                    value = uiState.nasaSearchQuery,
                    onValueChange = { viewModel.updateNasaSearchQuery(it) },
                    placeholder = {
                        Text(
                            "Filter EONET events by region, storm, wildfire, volcano...",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.SatelliteAlt,
                            contentDescription = "Filter events",
                            tint = CyanNeon
                        )
                    },
                    trailingIcon = {
                        if (uiState.nasaSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateNasaSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate200)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nasa_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NasaEonetCategory.values()) { category ->
                        val isSelected = uiState.nasaSelectedCategory == category
                        val catAccent = when (category) {
                            NasaEonetCategory.WILDFIRES -> CrimsonThreat
                            NasaEonetCategory.SEVERE_STORMS -> BlueSat
                            NasaEonetCategory.VOLCANOES -> AmberRadar
                            NasaEonetCategory.FLOODS -> CyanNeon
                            NasaEonetCategory.DUST_HAZE -> VioletVector
                            else -> EmeraldVerified
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectNasaCategory(category) },
                            label = {
                                Text(
                                    text = category.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catAccent,
                                selectedLabelColor = Slate950,
                                containerColor = Slate850,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = catAccent.copy(alpha = 0.45f),
                                selectedBorderColor = catAccent
                            ),
                            modifier = Modifier.testTag("nasa_cat_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // 4. FEED STATE (LOADING / ERROR / SUCCESS LIST)
            when (val feedState = uiState.nasaFeedState) {
                is NasaFeedLoadState.Loading -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                CircularProgressIndicator(color = CyanNeon, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Querying NASA GSFC EONET v3 Telemetry...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Text(
                                    text = "eonet.gsfc.nasa.gov/api/v3/events",
                                    fontSize = 11.sp,
                                    color = CyanNeon,
                                    fontFamily = JetBrainsMonoFontFamily
                                )
                            }
                        }
                    }
                }

                is NasaFeedLoadState.Error -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate850),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(CrimsonThreat, DarkBorder))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = CrimsonThreat,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "NASA EONET Telemetry Unavailable",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate100
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = feedState.message,
                                    fontSize = 12.sp,
                                    color = Slate300
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                ColorCodedActionButton(
                                    label = "Retry Connection",
                                    accentColor = CyanNeon,
                                    icon = Icons.Default.Refresh,
                                    filled = true,
                                    onClick = { viewModel.refreshNasaFeed() }
                                )
                            }
                        }
                    }
                }

                is NasaFeedLoadState.Success -> {
                    val filteredEvents = feedState.events.filter { event ->
                        uiState.nasaSearchQuery.isBlank() ||
                            event.title.contains(uiState.nasaSearchQuery, ignoreCase = true) ||
                            event.categoryTitle.contains(uiState.nasaSearchQuery, ignoreCase = true) ||
                            event.sourceId.contains(uiState.nasaSearchQuery, ignoreCase = true)
                    }

                    if (filteredEvents.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Icon(Icons.Outlined.SatelliteAlt, contentDescription = null, tint = AmberRadar, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No matching NASA satellite events for current filter.",
                                        fontSize = 13.sp,
                                        color = Slate200
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredEvents, key = { it.id }) { event ->
                            val isSelected = uiState.selectedNasaEvent?.id == event.id
                            NasaEventCard(
                                event = event,
                                isSelected = isSelected,
                                onSelect = { viewModel.selectNasaEvent(event) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomCoordDialog) {
        CustomGibsTargetDialog(
            initialLat = target.latitude,
            initialLon = target.longitude,
            initialDate = target.dateIso,
            initialLabel = target.label,
            onDismiss = { showCustomCoordDialog = false },
            onConfirm = { lat, lon, date, label ->
                viewModel.setCustomGibsTarget(lat, lon, date, label)
                showCustomCoordDialog = false
            }
        )
    }
}

@Composable
private fun GibsImageryCard(
    targetLabel: String,
    latitude: Double,
    longitude: Double,
    dateIso: String,
    snapshotUrl: String,
    activeLayer: NasaGibsLayer,
    spanDegrees: Double,
    visualZoom: Float,
    scrollLocked: Boolean,
    showReticle: Boolean,
    showCoordGrid: Boolean,
    showHud: Boolean,
    selectedEvent: NasaSatEvent?,
    onSelectLayer: (NasaGibsLayer) -> Unit,
    onSelectSpan: (Double) -> Unit,
    onVisualZoomChange: (Float) -> Unit,
    onToggleScrollLock: () -> Unit,
    onToggleReticle: () -> Unit,
    onToggleCoordGrid: () -> Unit,
    onToggleHud: () -> Unit,
    onReloadTile: () -> Unit,
    onShiftDate: (Int) -> Unit,
    onOpenCustomCoords: () -> Unit,
    onFocusOrchidland: () -> Unit,
    onOpenHawaiiCams: () -> Unit,
    onIngestCase: () -> Unit,
    onSendToAiIntel: () -> Unit,
    onOpenFullScreen: () -> Unit,
    onOpenSplitSatAirspace: () -> Unit,
    visualThemeMode: MapVisualThemeMode,
    onCycleVisualTheme: () -> Unit
) {
    val context = LocalContext.current
    var panOffset by remember(targetLabel, latitude, longitude, dateIso, spanDegrees) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var autoStepCount by remember(targetLabel, latitude, longitude, activeLayer.id) { mutableStateOf(0) }

    val currentVisualZoom by rememberUpdatedState(visualZoom)
    val currentScrollLocked by rememberUpdatedState(scrollLocked)
    val currentOnVisualZoomChange by rememberUpdatedState(onVisualZoomChange)
    val currentViewportSize by rememberUpdatedState(viewportSize)

    LaunchedEffect(visualZoom, viewportSize) {
        if (visualZoom <= 1.02f) {
            panOffset = Offset.Zero
        } else {
            val maxPanX = ((viewportSize.width * (visualZoom - 1f)) / 2f).coerceAtLeast(0f)
            val maxPanY = ((viewportSize.height * (visualZoom - 1f)) / 2f).coerceAtLeast(0f)
            panOffset = Offset(
                x = panOffset.x.coerceIn(-maxPanX, maxPanX),
                y = panOffset.y.coerceIn(-maxPanY, maxPanY)
            )
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.7f), DarkBorder))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gibs_imagery_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldVerified)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NASA WORLDVIEW GIBS • ZOOM VIEWPORT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = CyanNeon,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = targetLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldVerified)
                            .clickable(onClick = onFocusOrchidland)
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("gibs_orchidland_preset_btn")
                    ) {
                        Text(
                            text = "Puna HI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate950
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberRadar.copy(alpha = 0.22f))
                            .border(1.5.dp, AmberRadar, RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenHawaiiCams)
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                            .testTag("sat_open_hawaii_cams_btn")
                    ) {
                        Text(
                            text = "HI Cams",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar
                        )
                    }

                    IconButton(
                        onClick = onOpenCustomCoords,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanNeon.copy(alpha = 0.20f))
                            .border(1.5.dp, CyanNeon, RoundedCornerShape(8.dp))
                            .testTag("gibs_custom_coords_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Set Custom Coordinates",
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Satellite Tile Viewport with Optical Zoom, Drag Pan, Grid & Reticle Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate950)
                    .border(1.5.dp, if (scrollLocked) VioletVector else DarkBorder, RoundedCornerShape(10.dp))
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(scrollLocked) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (currentVisualZoom > 1.2f) {
                                    currentOnVisualZoomChange(1.0f)
                                    panOffset = Offset.Zero
                                } else {
                                    currentOnVisualZoomChange(2.5f)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectMapMultitouchGestures(
                            enableTilt = false,
                            allowSingleFingerPan = { currentScrollLocked || currentVisualZoom > 1.02f }
                        ) { zoomChange, pan, _, _ ->
                            val updatedZoom = (currentVisualZoom * zoomChange).coerceIn(1.0f, 5.0f)
                            currentOnVisualZoomChange(updatedZoom)
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
                            scaleX = visualZoom
                            scaleY = visualZoom
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                ) {
                    SubcomposeAsyncImage(
                        model = buildFeedImageRequest(
                            context = context,
                            url = snapshotUrl,
                            referer = "https://worldview.earthdata.nasa.gov/"
                        ),
                        contentDescription = "NASA GIBS satellite imagery for $targetLabel",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gibs_satellite_image")
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = CyanNeon, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Fetching ${activeLayer.shortLabel} (${spanDegrees}° bbox)...",
                                            fontSize = 11.sp,
                                            color = Slate200,
                                            fontFamily = JetBrainsMonoFontFamily
                                        )
                                    }
                                }
                            }

                            is AsyncImagePainter.State.Error -> {
                                LaunchedEffect(snapshotUrl) {
                                    if (autoStepCount < 2 && NasaGibsUrlBuilder.isRecentCompositeWindow(dateIso)) {
                                        autoStepCount += 1
                                        onShiftDate(-1)
                                    }
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.Satellite, contentDescription = null, tint = AmberRadar, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "GIBS Snapshot Unavailable for $dateIso",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate100
                                        )
                                        Text(
                                            text = "Daily VIIRS/MODIS swath may still be compositing.",
                                            fontSize = 11.sp,
                                            color = Slate300
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { onShiftDate(-1) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                border = BorderStroke(1.dp, AmberRadar),
                                                modifier = Modifier.heightIn(min = 36.dp)
                                            ) {
                                                Text("-1d Overpass", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberRadar)
                                            }
                                            if (activeLayer != NasaGibsLayer.VIIRS_TRUE_COLOR) {
                                                OutlinedButton(
                                                    onClick = { onSelectLayer(NasaGibsLayer.VIIRS_TRUE_COLOR) },
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    border = BorderStroke(1.dp, CyanNeon),
                                                    modifier = Modifier.heightIn(min = 36.dp)
                                                ) {
                                                    Text("VIIRS RGB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                                                }
                                            }
                                            OutlinedButton(
                                                onClick = onReloadTile,
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                border = BorderStroke(1.dp, EmeraldVerified),
                                                modifier = Modifier.heightIn(min = 36.dp)
                                            ) {
                                                Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldVerified)
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }

                    if (showCoordGrid || showReticle) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            if (showCoordGrid) {
                                for (i in 1..3) {
                                    val gx = w * (i / 4f)
                                    val gy = h * (i / 4f)
                                    drawLine(EmeraldVerified.copy(alpha = 0.45f), Offset(gx, 0f), Offset(gx, h), 1.3f)
                                    drawLine(EmeraldVerified.copy(alpha = 0.45f), Offset(0f, gy), Offset(w, gy), 1.3f)
                                }
                            }
                            if (showReticle) {
                                val cx = w / 2f
                                val cy = h / 2f
                                val r = 20f
                                drawCircle(
                                    color = CyanNeon.copy(alpha = 0.9f),
                                    radius = r,
                                    center = Offset(cx, cy),
                                    style = Stroke(width = 2.2f)
                                )
                                drawLine(CyanNeon, Offset(cx - r - 10f, cy), Offset(cx - 5f, cy), strokeWidth = 2.2f)
                                drawLine(CyanNeon, Offset(cx + 5f, cy), Offset(cx + r + 10f, cy), strokeWidth = 2.2f)
                                drawLine(CyanNeon, Offset(cx, cy - r - 10f), Offset(cx, cy - 5f), strokeWidth = 2.2f)
                                drawLine(CyanNeon, Offset(cx, cy + 5f), Offset(cx, cy + r + 10f), strokeWidth = 2.2f)
                            }
                        }
                    }
                }

                // Top-left spectral & zoom status badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate950.copy(alpha = 0.88f))
                            .border(1.dp, CyanNeon.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeLayer.instrument,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate950.copy(alpha = 0.88f))
                            .border(1.dp, AmberRadar.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("sat_zoom_readout")
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1fx • %.2f°", visualZoom, spanDegrees),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }

                // Floating Right-Side Optical Zoom Controls (+ / - / 1x)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    SatZoomSquareButton(
                        icon = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen Satellite Map",
                        accentColor = EmeraldVerified,
                        onClick = onOpenFullScreen,
                        modifier = Modifier.testTag("sat_fullscreen_btn")
                    )
                    SatZoomSquareButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In Satellite Feed",
                        accentColor = CyanNeon,
                        onClick = { onVisualZoomChange((visualZoom + 0.5f).coerceAtMost(5.0f)) },
                        modifier = Modifier.testTag("sat_zoom_in_btn")
                    )
                    SatZoomSquareButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out Satellite Feed",
                        accentColor = CyanNeon,
                        onClick = { onVisualZoomChange((visualZoom - 0.5f).coerceAtLeast(1.0f)) },
                        modifier = Modifier.testTag("sat_zoom_out_btn")
                    )
                    if (visualZoom > 1.05f || panOffset != Offset.Zero) {
                        SatZoomSquareButton(
                            icon = Icons.Default.CenterFocusStrong,
                            contentDescription = "Reset Satellite Zoom",
                            accentColor = AmberRadar,
                            onClick = {
                                onVisualZoomChange(1.0f)
                                panOffset = Offset.Zero
                            },
                            modifier = Modifier.testTag("sat_zoom_reset_btn")
                        )
                    }
                }

                // Bottom HUD telemetry bar
                if (showHud) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Slate950.copy(alpha = 0.90f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude),
                            fontSize = 11.sp,
                            color = AmberRadar,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "UTC: $dateIso • ${if (scrollLocked) "PAN LOCKED" else "PINCH / + TO ZOOM"}",
                            fontSize = 10.sp,
                            color = Slate200,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map Scroll Lock & Sat Overlay Options Strip
            Text(
                text = "SAT MAP SCROLL & OVERLAY OPTIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = Slate300,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sat_overlay_options_row")
            ) {
                item {
                    MapOverlayOptionChip(
                        label = if (scrollLocked) "Map Pan ON" else "Page Scroll",
                        active = scrollLocked,
                        accentColor = VioletVector,
                        icon = if (scrollLocked) Icons.Default.Lock else Icons.Default.PanTool,
                        onClick = onToggleScrollLock,
                        modifier = Modifier.testTag("sat_scroll_lock_btn")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = "Reticle",
                        active = showReticle,
                        accentColor = CyanNeon,
                        icon = Icons.Default.CenterFocusWeak,
                        onClick = onToggleReticle,
                        modifier = Modifier.testTag("sat_overlay_reticle")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = "Lat/Lon Grid",
                        active = showCoordGrid,
                        accentColor = EmeraldVerified,
                        icon = Icons.Default.GridOn,
                        onClick = onToggleCoordGrid,
                        modifier = Modifier.testTag("sat_overlay_grid")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = "HUD Bar",
                        active = showHud,
                        accentColor = AmberRadar,
                        icon = Icons.Default.Subtitles,
                        onClick = onToggleHud,
                        modifier = Modifier.testTag("sat_overlay_hud")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = "Reload Tile",
                        active = false,
                        accentColor = BlueSat,
                        icon = Icons.Default.Refresh,
                        onClick = onReloadTile,
                        modifier = Modifier.testTag("sat_reload_tile_btn")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = "Split Sat+Air",
                        active = false,
                        accentColor = EmeraldVerified,
                        icon = Icons.Default.Splitscreen,
                        onClick = onOpenSplitSatAirspace,
                        modifier = Modifier.testTag("sat_split_airspace_btn")
                    )
                }
                item {
                    MapOverlayOptionChip(
                        label = visualThemeMode.shortLabel,
                        active = visualThemeMode != MapVisualThemeMode.NIGHT,
                        accentColor = AmberRadar,
                        icon = Icons.Default.Contrast,
                        onClick = onCycleVisualTheme,
                        modifier = Modifier.testTag("sat_visual_mode_btn")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Spectral Layer Picker (High-Contrast Color-Coded Chips)
            Text(
                text = "SPECTRAL BANDS & SATELLITE INSTRUMENTS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = Slate300,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(NasaGibsLayer.values()) { layer ->
                    val isSelected = activeLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectLayer(layer) },
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
                            borderColor = CyanNeon.copy(alpha = 0.45f),
                            selectedBorderColor = CyanNeon
                        ),
                        modifier = Modifier.testTag("gibs_layer_${layer.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Temporal Step Row + Fine Spatial BBOX Span Zoom Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(-7 to "-7d", -1 to "-1d", 1 to "+1d").forEach { (delta, lbl) ->
                    OutlinedButton(
                        onClick = { onShiftDate(delta) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BlueSat.copy(alpha = 0.16f),
                            contentColor = Slate100
                        ),
                        border = BorderStroke(1.2.dp, BlueSat),
                        modifier = Modifier.heightIn(min = 38.dp)
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "TILE BBOX:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberRadar,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(listOf(0.25, 0.5, 1.0, 1.5, 3.0, 6.0, 12.0)) { span ->
                    val active = spanDegrees == span
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) AmberRadar else Slate850)
                            .border(1.5.dp, if (active) AmberRadar else AmberRadar.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .clickable { onSelectSpan(span) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("sat_span_${span}")
                    ) {
                        Text(
                            text = "${span}°",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Slate950 else Slate100,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activeLayer.forensicUse,
                fontSize = 12.sp,
                color = Slate300
            )
            Spacer(modifier = Modifier.height(8.dp))
            AudioSelectButton(
                sourceId = "sat_gibs_brief",
                title = "NASA GIBS Telemetry Brief",
                text = "Target $targetLabel at latitude ${String.format(Locale.US, "%.4f", latitude)}, longitude ${String.format(Locale.US, "%.4f", longitude)} on $dateIso. Active layer ${activeLayer.shortLabel}, span $spanDegrees degrees. ${activeLayer.forensicUse}",
                compact = true
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ColorCodedActionButton(
                    label = if (selectedEvent != null) "Save / Update Case" else "Log Sector Case",
                    accentColor = CyanNeon,
                    icon = Icons.Default.AddTask,
                    filled = true,
                    onClick = onIngestCase,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ingest_nasa_event_btn")
                )
                ColorCodedActionButton(
                    label = "Send to AI Intel",
                    accentColor = VioletVector,
                    icon = Icons.Default.AutoAwesome,
                    filled = false,
                    onClick = onSendToAiIntel,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sat_send_to_intel_btn")
                )
            }
        }
    }
}

@Composable
private fun SatZoomSquareButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Slate950.copy(alpha = 0.88f))
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
private fun NasaEventCard(
    event: NasaSatEvent,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val categoryColor = when (event.categoryId) {
        "wildfires" -> CrimsonThreat
        "severeStorms" -> BlueSat
        "volcanoes" -> AmberRadar
        "floods" -> CyanNeon
        else -> EmeraldVerified
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Slate850 else DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isSelected) listOf(CyanNeon, CyanNeon.copy(alpha = 0.6f))
                else listOf(DarkBorder, Slate800)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("nasa_event_${event.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(categoryColor.copy(alpha = 0.22f))
                        .border(1.dp, categoryColor.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.categoryTitle.uppercase(Locale.US),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Text(
                    text = "${event.id} • ${event.sourceId}",
                    fontSize = 10.sp,
                    color = Slate300,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Slate100
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AmberRadar,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.4f°, %.4f°", event.latitude, event.longitude),
                        fontSize = 11.sp,
                        color = Slate200,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }

                Text(
                    text = event.latestDateIso,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }

            if (event.magnitudeLabel != null || event.trackPointCount > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.magnitudeLabel != null) {
                        Text(
                            text = "Intensity: ${event.magnitudeLabel}",
                            fontSize = 11.sp,
                            color = AmberRadar,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (event.trackPointCount > 1) {
                        Text(
                            text = "${event.trackPointCount} track points",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AudioSelectButton(
                sourceId = "eonet_${event.id}",
                title = event.title,
                text = "${event.categoryTitle} event ${event.id} from ${event.sourceId}: ${event.title}, dated ${event.latestDateIso}, coordinates ${String.format(Locale.US, "%.4f", event.latitude)} by ${String.format(Locale.US, "%.4f", event.longitude)}. ${event.description}",
                compact = true
            )
        }
    }
}

@Composable
private fun CustomGibsTargetDialog(
    initialLat: Double,
    initialLon: Double,
    initialDate: String,
    initialLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String, String) -> Unit
) {
    var label by remember { mutableStateOf(initialLabel) }
    var latStr by remember { mutableStateOf(String.format(Locale.US, "%.4f", initialLat)) }
    var lonStr by remember { mutableStateOf(String.format(Locale.US, "%.4f", initialLon)) }
    var dateStr by remember { mutableStateOf(initialDate) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Target NASA GIBS Coordinates",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        validationError = null
                    },
                    label = { Text("Sector Label", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = latStr,
                        onValueChange = {
                            latStr = it
                            validationError = null
                        },
                        label = { Text("Latitude", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lonStr,
                        onValueChange = {
                            lonStr = it
                            validationError = null
                        },
                        label = { Text("Longitude", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {
                        dateStr = it
                        validationError = null
                    },
                    label = { Text("UTC Date (YYYY-MM-DD)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                validationError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonThreat
                    )
                }
            }
        },
        confirmButton = {
            ColorCodedActionButton(
                label = "Load Satellite Tile",
                accentColor = CyanNeon,
                filled = true,
                onClick = {
                    val lat = latStr.trim().toDoubleOrNull()
                    val lon = lonStr.trim().toDoubleOrNull()
                    val trimmedDate = dateStr.trim()
                    val isValidDate = Regex("\\d{4}-\\d{2}-\\d{2}").matches(trimmedDate) && runCatching {
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            isLenient = false
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.parse(trimmedDate) != null
                    }.getOrDefault(false)

                    when {
                        lat == null || !lat.isFinite() || lat !in -90.0..90.0 -> {
                            validationError = "Latitude must be a valid number between -90 and 90."
                        }
                        lon == null || !lon.isFinite() || lon !in -180.0..180.0 -> {
                            validationError = "Longitude must be a valid number between -180 and 180."
                        }
                        !isValidDate -> {
                            validationError = "UTC Date must be a valid calendar date in YYYY-MM-DD format."
                        }
                        else -> {
                            onConfirm(lat, lon, trimmedDate, label.trim())
                        }
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate200, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
