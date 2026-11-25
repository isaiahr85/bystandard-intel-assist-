package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrchidlandDicmData
import com.example.model.*
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.hypot

@Composable
fun CorrelateAndSnaContent(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    val report = uiState.snaNetworkReport

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitleBanner(
            tag = "PHASE 3 • CORRELATE & MULTI-INT FUSION",
            title = "Hybrid Cyber-Physical SNA Graph",
            subtitle = "Expose High-Degree Swarm Traps vs. Low-Degree / High-Betweenness Bridges"
        )

        // Live SNA Telemetry Strip
        val a11y = LocalAccessibilityAudio.current
        val snaSpeaking = a11y.playbackState.activeSourceId == "sna_report" && a11y.playbackState.isActive
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        visualAudioHighlightBorder(
                            snaSpeaking,
                            a11y.visualAudioEnabled,
                            if (report.fragmentationPercent >= 50f) EmeraldVerified else CyanNeon
                        ),
                        if (snaSpeaking) EmeraldVerified else DarkBorder
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sna_metrics_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SnaKpiCell("ACTIVE NODES", "${report.totalActiveNodes}/${OrchidlandDicmData.graphNodes.size}", CyanNeon)
                    SnaKpiCell("ACTIVE EDGES", "${report.totalActiveEdges}", Slate200)
                    SnaKpiCell("COMPONENTS", "${report.connectedComponents}", AmberRadar)
                    SnaKpiCell(
                        "FRAGMENTATION",
                        String.format(Locale.US, "%.0f%%", report.fragmentationPercent),
                        if (report.fragmentationPercent >= 50f) EmeraldVerified else CrimsonThreat
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = report.tacticalAssessment,
                    fontSize = 11.sp,
                    color = Slate100,
                    lineHeight = 15.sp
                )
                if (a11y.visualAudioEnabled || snaSpeaking) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AudioSelectButton(
                        sourceId = "sna_report",
                        title = "SNA Network Report",
                        text = "Active nodes ${report.totalActiveNodes} of ${OrchidlandDicmData.graphNodes.size}, ${report.connectedComponents} connected components, ${String.format(Locale.US, "%.0f", report.fragmentationPercent)} percent fragmentation. ${report.tacticalAssessment}",
                        compact = true
                    )
                }
            }
        }

        // One-tap SNA Experiment Controls: Trap vs Bridge (High-Contrast Color-Coded)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { viewModel.applySnaPresetTrapVsBridge(isolateSwarmTrap = true) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberRadar,
                    contentColor = Slate950
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .testTag("sna_test_trap_btn")
            ) {
                Text("Test FB Trap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.applySnaPresetTrapVsBridge(isolateSwarmTrap = false) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonThreat, contentColor = Slate950),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .testTag("sna_sever_bridge_btn")
            ) {
                Text("Sever Bridges", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { viewModel.resetSnaGraph() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Slate850,
                    contentColor = Slate100
                ),
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("sna_reset_btn")
            ) {
                Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // View Switcher: Node-Link vs Adjacency Matrix
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(false to "Node-Link Graph", true to "Adjacency Matrix").forEach { (isMatrix, title) ->
                val selected = uiState.snaMatrixMode == isMatrix
                val accent = if (isMatrix) VioletVector else CyanNeon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) accent else DarkSurface)
                        .border(1.5.dp, if (selected) accent else accent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .clickable { viewModel.setSnaMatrixMode(isMatrix) }
                        .padding(vertical = 8.dp)
                        .testTag(if (isMatrix) "sna_mode_matrix" else "sna_mode_graph")
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Slate950 else Slate100
                    )
                }
            }
        }

        if (!uiState.snaMatrixMode) {
            SnaNodeLinkCanvas(
                uiState = uiState,
                onSelectNode = { viewModel.selectGraphNode(it) }
            )
        } else {
            SnaAdjacencyMatrixCanvas(
                isolatedIds = uiState.isolatedGraphNodeIds
            )
        }

        // Selected Node Inspector & Isolate Action
        val selectedNode = OrchidlandDicmData.graphNodes.firstOrNull { it.id == uiState.selectedGraphNodeId }
            ?: OrchidlandDicmData.graphNodes.first()
        val selectedMetric = report.nodeMetrics.firstOrNull { it.node.id == selectedNode.id }
        val isIsolated = uiState.isolatedGraphNodeIds.contains(selectedNode.id)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(if (isIsolated) CrimsonThreat else CyanNeon, DarkBorder)
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedNode.cryptoId} • ${selectedNode.nodeClass.label}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = selectedNode.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleIsolateGraphNode(selectedNode.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIsolated) EmeraldVerified else CrimsonThreat,
                            contentColor = Slate950
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("sna_isolate_selected_btn")
                    ) {
                        Text(
                            text = if (isIsolated) "Restore Node" else "Sever / Isolate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = selectedNode.roleSummary, fontSize = 11.sp, color = Slate300)
                if (selectedMetric != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s | Degree=%d (%.2f) • Betweenness=%.2f • Closeness=%.2f",
                            selectedMetric.diagnosticTag,
                            selectedMetric.activeDegree,
                            selectedMetric.degreeNorm,
                            selectedMetric.betweennessNorm,
                            selectedMetric.closenessNorm
                        ),
                        fontSize = 10.sp,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Ranked Betweenness Leaderboard (Tap to Select)
        SectionTitleBanner(
            tag = "NETWORKX / GEPHI RANKING",
            title = "Computed Betweenness vs. Degree Ranking",
            subtitle = "Nodes sorted by Shortest-Path Betweenness Centrality"
        )

        report.nodeMetrics.forEach { metric ->
            val selected = metric.node.id == selectedNode.id
            val nodeColor = nodeClassColor(metric.node.nodeClass)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Slate850 else DarkSurface)
                    .border(
                        1.dp,
                        if (selected) CyanNeon else DarkBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { viewModel.selectGraphNode(metric.node.id) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(nodeColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${metric.node.label} (${metric.node.cryptoId})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    Text(
                        text = metric.diagnosticTag,
                        fontSize = 10.sp,
                        color = if (metric.betweennessNorm >= 0.6f) CrimsonThreat else AmberRadar
                    )
                }
                Text(
                    text = String.format(Locale.US, "B:%.2f | D:%d", metric.betweennessNorm, metric.activeDegree),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SnaNodeLinkCanvas(
    uiState: ForensicUiState,
    onSelectNode: (String) -> Unit
) {
    val nodes = OrchidlandDicmData.graphNodes
    val edges = OrchidlandDicmData.graphEdges
    val isolated = uiState.isolatedGraphNodeIds
    val metricMap = uiState.snaNetworkReport.nodeMetrics.associateBy { it.node.id }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate950),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.45f), DarkBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HYBRID CYBER-PHYSICAL GRAPH (TAP NODE)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Size = Betweenness",
                    fontSize = 10.sp,
                    color = Slate400
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate900)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(nodes) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val nearest = nodes.minByOrNull { node ->
                                    hypot(offset.x - node.xNorm * w, offset.y - node.yNorm * h)
                                }
                                if (nearest != null) {
                                    val dist = hypot(offset.x - nearest.xNorm * w, offset.y - nearest.yNorm * h)
                                    if (dist <= 64f) onSelectNode(nearest.id)
                                }
                            }
                        }
                ) {
                    val nodePos = nodes.associate { it.id to Offset(it.xNorm * size.width, it.yNorm * size.height) }

                    // Edges
                    edges.forEach { edge ->
                        val p1 = nodePos[edge.fromId] ?: return@forEach
                        val p2 = nodePos[edge.toId] ?: return@forEach
                        val severed = isolated.contains(edge.fromId) || isolated.contains(edge.toId)
                        val edgeColor = when {
                            severed -> Slate700.copy(alpha = 0.25f)
                            edge.edgeClass == GraphEdgeClass.COMMAND -> CrimsonThreat.copy(alpha = 0.75f)
                            edge.edgeClass == GraphEdgeClass.PHYSICAL -> EmeraldVerified.copy(alpha = 0.75f)
                            edge.edgeClass == GraphEdgeClass.FINANCE -> AmberRadar.copy(alpha = 0.8f)
                            else -> CyanNeon.copy(alpha = 0.45f)
                        }
                        drawLine(
                            color = edgeColor,
                            start = p1,
                            end = p2,
                            strokeWidth = if (severed) 1.5f else 3f * edge.weight,
                            pathEffect = if (severed) PathEffect.dashPathEffect(floatArrayOf(6f, 8f)) else null
                        )
                    }

                    // Nodes
                    nodes.forEach { node ->
                        val p = nodePos[node.id] ?: return@forEach
                        val isIso = isolated.contains(node.id)
                        val isSel = uiState.selectedGraphNodeId == node.id
                        val betw = metricMap[node.id]?.betweennessNorm ?: 0.1f
                        val radius = if (isIso) 11f else 12f + betw * 14f
                        val color = if (isIso) Slate700 else nodeClassColor(node.nodeClass)

                        if (isSel) {
                            drawCircle(
                                color = CyanNeon.copy(alpha = 0.35f),
                                radius = radius + 8f,
                                center = p
                            )
                        }
                        drawCircle(color = color, radius = radius, center = p)
                        drawCircle(
                            color = if (isIso) CrimsonThreat else Slate100.copy(alpha = 0.75f),
                            radius = radius,
                            center = p,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isSel) 3f else 1.5f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LegendDot("Cyber/Bot", CyanNeon)
                LegendDot("Proxy/WABA", AmberRadar)
                LegendDot("Human", CrimsonThreat)
                LegendDot("Terrain/RF", EmeraldVerified)
            }
        }
    }
}

