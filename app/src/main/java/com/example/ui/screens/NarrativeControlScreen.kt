package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.AudioTextBoxStrip
import com.example.ui.components.ColorCodedActionButton
import com.example.ui.components.ForensicTopBar
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*

private enum class ComposerInputTab(val label: String) {
    SIGNAL("Compose Signal"),
    WEBHOOK_JSON("Webhook JSON"),
    FB_HANDSHAKE("FB Handshake")
}

private const val SAMPLE_FB_WEBHOOK_JSON = """{
  "object": "page",
  "entry": [
    {
      "changes": [
        {
          "field": "feed",
          "value": {
            "comment_id": "9988776655_112233",
            "from": {"id": "1004512", "name": "Lower Puna Rant Page"},
            "message": "BLIND ITEM: Who closed the 16th Ave gate near Orchidland? People saying aid ayuda trucks were turned away!"
          }
        }
      ],
      "messaging": [
        {
          "sender": {"id": "7744110022"},
          "message": {"text": "Is Highway 130 really flooded or just a rumor?", "is_echo": false}
        }
      ]
    }
  ]
}"""

private const val SAMPLE_TG_WEBHOOK_JSON = """{
  "update_id": 884210,
  "message": {
    "message_id": 412,
    "from": {"id": 551029, "username": "puna_field_watch"},
    "chat": {"id": -100192837465, "title": "Puna Situation Room"},
    "text": "URGENT SHARE: Hearing resbak chatter about convoy movement near Saddle Road tonight."
  }
}"""

