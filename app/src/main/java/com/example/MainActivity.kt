package com.example

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import com.example.model.BtamOperationalRole
import com.example.tools.AccessibilityAudioManager
import com.example.tools.AccessibilityAudioUiBridge
import com.example.tools.LocalAccessibilityAudio
import com.example.ui.ForensicViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.AccessibilityOptionsSheet
import com.example.ui.components.AppApiDirectoryDialog
import com.example.ui.components.BtamTopBarBridge
import com.example.ui.components.ContextSensitiveMapLegendDialog
import com.example.ui.components.FloatingAudioPlayerBar
import com.example.ui.components.InAppPipMiniMapOverlay
import com.example.ui.components.LocalBtamTopBarBridge
import com.example.ui.components.PolicyUserAgreementDialog
import com.example.ui.components.QuickToolbarConfigDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val systemPipActive = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeriScopeApp(
                externalSystemPip = systemPipActive.value,
                onRequestSystemPip = { enterMapPictureInPicture() }
            )
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        systemPipActive.value = isInPictureInPictureMode
    }

    private fun enterMapPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 10))
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }
}

@Composable
fun VeriScopeApp(
    viewModel: ForensicViewModel = viewModel(),
    externalSystemPip: Boolean = false,
    onRequestSystemPip: () -> Unit = {}
) {
    val context = LocalContext.current
    val baseDensity = LocalDensity.current
    val uiState by viewModel.uiState.collectAsState()

    // Defer disk cache, preferences, and ImageLoader setup off the first composition frame
    LaunchedEffect(context) {
        val mgr = viewModel.hydrateRuntimeServicesAsync(context)
        if (mgr != null) {
            runCatching { Coil.setImageLoader(mgr.imageLoader) }
        }
    }
    LaunchedEffect(externalSystemPip) {
        viewModel.setSystemPipActive(externalSystemPip)
    }

    val audioManager = remember(context) { AccessibilityAudioManager(context, autoInitialize = false) }
    LaunchedEffect(audioManager) {
        delay(120L)
        runCatching { audioManager.ensureInitialized() }
    }
    DisposableEffect(audioManager) {
        onDispose { audioManager.shutdown() }
    }
    val playbackState by audioManager.state.collectAsState()

    val audioBridge = remember(uiState.visualAudioEnabled, uiState.textScalePreset, playbackState) {
        AccessibilityAudioUiBridge(
            visualAudioEnabled = uiState.visualAudioEnabled,
            textScalePreset = uiState.textScalePreset,
            playbackState = playbackState,
            onPlayOrToggle = { id, title, text -> audioManager.playOrToggle(id, title, text) },
            onPauseOrResume = { audioManager.pauseOrResume() },
            onStop = { audioManager.stop() },
            onCycleSpeechRate = { audioManager.cycleSpeechRate() },
            onSetSpeechRate = { audioManager.setSpeechRate(it) },
            onToggleVisualAudio = { viewModel.toggleVisualAudioEnabled() },
            onCycleTextScale = { viewModel.cycleTextScalePreset() },
            onSetTextScale = { viewModel.setTextScalePreset(it) },
            onOpenAccessibilitySheet = { viewModel.setAccessibilitySheetOpen(true) }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val bystanderPortalState by viewModel.bystanderPortalState.collectAsState()

    val btamTopBarBridge = remember(uiState.selectedBtamRole) {
        val roleShort = when (uiState.selectedBtamRole) {
            BtamOperationalRole.BTAM_INVESTIGATOR -> "Investigator"
            BtamOperationalRole.TEAM_LEAD -> "Team Lead"
            BtamOperationalRole.COMPLIANCE_AUDITOR -> "Auditor"
            BtamOperationalRole.PUBLIC_BYSTANDER -> "Bystander"
            null -> "BTAM Role"
        }
        BtamTopBarBridge(
            roleShortLabel = roleShort,
            onOpenOversightConsole = { viewModel.setBtamWorkspaceModalOpen(true) },
            onRequest911Confirm = { viewModel.request911CallConfirmation() },
            onOpenSideMenu = { drawerScope.launch { drawerState.open() } }
        )
    }

    val scaledDensity = remember(baseDensity, uiState.textScalePreset) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * uiState.textScalePreset.scaleFactor
        )
    }

    VeriScopeTheme(visualThemeMode = uiState.mapVisualThemeMode) {
        val palette = LocalMapPalette.current
        CompositionLocalProvider(
            LocalAccessibilityAudio provides audioBridge,
            LocalBtamTopBarBridge provides btamTopBarBridge,
            LocalDensity provides scaledDensity
        ) {
            if (!uiState.btamSessionUnlocked) {
                // Mandatory Startup Role-Selection Gateway: blocks all workspace tools until unlocked
                BtamStartupGatewayScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = !uiState.isFullScreenMapActive && !uiState.isInSystemPip,
                    drawerContent = {
                        BystanderSideMenuDrawerContent(
                            state = bystanderPortalState,
                            onSelectOption = { opt -> viewModel.openBystanderPortalOption(opt) },
                            onToggleElderMode = { viewModel.bystanderReferralCoordinator.toggleElderFriendlyMode(it) },
                            onCloseDrawer = { drawerScope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    when {
                        uiState.bystanderPortalOpen -> {
                            BystanderReferralPortalScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                                onBackToWorkspace = { viewModel.setBystanderPortalOpen(false) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        uiState.selectedBtamRole == BtamOperationalRole.PUBLIC_BYSTANDER -> {
                            BackHandler {
                                viewModel.lockBtamSessionToGateway()
                            }
                            BtamPublicBystanderWorkspace(
                                viewModel = viewModel,
                                uiState = uiState,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        uiState.isFullScreenMapActive || uiState.isInSystemPip -> {
                    FullScreenMapViewer(
                        viewModel = viewModel,
                        uiState = uiState,
                        onRequestSystemPip = {
                            if (context is Activity) {
                                onRequestSystemPip()
                            }
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                viewModel.toggleInAppMiniPip(true)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Handle back button: close open modals or return to primary Dashboard tab
                    BackHandler(
                        enabled = uiState.btamWorkspaceModalOpen ||
                            uiState.isAccessibilitySheetOpen ||
                            uiState.showMapLegendSheet ||
                            uiState.showQuickToolbarConfig ||
                            uiState.selectedWebcamStill != null ||
                            uiState.selectedAgentProfile != null ||
                            uiState.selectedPlatformNode != null ||
                            uiState.selectedRepository != null ||
                            uiState.selectedCase != null ||
                            uiState.selectedCbrnHazard != null ||
                            uiState.isCreatingNewCase ||
                            uiState.currentTab != NavigationTab.DASHBOARD
                    ) {
                        when {
                            uiState.btamWorkspaceModalOpen -> viewModel.setBtamWorkspaceModalOpen(false)
                            uiState.isAccessibilitySheetOpen -> viewModel.setAccessibilitySheetOpen(false)
                            uiState.showMapLegendSheet -> viewModel.toggleMapLegendSheet(false)
                            uiState.showQuickToolbarConfig -> viewModel.toggleQuickToolbarConfig(false)
                            uiState.selectedWebcamStill != null -> viewModel.selectWebcamStill(null)
                            uiState.selectedAgentProfile != null -> viewModel.selectAgentProfile(null)
                            uiState.selectedPlatformNode != null -> viewModel.selectPlatformNode(null)
                            uiState.selectedRepository != null -> viewModel.selectRepository(null)
                            uiState.selectedCase != null -> viewModel.selectCase(null)
                            uiState.selectedCbrnHazard != null -> viewModel.selectCbrnHazard(null)
                            uiState.isCreatingNewCase -> viewModel.setCreatingNewCase(false)
                            uiState.currentTab != NavigationTab.DASHBOARD -> viewModel.selectTab(NavigationTab.DASHBOARD)
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            Column {
                                FloatingAudioPlayerBar()
                                NavigationBar(
                                    containerColor = palette.panelSurface,
                                    contentColor = palette.advisoryBlue,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.testTag("main_navigation_bar")
                                ) {
                                    val navActiveBg = palette.advisoryBlue
                                    val navActiveIcon = contrastContentColor(navActiveBg)
                                    NavigationTab.values().forEach { tab ->
                                        val isSelected = uiState.currentTab == tab
                                        val (iconFilled, iconOutlined) = when (tab) {
                                            NavigationTab.DASHBOARD -> Pair(Icons.Filled.Hub, Icons.Outlined.Hub)
                                            NavigationTab.SAT_FEED -> Pair(Icons.Filled.SatelliteAlt, Icons.Outlined.SatelliteAlt)
                                            NavigationTab.CATALOG -> Pair(Icons.Filled.FolderCopy, Icons.Outlined.FolderCopy)
                                            NavigationTab.LAB -> Pair(Icons.Filled.Biotech, Icons.Outlined.Biotech)
                                            NavigationTab.INTEL -> Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
                                        }

                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { viewModel.selectTab(tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (isSelected) iconFilled else iconOutlined,
                                                    contentDescription = tab.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = tab.label,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = PlusJakartaFontFamily,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = navActiveIcon,
                                                selectedTextColor = navActiveBg,
                                                indicatorColor = navActiveBg,
                                                unselectedIconColor = palette.textSecondary,
                                                unselectedTextColor = palette.textSecondary
                                            ),
                                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        },
                        containerColor = palette.canvasBackground,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            Crossfade(
                                targetState = uiState.currentTab,
                                label = "screen_crossfade"
                            ) { tab ->
                                when (tab) {
                                    NavigationTab.DASHBOARD -> OrchidlandDicmScreen(viewModel, uiState)
                                    NavigationTab.SAT_FEED -> NasaSatFeedScreen(viewModel, uiState)
                                    NavigationTab.CATALOG -> CatalogScreen(viewModel, uiState)
                                    NavigationTab.LAB -> ForensicLabScreen(viewModel, uiState)
                                    NavigationTab.INTEL -> IntelHubScreen(viewModel, uiState)
                                }
                            }

                            if (uiState.isInAppMiniPipActive) {
                                InAppPipMiniMapOverlay(
                                    uiState = uiState,
                                    onExpandFullScreen = {
                                        viewModel.openFullScreenMap(uiState.fullScreenMapDomain)
                                    },
                                    onClosePip = { viewModel.toggleInAppMiniPip(false) }
                                )
                            }
                        }
                    }
                }
            }
                }
            }

            if (uiState.btamWorkspaceModalOpen && uiState.btamSessionUnlocked) {
                BtamOversightConsoleModal(
                    viewModel = viewModel,
                    uiState = uiState,
                    onDismiss = { viewModel.setBtamWorkspaceModalOpen(false) }
                )
            }

            if (uiState.isAccessibilitySheetOpen) {
                AccessibilityOptionsSheet(
                    onDismiss = { viewModel.setAccessibilitySheetOpen(false) },
                    visualThemeMode = uiState.mapVisualThemeMode,
                    onSelectVisualThemeMode = { viewModel.setMapVisualThemeMode(it) }
                )
            }

            if (uiState.showMapLegendSheet) {
                ContextSensitiveMapLegendDialog(
                    uiState = uiState,
                    onDismiss = { viewModel.toggleMapLegendSheet(false) }
                )
            }

            if (uiState.showQuickToolbarConfig) {
                QuickToolbarConfigDialog(
                    selectedTools = uiState.quickToolbarTools,
                    onToggleTool = { viewModel.toggleQuickToolbarTool(it) },
                    onDismiss = { viewModel.toggleQuickToolbarConfig(false) }
                )
            }

            if (uiState.btamShow911ConfirmDialog) {
                Call911ConfirmationDialog(
                    onConfirmCall911 = { viewModel.confirmAndLaunch911Dialer(context) },
                    onDismiss = { viewModel.dismiss911CallConfirmation() }
                )
            }

            if (uiState.btamPolicyDialogOpen) {
                PolicyUserAgreementDialog(
                    alreadyAccepted = uiState.btamUserAgreementAccepted,
                    onAccept = { viewModel.acceptPolicyFromDialog() },
                    onDismiss = { viewModel.setBtamPolicyDialogOpen(false) }
                )
            }

            if (uiState.btamApiDirectoryOpen) {
                AppApiDirectoryDialog(
                    onDismiss = { viewModel.setBtamApiDirectoryOpen(false) }
                )
            }
        }
    }
}