@Composable
private fun SnaAdjacencyMatrixCanvas(isolatedIds: Set<String>) {
    val nodes = OrchidlandDicmData.graphNodes
    val edgeLookup = OrchidlandDicmData.graphEdges.flatMap {
        listOf((it.fromId to it.toId) to it.edgeClass, (it.toId to it.fromId) to it.edgeClass)
    }.toMap()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate950),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(VioletVector.copy(alpha = 0.5f), DarkBorder))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "CLUSTERED ADJACENCY MATRIX (${nodes.size}x${nodes.size} HYBRID TOPOLOGY)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = VioletVector,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate900)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val n = nodes.size
                    val cellW = size.width / n
                    val cellH = size.height / n
                    for (r in 0 until n) {
                        for (c in 0 until n) {
                            val nr = nodes[r]
                            val nc = nodes[c]
                            val edgeType = edgeLookup[nr.id to nc.id]
                            val isIso = isolatedIds.contains(nr.id) || isolatedIds.contains(nc.id)
                            val cellColor = when {
                                r == c -> Slate700.copy(alpha = 0.45f)
                                edgeType == null -> Slate850
                                isIso -> CrimsonThreat.copy(alpha = 0.18f)
                                edgeType == GraphEdgeClass.COMMAND -> CrimsonThreat
                                edgeType == GraphEdgeClass.FINANCE -> AmberRadar
                                edgeType == GraphEdgeClass.PHYSICAL -> EmeraldVerified
                                else -> CyanNeon
                            }
                            drawRect(
                                color = cellColor,
                                topLeft = Offset(c * cellW + 1f, r * cellH + 1f),
                                size = Size((cellW - 2f).coerceAtLeast(1f), (cellH - 2f).coerceAtLeast(1f))
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Upper-left block = dense FB swarm clique (AI-SRX). Center-right = sparse high-betweenness bridges (AI-AUT / WABA / HUM-OPR).",
                fontSize = 10.sp,
                color = Slate400
            )
        }
    }
}

