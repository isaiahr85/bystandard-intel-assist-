package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MapVisualThemeMode
import com.example.tools.AudioSpeechRate
import com.example.tools.LocalAccessibilityAudio
import com.example.tools.TextScalePreset
import com.example.ui.theme.*

fun visualAudioHighlightBorder(
    isSpeakingThis: Boolean,
    visualAudioEnabled: Boolean,
    defaultColor: Color = DarkBorder
): Color = when {
    isSpeakingThis -> EmeraldVerified
    visualAudioEnabled -> defaultColor
    else -> defaultColor
}

/**
 * Visual Select-to-Speak pill button attached to reports, dossiers, advisories, and cards.
 * Only renders when Visual Select Audio is enabled or when this item is currently active.
 */
@Composable
fun AudioSelectButton(
    sourceId: String,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    labelOverride: String? = null,
    accentColor: Color = CyanNeon
) {
    val a11y = LocalAccessibilityAudio.current
    val playback = a11y.playbackState
    val isThisSource = playback.activeSourceId == sourceId
    val isPlayingThis = isThisSource && playback.isPlaying
    val isPausedThis = isThisSource && playback.isPaused

    if (!a11y.visualAudioEnabled && !isThisSource) return

    val activeAccent = when {
        isPlayingThis -> EmeraldVerified
        isPausedThis -> AmberRadar
        else -> accentColor
    }
    val bgColor = if (isThisSource) activeAccent else activeAccent.copy(alpha = 0.18f)
    val contentColor = if (isThisSource) Slate950 else activeAccent
    val icon = when {
        isPlayingThis -> Icons.Filled.Pause
        isPausedThis -> Icons.Filled.PlayArrow
        else -> Icons.AutoMirrored.Filled.VolumeUp
    }
    val labelText = when {
        isPlayingThis -> "Pause"
        isPausedThis -> "Resume"
        labelOverride != null -> labelOverride
        compact -> "Listen"
        else -> "Listen"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = if (compact) 34.dp else 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.5.dp, activeAccent, RoundedCornerShape(8.dp))
            .clickable { a11y.onPlayOrToggle(sourceId, title, text) }
            .padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 5.dp else 7.dp
            )
            .testTag("audio_btn_$sourceId")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isPlayingThis) "Pause audio for $title" else "Listen to $title",
            tint = contentColor,
            modifier = Modifier.size(if (compact) 15.dp else 16.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = labelText,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaFontFamily,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Companion select-to-speak bar placed directly beneath or above text boxes so operators
 * can hear any editable prompt, note, webhook JSON, or reply draft read aloud.
 */
@Composable
fun AudioTextBoxStrip(
    sourceId: String,
    title: String,
    textToRead: String,
    modifier: Modifier = Modifier,
    accentColor: Color = CyanNeon
) {
    val a11y = LocalAccessibilityAudio.current
    val playback = a11y.playbackState
    val isThisSource = playback.activeSourceId == sourceId && playback.isActive

    if (!a11y.visualAudioEnabled && !isThisSource) return

    val cleanLength = textToRead.trim().length
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isThisSource) EmeraldVerified.copy(alpha = 0.14f) else Slate900)
            .border(
                1.dp,
                if (isThisSource) EmeraldVerified else accentColor.copy(alpha = 0.45f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.RecordVoiceOver,
                contentDescription = null,
                tint = if (isThisSource) EmeraldVerified else accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isThisSource) {
                    "Speaking box ($cleanLength chars)"
                } else {
                    "$title • $cleanLength chars"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isThisSource) EmeraldVerified else Slate200,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        AudioSelectButton(
            sourceId = sourceId,
            title = title,
            text = textToRead.ifBlank { "Text box is currently empty." },
            compact = true,
            labelOverride = "Read Box",
            accentColor = accentColor
        )
    }
}

/**
 * Persistent mini-player bar docked above the bottom NavigationBar whenever audio playback
 * is active or paused, exposing Speed, Text Size, Play/Pause, and Stop controls.
 */
