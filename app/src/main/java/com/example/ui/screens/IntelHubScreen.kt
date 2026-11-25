package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.IntelSubMode
import com.example.ui.theme.*

@Composable
fun IntelHubScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        when (uiState.selectedIntelSubMode) {
            IntelSubMode.CASEBOOK -> CasebookScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
            IntelSubMode.AI_VECTOR -> AiIntelScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
            IntelSubMode.NARRATIVE_OPS -> NarrativeControlScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
            IntelSubMode.CBRN_HAZARD -> HazardScreen(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
        }

        // Compact high-contrast color-coded bottom mode bar right above main navigation
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .border(1.dp, DarkBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            IntelSubMode.values().forEach { mode ->
                val isSelected = uiState.selectedIntelSubMode == mode
                val modeColor = when (mode) {
                    IntelSubMode.CASEBOOK -> CyanNeon
                    IntelSubMode.AI_VECTOR -> VioletVector
                    IntelSubMode.NARRATIVE_OPS -> EmeraldVerified
                    IntelSubMode.CBRN_HAZARD -> AmberRadar
                }
                val tabLabel = when (mode) {
                    IntelSubMode.CASEBOOK -> "Cases (${uiState.cases.size})"
                    IntelSubMode.NARRATIVE_OPS -> "Ops (${uiState.narrativeIncidents.size})"
                    else -> mode.label
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) modeColor else Slate850)
                        .border(
                            1.5.dp,
                            if (isSelected) modeColor else modeColor.copy(alpha = 0.45f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectIntelSubMode(mode) }
                        .padding(vertical = 8.dp, horizontal = 3.dp)
                        .testTag("intel_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = tabLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaFontFamily,
                        color = if (isSelected) Slate950 else Slate100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
