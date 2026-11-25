package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.ForensicUiState
import com.example.ui.ForensicViewModel
import com.example.ui.IntelSubMode
import com.example.ui.NavigationTab
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: ForensicViewModel,
    uiState: ForensicUiState,
    modifier: Modifier = Modifier
) {
    val filteredRepos = remember(
        uiState.repositories,
        uiState.selectedScopeFilter,
        uiState.repositorySearchQuery
    ) {
        uiState.repositories.filter { repo ->
            val matchesScope = uiState.selectedScopeFilter == OperationalScope.ALL ||
                    repo.operationalScope == uiState.selectedScopeFilter
            val matchesQuery = uiState.repositorySearchQuery.isBlank() ||
                    repo.name.contains(uiState.repositorySearchQuery, ignoreCase = true) ||
                    repo.summary.contains(uiState.repositorySearchQuery, ignoreCase = true) ||
                    repo.primaryAssetClass.contains(uiState.repositorySearchQuery, ignoreCase = true) ||
                    repo.metadataSchema.contains(uiState.repositorySearchQuery, ignoreCase = true)
            matchesScope && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            ForensicTopBar(
                title = "Evidence Catalogs",
                subtitle = "Authoritative Global Archives & Discovery Repositories"
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
                value = uiState.repositorySearchQuery,
                onValueChange = { viewModel.updateRepositorySearch(it) },
                placeholder = {
                    Text(
                        "Search archives, STAC, ballistics, SAR, CAS RN...",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CyanNeon
                    )
                },
                trailingIcon = {
                    if (uiState.repositorySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateRepositorySearch("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Slate400
                            )
                        }
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
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("catalog_search_input")
            )

            // Scope Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OperationalScope.values()) { scope ->
                    ScopeFilterChip(
                        scope = scope,
                        isSelected = uiState.selectedScopeFilter == scope,
                        onClick = { viewModel.setScopeFilter(scope) }
                    )
                }
            }

            // Summary Stats Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "INDEXED ARCHIVES",
                    value = "${filteredRepos.size} Sources",
                    color = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "SCHEMA STANDARD",
                    value = "STAC / ISO 19115",
                    color = EmeraldVerified,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "GOVERNANCE",
                    value = "ICC / NIST Validated",
                    color = AmberRadar,
                    modifier = Modifier.weight(1f)
                )
            }

            // Repositories List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRepos, key = { it.id }) { repo ->
                    RepositoryCard(
                        repo = repo,
                        isBookmarked = uiState.bookmarks.contains(repo.id),
                        onBookmarkToggle = { viewModel.toggleBookmark(repo.id) },
                        onSelect = { viewModel.selectRepository(repo) }
                    )
                }
            }
        }
    }

    // Repository Details Modal Sheet
    uiState.selectedRepository?.let { repo ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectRepository(null) },
            containerColor = DarkSurface,
            contentColor = Slate100,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Slate400) }
        ) {
            RepositoryDetailContent(
                repo = repo,
                onDismiss = { viewModel.selectRepository(null) },
                onLaunchVectorQuery = { query ->
                    viewModel.selectRepository(null)
                    viewModel.executeVectorSearch(query)
                    viewModel.selectIntelSubMode(IntelSubMode.AI_VECTOR)
                    viewModel.selectTab(NavigationTab.INTEL)
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate850)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RepositoryCard(
    repo: ArchivalRepository,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
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
            .testTag("repo_card_${repo.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate100
                    )
                    Text(
                        text = repo.organization,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanNeon
                    )
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) AmberRadar else Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = repo.summary,
                style = MaterialTheme.typography.bodySmall,
                color = Slate400,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AccessTierBadge(tier = repo.accessTier)
                ResearchValueBadge(value = repo.researchValue)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Schema & asset pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate850)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DataObject,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Schema: ${repo.metadataSchema}",
                    fontSize = 11.sp,
                    color = Slate200,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RepositoryDetailContent(
    repo: ArchivalRepository,
    onDismiss: () -> Unit,
    onLaunchVectorQuery: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Slate100
                )
                Text(
                    text = repo.organization,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CyanNeon
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccessTierBadge(tier = repo.accessTier)
            MetadataQualityBadge(quality = repo.metadataQuality)
            ResearchValueBadge(value = repo.researchValue)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "EXECUTIVE SUMMARY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            AudioSelectButton(
                sourceId = "repo_${repo.id}",
                title = repo.name,
                text = "${repo.name} by ${repo.organization}. ${repo.summary}. Primary asset class: ${repo.primaryAssetClass}. Indexing: ${repo.indexingStructure}",
                compact = true
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = repo.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate200
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "PRIMARY ASSET CLASS & INDEXING",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate850)
                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Asset Class: ${repo.primaryAssetClass}",
                    fontSize = 12.sp,
                    color = Slate100,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Indexing: ${repo.indexingStructure}",
                    fontSize = 12.sp,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "TECHNICAL SPECIFICATIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            repo.technicalSpecs.forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = key, fontSize = 12.sp, color = Slate400)
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyanNeon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SAMPLE FORENSIC QUERIES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        repo.sampleQueries.forEach { query ->
            OutlinedButton(
                onClick = { onLaunchVectorQuery(query) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(DarkBorder, CyanNeon.copy(alpha = 0.5f)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = query,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Run Vector Search",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
