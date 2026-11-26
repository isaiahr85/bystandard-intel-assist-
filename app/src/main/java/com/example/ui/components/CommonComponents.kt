package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.tools.LocalAccessibilityAudio
import com.example.tools.TextScalePreset
import com.example.ui.theme.*

@Immutable
data class BtamTopBarBridge(
    val roleShortLabel: String,
    val onOpenOversightConsole: () -> Unit,
    val onRequest911Confirm: () -> Unit,
    val onOpenSideMenu: () -> Unit = {}
)

val LocalBtamTopBarBridge = staticCompositionLocalOf<BtamTopBarBridge?> { null }

@Composable
fun ForensicTopBar(
    title: String,
    subtitle: String? = null,
    onSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val a11y = LocalAccessibilityAudio.current
    val btamBridge = LocalBtamTopBarBridge.current
    val palette = LocalMapPalette.current
    val isLargeText = a11y.textScalePreset != TextScalePreset.STANDARD

    Surface(
        color = palette.panelSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
                    if (btamBridge != null) {
                        IconButton(
                            onClick = { btamBridge.onOpenSideMenu() },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("topbar_side_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Open Bystander Referrals Side Menu",
                                tint = palette.advisoryBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(palette.advisoryBlue, palette.normalGreen)))
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "VeriScope Emblem",
                            tint = contrastContentColor(palette.advisoryBlue),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = palette.textPrimary,
                                maxLines = if (isLargeText) 2 else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(palette.advisoryBlue.copy(alpha = 0.18f))
                                    .border(1.dp, palette.advisoryBlue.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v2.6 OSINT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    color = palette.advisoryBlue
                                )
                            }
                        }
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PlusJakartaFontFamily
                                ),
                                color = palette.textSecondary,
                                maxLines = if (isLargeText) 2 else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(EmeraldVerified.copy(alpha = 0.16f))
                        .border(1.5.dp, EmeraldVerified.copy(alpha = 0.7f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(EmeraldVerified)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = EmeraldVerified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Accessibility Bar: Larger Readable Text + Visual Select Audio + Settings Sheet
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val scaleColor = if (isLargeText) palette.warningAmber else palette.advisoryBlue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLargeText) palette.warningAmber.copy(alpha = 0.22f) else palette.canvasBackground)
                        .border(1.dp, scaleColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .clickable { a11y.onCycleTextScale() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("topbar_text_scale_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatSize,
                        contentDescription = "Cycle Readable Text Size",
                        tint = scaleColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Text ${a11y.textScalePreset.shortLabel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                }

                val audioColor = if (a11y.visualAudioEnabled) palette.normalGreen else palette.textSecondary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (a11y.visualAudioEnabled) palette.normalGreen.copy(alpha = 0.18f) else palette.canvasBackground)
                        .border(1.dp, audioColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .clickable { a11y.onToggleVisualAudio() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("topbar_visual_audio_btn")
                ) {
                    Icon(
                        imageVector = if (a11y.visualAudioEnabled) Icons.Filled.RecordVoiceOver else Icons.Filled.VoiceOverOff,
                        contentDescription = "Toggle Visual Select Audio",
                        tint = audioColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (a11y.visualAudioEnabled) "Select Audio: ON" else "Select Audio: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (a11y.visualAudioEnabled) palette.normalGreen else palette.textSecondary,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.canvasBackground)
                        .border(1.dp, palette.classAColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .clickable { a11y.onOpenAccessibilitySheet() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("topbar_a11y_settings_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessibilityNew,
                        contentDescription = "Open Audio & Readability Settings",
                        tint = palette.classAColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "A11y",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                }

                btamBridge?.let { btam ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.advisoryBlue.copy(alpha = 0.16f))
                            .border(1.dp, palette.advisoryBlue.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .clickable { btam.onOpenOversightConsole() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("topbar_btam_console_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Open BTAM Role Console",
                            tint = palette.advisoryBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = btam.roleShortLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.criticalRed.copy(alpha = 0.16f))
                            .border(1.dp, palette.criticalRed, RoundedCornerShape(8.dp))
                            .clickable { btam.onRequest911Confirm() }
                            .padding(horizontal = 7.dp, vertical = 5.dp)
                            .testTag("topbar_call_911_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Emergency 911 Dialer Confirmation",
                            tint = palette.criticalRed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-contrast color-coded action button designed for effortless scanning and 48dp+ accessibility.
 */
@Composable
fun ColorCodedActionButton(
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    filled: Boolean = false,
    enabled: Boolean = true
) {
    val palette = LocalMapPalette.current
    val container = if (filled) accentColor else accentColor.copy(alpha = 0.18f)
    val content = if (filled) contrastContentColor(accentColor) else palette.textPrimary
    val borderCol = if (enabled) accentColor else palette.panelBorder

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = palette.panelSurface,
            disabledContentColor = palette.textSecondary
        ),
        border = BorderStroke(1.5.dp, borderCol),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = modifier.heightIn(min = 42.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (filled) contrastContentColor(accentColor) else accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * High-contrast toggle pill for map scroll & overlay layer options.
 */
@Composable
fun MapOverlayOptionChip(
    label: String,
    active: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val palette = LocalMapPalette.current
    val bgColor = if (active) accentColor.copy(alpha = 0.24f) else palette.panelSurface
    val borderColor = if (active) accentColor else palette.panelBorder
    val textColor = if (active) palette.textPrimary else palette.textSecondary
    val iconColor = if (active) accentColor else palette.textSecondary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon ?: if (active) Icons.Filled.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaFontFamily,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun AccessTierBadge(tier: AccessTier, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (tier) {
        AccessTier.PUBLIC_OPEN -> Triple(
            EmeraldVerified.copy(alpha = 0.20f),
            EmeraldVerified,
            Icons.Outlined.Public
        )
        AccessTier.RESTRICTED_LEGAL -> Triple(
            CrimsonThreat.copy(alpha = 0.20f),
            CrimsonThreat,
            Icons.Outlined.Gavel
        )
        AccessTier.GOVERNMENTAL -> Triple(
            BlueSat.copy(alpha = 0.20f),
            BlueSat,
            Icons.Outlined.AccountBalance
        )
        AccessTier.ACADEMIC -> Triple(
            VioletVector.copy(alpha = 0.20f),
            VioletVector,
            Icons.Outlined.School
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tier.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun MetadataQualityBadge(quality: MetadataQuality, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (quality) {
        MetadataQuality.VERY_HIGH -> Pair(CyanNeon.copy(alpha = 0.20f), CyanNeon)
        MetadataQuality.HIGH -> Pair(EmeraldVerified.copy(alpha = 0.20f), EmeraldVerified)
        MetadataQuality.STANDARD -> Pair(AmberRadar.copy(alpha = 0.20f), AmberRadar)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = quality.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ResearchValueBadge(value: ResearchValue, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (value) {
        ResearchValue.CRITICAL -> Pair(AmberRadar.copy(alpha = 0.22f), AmberRadar)
        ResearchValue.HIGH -> Pair(CyanNeon.copy(alpha = 0.20f), CyanNeon)
        ResearchValue.MODERATE -> Pair(Slate700.copy(alpha = 0.45f), Slate200)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = value.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun VerificationStatusBadge(status: VerificationStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        VerificationStatus.AUTHENTIC_VERIFIED -> Triple(
            EmeraldVerified.copy(alpha = 0.22f),
            EmeraldVerified,
            Icons.Filled.Verified
        )
        VerificationStatus.FLAGGED_ANOMALY -> Triple(
            CrimsonThreat.copy(alpha = 0.22f),
            CrimsonThreat,
            Icons.Filled.Warning
        )
        VerificationStatus.METADATA_STRIPPED -> Triple(
            AmberRadar.copy(alpha = 0.22f),
            AmberRadar,
            Icons.Filled.Info
        )
        VerificationStatus.C2PA_SIGNED -> Triple(
            CyanNeon.copy(alpha = 0.22f),
            CyanNeon,
            Icons.Filled.Lock
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun ScopeFilterChip(
    scope: OperationalScope,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = scope.shortName,
                fontSize = 12.sp,
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
            borderColor = DarkBorder,
            selectedBorderColor = CyanNeon
        ),
        modifier = modifier.testTag("scope_chip_${scope.shortName.lowercase()}")
    )
}
