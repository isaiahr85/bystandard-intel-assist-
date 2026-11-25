package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CbrnHazard
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.components.AudioSelectButton
import com.example.ui.components.ForensicTopBar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val filteredHazards = remember(uiState.cbrnHazards, uiState.cbrnSearchQuery) {
        if (uiState.cbrnSearchQuery.isBlank()) {
            uiState.cbrnHazards
        } else {
            uiState.cbrnHazards.filter {
                it.commonName.contains(uiState.cbrnSearchQuery, ignoreCase = true) ||
                        it.casNumber.contains(uiState.cbrnSearchQuery, ignoreCase = true) ||
                        it.molecularFormula.contains(uiState.cbrnSearchQuery, ignoreCase = true) ||
                        it.hazardClass.contains(uiState.cbrnSearchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "CBRN Hazard Registry",
                subtitle = "CAS Chemical Safety Library, EPA IRIS & OPCW Standards"
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
            // Search Input
            OutlinedTextField(
                value = uiState.cbrnSearchQuery,
                onValueChange = { viewModel.updateCbrnSearch(it) },
                placeholder = {
                    Text(
                        "Search agent by name, CAS RN (e.g. 7782-50-5), SMILES...",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Search Chemical",
                        tint = AmberRadar
                    )
                },
                trailingIcon = {
                    if (uiState.cbrnSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateCbrnSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Slate400)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = AmberRadar,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("cbrn_search_input")
            )

            // Header Banner
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Slate850),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AmberRadar.copy(alpha = 0.4f), DarkBorder))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = AmberRadar,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INCIDENT HAZARD & REACTIVITY DATABASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberRadar
                        )
                        Text(
                            text = "Standardized SMILES, thermal runaway curves, and continuous chain-of-custody protocols for environmental and biological sampling.",
                            fontSize = 11.sp,
                            color = Slate300
                        )
                    }
                }
            }

            // List of Hazards
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredHazards, key = { it.casNumber }) { hazard ->
                    HazardCard(
                        hazard = hazard,
                        onSelect = { viewModel.selectCbrnHazard(hazard) }
                    )
                }
            }
        }
    }

    // Hazard Detail Modal
    uiState.selectedCbrnHazard?.let { hazard ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectCbrnHazard(null) },
            containerColor = DarkSurface,
            contentColor = Slate100,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Slate400) }
        ) {
            HazardDetailContent(
                hazard = hazard,
                onDismiss = { viewModel.selectCbrnHazard(null) }
            )
        }
    }
}

@Composable
fun HazardCard(
    hazard: CbrnHazard,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Slate800))),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("hazard_card_${hazard.casNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = hazard.commonName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate100
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AmberRadar.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CAS ${hazard.casNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberRadar,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = hazard.hazardClass,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = CrimsonThreat
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thermal Runaway: ${hazard.thermalRunawayRisk}",
                fontSize = 11.sp,
                color = Slate400,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate850)
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Formula: ${hazard.molecularFormula}",
                        fontSize = 11.sp,
                        color = CyanNeon,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SMILES: ${hazard.smiles}",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun HazardDetailContent(
    hazard: CbrnHazard,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hazard.commonName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Slate100
                )
                Text(
                    text = "CAS RN: ${hazard.casNumber} • Formula: ${hazard.molecularFormula}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberRadar,
                    fontFamily = FontFamily.Monospace
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CrimsonThreat.copy(alpha = 0.15f))
                .border(1.dp, CrimsonThreat.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(
                text = hazard.hazardClass,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CrimsonThreat
            )
        }

        AudioSelectButton(
            sourceId = "hazard_${hazard.casNumber}",
            title = "${hazard.commonName} Safety Sheet",
            text = "${hazard.commonName}, CAS ${hazard.casNumber}, ${hazard.hazardClass}. Thermal runaway risk: ${hazard.thermalRunawayRisk}. Exposure limit: ${hazard.exposureLimit}. Sampling standard: ${hazard.samplingStandard}",
            compact = true
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "THERMAL RUNAWAY & REACTIVE HAZARD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Text(text = hazard.thermalRunawayRisk, fontSize = 12.sp, color = Slate200)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "EXPOSURE LIMITS & TOXICOLOGY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Text(text = hazard.exposureLimit, fontSize = 12.sp, color = CyanNeon, fontFamily = FontFamily.Monospace)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "ENVIRONMENTAL FATE & PERSISTENCE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Text(text = hazard.environmentalFate, fontSize = 12.sp, color = Slate200)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "FIELD SAMPLING STANDARD & CHAIN-OF-CUSTODY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            Text(text = hazard.samplingStandard, fontSize = 12.sp, color = EmeraldVerified)
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
