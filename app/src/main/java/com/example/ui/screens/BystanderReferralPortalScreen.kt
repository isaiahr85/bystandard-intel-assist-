package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bystander.*
import com.example.ui.ForensicViewModel
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalMapPalette
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.contrastContentColor

@Composable
fun BystanderSideMenuDrawerContent(
    state: BystanderPortalUiState,
    onSelectOption: (BystanderSideMenuOption) -> Unit,
    onToggleElderMode: (Boolean) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val palette = LocalMapPalette.current
    ModalDrawerSheet(
        drawerContainerColor = palette.panelSurface,
        drawerContentColor = palette.textPrimary,
        modifier = Modifier
            .widthIn(max = 320.dp)
            .testTag("bystander_side_menu_drawer")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(14.dp)
        ) {
            Text(
                text = BystanderPortalConstants.PORTAL_TITLE,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGroteskFontFamily,
                color = palette.advisoryBlue
            )
            Text(
                text = "Voluntary observation, early intervention & community resource support (non-surveillance, non-profiling).",
                fontSize = 11.sp,
                color = palette.textSecondary,
                lineHeight = 15.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.canvasBackground)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Elder-Friendly Text & Layout", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    Text("Larger type & simplified takeaways", fontSize = 10.sp, color = palette.textSecondary)
                }
                Switch(
                    checked = state.elderFriendlyMode,
                    onCheckedChange = onToggleElderMode,
                    modifier = Modifier.testTag("bystander_drawer_elder_switch")
                )
            }

            HorizontalDivider(color = palette.panelBorder)

            BystanderSideMenuOption.entries.forEach { option ->
                val selected = state.activeOption == option
                NavigationDrawerItem(
                    label = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${option.number}. ${option.title}",
                                fontSize = if (state.elderFriendlyMode) 14.sp else 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = option.shortSubtitle,
                                fontSize = 10.sp,
                                color = palette.textSecondary
                            )
                        }
                    },
                    selected = selected,
                    onClick = {
                        onSelectOption(option)
                        onCloseDrawer()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = palette.advisoryBlue.copy(alpha = 0.2f),
                        selectedTextColor = palette.advisoryBlue,
                        unselectedTextColor = palette.textPrimary
                    ),
                    modifier = Modifier
                        .heightIn(min = 52.dp)
                        .testTag("bystander_menu_opt_${option.number}")
                )
            }

            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = onCloseDrawer,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("bystander_drawer_close_btn")
            ) {
                Text("Close Side Menu")
            }
        }
    }
}

