package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.LocalBtamTopBarBridge
import com.example.ui.theme.*

// ---------------------------------------------------------------------------
// 1. Mandatory Startup Role-Selection Gateway (Blocks Tools Until Unlocked)
// ---------------------------------------------------------------------------

@Composable
fun BtamStartupGatewayScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .safeDrawingPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("btam_role_gateway_screen")
    ) {
        // Header + 911 Immediate Emergency Shortcut (Always confirmation-gated)
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.advisoryBlue.copy(alpha = 0.2f))
                        .border(1.5.dp, palette.advisoryBlue, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "VeriScope BTAM Access Gateway",
                        tint = palette.advisoryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "VeriScope Role Gateway",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFontFamily,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Independent Platform • No Gov Affiliation • Session Lock",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = palette.advisoryBlue
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.request911CallConfirmation() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.criticalRed,
                    contentColor = contrastContentColor(palette.criticalRed)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btam_call_911_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Emergency 911 Dialer",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "911",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily
                )
            }
        }

        // Non-Affiliation, Non-Endorsement & Session Access Lock Notice
        Surface(
            color = palette.warningAmber.copy(alpha = 0.14f),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btam_non_affiliation_banner")
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Session Access Locked",
                    tint = palette.warningAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NON-AFFILIATION & SESSION LOCK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = palette.warningAmber
                    )
                    Text(
                        text = "VeriScope is an independent tool with NO affiliation to or endorsement by any government agency (U.S. Secret Service, NASA, NOAA, FAA, USGS) or social platform. Tools stay locked until you choose a role, set pathway, and accept Use-At-Own-Risk terms.",
                        fontSize = 12.sp,
                        color = palette.textPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        uiState.btam911StatusNote?.let { note ->
            Surface(
                color = palette.panelSurface,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.advisoryBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note,
                    fontSize = 11.sp,
                    color = palette.advisoryBlue,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Step 1: Select Operational Role
        Text(
            text = "1. SELECT OPERATIONAL ROLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            color = palette.advisoryBlue
        )

        BtamOperationalRole.entries.forEachIndexed { idx, role ->
            val selected = uiState.selectedBtamRole == role
            val accent = when (role) {
                BtamOperationalRole.PUBLIC_BYSTANDER -> palette.normalGreen
                BtamOperationalRole.BTAM_INVESTIGATOR -> palette.advisoryBlue
                BtamOperationalRole.TEAM_LEAD -> palette.warningAmber
                BtamOperationalRole.COMPLIANCE_AUDITOR -> palette.classAColor
            }
            val roleIcon = when (role) {
                BtamOperationalRole.PUBLIC_BYSTANDER -> Icons.Filled.Visibility
                BtamOperationalRole.BTAM_INVESTIGATOR -> Icons.Filled.ManageSearch
                BtamOperationalRole.TEAM_LEAD -> Icons.Filled.Groups
                BtamOperationalRole.COMPLIANCE_AUDITOR -> Icons.Filled.Policy
            }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) accent.copy(alpha = 0.16f) else palette.panelSurface
                ),
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) accent else palette.panelBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectBtamRoleOption(role) }
                    .testTag("btam_role_${role.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = role.title,
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${idx + 1}. ${role.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = role.subtitle,
                            fontSize = 12.sp,
                            color = palette.textSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = role.clearanceSummary,
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = accent
                        )
                        Text(
                            text = if (selected) "Selected • Review Steps 2 & 3 below" else "Tap to choose ${role.title} workspace",
                            fontSize = 10.sp,
                            color = palette.textSecondary
                        )
                    }
                    RadioButton(
                        selected = selected,
                        onClick = { viewModel.selectBtamRoleOption(role) },
                        colors = RadioButtonDefaults.colors(selectedColor = accent)
                    )
                }
            }
        }

        // Step 2: Declare Organizational Affiliation
        Text(
            text = "2. DECLARE ORGANIZATIONAL AFFILIATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            color = palette.advisoryBlue
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            BtamAffiliationPathway.entries.forEach { pathway ->
                val active = uiState.btamAffiliationPathway == pathway
                val accent = if (pathway == BtamAffiliationPathway.AFFILIATED) palette.advisoryBlue else palette.normalGreen
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) accent.copy(alpha = 0.16f) else palette.panelSurface
                    ),
                    border = BorderStroke(if (active) 2.dp else 1.dp, if (active) accent else palette.panelBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setBtamAffiliationPathway(pathway) }
                        .testTag("btam_pathway_${pathway.id}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = pathway.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) accent else palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pathway.description,
                            fontSize = 11.sp,
                            color = palette.textSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        if (uiState.btamAffiliationPathway == BtamAffiliationPathway.AFFILIATED) {
            OutlinedTextField(
                value = uiState.btamAffiliationOrgName,
                onValueChange = { viewModel.updateBtamAffiliationOrgName(it) },
                label = { Text("Participating Organization, School, Workplace, or Venue") },
                placeholder = { Text("e.g., Orchidland Operations / Campus Safety Unit") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btam_org_name_input")
            )
        }

        // Step 3: Non-Affiliation, Non-Endorsement & Use-At-Own-Risk User Agreement
        Text(
            text = "3. POLICY & USE-AT-OWN-RISK USER AGREEMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            color = palette.warningAmber
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.btamUserAgreementAccepted) {
                    palette.normalGreen.copy(alpha = 0.12f)
                } else {
                    palette.panelSurface
                }
            ),
            border = BorderStroke(
                if (uiState.btamUserAgreementAccepted) 1.5.dp else 1.dp,
                if (uiState.btamUserAgreementAccepted) palette.normalGreen else palette.warningAmber.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btam_disclaimer_card")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Informational Only • Use At Your Own Risk: External feeds (NASA, FAA, NOAA, USGS, OpenSky, FB/TG) and on-device AI insights carry zero warranty. Never rely on this app instead of dialing 911 or official emergency alerts.",
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    lineHeight = 15.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.setBtamUserAgreementAccepted(!uiState.btamUserAgreementAccepted)
                        }
                        .padding(vertical = 4.dp)
                        .testTag("btam_user_agreement_checkbox")
                ) {
                    Checkbox(
                        checked = uiState.btamUserAgreementAccepted,
                        onCheckedChange = { viewModel.setBtamUserAgreementAccepted(it) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I Agree — Non-Affiliation, Non-Endorsement & Use-At-Own-Risk Policy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Required before entering any role workspace",
                            fontSize = 10.sp,
                            color = if (uiState.btamUserAgreementAccepted) palette.normalGreen else palette.warningAmber
                        )
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.setBtamPolicyDialogOpen(true) },
                    border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("btam_open_policy_dialog_btn")
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Read Full Policy & User Agreement Disclaimer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.warningAmber
                        )
                        Text(
                            text = "Covers non-affiliation with agencies, no endorsement, AS-IS risk notice & PII safeguards",
                            fontSize = 10.sp,
                            color = palette.textSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        uiState.btamGatewayError?.let { errorMsg ->
            Surface(
                color = palette.criticalRed.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.criticalRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btam_gateway_error")
            ) {
                Text(
                    text = errorMsg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.criticalRed,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Button(
            onClick = { viewModel.unlockBtamSession() },
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.normalGreen,
                contentColor = contrastContentColor(palette.normalGreen)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp)
                .testTag("btam_unlock_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = "Unlock Selected Role Workspace",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.selectedBtamRole?.let { "Enter as ${it.title}" } ?: "Select Role & Enter Workspace",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.selectedBtamRole == null) {
                        "Prompt: Pick a role (Step 1), confirm affiliation (Step 2) & accept policy (Step 3)"
                    } else if (!uiState.btamUserAgreementAccepted) {
                        "Prompt: Check the User Agreement box above to unlock ${uiState.selectedBtamRole.title}"
                    } else {
                        "Ready: Opens ${uiState.selectedBtamRole.title} (${uiState.btamAffiliationPathway.label})"
                    },
                    fontSize = 10.sp
                )
            }
        }

        // All App APIs, App Relevance & Usage Directory Button with Sub-Text Prompt
        OutlinedButton(
            onClick = { viewModel.setBtamApiDirectoryOpen(true) },
            border = BorderStroke(1.2.dp, palette.advisoryBlue),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .testTag("btam_open_api_directory_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.Api,
                contentDescription = "All App APIs and Relevance",
                tint = palette.advisoryBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inspect All App APIs & Relevance (${AppApiCatalog.entries.size} Integrated Sources)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.advisoryBlue
                )
                Text(
                    text = "NASA EONET/GIBS, FAA TFR & WeatherCams, NOAA NWS/NEXRAD, USGS/UH/Hilo Cams, OpenSky ADS-B, FB/TG, 911 Dialer/GPS/TTS & Civic AI — tap to view why each API is used & disclaimers.",
                    fontSize = 10.sp,
                    color = palette.textSecondary,
                    lineHeight = 14.sp
                )
            }
        }

        // Anonymous Two-Way Case Token Quick Lookup (Available without unlocking analyst workspace)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
            border = BorderStroke(1.dp, palette.panelBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btam_gateway_token_lookup")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "ANONYMOUS CASE TOKEN STATUS CHECK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.advisoryBlue
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.btamTokenLookupQuery,
                        onValueChange = { viewModel.updateBtamTokenLookupQuery(it) },
                        placeholder = { Text("VS-BTAM-XXXX-XXXX") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btam_gateway_token_input")
                    )
                    Button(
                        onClick = { viewModel.lookupBtamCaseByToken() },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btam_gateway_token_btn")
                    ) {
                        Text("Check", fontWeight = FontWeight.Bold)
                    }
                }
                uiState.btamTokenLookupMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        color = palette.warningAmber
                    )
                }
                uiState.btamTokenLookupResult?.let { matched ->
                    BtamTokenThreadCard(
                        submission = matched,
                        followUpDraft = uiState.btamBystanderFollowUpDraft,
                        onFollowUpDraftChange = { viewModel.updateBtamBystanderFollowUpDraft(it) },
                        onSendFollowUp = { viewModel.sendAnonymousBystanderFollowUp() }
                    )
                }
            }
        }

        // Ethical & Governance Safeguards Summary
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
            border = BorderStroke(1.dp, palette.panelBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "NTAC SAFEGUARDS & ETHICAL CONSTRAINTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.normalGreen
                )
                Text(
                    text = "• Anti-Profiling Mandate: Evaluates strictly observable behaviors & leakage—never race, religion, ethnicity, disability, or political affiliation.",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = "• Non-Clinical Boundary: Logs distress for supportive, non-punitive evaluation without clinical diagnoses.",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
                Text(
                    text = "• Human-in-the-Loop Governance: No automated enforcement; humans retain all investigative & intervention authority.",
                    fontSize = 11.sp,
                    color = palette.textSecondary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. Public / Bystander 5-Stage U.S. Secret Service NTAC Workflow
// ---------------------------------------------------------------------------

@Composable
fun BtamPublicBystanderWorkspace(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val stage = uiState.btamBystanderStage

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .safeDrawingPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("btam_bystander_workspace")
    ) {
        // Header with Role, 911 button, and Switch Role / Lock button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Public / Bystander BTAM Intake",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFontFamily,
                    color = palette.textPrimary
                )
                Text(
                    text = "Independent NTAC-Inspired Pathway • No Gov Affiliation • ${uiState.btamAffiliationPathway.label}",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.normalGreen
                )
            }
            val bridge = LocalBtamTopBarBridge.current
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { bridge?.onOpenSideMenu?.invoke() },
                    border = BorderStroke(1.dp, palette.advisoryBlue),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("bystander_workspace_side_menu_btn")
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "Open Bystander Referrals Side Menu", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Menu", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.request911CallConfirmation() },
                    border = BorderStroke(1.5.dp, palette.criticalRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.criticalRed),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("btam_call_911_btn")
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Call 911", modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("911", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.lockBtamSessionToGateway() },
                    border = BorderStroke(1.dp, palette.advisoryBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("btam_switch_role_btn")
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch Operational Role", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Role", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        uiState.btam911StatusNote?.let { status ->
            Text(
                text = status,
                fontSize = 11.sp,
                color = palette.warningAmber,
                fontFamily = JetBrainsMonoFontFamily
            )
        }

        // 5-Stage Progress Stepper
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            BtamBystanderStage.entries.forEach { s ->
                val active = stage == s
                FilterChip(
                    selected = active,
                    onClick = { viewModel.setBtamBystanderStage(s) },
                    label = {
                        Text(
                            text = "${s.stepNumber}. ${s.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.testTag("btam_stage_chip_${s.stepNumber}")
                )
            }
        }

        when (stage) {
            BtamBystanderStage.STAGE_1_LIFE_SAFETY -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.5.dp, palette.criticalRed.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "STAGE 1: IMMEDIATE LIFE-SAFETY TRIAGE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.criticalRed
                        )
                        Text(
                            text = "Does this situation involve an active attack, a weapon on scene, or an imminent threat to human life?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                        Button(
                            onClick = { viewModel.setBtamLifeSafetyImminentThreat(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.criticalRed,
                                contentColor = contrastContentColor(palette.criticalRed)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btam_stage1_yes_btn")
                        ) {
                            Text("YES — Active Attack, Weapon, or Imminent Threat", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.setBtamLifeSafetyImminentThreat(false) },
                            border = BorderStroke(1.5.dp, palette.normalGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btam_stage1_no_btn")
                        ) {
                            Text("NO — Non-Imminent Concerning Behavior (Continue)", fontWeight = FontWeight.Bold, color = palette.normalGreen)
                        }

                        if (uiState.btamLifeSafetyImminentThreat == true) {
                            Surface(
                                color = palette.criticalRed.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, palette.criticalRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "TIER 1: EMERGENCY — CALL 911 IMMEDIATELY",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.criticalRed
                                    )
                                    Text(
                                        text = "Move to a safe location and contact local emergency services (911). Your submission is flagged Tier 1: Emergency for expedited organizational responder routing.",
                                        fontSize = 12.sp,
                                        color = palette.textPrimary
                                    )
                                    Button(
                                        onClick = { viewModel.request911CallConfirmation() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.criticalRed,
                                            contentColor = contrastContentColor(palette.criticalRed)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("btam_stage1_911_btn")
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Open Default Phone Dialer (911)", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.setBtamBystanderStage(BtamBystanderStage.STAGE_2_AFFILIATION) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 48.dp)
                                            .testTag("btam_stage1_continue_emergency_btn")
                                    ) {
                                        Text("Log Emergency Alert Packet (Stage 2 →)")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            BtamBystanderStage.STAGE_2_AFFILIATION -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "STAGE 2: ORGANIZATIONAL AFFILIATION & ROUTING CHECK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.advisoryBlue
                        )
                        BtamAffiliationPathway.entries.forEach { pathway ->
                            val selected = uiState.btamAffiliationPathway == pathway
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) palette.advisoryBlue.copy(alpha = 0.14f) else palette.canvasBackground)
                                    .border(1.dp, if (selected) palette.advisoryBlue else palette.panelBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setBtamAffiliationPathway(pathway) }
                                    .padding(10.dp)
                                    .testTag("btam_stage2_pathway_${pathway.id}")
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.setBtamAffiliationPathway(pathway) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pathway.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = palette.textPrimary)
                                    Text(pathway.description, fontSize = 11.sp, color = palette.textSecondary)
                                }
                            }
                        }
                        if (uiState.btamAffiliationPathway == BtamAffiliationPathway.AFFILIATED) {
                            OutlinedTextField(
                                value = uiState.btamAffiliationOrgName,
                                onValueChange = { viewModel.updateBtamAffiliationOrgName(it) },
                                label = { Text("Participating Organization / Workplace / School / Venue") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btam_stage2_org_input")
                            )
                        } else {
                            Surface(
                                color = palette.normalGreen.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, palette.normalGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "PII-SCRUBBED VOLUNTEER PREVENTION POOL + NTAC RESOURCES",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = palette.normalGreen
                                    )
                                    Text(
                                        text = "All personally identifiable information (names, emails, phones, handles, device metadata) is automatically stripped before archival.",
                                        fontSize = 11.sp,
                                        color = palette.textPrimary
                                    )
                                    OutlinedButton(
                                        onClick = {
                                            runCatching { uriHandler.openUri(BtamAntiProfilingGuard.NTAC_OFFICIAL_URL) }
                                        },
                                        modifier = Modifier
                                            .heightIn(min = 44.dp)
                                            .testTag("btam_ntac_link_btn")
                                    ) {
                                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Open U.S. Secret Service NTAC Prevention Guide", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.setBtamBystanderStage(BtamBystanderStage.STAGE_3_BEHAVIORAL_INTAKE) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btam_stage2_next_btn")
                        ) {
                            Text("Continue to Behavioral Intake (Stage 3 →)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            BtamBystanderStage.STAGE_3_BEHAVIORAL_INTAKE -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.dp, palette.panelBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "STAGE 3: STRUCTURED BEHAVIORAL OBSERVATION INTAKE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.advisoryBlue
                        )
                        Text(
                            text = "Report strictly observable behaviors, communications, and situational context (no demographic profiling or clinical labels).",
                            fontSize = 11.sp,
                            color = palette.textSecondary
                        )

                        NtacIndicatorCategory.entries.forEach { cat ->
                            val checked = uiState.btamSelectedIndicators.contains(cat)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (checked) palette.advisoryBlue.copy(alpha = 0.14f) else palette.canvasBackground)
                                    .border(1.dp, if (checked) palette.advisoryBlue else palette.panelBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.toggleNtacIndicator(cat) }
                                    .padding(8.dp)
                                    .testTag("btam_indicator_${cat.id}")
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { viewModel.toggleNtacIndicator(cat) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = palette.textPrimary)
                                    Text(cat.description, fontSize = 11.sp, color = palette.textSecondary)
                                }
                            }
                        }

                        uiState.btamAntiProfilingAlert?.let { alert ->
                            Surface(
                                color = palette.criticalRed.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.5.dp, palette.criticalRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btam_antiprofiling_alert")
                            ) {
                                Text(
                                    text = alert,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.criticalRed,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.btamLeakageDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(leakage = it) },
                            label = { Text("Leakage & Communications (quoted threats / digital posts)") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_leakage_input")
                        )
                        OutlinedTextField(
                            value = uiState.btamGrievanceDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(grievance = it) },
                            label = { Text("Escalating Grievance & Fixation (targets / escalating hostility)") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_grievance_input")
                        )
                        OutlinedTextField(
                            value = uiState.btamProbingDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(probing = it) },
                            label = { Text("Pre-Attack / Probing Behaviors (surveillance / weapon display)") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_probing_input")
                        )
                        OutlinedTextField(
                            value = uiState.btamContextArtifactsDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(contextArtifacts = it) },
                            label = { Text("Acute Stressors & Preserved Digital Artifacts (non-clinical)") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_context_input")
                        )
                        OutlinedTextField(
                            value = uiState.btamObsDateTimeLocDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(obsDateTimeLoc = it) },
                            label = { Text("Observation Date, Time & Location") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_obs_loc_input")
                        )
                        OutlinedTextField(
                            value = uiState.btamWitnessNoteDraft,
                            onValueChange = { viewModel.updateBtamIntakeDrafts(witnessNote = it) },
                            label = { Text("Witnesses & Additional Context (no PII required)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btam_witness_input")
                        )

                        Button(
                            onClick = { viewModel.setBtamBystanderStage(BtamBystanderStage.STAGE_4_URGENCY_PACKAGE) },
                            enabled = uiState.btamAntiProfilingAlert == null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("btam_stage3_next_btn")
                        ) {
                            Text("Review Urgency Tier Brief (Stage 4 →)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            BtamBystanderStage.STAGE_4_URGENCY_PACKAGE -> {
                val plannedTier = viewModel.computePlannedBtamUrgencyTier(uiState)
                val tierColor = when (plannedTier) {
                    BtamUrgencyTier.EMERGENCY -> palette.criticalRed
                    BtamUrgencyTier.PRIORITY -> palette.warningAmber
                    BtamUrgencyTier.STANDARD -> palette.normalGreen
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.5.dp, tierColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "STAGE 4: URGENCY TIER PACKAGING BRIEF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = tierColor
                        )
                        Surface(
                            color = tierColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, tierColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(plannedTier.label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = tierColor)
                                Text(plannedTier.guidance, fontSize = 12.sp, color = palette.textPrimary)
                            }
                        }
                        Text(
                            text = "Routing Pathway: ${uiState.btamAffiliationPathway.label}" +
                                if (uiState.btamAffiliationPathway == BtamAffiliationPathway.AFFILIATED) {
                                    " (${uiState.btamAffiliationOrgName.ifBlank { "Participating Org" }})"
                                } else " (Automatic PII Scrubbing Enabled)",
                            fontSize = 12.sp,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Selected Indicators: " + uiState.btamSelectedIndicators
                                .ifEmpty { setOf(NtacIndicatorCategory.ACUTE_STRESSORS) }
                                .joinToString(", ") { it.title },
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )
                        Text(
                            text = "Human-in-the-Loop Governance: Automated packaging only standardizes triage order; human reviewers make all case decisions.",
                            fontSize = 11.sp,
                            color = palette.advisoryBlue
                        )
                        Button(
                            onClick = { viewModel.submitBtamBystanderReport() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tierColor,
                                contentColor = contrastContentColor(tierColor)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("btam_submit_intake_btn")
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Package Intake & Issue Anonymous Case Token", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            BtamBystanderStage.STAGE_5_CASE_TOKEN -> {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                    border = BorderStroke(1.5.dp, palette.normalGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "STAGE 5: ANONYMOUS CASE TOKEN & TWO-WAY FOLLOW-UP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.normalGreen
                        )
                        uiState.btamLatestIssuedToken?.let { token ->
                            Surface(
                                color = palette.normalGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, palette.normalGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ISSUED CRYPTOGRAPHIC CASE TOKEN", fontSize = 10.sp, fontFamily = JetBrainsMonoFontFamily, color = palette.normalGreen)
                                    Text(
                                        text = token,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = palette.textPrimary,
                                        modifier = Modifier.testTag("btam_issued_token_text")
                                    )
                                    Text(
                                        text = "Use this token to track case status or answer investigator questions anonymously.",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.btamTokenLookupQuery,
                                onValueChange = { viewModel.updateBtamTokenLookupQuery(it) },
                                label = { Text("Lookup Token") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btam_token_lookup_input")
                            )
                            Button(
                                onClick = { viewModel.lookupBtamCaseByToken() },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btam_token_lookup_btn")
                            ) {
                                Text("Lookup", fontWeight = FontWeight.Bold)
                            }
                        }
                        uiState.btamTokenLookupMessage?.let { msg ->
                            Text(msg, fontSize = 11.sp, color = palette.advisoryBlue)
                        }
                        uiState.btamTokenLookupResult?.let { sub ->
                            BtamTokenThreadCard(
                                submission = sub,
                                followUpDraft = uiState.btamBystanderFollowUpDraft,
                                onFollowUpDraftChange = { viewModel.updateBtamBystanderFollowUpDraft(it) },
                                onSendFollowUp = { viewModel.sendAnonymousBystanderFollowUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BtamTokenThreadCard(
    submission: BtamCaseSubmission,
    followUpDraft: String,
    onFollowUpDraftChange: (String) -> Unit,
    onSendFollowUp: () -> Unit
) {
    val palette = LocalMapPalette.current
    Surface(
        color = palette.canvasBackground,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, palette.advisoryBlue.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("btam_token_thread_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = "${submission.token} • ${submission.urgencyTier.label}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.advisoryBlue
            )
            Text(
                text = "Status: ${submission.statusLabel}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )
            if (submission.piiScrubApplied) {
                Text(
                    text = "PII Minimization: Active (${submission.piiRedactionCount} identifier(s) redacted)",
                    fontSize = 11.sp,
                    color = palette.normalGreen
                )
            }
            if (submission.followUpMessages.isNotEmpty()) {
                HorizontalDivider(color = palette.panelBorder)
                submission.followUpMessages.forEach { msg ->
                    Text(
                        text = "[${msg.senderRole}] ${msg.body}",
                        fontSize = 11.sp,
                        color = palette.textPrimary
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = followUpDraft,
                    onValueChange = onFollowUpDraftChange,
                    placeholder = { Text("Reply anonymously (PII scrubbed)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btam_followup_input")
                )
                Button(
                    onClick = onSendFollowUp,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btam_send_followup_btn")
                ) {
                    Text("Send")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Investigator / Team Lead / Compliance Auditor Console Modal
// ---------------------------------------------------------------------------

@Composable
fun BtamOversightConsoleModal(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    val role = uiState.selectedBtamRole ?: BtamOperationalRole.BTAM_INVESTIGATOR
    val disciplines = listOf("HR", "Security", "Legal", "Clinical/Support")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
            border = BorderStroke(1.5.dp, palette.advisoryBlue),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("btam_oversight_console")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = role.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = if (uiState.btamAffiliationPathway == BtamAffiliationPathway.AFFILIATED) {
                                "Org: ${uiState.btamAffiliationOrgName}"
                            } else "Public Oversight Scope",
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.advisoryBlue
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.request911CallConfirmation() },
                            border = BorderStroke(1.dp, palette.criticalRed),
                            modifier = Modifier.testTag("btam_console_911_btn")
                        ) {
                            Text("911", color = palette.criticalRed, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.lockBtamSessionToGateway() },
                            modifier = Modifier.testTag("btam_console_lock_btn")
                        ) {
                            Text("Switch Role")
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btam_console_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close BTAM Console", tint = palette.textPrimary)
                        }
                    }
                }

                HorizontalDivider(color = palette.panelBorder)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setBtamApiDirectoryOpen(true) },
                        border = BorderStroke(1.dp, palette.advisoryBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp)
                            .testTag("btam_console_api_directory_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                            Text("All App APIs (${AppApiCatalog.entries.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.advisoryBlue)
                            Text("Inspect endpoint relevance & disclaimers", fontSize = 9.sp, color = palette.textSecondary, lineHeight = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.setBtamPolicyDialogOpen(true) },
                        border = BorderStroke(1.dp, palette.warningAmber),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp)
                            .testTag("btam_console_policy_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                            Text("Policy & Agreement", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.warningAmber)
                            Text("Non-affiliation & use-at-own-risk terms", fontSize = 9.sp, color = palette.textSecondary, lineHeight = 12.sp)
                        }
                    }
                }

                if (role == BtamOperationalRole.COMPLIANCE_AUDITOR) {
                    Text(
                        text = "PII REDACTION, TOKEN INTEGRITY & ANTI-PROFILING AUDIT LOGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = palette.classAColor
                    )
                    if (uiState.btamPiiAuditLogs.isEmpty()) {
                        Text(
                            text = "No bystander/public intake submissions logged yet in this session. Zero PII retained.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("btam_audit_logs_list")
                        ) {
                            uiState.btamPiiAuditLogs.forEach { entry ->
                                Surface(
                                    color = palette.canvasBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, palette.classAColor.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "${entry.token} • ${entry.pathway.label}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            color = palette.textPrimary
                                        )
                                        Text(
                                            text = "Redacted (${entry.redactionCount}): ${entry.redactedCategories.joinToString(", ").ifEmpty { "None needed" }}",
                                            fontSize = 11.sp,
                                            color = palette.normalGreen
                                        )
                                        Text(
                                            text = "Anti-Profiling Check: ${if (entry.antiProfilingPassed) "PASS" else "FLAGGED"} • ${entry.timestampUtc}",
                                            fontSize = 10.sp,
                                            color = palette.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "BTAM INTAKE & MULTIDISCIPLINARY CASE QUEUE (${uiState.btamSubmissions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = palette.advisoryBlue
                    )
                    if (uiState.btamSubmissions.isEmpty()) {
                        Text(
                            text = "No active BTAM intake packets in queue yet. Switch to Public / Bystander to submit an intake packet.",
                            fontSize = 12.sp,
                            color = palette.textSecondary
                        )
                    } else {
                        uiState.btamSubmissions.forEach { sub ->
                            val isSel = uiState.btamSelectedQueueToken == sub.token
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = palette.canvasBackground),
                                border = BorderStroke(1.5.dp, if (isSel) palette.advisoryBlue else palette.panelBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectBtamQueueCase(sub.token) }
                                    .testTag("btam_queue_case_${sub.token}")
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Text(
                                        text = "${sub.token} • ${sub.urgencyTier.label}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.advisoryBlue
                                    )
                                    Text(
                                        text = "${sub.organizationName} • ${sub.statusLabel}",
                                        fontSize = 11.sp,
                                        color = palette.textPrimary
                                    )
                                    Text(
                                        text = "Indicators: ${sub.indicators.joinToString(", ") { it.title }}",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                    Text(
                                        text = "Governance: ${sub.humanAuthorityNote}",
                                        fontSize = 11.sp,
                                        color = palette.warningAmber
                                    )
                                    if (isSel) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = uiState.btamInvestigatorQuestionDraft,
                                                onValueChange = { viewModel.updateBtamInvestigatorQuestionDraft(it) },
                                                placeholder = { Text("Post anonymous follow-up question") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("btam_investigator_question_input")
                                            )
                                            Button(
                                                onClick = { viewModel.postInvestigatorFollowUpQuestion(sub.token) },
                                                modifier = Modifier.testTag("btam_post_question_btn")
                                            ) {
                                                Text("Send")
                                            }
                                        }
                                        if (role == BtamOperationalRole.TEAM_LEAD) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                            ) {
                                                disciplines.forEach { disc ->
                                                    val assigned = sub.assignedDisciplines.contains(disc)
                                                    FilterChip(
                                                        selected = assigned,
                                                        onClick = { viewModel.toggleBtamCaseDiscipline(sub.token, disc) },
                                                        label = { Text(disc, fontSize = 11.sp) },
                                                        modifier = Modifier.testTag("btam_discipline_${disc.lowercase()}")
                                                    )
                                                }
                                            }
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.recordHumanAuthorityDecision(
                                                            token = sub.token,
                                                            decisionStatus = "Multidisciplinary Escalation Approved",
                                                            authorityNote = "Human BTAM Team Lead authorized cross-functional intervention (${sub.assignedDisciplines.joinToString(", ").ifEmpty { "Core Team" }})."
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("btam_approve_escalation_btn")
                                                ) {
                                                    Text("Approve Escalation", fontSize = 11.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.recordHumanAuthorityDecision(
                                                            token = sub.token,
                                                            decisionStatus = "Supportive Non-Punitive Outreach",
                                                            authorityNote = "Human BTAM Team Lead directed non-clinical supportive outreach."
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("btam_supportive_outreach_btn")
                                                ) {
                                                    Text("Supportive Outreach", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 4. 911 Emergency Call Confirmation Dialog (Anti-Pocket-Dial Safeguard)
// ---------------------------------------------------------------------------

@Composable
fun Call911ConfirmationDialog(
    onConfirmCall911: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalMapPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.panelSurface,
        icon = {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = "Emergency 911 Confirmation",
                tint = palette.criticalRed,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Are you sure you want to call 911?",
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Confirm this is a genuine emergency before launching your Android mobile Phone dialer with 911 pre-filled.",
                    fontSize = 13.sp,
                    color = palette.textPrimary
                )
                Text(
                    text = "This confirmation step protects against accidental touch-screen taps or pocket calls.",
                    fontSize = 12.sp,
                    color = palette.warningAmber
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmCall911,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.criticalRed,
                    contentColor = contrastContentColor(palette.criticalRed)
                ),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("confirm_dial_911_btn")
            ) {
                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Yes, Open 911 Dialer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("cancel_dial_911_btn")
            ) {
                Text("Cancel (Accidental Tap)")
            }
        },
        modifier = Modifier.testTag("call_911_confirm_dialog")
    )
}
