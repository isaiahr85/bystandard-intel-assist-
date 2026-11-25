package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.AudioTextBoxStrip
import com.example.ui.components.ForensicTopBar
import com.example.ui.components.VerificationStatusBadge
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasebookScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val filteredCases = remember(uiState.cases, uiState.caseSearchQuery, uiState.caseStatusFilter) {
        uiState.cases.filter { c ->
            val matchesStatus = uiState.caseStatusFilter == null || c.status == uiState.caseStatusFilter
            val matchesQuery = uiState.caseSearchQuery.isBlank() ||
                c.title.contains(uiState.caseSearchQuery, ignoreCase = true) ||
                c.caseNumber.contains(uiState.caseSearchQuery, ignoreCase = true) ||
                c.locationName.contains(uiState.caseSearchQuery, ignoreCase = true) ||
                c.summary.contains(uiState.caseSearchQuery, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "Evidence Casebook",
                subtitle = "Chain-of-Custody, Provenance & Multi-Sensor Triangulation"
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setCreatingNewCase(true) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null, tint = Slate950) },
                text = { Text("Log New Case", fontWeight = FontWeight.Bold, color = Slate950) },
                containerColor = CyanNeon,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("log_case_fab")
            )
        },
        containerColor = Slate950,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.caseSearchQuery,
                    onValueChange = { viewModel.updateCaseSearchQuery(it) },
                    placeholder = { Text("Search case ID, sector, Orchidland, satellite...", fontSize = 12.sp, color = Slate400) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Cases", tint = CyanNeon) },
                    trailingIcon = {
                        if (uiState.caseSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateCaseSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate300)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("casebook_search_input")
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statuses: List<CaseStatus?> = listOf(null) + CaseStatus.values().toList()
                    statuses.forEach { status ->
                        val selected = uiState.caseStatusFilter == status
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setCaseStatusFilter(status) },
                            label = {
                                Text(
                                    text = status?.label ?: "All (${uiState.cases.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
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
                                selected = selected,
                                borderColor = DarkBorder,
                                selectedBorderColor = CyanNeon
                            ),
                            modifier = Modifier.testTag("case_filter_${status?.name?.lowercase() ?: "all"}")
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE DOSSIERS (${filteredCases.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ISO 27037 Standard",
                        fontSize = 10.sp,
                        color = EmeraldVerified,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(filteredCases, key = { it.id }) { forensicCase ->
                CaseCard(
                    forensicCase = forensicCase,
                    onSelect = { viewModel.selectCase(forensicCase) }
                )
            }
        }
    }

    // Case Details Bottom Sheet
    uiState.selectedCase?.let { forensicCase ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectCase(null) },
            containerColor = DarkSurface,
            contentColor = Slate100,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Slate400) }
        ) {
            CaseDetailContent(
                forensicCase = forensicCase,
                onDismiss = { viewModel.selectCase(null) },
                onInspectSatFeed = { viewModel.inspectCaseInSatFeed(forensicCase) },
                onSendToAiIntel = { viewModel.sendCaseToAiIntel(forensicCase) },
                onUpdateStatus = { newStatus -> viewModel.updateCaseStatus(forensicCase.id, newStatus) },
                onAppendIntel = { title, sensor, note ->
                    viewModel.appendCaseIntelUpdate(forensicCase.id, title, note, sensor)
                }
            )
        }
    }

    // New Case Dialog Modal
    if (uiState.isCreatingNewCase) {
        NewCaseDialog(
            onDismiss = { viewModel.setCreatingNewCase(false) },
            onConfirm = { title, location, lat, lon, scope, weapon, summary ->
                viewModel.addNewCase(title, location, lat, lon, scope, weapon, summary)
            }
        )
    }
}

