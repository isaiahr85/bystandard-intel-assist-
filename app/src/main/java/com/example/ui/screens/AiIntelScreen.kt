package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tools.AiVectorSimulator
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.AudioTextBoxStrip
import com.example.ui.components.ForensicTopBar
import com.example.ui.components.visualAudioHighlightBorder
import com.example.ui.theme.*

@Composable
fun AiIntelScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Vector Search, 1: LMM Extraction
    var searchInput by remember { mutableStateOf(uiState.vectorQuery) }
    var sitrepInput by remember { mutableStateOf(uiState.lmmInputText) }

    LaunchedEffect(uiState.vectorQuery) {
        if (searchInput != uiState.vectorQuery) {
            searchInput = uiState.vectorQuery
        }
    }
    LaunchedEffect(uiState.lmmInputText) {
        if (sitrepInput != uiState.lmmInputText) {
            sitrepInput = uiState.lmmInputText
        }
    }

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "AI Vector Intelligence",
                subtitle = "Multimodal Embedding Search & LMM Metadata Synthesis"
            )
        },
        containerColor = Slate950,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub-tab row
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Slate950,
                contentColor = CyanNeon,
                divider = { HorizontalDivider(color = DarkBorder) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text("High-Dimensional Vector Search", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("ai_tab_vector")
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text("LMM STAC Extractor", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("ai_tab_lmm")
                )
            }

            if (activeSubTab == 0) {
                // VECTOR SEARCH VIEW
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "SEMANTIC MULTIMODAL VECTOR RETRIEVAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search across unstructured video transcripts, satellite descriptions, and ballistic metrology point clouds via high-dimensional embedding similarity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    item {
                        // Natural Language Search Bar
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            label = { Text("Semantic Investigation Query", fontSize = 11.sp) },
                            placeholder = { Text("e.g. locate all bridge collapses with secondary fire damage...", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.executeVectorSearch(searchInput) }) {
                                    Icon(Icons.Default.Send, contentDescription = "Run Vector Search", tint = CyanNeon)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vector_query_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioTextBoxStrip(
                            sourceId = "vector_query_input",
                            title = "Semantic Query Box",
                            textToRead = searchInput
                        )
                    }

                    item {
                        // Quick Presets
                        Text(
                            text = "SUGGESTED FORENSIC PROMPTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(
                                "bridge collapses secondary fire urban",
                                "155mm crater patterns NIR soil moisture",
                                "Sarin precursor methylphosphonic difluoride",
                                "casing breech face shear Glock 9mm",
                                "InSAR phase coherence loss waterfront"
                            )) { prompt ->
                                OutlinedButton(
                                    onClick = {
                                        searchInput = prompt
                                        viewModel.executeVectorSearch(prompt)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Slate850, contentColor = Slate200),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate700))),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(prompt, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "RANKED EMBEDDING MATCHES (${uiState.vectorResults.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar,
                            letterSpacing = 1.sp
                        )
                    }

                    items(uiState.vectorResults) { res ->
                        VectorResultCard(result = res)
                    }
                }
            } else {
                // LMM STAC EXTRACTOR VIEW
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ai_lmm_list")
                ) {
                    item {
                        Text(
                            text = "AUTOMATED STAC GEOJSON METADATA SYNTHESIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Converts raw field intelligence, witness statements, and after-action logs into standardized SpatioTemporal Asset Catalog (STAC) structures with bounding boxes and sensor triangulation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = sitrepInput,
                            onValueChange = { sitrepInput = it },
                            label = { Text("Raw Incident Report / Witness Field Narrative", fontSize = 11.sp) },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AudioTextBoxStrip(
                            sourceId = "lmm_sitrep_input",
                            title = "Field Narrative Box",
                            textToRead = sitrepInput
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.updateLmmInputText(sitrepInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = Slate950),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesize Standardized STAC Schema", fontWeight = FontWeight.Bold)
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "VALIDATED STAC 1.0.0 FEATURE COLLECTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldVerified,
                                letterSpacing = 1.sp,
                                modifier = Modifier.weight(1f)
                            )
                            AudioSelectButton(
                                sourceId = "stac_report",
                                title = "STAC Metadata Report",
                                text = uiState.lmmExtractedStac,
                                compact = true
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        val a11yStac = LocalAccessibilityAudio.current
                        val stacSpeaking = a11yStac.playbackState.activeSourceId == "stac_report" && a11yStac.playbackState.isActive
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(
                                    listOf(
                                        visualAudioHighlightBorder(stacSpeaking, a11yStac.visualAudioEnabled, EmeraldVerified.copy(alpha = 0.5f)),
                                        DarkBorder
                                    )
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = uiState.lmmExtractedStac,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldVerified,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.ingestStacIntelToCasebook(sitrepInput) },
                            enabled = sitrepInput.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldVerified),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(EmeraldVerified, CyanNeon))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stac_promote_to_casebook_btn")
                        ) {
                            Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Promote STAC Intel to Casebook", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VectorResultCard(result: AiVectorSimulator.VectorSearchResult) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate100,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyanNeon.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COSINE: ${(result.similarityScore * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Domain: ${result.category}",
                fontSize = 11.sp,
                color = AmberRadar,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.snippet,
                fontSize = 12.sp,
                color = Slate300
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate850)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Matched Schema: ${result.metadataMatch}",
                    fontSize = 10.sp,
                    color = Slate400,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AudioSelectButton(
                sourceId = "vector_${result.title.lowercase().replace(Regex("[^a-z0-9]+"), "_").take(24)}",
                title = result.title,
                text = "${result.title}, domain ${result.category}, cosine similarity ${(result.similarityScore * 100).toInt()} percent. ${result.snippet}",
                compact = true
            )
        }
    }
}
