package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.network.*
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.ColorCodedActionButton
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HawaiiLiveCamsSection(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    val snap = uiState.webcamsSnapshot
    val isLoading = uiState.webcamsLoadState == WebcamsLoadState.Loading
    val filteredCams = remember(snap, uiState.selectedWebcamFilter) {
        snap.filtered(uiState.selectedWebcamFilter)
    }
    var expandedGrid by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hawaii_webcams_section")
    ) {
        // Section Header + Refresh Cams Button
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE HAWAIʻI STILL CAMS (FAA • USGS • UH)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = JetBrainsMonoFontFamily
                )
                Text(
                    text = "WeatherCams, HVO Volcano & Mauna Kea Stills",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100
                )
                Text(
                    text = if (isLoading) {
                        "Polling FAA WeatherCams HI stations..."
                    } else {
                        "Updated: ${snap.updatedAtHst} • ${snap.allCamsSorted.size} active stills"
                    },
                    fontSize = 11.sp,
                    color = if (isLoading) AmberRadar else Slate300,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.refreshHawaiiWebcams() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberRadar,
                    contentColor = Slate950
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .testTag("webcams_refresh_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Slate950,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Refresh Cams", modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(5.dp))
                Text("Refresh Cams", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Agency KPI Summary Strip
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CamAgencyStatBadge(
                label = "FAA HI",
                count = "${snap.faaCams.size}",
                sub = "${snap.faaSiteCount} sites",
                accent = BlueSat,
                modifier = Modifier.weight(1f)
            )
            CamAgencyStatBadge(
                label = "USGS HVO",
                count = "${snap.usgsCams.size}",
                sub = "Kīlauea/ML",
                accent = AmberRadar,
                modifier = Modifier.weight(1f)
            )
            CamAgencyStatBadge(
                label = "UH SYSTEM",
                count = "${snap.uhCams.size}",
                sub = "IfA/MKWC",
                accent = EmeraldVerified,
                modifier = Modifier.weight(1f)
            )
        }

        // Source Filter Chips + Carousel/Grid Layout Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            ) {
                WebcamFilter.values().forEach { filter ->
                    val isSel = uiState.selectedWebcamFilter == filter
                    val chipAccent = when (filter) {
                        WebcamFilter.ALL -> CyanNeon
                        WebcamFilter.FAA -> BlueSat
                        WebcamFilter.USGS -> AmberRadar
                        WebcamFilter.UH -> EmeraldVerified
                    }
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.selectWebcamFilter(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipAccent,
                            selectedLabelColor = Slate950,
                            containerColor = Slate850,
                            labelColor = Slate200
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = chipAccent.copy(alpha = 0.5f),
                            selectedBorderColor = chipAccent
                        ),
                        modifier = Modifier.testTag("cam_filter_${filter.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { expandedGrid = !expandedGrid },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .border(1.2.dp, CyanNeon.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .testTag("webcams_layout_toggle_btn")
            ) {
                Icon(
                    imageVector = if (expandedGrid) Icons.Default.ViewCarousel else Icons.Default.GridView,
                    contentDescription = if (expandedGrid) "Carousel View" else "Grid View",
                    tint = CyanNeon,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Non-blocking FAA API Warning Banner (still allows USGS & UH stills below)
        if (snap.faaError != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AmberRadar.copy(alpha = 0.75f), DarkBorder))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AmberRadar, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FAA WeatherCams Notice (USGS & UH Active)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar
                        )
                        Text(text = snap.faaError, fontSize = 11.sp, color = Slate200)
                    }
                    TextButton(onClick = { viewModel.refreshHawaiiWebcams() }) {
                        Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                    }
                }
            }
        }

        // Still Feed Gallery (Horizontal Carousel or Expandable 2-Column Grid)
        if (filteredCams.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Outlined.VideocamOff, contentDescription = null, tint = AmberRadar)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isLoading) "Loading FAA WeatherCams HI stations..." else "No stations in selected camera filter.",
                        fontSize = 12.sp,
                        color = Slate200
                    )
                }
            }
        } else if (expandedGrid) {
            filteredCams.chunked(2).forEach { rowCams ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowCams.forEach { cam ->
                        WebcamStillCard(
                            cam = cam,
                            refreshNonce = uiState.webcamRefreshNonce,
                            compact = true,
                            onInspect = { viewModel.selectWebcamStill(cam) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCams.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("webcams_carousel_row")
            ) {
                items(filteredCams, key = { it.id }) { cam ->
                    WebcamStillCard(
                        cam = cam,
                        refreshNonce = uiState.webcamRefreshNonce,
                        compact = false,
                        onInspect = { viewModel.selectWebcamStill(cam) },
                        modifier = Modifier.width(268.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WebcamStillCard(
    cam: HawaiiWebcamStill,
    refreshNonce: Long,
    compact: Boolean,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var localRetryNonce by remember(cam.id) { mutableStateOf(0L) }
    val effectiveNonce = maxOf(refreshNonce, localRetryNonce)
    val stillUrl = remember(cam.imageUrl, effectiveNonce) {
        cam.buildCacheBustedUrl(effectiveNonce)
    }

    val agencyAccent = when (cam.source) {
        WebcamFeedSource.FAA_WEATHERCAM -> BlueSat
        WebcamFeedSource.USGS_VOLCANO -> if (cam.isThermal) CrimsonThreat else AmberRadar
        WebcamFeedSource.UH_SYSTEM -> EmeraldVerified
    }
    val distNm = remember(cam.id) { cam.distanceToOrchidlandNm() }

    Card(
        onClick = onInspect,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(agencyAccent.copy(alpha = 0.72f), DarkBorder))
        ),
        modifier = modifier.testTag("webcam_card_${cam.id}")
    ) {
        Column {
            // Still Frame Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 118.dp else 154.dp)
                    .background(Slate950)
            ) {
                SubcomposeAsyncImage(
                    model = buildFeedImageRequest(
                        context = context,
                        url = stillUrl,
                        referer = cam.refererUrl
                    ),
                    contentDescription = "${cam.title} live camera still",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = agencyAccent,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Fetching ${cam.stationCode}...",
                                        fontSize = 10.sp,
                                        color = Slate300,
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                }
                            }
                        }

                        is AsyncImagePainter.State.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Slate900)
                                    .padding(10.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BrokenImage,
                                        contentDescription = "Camera still unavailable",
                                        tint = CrimsonThreat,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Still Offline / Timeout",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate200
                                    )
                                    Text(
                                        text = "Tap card for Lightbox",
                                        fontSize = 9.sp,
                                        color = Slate400
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyanNeon.copy(alpha = 0.2f))
                                        .border(1.dp, CyanNeon, RoundedCornerShape(6.dp))
                                        .clickable { localRetryNonce = System.currentTimeMillis() }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                        .testTag("webcam_retry_${cam.id}")
                                ) {
                                    Text(
                                        text = "Retry",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanNeon
                                    )
                                }
                            }
                        }

                        else -> SubcomposeAsyncImageContent()
                    }
                }

                // Top-left agency & thermal badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(agencyAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = cam.source.badgeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = contrastContentColor(agencyAccent),
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    }
                    if (cam.isThermal) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CrimsonThreat)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "IR THERMAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = contrastContentColor(CrimsonThreat),
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                }

                // Bottom overlay: Direction + NM to Orchidland + Zoom affordance
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Slate950.copy(alpha = 0.84f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${cam.stationCode} • ${cam.direction}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100,
                        fontFamily = JetBrainsMonoFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f NM", distNm),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldVerified,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                }
            }

            // Metadata Body
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = cam.title,
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = cam.subtitle,
                    fontSize = 10.sp,
                    color = Slate300,
                    maxLines = if (compact) 2 else 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                if (!cam.captureTimestamp.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Capture: ${cam.captureTimestamp}",
                        fontSize = 9.sp,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CamAgencyStatBadge(
    label: String,
    count: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate850)
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent, fontFamily = JetBrainsMonoFontFamily)
        Text(text = count, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100, fontFamily = JetBrainsMonoFontFamily)
        Text(text = sub, fontSize = 9.sp, color = Slate300, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun WebcamLightboxDialog(
    cam: HawaiiWebcamStill,
    refreshNonce: Long,
    onReload: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var zoom by remember(cam.id) { mutableStateOf(1.0f) }
    var panOffset by remember(cam.id) { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    val effectiveUrl = remember(cam.imageUrl, refreshNonce) {
        cam.buildCacheBustedUrl(refreshNonce)
    }
    val agencyAccent = when (cam.source) {
        WebcamFeedSource.FAA_WEATHERCAM -> BlueSat
        WebcamFeedSource.USGS_VOLCANO -> if (cam.isThermal) CrimsonThreat else AmberRadar
        WebcamFeedSource.UH_SYSTEM -> EmeraldVerified
    }

    val currentZoom by rememberUpdatedState(zoom)
    val currentViewportSize by rememberUpdatedState(viewportSize)

    LaunchedEffect(zoom, viewportSize) {
        if (zoom <= 1.02f) {
            panOffset = Offset.Zero
        } else {
            val maxX = ((viewportSize.width * (zoom - 1f)) / 2f).coerceAtLeast(0f)
            val maxY = ((viewportSize.height * (zoom - 1f)) / 2f).coerceAtLeast(0f)
            panOffset = Offset(
                x = panOffset.x.coerceIn(-maxX, maxX),
                y = panOffset.y.coerceIn(-maxY, maxY)
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(agencyAccent, DarkBorder))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp)
                .testTag("webcam_lightbox_dialog")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${cam.source.fullAgencyName.uppercase(Locale.US)} • ${cam.stationCode}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = agencyAccent,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                        Text(
                            text = cam.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("webcam_lightbox_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Still", tint = Slate200)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zoomable High-Res Still Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.5.dp, agencyAccent.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .onSizeChanged { viewportSize = it }
                        .pointerInput(cam.id) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (currentZoom > 1.2f) {
                                        zoom = 1.0f
                                        panOffset = Offset.Zero
                                    } else {
                                        zoom = 2.5f
                                    }
                                }
                            )
                        }
                        .pointerInput(cam.id) {
                            detectTransformGestures { _, pan, z, _ ->
                                val nextZoom = (currentZoom * z).coerceIn(1.0f, 4.5f)
                                zoom = nextZoom
                                val maxX = ((currentViewportSize.width * (nextZoom - 1f)) / 2f).coerceAtLeast(0f)
                                val maxY = ((currentViewportSize.height * (nextZoom - 1f)) / 2f).coerceAtLeast(0f)
                                panOffset = Offset(
                                    x = (panOffset.x + pan.x).coerceIn(-maxX, maxX),
                                    y = (panOffset.y + pan.y).coerceIn(-maxY, maxY)
                                )
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = panOffset.x
                                translationY = panOffset.y
                            }
                    ) {
                        SubcomposeAsyncImage(
                            model = buildFeedImageRequest(
                                context = context,
                                url = effectiveUrl,
                                referer = cam.refererUrl
                            ),
                            contentDescription = "${cam.title} enlarged still",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(color = agencyAccent)
                                    }
                                }

                                is AsyncImagePainter.State.Error -> {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Outlined.BrokenImage, contentDescription = null, tint = CrimsonThreat, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Unable to load live still from ${cam.stationCode}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate100
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            ColorCodedActionButton(
                                                label = "Retry Feed",
                                                accentColor = CyanNeon,
                                                icon = Icons.Default.Refresh,
                                                filled = true,
                                                onClick = onReload
                                            )
                                        }
                                    }
                                }

                                else -> SubcomposeAsyncImageContent()
                            }
                        }
                    }

                    // Zoom level readout + buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate950.copy(alpha = 0.88f))
                                .border(1.dp, AmberRadar, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1fx", zoom),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberRadar,
                                fontFamily = JetBrainsMonoFontFamily
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = String.format(
                        Locale.US,
                        "Direction: %s • %.4f°, %.4f° (%.1f NM to Orchidland)",
                        cam.direction,
                        cam.latitude,
                        cam.longitude,
                        cam.distanceToOrchidlandNm()
                    ),
                    fontSize = 11.sp,
                    color = EmeraldVerified,
                    fontFamily = JetBrainsMonoFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = cam.subtitle, fontSize = 12.sp, color = Slate200)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ColorCodedActionButton(
                        label = "-",
                        accentColor = CyanNeon,
                        icon = Icons.Default.ZoomOut,
                        filled = false,
                        onClick = { zoom = (zoom - 0.5f).coerceAtLeast(1.0f) },
                        modifier = Modifier.weight(0.9f)
                    )
                    ColorCodedActionButton(
                        label = "+",
                        accentColor = CyanNeon,
                        icon = Icons.Default.ZoomIn,
                        filled = false,
                        onClick = { zoom = (zoom + 0.5f).coerceAtMost(4.5f) },
                        modifier = Modifier.weight(0.9f)
                    )
                    ColorCodedActionButton(
                        label = "1x",
                        accentColor = AmberRadar,
                        filled = false,
                        onClick = {
                            zoom = 1.0f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.weight(0.8f)
                    )
                    ColorCodedActionButton(
                        label = "Reload",
                        accentColor = EmeraldVerified,
                        icon = Icons.Default.Refresh,
                        filled = true,
                        onClick = onReload,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("webcam_lightbox_reload")
                    )
                }
            }
        }
    }
}