@Composable
fun CaseCard(
    forensicCase: ForensicCase,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val a11y = LocalAccessibilityAudio.current
    val caseAudioId = "case_${forensicCase.caseNumber.lowercase(Locale.US)}"
    val isSpeaking = a11y.playbackState.activeSourceId == caseAudioId && a11y.playbackState.isActive

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    visualAudioHighlightBorder(isSpeaking, a11y.visualAudioEnabled, DarkBorder),
                    if (isSpeaking) EmeraldVerified else Slate800
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("case_card_${forensicCase.caseNumber.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyanNeon.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = forensicCase.caseNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (forensicCase.status) {
                                CaseStatus.COURT_ADMISSIBLE -> EmeraldVerified.copy(alpha = 0.2f)
                                CaseStatus.CORROBORATED -> CyanNeon.copy(alpha = 0.15f)
                                CaseStatus.OPEN -> AmberRadar.copy(alpha = 0.2f)
                                CaseStatus.UNDER_REVIEW -> Slate400.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = forensicCase.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (forensicCase.status) {
                            CaseStatus.COURT_ADMISSIBLE -> EmeraldVerified
                            CaseStatus.CORROBORATED -> CyanNeon
                            CaseStatus.OPEN -> AmberRadar
                            CaseStatus.UNDER_REVIEW -> Slate200
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = forensicCase.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Slate100
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AmberRadar,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${forensicCase.locationName} (${String.format(Locale.US, "%.4f", forensicCase.latitude)}, ${String.format(Locale.US, "%.4f", forensicCase.longitude)})",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = forensicCase.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Evidence count and Triangulation badge
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${forensicCase.evidenceItems.size} Evidence Items • ${forensicCase.chainOfCustody.size} Custody Events",
                    fontSize = 11.sp,
                    color = Slate200
                )
                Text(
                    text = forensicCase.corroborationTier,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanNeon
                )
            }

            if (a11y.visualAudioEnabled || isSpeaking) {
                Spacer(modifier = Modifier.height(8.dp))
                AudioSelectButton(
                    sourceId = caseAudioId,
                    title = "${forensicCase.caseNumber} Summary",
                    text = "Case ${forensicCase.caseNumber}, ${forensicCase.status.label}. ${forensicCase.title} at ${forensicCase.locationName}. ${forensicCase.summary}",
                    compact = true
                )
            }
        }
    }
}