@Composable
fun NarrativeControlScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    var inputTab by remember { mutableStateOf(ComposerInputTab.SIGNAL) }
    var selectedPlatform by remember { mutableStateOf(SocialPlatform.FACEBOOK) }
    var senderInput by remember { mutableStateOf("Orchidland Watch") }
    var messageInput by remember {
        mutableStateOf("Rumor spreading on Buy & Sell: resbak and road closed near Orchidland Estates tonight. Can anyone verify?")
    }
    var rawJsonInput by remember { mutableStateOf(SAMPLE_FB_WEBHOOK_JSON) }

    var verifyMode by remember { mutableStateOf("subscribe") }
    var verifyTokenInput by remember { mutableStateOf("test_verify_token") }
    var verifyChallenge by remember { mutableStateOf("challenge_94821") }

    // Track per-incident editable counter-narrative drafts
    val draftEdits = remember { mutableStateMapOf<String, String>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        ForensicTopBar(
            title = "Narrative Control",
            subtitle = "FB Graph v21.0 & Telegram Bot Triage Queue"
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("narrative_control_list")
        ) {
            // 1. Mandatory In-App Firebase AI Logic Notice Card
            item(key = "firebase_notice") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberRadar.copy(alpha = 0.14f)),
                    border = BorderStroke(1.5.dp, AmberRadar.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("narrative_firebase_notice")
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Firebase AI Logic Notice",
                            tint = AmberRadar,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SAMPLE TRIAGE ENGINE ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = JetBrainsMonoFontFamily,
                                color = AmberRadar
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gemini AI triage requires Firebase AI Logic (setup declined). Keyword triage and counter-narrative drafts below return clearly labeled Sample Responses.",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = Slate100
                            )
                        }
                    }
                }
            }

            // 2. Connector & Secrets Readiness Strip
            item(key = "connector_status_strip") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("narrative_readiness_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            ReadinessPill(
                                label = if (uiState.narrativeFbConfigured) "FB Graph v21.0: LIVE SECRET" else "FB Graph v21.0: DRY-RUN",
                                accent = if (uiState.narrativeFbConfigured) EmeraldVerified else BlueSat
                            )
                            ReadinessPill(
                                label = if (uiState.narrativeTgConfigured) "TG Bot API: LIVE SECRET" else "TG Bot API: DRY-RUN",
                                accent = if (uiState.narrativeTgConfigured) EmeraldVerified else CyanNeon
                            )
                            ReadinessPill(
                                label = "AI Engine: SAMPLE MODE",
                                accent = AmberRadar
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Set FB_VERIFY_TOKEN, FB_PAGE_ACCESS_TOKEN, and TELEGRAM_BOT_TOKEN in the AI Studio Secrets panel for live Meta/Telegram HTTP dispatch.",
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 3. Webhook & Signal Composer Card
            item(key = "signal_composer_card") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("narrative_composer_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "WEBHOOK & SIGNAL INTAKE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Mode Tabs
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ComposerInputTab.values().forEach { tab ->
                                val selected = inputTab == tab
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) CyanNeon else Slate850)
                                        .border(
                                            1.dp,
                                            if (selected) CyanNeon else DarkBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { inputTab = tab }
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                        .testTag("composer_tab_${tab.name.lowercase()}")
                                ) {
                                    Text(
                                        text = tab.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Slate950 else Slate200,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (inputTab) {
                            ComposerInputTab.SIGNAL -> {
                                // Channel Selector
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SocialPlatform.values().forEach { platform ->
                                        val isChosen = selectedPlatform == platform
                                        val col = if (platform == SocialPlatform.FACEBOOK) BlueSat else CyanNeon
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isChosen) col.copy(alpha = 0.25f) else Slate850)
                                                .border(1.5.dp, if (isChosen) col else DarkBorder, RoundedCornerShape(8.dp))
                                                .clickable { selectedPlatform = platform }
                                                .testTag("platform_chip_${platform.id}")
                                        ) {
                                            Text(
                                                text = platform.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) Slate100 else Slate300
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Preset Rumors
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    PresetChip("Puna Blind Item") {
                                        selectedPlatform = SocialPlatform.FACEBOOK
                                        senderInput = "Puna Rant Page"
                                        messageInput = "BLIND ITEM: Who authorized the road closed checkpoint near Orchidland 16th Ave? People talking about convoy alert tonight."
                                    }
                                    PresetChip("Hulidap / Ayuda Claim") {
                                        selectedPlatform = SocialPlatform.FACEBOOK
                                        senderInput = "Big Island Buy & Sell"
                                        messageInput = "Urgent share: heard ayuda food relief packets were withheld in Lower Puna unless people pledge support!"
                                    }
                                    PresetChip("TG Flood / Convoy") {
                                        selectedPlatform = SocialPlatform.TELEGRAM
                                        senderInput = "puna_watch_channel"
                                        messageInput = "Flash update: flood rumor along Hwy 130 plus resbak chatter near Pahoa junction."
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = senderInput,
                                    onValueChange = { senderInput = it },
                                    label = { Text("Source / Handle", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = narrativeFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("narrative_sender_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = messageInput,
                                    onValueChange = { messageInput = it },
                                    label = { Text("Raw Chatter / Blind Item Message", fontSize = 12.sp) },
                                    minLines = 2,
                                    maxLines = 4,
                                    colors = narrativeFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("narrative_text_input")
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                AudioTextBoxStrip(
                                    sourceId = "narrative_text_input",
                                    title = "Signal Chatter Box",
                                    textToRead = messageInput
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                ColorCodedActionButton(
                                    label = if (uiState.isNarrativeAnalyzing) "Analyzing Signal..." else "Ingest & Analyze Signal",
                                    accentColor = CyanNeon,
                                    icon = Icons.Filled.Bolt,
                                    filled = true,
                                    enabled = !uiState.isNarrativeAnalyzing,
                                    onClick = {
                                        viewModel.ingestSocialSignal(
                                            platform = selectedPlatform,
                                            senderName = senderInput,
                                            text = messageInput
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("narrative_ingest_btn")
                                )
                            }

                            ComposerInputTab.WEBHOOK_JSON -> {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SocialPlatform.values().forEach { platform ->
                                        val isChosen = selectedPlatform == platform
                                        val col = if (platform == SocialPlatform.FACEBOOK) BlueSat else CyanNeon
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isChosen) col.copy(alpha = 0.25f) else Slate850)
                                                .border(1.5.dp, if (isChosen) col else DarkBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    selectedPlatform = platform
                                                    rawJsonInput = if (platform == SocialPlatform.FACEBOOK) {
                                                        SAMPLE_FB_WEBHOOK_JSON
                                                    } else {
                                                        SAMPLE_TG_WEBHOOK_JSON
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = "${platform.label} Payload",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) Slate100 else Slate300
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = rawJsonInput,
                                    onValueChange = { rawJsonInput = it },
                                    label = { Text("Raw Webhook JSON (Meta Page or Telegram Update)", fontSize = 12.sp) },
                                    minLines = 4,
                                    maxLines = 7,
                                    colors = narrativeFieldColors(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("narrative_webhook_json_input")
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                AudioTextBoxStrip(
                                    sourceId = "narrative_webhook_json",
                                    title = "Webhook JSON Box",
                                    textToRead = rawJsonInput
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                ColorCodedActionButton(
                                    label = "Parse & Ingest Webhook",
                                    accentColor = VioletVector,
                                    icon = Icons.Filled.Webhook,
                                    filled = true,
                                    enabled = !uiState.isNarrativeAnalyzing,
                                    onClick = {
                                        viewModel.ingestRawWebhookJson(selectedPlatform, rawJsonInput)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("narrative_parse_webhook_btn")
                                )
                            }

                            ComposerInputTab.FB_HANDSHAKE -> {
                                Text(
                                    text = "Simulates Meta GET /webhook/facebook verification against configured FB_VERIFY_TOKEN in Secrets.",
                                    fontSize = 11.sp,
                                    color = Slate300
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = verifyMode,
                                        onValueChange = { verifyMode = it },
                                        label = { Text("hub.mode", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = narrativeFieldColors(),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = verifyChallenge,
                                        onValueChange = { verifyChallenge = it },
                                        label = { Text("hub.challenge", fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = narrativeFieldColors(),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = verifyTokenInput,
                                    onValueChange = { verifyTokenInput = it },
                                    label = { Text("Incoming hub.verify_token query param", fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = narrativeFieldColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                ColorCodedActionButton(
                                    label = "Verify Webhook Handshake",
                                    accentColor = EmeraldVerified,
                                    icon = Icons.Filled.VerifiedUser,
                                    filled = true,
                                    onClick = {
                                        viewModel.verifyFacebookWebhookChallenge(
                                            mode = verifyMode,
                                            token = verifyTokenInput,
                                            challenge = verifyChallenge
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("narrative_verify_btn")
                                )
                                if (uiState.webhookVerifyStatus != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.webhookVerifyStatus,
                                        fontSize = 11.sp,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        color = if (uiState.webhookVerifyStatus.startsWith("HTTP 200")) EmeraldVerified else AmberRadar
                                    )
                                }
                            }
                        }

                        if (uiState.narrativeError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CrimsonThreat.copy(alpha = 0.16f))
                                    .border(1.dp, CrimsonThreat.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = uiState.narrativeError,
                                    fontSize = 11.sp,
                                    color = CrimsonThreat,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.clearNarrativeError() }) {
                                    Text("Dismiss", fontSize = 11.sp, color = Slate100)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Queue Section Header
            item(key = "queue_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TRIAGE & COUNTER-NARRATIVE QUEUE (${uiState.narrativeIncidents.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = Slate200
                    )
                }
            }

            // 5. Queue Cards
            if (uiState.narrativeIncidents.isEmpty()) {
                item(key = "queue_empty") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MarkChatRead,
                                contentDescription = "Empty queue",
                                tint = Slate400,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No signals in queue yet. Ingest a Facebook or Telegram signal above.",
                                fontSize = 12.sp,
                                color = Slate300
                            )
                        }
                    }
                }
            } else {
                items(items = uiState.narrativeIncidents, key = { it.id }) { incident ->
                    val editedDraft = draftEdits[incident.id]
                        ?: incident.triagePackage.drafts.communityReply

                    NarrativeIncidentCard(
                        incident = incident,
                        editedReply = editedDraft,
                        onReplyChange = { draftEdits[incident.id] = it },
                        onApproveAndDispatch = {
                            viewModel.approveAndDispatchNarrative(incident.id, editedDraft)
                        }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NarrativeIncidentCard(
    incident: NarrativeIncidentRecord,
    editedReply: String,
    onReplyChange: (String) -> Unit,
    onApproveAndDispatch: () -> Unit
) {
    val pkg = incident.triagePackage
    val triage = pkg.triage
    val sig = incident.signal
    val platformColor = if (sig.platform == SocialPlatform.FACEBOOK) BlueSat else CyanNeon
    val classColor = when (triage.classification) {
        TriageClassification.VIRAL_DISINFORMATION -> CrimsonThreat
        TriageClassification.ACTIONABLE_BLIND_ITEM -> AmberRadar
        TriageClassification.LOCAL_DISPUTE_FRAMING -> VioletVector
        TriageClassification.EMERGING_GRIEVANCE -> BlueSat
        TriageClassification.LOW_SIGNAL_NOISE -> Slate400
    }

    val a11y = LocalAccessibilityAudio.current
    val reportSourceId = "narrative_report_${incident.id}"
    val isSpeakingReport = a11y.playbackState.activeSourceId == reportSourceId && a11y.playbackState.isActive

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = BorderStroke(
            1.5.dp,
            visualAudioHighlightBorder(isSpeakingReport, a11y.visualAudioEnabled, classColor.copy(alpha = 0.55f))
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("narrative_card_${incident.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                ReadinessPill(
                    label = "${sig.platform.label.uppercase()} • ${sig.replyType.uppercase()}",
                    accent = platformColor
                )
                ReadinessPill(
                    label = triage.classification.label,
                    accent = classColor
                )
                ReadinessPill(
                    label = "Virality ${triage.viralityScore}/10",
                    accent = if (triage.viralityScore >= 7) CrimsonThreat else AmberRadar
                )
                ReadinessPill(
                    label = "Fidelity ${triage.fidelityScore}/10",
                    accent = EmeraldVerified
                )
                if (pkg.isSamplePlaceholder) {
                    ReadinessPill(label = "SAMPLE", accent = AmberRadar)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${sig.senderName}  •  Target: ${sig.targetId}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )
            Text(
                text = incident.timestampIso,
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFontFamily,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Original Inbound Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate950)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "\"${sig.text}\"",
                    fontSize = 12.sp,
                    color = Slate200,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Decoded Summary + Keywords
            Text(
                text = triage.decodedSummary,
                fontSize = 12.sp,
                color = CyanNeon,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                triage.detectedKeywords.forEach { kw ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate850)
                            .border(1.dp, VioletVector.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "#$kw",
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMonoFontFamily,
                            color = VioletVector
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ground Check: ${pkg.drafts.groundVerificationQuestion}",
                fontSize = 11.sp,
                color = Slate300,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            AudioSelectButton(
                sourceId = reportSourceId,
                title = "${sig.platform.label} Triage Report",
                text = "${sig.platform.label} signal from ${sig.senderName}, classified ${triage.classification.label}, virality ${triage.viralityScore} out of 10. Original message: ${sig.text}. Decoded summary: ${triage.decodedSummary}. Ground check: ${pkg.drafts.groundVerificationQuestion}",
                compact = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editedReply,
                onValueChange = onReplyChange,
                label = { Text("Editable Counter-Narrative Reply", fontSize = 11.sp) },
                minLines = 2,
                maxLines = 4,
                colors = narrativeFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("narrative_reply_input_${incident.id}")
            )

            Spacer(modifier = Modifier.height(6.dp))
            AudioTextBoxStrip(
                sourceId = "narrative_reply_${incident.id}",
                title = "Counter-Narrative Box",
                textToRead = editedReply
            )

            Spacer(modifier = Modifier.height(10.dp))

            ColorCodedActionButton(
                label = when (incident.status) {
                    IncidentQueueStatus.DISPATCHING -> "Dispatching..."
                    IncidentQueueStatus.DISPATCHED -> "Re-Send Approved Reply"
                    else -> "Approve & Dispatch Reply"
                },
                accentColor = if (incident.status == IncidentQueueStatus.DISPATCHED) EmeraldVerified else CyanNeon,
                icon = Icons.AutoMirrored.Filled.Send,
                filled = true,
                enabled = incident.status != IncidentQueueStatus.DISPATCHING,
                onClick = onApproveAndDispatch,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("narrative_approve_btn_${incident.id}")
            )

            if (incident.dispatchResultNote != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val bannerColor = if (incident.status == IncidentQueueStatus.DISPATCH_ERROR) {
                    CrimsonThreat
                } else {
                    EmeraldVerified
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerColor.copy(alpha = 0.14f))
                        .border(1.dp, bannerColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .testTag("narrative_dispatch_status_${incident.id}")
                ) {
                    Text(
                        text = "${incident.status.label}: ${incident.dispatchResultNote}",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMonoFontFamily,
                        color = bannerColor,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadinessPill(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.18f))
            .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMonoFontFamily,
            color = accent,
            maxLines = 1
        )
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Slate850)
            .border(1.dp, AmberRadar.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AmberRadar,
            maxLines = 1
        )
    }
}

@Composable
private fun narrativeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyanNeon,
    unfocusedBorderColor = DarkBorder,
    focusedTextColor = Slate100,
    unfocusedTextColor = Slate100,
    focusedLabelColor = CyanNeon,
    unfocusedLabelColor = Slate400,
    focusedContainerColor = Slate950,
    unfocusedContainerColor = Slate950
)