@Composable
fun BystanderReferralPortalScreen(
    viewModel: ForensicViewModel,
    onOpenDrawer: () -> Unit,
    onBackToWorkspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMapPalette.current
    val coordinator = viewModel.bystanderReferralCoordinator
    val state by viewModel.bystanderPortalState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.activeOption, state.lastSubmissionMessage) {
        listState.scrollToItem(0)
    }

    BackHandler { onBackToWorkspace() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvasBackground)
            .safeDrawingPadding()
            .testTag("bystander_referral_portal_screen")
    ) {
        // Portal Header with Side-Menu Hamburger, 911 Dialer Guard, Elder Toggle, and Back
        Surface(color = palette.panelSurface, modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("bystander_portal_menu_btn")
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Bystander Side Menu", tint = palette.advisoryBlue)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = BystanderPortalConstants.PORTAL_TITLE,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = palette.advisoryBlue
                        )
                        Text(
                            text = "${state.activeOption.number}. ${state.activeOption.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.request911CallConfirmation() },
                        border = BorderStroke(1.dp, palette.criticalRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("bystander_portal_911_btn")
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Call 911", tint = palette.criticalRed, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("911", fontSize = 11.sp, color = palette.criticalRed, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(4.dp))
                    OutlinedButton(
                        onClick = onBackToWorkspace,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("bystander_portal_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Exit", fontSize = 11.sp)
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("bystander_portal_lazy_list")
        ) {
            state.guardError?.let { err ->
                item(key = "guard_error") {
                    Surface(
                        color = palette.criticalRed.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.criticalRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bystander_guard_error")
                    ) {
                        Text(
                            text = err,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.criticalRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            state.lastSubmissionMessage?.let { ok ->
                item(key = "submit_success") {
                    Surface(
                        color = palette.normalGreen.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, palette.normalGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bystander_submit_success_banner")
                    ) {
                        Text(
                            text = ok,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.normalGreen,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            when (state.activeOption) {
                BystanderSideMenuOption.SUBMIT_CONCERN,
                BystanderSideMenuOption.REFER_ASSISTANCE,
                BystanderSideMenuOption.REQUEST_WELFARE_CHECK,
                BystanderSideMenuOption.CLARIFY_ANOMALY -> {
                    if (state.activeOption == BystanderSideMenuOption.CLARIFY_ANOMALY) {
                        item(key = "clarify_anomalies") {
                            ClarifyingAnomaliesCard(elderMode = state.elderFriendlyMode)
                        }
                    }
                    item(key = "concern_intake_form") {
                        CommunityConcernIntakeFormCard(
                            state = state,
                            coordinator = coordinator
                        )
                    }
                    item(key = "validation_checklist_card") {
                        ValidationChecklistOnlyCard(
                            state = state,
                            coordinator = coordinator
                        )
                    }
                    item(key = "confidence_scale_card") {
                        ConfidenceScaleOnlyCard(
                            state = state,
                            coordinator = coordinator
                        )
                    }
                    item(key = "proceed_to_review") {
                        Button(
                            onClick = { coordinator.selectOption(BystanderSideMenuOption.ACKNOWLEDGEMENT_REVIEW) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("bystander_proceed_to_review_btn")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Continue to Mandatory Acknowledgement & Review (Step 10 →)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Review 4-tier routing & submit observation", fontSize = 10.sp)
                            }
                        }
                    }
                }

                BystanderSideMenuOption.COMMUNITY_RESOURCE_REFERRAL -> {
                    item(key = "resource_ref") {
                        BystanderResourceReferralSection(
                            elderMode = state.elderFriendlyMode,
                            query = state.resourceSearchQuery,
                            onQueryChange = { coordinator.updateResourceSearchQuery(it) }
                        )
                    }
                }

                BystanderSideMenuOption.PUBLIC_SAFETY_LIBRARY -> {
                    item(key = "btam_library") {
                        BystanderBtamEducationSection(elderMode = state.elderFriendlyMode)
                    }
                }

                BystanderSideMenuOption.NTAC_LEARNING_CENTER -> {
                    item(key = "ntac_center") {
                        BystanderNtacLearningCenterSection(
                            elderMode = state.elderFriendlyMode,
                            onMarkReviewed = { coordinator.markNtacEducationReviewed() }
                        )
                    }
                }

                BystanderSideMenuOption.TOWN_HALL_RESOURCES -> {
                    item(key = "town_hall") {
                        BystanderTownHallSection(elderMode = state.elderFriendlyMode)
                    }
                }

                BystanderSideMenuOption.REPORTING_FAQS -> {
                    item(key = "faqs") {
                        BystanderFaqsSection(elderMode = state.elderFriendlyMode)
                    }
                }

                BystanderSideMenuOption.ACKNOWLEDGEMENT_REVIEW -> {
                    item(key = "coordination_workflow_item") {
                        CoordinationWorkflowOverviewCard(
                            state = state,
                            coordinator = coordinator
                        )
                    }
                    item(key = "mandatory_ack_item") {
                        MandatoryAcknowledgementCard(
                            state = state,
                            coordinator = coordinator
                        )
                    }
                    item(key = "mandatory_submit_item") {
                        Button(
                            onClick = { coordinator.submitConcern() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.normalGreen,
                                contentColor = contrastContentColor(palette.normalGreen)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .testTag("bystander_final_submit_btn")
                        ) {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Submit Community Observation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Applies automatic PII scrubbing & routes through Tiers 1–4", fontSize = 10.sp)
                            }
                        }
                    }
                    items(state.submissions, key = { it.id }) { sub ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
                            border = BorderStroke(1.dp, palette.advisoryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bystander_submission_card_${sub.referenceCode}")
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "${sub.referenceCode} • ${sub.assignedTier.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.advisoryBlue
                                )
                                Text(
                                    text = "Behavior: ${sub.specificConcernBehavior}",
                                    fontSize = 12.sp,
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = "Confidence: ${sub.confidenceLevel.label} • Partners: ${sub.recommendedPartners.joinToString(", ") { it.title }}",
                                    fontSize = 11.sp,
                                    color = palette.normalGreen
                                )
                                Text(
                                    text = "${sub.statusNote} (PII scrubbed: ${sub.piiRedactedCount})",
                                    fontSize = 10.sp,
                                    color = palette.warningAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityConcernIntakeFormCard(
    state: BystanderPortalUiState,
    coordinator: BystanderReferralCoordinator
) {
    val palette = LocalMapPalette.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.panelBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_intake_form")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "COMMUNITY CONCERN & OBSERVATION FORM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.advisoryBlue
            )
            Surface(
                color = palette.warningAmber.copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, palette.warningAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = BystanderPortalConstants.ANTI_SPECULATION_WARNING,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.warningAmber,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Text(
                text = "Select Information Basis (Check all that apply):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            ObservationSourceType.entries.forEach { src ->
                val checked = state.selectedSourceTypes.contains(src)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { coordinator.toggleSourceType(src) }
                        .testTag("bystander_source_${src.id}")
                ) {
                    Checkbox(checked = checked, onCheckedChange = { coordinator.toggleSourceType(src) })
                    Text(src.label, fontSize = 12.sp, color = palette.textPrimary)
                }
            }

            OutlinedTextField(
                value = state.specificConcernBehavior,
                onValueChange = { coordinator.updateIntakeFields(specificConcernBehavior = it) },
                label = { Text("What specific behavior, statement, action, communication, or circumstance caused concern? *") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_specific_behavior_input")
            )
            OutlinedTextField(
                value = state.whatObserved,
                onValueChange = { coordinator.updateIntakeFields(whatObserved = it) },
                label = { Text("What was observed (objective facts) *") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_what_observed_input")
            )
            OutlinedTextField(
                value = state.dateAndTime,
                onValueChange = { coordinator.updateIntakeFields(dateAndTime = it) },
                label = { Text("Date and time *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_datetime_input")
            )
            OutlinedTextField(
                value = state.location,
                onValueChange = { coordinator.updateIntakeFields(location = it) },
                label = { Text("Location / public setting *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_location_input")
            )
            OutlinedTextField(
                value = state.infoSourceDetail,
                onValueChange = { coordinator.updateIntakeFields(infoSourceDetail = it) },
                label = { Text("Source of information & direct vs. second-hand context *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_source_detail_input")
            )
            OutlinedTextField(
                value = state.supportingEvidence,
                onValueChange = { coordinator.updateIntakeFields(supportingEvidence = it) },
                label = { Text("Supporting evidence (public messages, dates, witnesses)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_evidence_input")
            )
            OutlinedTextField(
                value = state.knownContext,
                onValueChange = { coordinator.updateIntakeFields(knownContext = it) },
                label = { Text("Known context (recent stressors, misunderstandings)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_context_input")
            )
            OutlinedTextField(
                value = state.clarificationReason,
                onValueChange = { coordinator.updateIntakeFields(clarificationReason = it) },
                label = { Text("Why clarification or supportive outreach is requested *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bystander_clarification_input")
            )
        }
    }
}

@Composable
private fun ValidationChecklistOnlyCard(
    state: BystanderPortalUiState,
    coordinator: BystanderReferralCoordinator
) {
    val palette = LocalMapPalette.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.advisoryBlue.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_validation_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VALIDATION & VETTING CHECKLIST (ALL 7 REQUIRED)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = palette.advisoryBlue,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { coordinator.checkAllValidationItems() },
                    modifier = Modifier.testTag("bystander_check_all_btn")
                ) {
                    Text("Check All", fontSize = 11.sp)
                }
            }
            ValidationChecklistItem.entries.forEach { item ->
                val checked = state.checkedValidationItems.contains(item)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { coordinator.toggleValidationItem(item) }
                        .testTag("bystander_check_${item.id}")
                ) {
                    Checkbox(checked = checked, onCheckedChange = { coordinator.toggleValidationItem(item) })
                    Text(item.statement, fontSize = 12.sp, color = palette.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceScaleOnlyCard(
    state: BystanderPortalUiState,
    coordinator: BystanderReferralCoordinator
) {
    val palette = LocalMapPalette.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.warningAmber.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_confidence_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "CONFIDENCE SCALE (1–5)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.warningAmber
            )
            ConfidenceScaleLevel.entries.forEach { lvl ->
                val selected = state.confidenceLevel == lvl
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) palette.advisoryBlue.copy(alpha = 0.14f) else palette.canvasBackground)
                        .border(1.dp, if (selected) palette.advisoryBlue else palette.panelBorder, RoundedCornerShape(8.dp))
                        .clickable { coordinator.selectConfidenceLevel(lvl) }
                        .padding(8.dp)
                        .testTag("bystander_confidence_${lvl.level}")
                ) {
                    RadioButton(selected = selected, onClick = { coordinator.selectConfidenceLevel(lvl) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lvl.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                        Text(lvl.description, fontSize = 10.sp, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinationWorkflowOverviewCard(
    state: BystanderPortalUiState,
    coordinator: BystanderReferralCoordinator
) {
    val palette = LocalMapPalette.current
    val previewRouting = coordinator.determineCoordinationRouting(
        workflowCategory = state.activeOption,
        confidence = state.confidenceLevel,
        sources = state.selectedSourceTypes
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.dp, palette.normalGreen.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_coordination_workflow_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "4-TIER COORDINATION WORKFLOW & ANALYTICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.normalGreen
            )
            CoordinationTier.entries.forEach { tier ->
                val isPlanned = previewRouting.first == tier
                Text(
                    text = "${if (isPlanned) "▶ " else "• "}${tier.title} — ${tier.description}",
                    fontSize = 11.sp,
                    fontWeight = if (isPlanned) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPlanned) palette.advisoryBlue else palette.textSecondary
                )
            }
            Text(
                text = "Portal Analytics: ${state.submissions.size} submitted • Non-punitive clarification / resource tiers prioritized • Zero profiling violations.",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.normalGreen
            )
        }
    }
}

@Composable
private fun MandatoryAcknowledgementCard(
    state: BystanderPortalUiState,
    coordinator: BystanderReferralCoordinator
) {
    val palette = LocalMapPalette.current
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.panelSurface),
        border = BorderStroke(1.5.dp, palette.warningAmber),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bystander_mandatory_ack_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "10. MANDATORY ACKNOWLEDGEMENT & SUBMISSION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFontFamily,
                color = palette.warningAmber
            )
            BystanderPortalConstants.MANDATORY_ACKNOWLEDGEMENT_PARAGRAPHS.forEach { para ->
                Text("• $para", fontSize = 11.sp, color = palette.textPrimary, lineHeight = 16.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { coordinator.setMandatoryAcknowledgement(!state.mandatoryAcknowledgementAccepted) }
                    .padding(vertical = 4.dp)
                    .testTag("bystander_mandatory_ack_checkbox")
            ) {
                Checkbox(
                    checked = state.mandatoryAcknowledgementAccepted,
                    onCheckedChange = { coordinator.setMandatoryAcknowledgement(it) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I acknowledge and certify all statements above",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Required with all 7 validation checkboxes before submission",
                        fontSize = 10.sp,
                        color = palette.warningAmber
                    )
                }
            }
        }
    }
}