@Composable
fun MitigateAndRoeContent(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState
) {
    val deployedWeight = OrchidlandDicmData.nistCountermeasures
        .filter { uiState.deployedNistIds.contains(it.id) }
        .sumOf { it.impactScore }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitleBanner(
            tag = "PHASE 4 • ADAPTED NIST CSF 2.0",
            title = "Active Cyber-Physical Mitigation Controls",
            subtitle = "Toggle countermeasures to model readiness against FB/Snap/WhatsApp/LoRa cells"
        )

        val a11y = LocalAccessibilityAudio.current
        val mitSpeaking = a11y.playbackState.activeSourceId == "mitigation_brief" && a11y.playbackState.isActive
        val deployedTitles = OrchidlandDicmData.nistCountermeasures
            .filter { uiState.deployedNistIds.contains(it.id) }
            .joinToString(", ") { it.title }
            .ifBlank { "none deployed yet" }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Slate850),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        visualAudioHighlightBorder(mitSpeaking, a11y.visualAudioEnabled, EmeraldVerified.copy(alpha = 0.5f)),
                        if (mitSpeaking) EmeraldVerified else DarkBorder
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MITIGATION READINESS INDEX",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldVerified,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$deployedWeight% / 100%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                        AudioSelectButton(
                            sourceId = "mitigation_brief",
                            title = "Mitigation Readiness Brief",
                            text = "NIST CSF 2.0 Mitigation Readiness Index is $deployedWeight percent. Active countermeasures: $deployedTitles. ROE compliance guardrails checked: ${uiState.checkedGuardrailIds.size} of ${OrchidlandDicmData.complianceGuardrails.size}.",
                            compact = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (deployedWeight / 100f).coerceIn(0f, 1f) },
                    color = EmeraldVerified,
                    trackColor = Slate800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        OrchidlandDicmData.nistCountermeasures.forEach { nist ->
            val isDeployed = uiState.deployedNistIds.contains(nist.id)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDeployed) Slate850 else DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(if (isDeployed) EmeraldVerified else DarkBorder, Slate800)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleNistCountermeasure(nist.id) }
                    .testTag("nist_toggle_${nist.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = nist.nistPhase,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeployed) EmeraldVerified else AmberRadar,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = nist.targetClass,
                                fontSize = 10.sp,
                                color = CyanNeon
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = nist.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = nist.mechanism, fontSize = 11.sp, color = Slate300, lineHeight = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isDeployed,
                        onCheckedChange = { viewModel.toggleNistCountermeasure(nist.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = EmeraldVerified
                        )
                    )
                }
            }
        }

        // 6-Phase Joint Targeting Cycle
        SectionTitleBanner(
            tag = "6-PHASE JOINT TARGETING CYCLE",
            title = "Synchronized Cyber & Puna Field Execution",
            subtitle = "Mapping digital Context-Setter bans with lawful Hwy 130 / HPD actions"
        )

        OrchidlandDicmData.jointTargetingSteps.forEach { step ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(DarkBorder, Slate800))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "P${step.phaseNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate100
                        )
                    }
                    Text(text = "Lead: ${step.leadRole}", fontSize = 10.sp, color = AmberRadar)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = step.cyberAction, fontSize = 11.sp, color = Slate200)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Guardrail: ${step.orchidlandGuardrail}",
                        fontSize = 10.sp,
                        color = EmeraldVerified
                    )
                }
            }
        }

        // Legal & ROD Biosecurity Checklist
        SectionTitleBanner(
            tag = "ORCHIDLAND LEGAL & BIOSECURITY ROE",
            title = "Mandatory Compliance Guardrails (${uiState.checkedGuardrailIds.size}/${OrchidlandDicmData.complianceGuardrails.size})",
            subtitle = "Zero-suppression warrant boundaries, Part 107 & Rapid ʻŌhiʻa Death decon"
        )

        OrchidlandDicmData.complianceGuardrails.forEach { rule ->
            val checked = uiState.checkedGuardrailIds.contains(rule.id)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(if (checked) EmeraldVerified.copy(alpha = 0.6f) else AmberRadar.copy(alpha = 0.4f), DarkBorder)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleGuardrailCheck(rule.id) }
                    .testTag("guardrail_${rule.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { viewModel.toggleGuardrailCheck(rule.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldVerified,
                            checkmarkColor = Slate950
                        )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.statuteOrStandard,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(text = rule.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate100)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = rule.requirement, fontSize = 11.sp, color = Slate300, lineHeight = 14.sp)
                    }
                }
            }
        }
    }
}

private fun nodeClassColor(cls: GraphNodeClass): Color = when (cls) {
    GraphNodeClass.CYBER_AGENT -> CyanNeon
    GraphNodeClass.TELECOM_PROXY -> AmberRadar
    GraphNodeClass.HUMAN_CELL -> CrimsonThreat
    GraphNodeClass.TERRAIN_RF -> EmeraldVerified
}

@Composable
private fun SnaKpiCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = Slate400, fontFamily = FontFamily.Monospace)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 9.sp, color = Slate300)
    }
}