@Composable
fun CaseDetailContent(
    forensicCase: ForensicCase,
    onDismiss: () -> Unit,
    onInspectSatFeed: () -> Unit = {},
    onSendToAiIntel: () -> Unit = {},
    onUpdateStatus: (CaseStatus) -> Unit = {},
    onAppendIntel: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var showIntelUpdateForm by remember { mutableStateOf(false) }
    var updateTitle by remember { mutableStateOf("") }
    var updateSensor by remember { mutableStateOf("NASA GIBS / Field SITREP") }
    var updateNote by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = forensicCase.caseNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = forensicCase.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Slate100
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                }
            }
        }

        item {
            // Dossier Metadata Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate850)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Incident Date:", fontSize = 11.sp, color = Slate400)
                        Text(forensicCase.incidentDate, fontSize = 11.sp, color = Slate100, fontWeight = FontWeight.Medium)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("GPS Coordinates (6-dec):", fontSize = 11.sp, color = Slate400)
                        Text("${forensicCase.latitude}, ${forensicCase.longitude}", fontSize = 11.sp, color = AmberRadar, fontFamily = FontFamily.Monospace)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Casualty Estimate:", fontSize = 11.sp, color = Slate400)
                        Text(forensicCase.casualtyEstimate, fontSize = 11.sp, color = Slate100)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Corroboration Tier:", fontSize = 11.sp, color = Slate400)
                        Text(forensicCase.corroborationTier, fontSize = 11.sp, color = EmeraldVerified, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    AudioSelectButton(
                        sourceId = "case_dossier_${forensicCase.caseNumber.lowercase(Locale.US)}",
                        title = "Dossier ${forensicCase.caseNumber}",
                        text = "Forensic Dossier ${forensicCase.caseNumber}: ${forensicCase.title}. Location ${forensicCase.locationName}, incident date ${forensicCase.incidentDate}, status ${forensicCase.status.label}, corroboration ${forensicCase.corroborationTier}. ${forensicCase.summary}",
                        compact = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Case Status Update Chips
            Text(
                text = "UPDATE DOSSIER STATUS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CaseStatus.values().forEach { status ->
                    val isSel = forensicCase.status == status
                    FilterChip(
                        selected = isSel,
                        onClick = { onUpdateStatus(status) },
                        label = { Text(status.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldVerified,
                            selectedLabelColor = Slate950,
                            containerColor = Slate900,
                            labelColor = Slate200
                        ),
                        modifier = Modifier.testTag("case_status_chip_${status.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onInspectSatFeed,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(CyanNeon, BlueSat))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("case_inspect_sat_btn")
            ) {
                Icon(Icons.Default.SatelliteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Inspect Coordinates in Live NASA Sat Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onSendToAiIntel,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletVector),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(VioletVector, CyanNeon))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("case_send_to_ai_intel_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Intel STAC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { showIntelUpdateForm = !showIntelUpdateForm },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberRadar, contentColor = Slate950),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("case_toggle_intel_update_btn")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showIntelUpdateForm) "Hide Update" else "+ Intel Update", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (showIntelUpdateForm) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate900)
                        .border(1.dp, AmberRadar.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    OutlinedTextField(
                        value = updateTitle,
                        onValueChange = { updateTitle = it },
                        label = { Text("Intel Update Title", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = updateSensor,
                        onValueChange = { updateSensor = it },
                        label = { Text("Sensor / Source", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = updateNote,
                        onValueChange = { updateNote = it },
                        label = { Text("Field Observation / Satellite Delta", fontSize = 11.sp) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AudioTextBoxStrip(
                        sourceId = "case_intel_note_input",
                        title = "Field Intel Note Box",
                        textToRead = updateNote
                    )
                    Button(
                        onClick = {
                            onAppendIntel(updateTitle, updateSensor, updateNote)
                            updateTitle = ""
                            updateNote = ""
                            showIntelUpdateForm = false
                        },
                        enabled = updateNote.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldVerified, contentColor = Slate950),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("case_append_intel_btn")
                    ) {
                        Text("Commit Intel & Custody Entry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "EVIDENCE REPOSITORY LOCKER (${forensicCase.evidenceItems.size})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
        }

        items(forensicCase.evidenceItems) { item ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100,
                            modifier = Modifier.weight(1f)
                        )
                        VerificationStatusBadge(status = item.verificationStatus)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Device / Sensor: ${item.sensorOrDevice}",
                        fontSize = 11.sp,
                        color = CyanNeon
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SHA-256: ${item.hashSha256}",
                        fontSize = 9.sp,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    item.technicalAttributes.forEach { (k, v) ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(k, fontSize = 10.sp, color = Slate400)
                            Text(v, fontSize = 10.sp, color = Slate200, fontFamily = FontFamily.Monospace)
                        }
                    }

                    if (item.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Notes: ${item.notes}",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AudioSelectButton(
                        sourceId = "evidence_${item.id}",
                        title = item.title,
                        text = "Evidence item ${item.title}, status ${item.verificationStatus.label}, sensor ${item.sensorOrDevice}. ${item.notes}",
                        compact = true
                    )
                }
            }
        }

        item {
            Text(
                text = "IMMUTABLE CHAIN-OF-CUSTODY AUDIT LOG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
        }

        items(forensicCase.chainOfCustody) { log ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CyanNeon)
                        .padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${log.custodian} (${log.organization})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                        Text(
                            text = log.timestamp,
                            fontSize = 10.sp,
                            color = Slate400,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = log.actionTaken,
                        fontSize = 11.sp,
                        color = Slate200
                    )
                    Text(
                        text = "Digital Sig: ${log.digitalSignatureHash}",
                        fontSize = 9.sp,
                        color = EmeraldVerified,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun NewCaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, OperationalScope, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitudeStr by remember { mutableStateOf("19.5614") }
    var longitudeStr by remember { mutableStateOf("-155.0131") }
    var scope by remember { mutableStateOf(OperationalScope.URBAN_CONFLICT) }
    var weapon by remember { mutableStateOf("155mm Artillery Fragment / Remnant") }
    var summary by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Intake New Forensic Evidence Dossier",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Case Title", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Bridge Strike Structural Collapse", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Incident Location Name", fontSize = 11.sp) },
                    placeholder = { Text("Sector 7 Bridge Crossing", fontSize = 11.sp) },
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
                        value = latitudeStr,
                        onValueChange = {
                            latitudeStr = it
                            validationError = null
                        },
                        label = { Text("Latitude", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitudeStr,
                        onValueChange = {
                            longitudeStr = it
                            validationError = null
                        },
                        label = { Text("Longitude", fontSize = 11.sp) },
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
                    value = weapon,
                    onValueChange = { weapon = it },
                    label = { Text("Weapon Indicator / Munition", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Investigation Summary", fontSize = 11.sp) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Slate100,
                        unfocusedTextColor = Slate100
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                AudioTextBoxStrip(
                    sourceId = "new_case_summary_input",
                    title = "Summary Box",
                    textToRead = summary
                )

                validationError?.let { err ->
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonThreat
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latitudeStr.trim().toDoubleOrNull()
                    val lon = longitudeStr.trim().toDoubleOrNull()
                    when {
                        title.isBlank() -> {
                            validationError = "Case Title is required."
                        }
                        lat == null || !lat.isFinite() || lat !in -90.0..90.0 -> {
                            validationError = "Latitude must be between -90 and 90."
                        }
                        lon == null || !lon.isFinite() || lon !in -180.0..180.0 -> {
                            validationError = "Longitude must be between -180 and 180."
                        }
                        else -> {
                            onConfirm(
                                title.trim(),
                                locationName.trim().ifBlank { "Orchidland / Puna Sector" },
                                lat,
                                lon,
                                scope,
                                weapon.trim().ifBlank { "Unclassified Indicator" },
                                summary.trim()
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950)
            ) {
                Text("Open Case Locker", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate400)
            }
        }
    )
}