@Composable
fun FloatingAudioPlayerBar(modifier: Modifier = Modifier) {
    val a11y = LocalAccessibilityAudio.current
    val playback = a11y.playbackState

    AnimatedVisibility(
        visible = playback.isActive,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = Slate900,
            tonalElevation = 10.dp,
            border = BorderStroke(
                1.5.dp,
                if (playback.isPlaying) EmeraldVerified else AmberRadar
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("floating_audio_player_bar")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    (if (playback.isPlaying) EmeraldVerified else AmberRadar)
                                        .copy(alpha = 0.22f)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Active audio playback",
                                tint = if (playback.isPlaying) EmeraldVerified else AmberRadar,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playback.activeTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = playback.activeSentence.ifBlank { playback.statusNote },
                                fontSize = 11.sp,
                                color = if (playback.isPlaying) EmeraldVerified else AmberRadar,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Speech Speed Pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate850)
                                .border(1.dp, CyanNeon.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { a11y.onCycleSpeechRate() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("audio_bar_speed_btn")
                        ) {
                            Text(
                                text = playback.speechRate.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = CyanNeon
                            )
                        }

                        // Text Scale Pill
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate850)
                                .border(1.dp, VioletVector.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { a11y.onCycleTextScale() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("audio_bar_text_scale_btn")
                        ) {
                            Text(
                                text = a11y.textScalePreset.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = VioletVector
                            )
                        }

                        IconButton(
                            onClick = { a11y.onPauseOrResume() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (playback.isPlaying) AmberRadar else EmeraldVerified)
                                .testTag("audio_bar_play_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playback.isPlaying) "Pause Audio" else "Resume Audio",
                                tint = Slate950,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { a11y.onStop() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CrimsonThreat.copy(alpha = 0.2f))
                                .border(1.dp, CrimsonThreat, RoundedCornerShape(8.dp))
                                .testTag("audio_bar_stop_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop Audio",
                                tint = CrimsonThreat,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { playback.progressFraction },
                    color = if (playback.isPlaying) EmeraldVerified else AmberRadar,
                    trackColor = Slate800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

/**
 * Full Accessibility Options sheet for toggling Visual Select-to-Speak buttons,
 * selecting Larger Readable Text scale (1.0x / 1.2x / 1.4x), and adjusting TTS speed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityOptionsSheet(
    onDismiss: () -> Unit,
    visualThemeMode: MapVisualThemeMode = MapVisualThemeMode.NIGHT,
    onSelectVisualThemeMode: (MapVisualThemeMode) -> Unit = {}
) {
    val a11y = LocalAccessibilityAudio.current
    val playback = a11y.playbackState

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        contentColor = Slate100,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Slate400) },
        modifier = Modifier.testTag("accessibility_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACCESSIBILITY & AUDIO READER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = CyanNeon,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Visual Select Audio & Readable Text",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate100
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("a11y_sheet_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Accessibility Options",
                        tint = Slate300
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

            // 1. Visual Select-to-Speak Toggle Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = BorderStroke(
                    1.5.dp,
                    if (a11y.visualAudioEnabled) EmeraldVerified.copy(alpha = 0.7f) else DarkBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { a11y.onToggleVisualAudio() }
                        .padding(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = if (a11y.visualAudioEnabled) EmeraldVerified else Slate400,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Visual Select-to-Speak Controls",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = "Display Listen & Read Box buttons on reports, advisories, dossiers, and text inputs.",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = a11y.visualAudioEnabled,
                        onCheckedChange = { a11y.onToggleVisualAudio() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = EmeraldVerified
                        ),
                        modifier = Modifier.testTag("a11y_sheet_visual_audio_switch")
                    )
                }
            }

            // 2. Larger Readable Text Scale Selector
            Text(
                text = "LARGER READABLE TEXT SIZE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = AmberRadar
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextScalePreset.entries.forEach { preset ->
                    val selected = a11y.textScalePreset == preset
                    val accent = when (preset) {
                        TextScalePreset.STANDARD -> CyanNeon
                        TextScalePreset.LARGE -> AmberRadar
                        TextScalePreset.EXTRA_LARGE -> EmeraldVerified
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) accent else Slate900)
                            .border(1.5.dp, if (selected) accent else accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { a11y.onSetTextScale(preset) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                            .testTag("a11y_scale_${preset.id}")
                    ) {
                        Text(
                            text = preset.shortLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = if (selected) Slate950 else accent
                        )
                        Text(
                            text = preset.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Slate950 else Slate200,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Live Typography Preview Box
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate950),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "READABILITY PREVIEW (${a11y.textScalePreset.displayName})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = JetBrainsMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Orchidland DICM advisory: Highway 130 remains open. Tap any Listen button to hear reports aloud.",
                        fontSize = 12.sp,
                        color = Slate100,
                        lineHeight = 17.sp
                    )
                }
            }

            // 3. Speech Speed Selector
            Text(
                text = "VOICE PLAYBACK SPEED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = VioletVector
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AudioSpeechRate.entries.forEach { rate ->
                    val selected = playback.speechRate == rate
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) VioletVector else Slate900)
                            .border(1.5.dp, if (selected) VioletVector else VioletVector.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { a11y.onSetSpeechRate(rate) }
                            .padding(vertical = 8.dp)
                            .testTag("a11y_speed_${rate.name.lowercase()}")
                    ) {
                        Text(
                            text = rate.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = if (selected) Slate950 else Slate100
                        )
                    }
                }
            }

            // 4. Test & Stop Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ColorCodedActionButton(
                    label = "Test Audio Playback",
                    accentColor = CyanNeon,
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    filled = true,
                    onClick = {
                        a11y.onPlayOrToggle(
                            "a11y_sample_test",
                            "Accessibility Voice Check",
                            "VeriScope accessibility audio reader active at ${playback.speechRate.label} speed with ${a11y.textScalePreset.displayName} typography."
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("a11y_test_audio_btn")
                )
                ColorCodedActionButton(
                    label = "Stop Audio",
                    accentColor = CrimsonThreat,
                    icon = Icons.Filled.Stop,
                    filled = false,
                    enabled = playback.isActive,
                    onClick = { a11y.onStop() },
                    modifier = Modifier.testTag("a11y_stop_audio_btn")
                )
            }

            MapVisualThemeSelectorRow(
                selectedMode = visualThemeMode,
                onSelectMode = onSelectVisualThemeMode
            )

            Text(
                text = "Engine Status: ${playback.statusNote}",
                fontSize = 11.sp,
                color = Slate300,
                fontFamily = JetBrainsMonoFontFamily
            )

            Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
