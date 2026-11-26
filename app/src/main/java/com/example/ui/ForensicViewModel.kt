package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bystander.BystanderPortalUiState
import com.example.bystander.BystanderReferralCoordinator
import com.example.bystander.BystanderSideMenuOption
import com.example.data.*
import com.example.model.*
import com.example.network.*
import com.example.tools.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

enum class NavigationTab(val label: String) {
    DASHBOARD("Dashboard"),
    SAT_FEED("Sat Feed"),
    CATALOG("Catalog"),
    LAB("Lab"),
    INTEL("Cases & Intel")
}

enum class LabTool(val label: String) {
    CHRONOLOCATION("Chronolocation & Shadows"),
    ELA_INSPECTOR("ELA & Media Integrity"),
    BALLISTICS_SCOPE("Ballistic Split-Scope"),
    GEOINT_SENSOR("GEOINT & SAR Sensors")
}

enum class IntelSubMode(val label: String) {
    CASEBOOK("Casebook"),
    AI_VECTOR("AI Vector"),
    NARRATIVE_OPS("FB / TG Ops"),
    CBRN_HAZARD("CBRN Hazards"),
    CIVIC_SYSTEM("Civic AI")
}

enum class FullScreenMapDomain {
    AIRSPACE,
    SATELLITE
}

sealed interface NasaFeedLoadState {
    data object Loading : NasaFeedLoadState
    data class Success(val events: List<NasaSatEvent>) : NasaFeedLoadState
    data class Error(val message: String) : NasaFeedLoadState
}

data class GibsTarget(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val dateIso: String,
    val eventId: String? = null
)

data class ForensicUiState(
    val currentTab: NavigationTab = NavigationTab.DASHBOARD,
    val selectedIntelSubMode: IntelSubMode = IntelSubMode.CASEBOOK,
    val selectedScopeFilter: OperationalScope = OperationalScope.ALL,
    val repositorySearchQuery: String = "",
    val repositories: List<ArchivalRepository> = ForensicDataSource.repositories,
    val selectedRepository: ArchivalRepository? = null,
    val cases: List<ForensicCase> = ForensicDataSource.preloadedCases,
    val selectedCase: ForensicCase? = null,
    val isCreatingNewCase: Boolean = false,
    val caseSearchQuery: String = "",
    val caseStatusFilter: CaseStatus? = null,
    val bookmarks: Set<String> = setOf("repo_bellingcat", "repo_copernicus", "case_2026_01"),
    // NASA Satellite Feed State
    val nasaFeedState: NasaFeedLoadState = NasaFeedLoadState.Loading,
    val nasaSelectedCategory: NasaEonetCategory = NasaEonetCategory.ALL,
    val nasaIncludeClosed: Boolean = false,
    val nasaSearchQuery: String = "",
    val selectedNasaEvent: NasaSatEvent? = null,
    val activeGibsLayer: NasaGibsLayer = NasaGibsLayer.VIIRS_THERMAL,
    val gibsSpanDegrees: Double = 3.0,
    val hasCustomGibsTarget: Boolean = false,
    val customGibsTarget: GibsTarget = GibsTarget(
        label = "Sector 4 Industrial Waterfront (Case VERI-2026-0881)",
        latitude = 46.482526,
        longitude = 30.723314,
        dateIso = "2024-08-14"
    ),
    val gibsRefreshNonce: Long = 0L,
    val satVisualZoom: Float = 1.0f,
    val satScrollLocked: Boolean = false,
    val satShowReticle: Boolean = true,
    val satShowCoordGrid: Boolean = false,
    val satShowHud: Boolean = true,
    // Forensic Lab State
    val activeLabTool: LabTool = LabTool.CHRONOLOCATION,
    // Chronolocation
    val chronoObjectHeight: Double = 12.0,
    val chronoShadowLength: Double = 18.5,
    val chronoLatitude: Double = 46.4825,
    val chronoLongitude: Double = 30.7233,
    val chronoDayOfYear: Int = 226,
    val chronoResult: SunChronolocationCalculator.ChronoResult = SunChronolocationCalculator.calculate(12.0, 18.5, 46.4825, 30.7233, 226),
    // ELA Inspector
    val selectedElaPreset: String = "PRISTINE_CAMERA",
    val elaAssessment: ErrorLevelAnalysisEngine.ElaAssessment = ErrorLevelAnalysisEngine.evaluate("PRISTINE_CAMERA"),
    // Ballistics Split-Scope
    val ballisticCasingA: String = "Glock 19 Gen 5 (9x19mm)",
    val ballisticCasingB: String = "Glock 19 Gen 5 (9x19mm)",
    val ballisticSplitSlider: Float = 0.5f,
    val ballisticComparison: BallisticMetrologyEngine.ComparisonResult = BallisticMetrologyEngine.compare("Glock 19 Gen 5 (9x19mm)", "Glock 19 Gen 5 (9x19mm)"),
    // CBRN Hazards
    val cbrnSearchQuery: String = "",
    val cbrnHazards: List<CbrnHazard> = ForensicDataSource.cbrnHazards,
    val selectedCbrnHazard: CbrnHazard? = null,
    // GEOINT Sensors
    val geointSensors: List<GeointSensor> = ForensicDataSource.geointSensors,
    val selectedGeointSensor: GeointSensor? = null,
    // AI Vector Search & Extraction
    val vectorQuery: String = "locate all bridge collapses with secondary fire damage in urban sectors",
    val vectorResults: List<AiVectorSimulator.VectorSearchResult> = AiVectorSimulator.search("locate all bridge collapses with secondary fire damage in urban sectors"),
    val lmmInputText: String = "Preliminary SITREP 14-AUG: A strike impacted the elevator silo tower at the waterfront dock. Video captured at 06:42 UTC shows distinctive high-angle glide trajectory with tail-fins. Sentinel-1 SAR imagery shows loss of interferometric phase coherence.",
    val lmmExtractedStac: String = AiVectorSimulator.simulateLmmExtraction(
        "Preliminary SITREP 14-AUG: A strike impacted the elevator silo tower at the waterfront dock. Video captured at 06:42 UTC shows distinctive high-angle glide trajectory with tail-fins. Sentinel-1 SAR imagery shows loss of interferometric phase coherence."
    ),
    // Orchidland Estates DICM & Cyber Operations Dashboard
    val selectedDicmTab: DicmPhaseTab = DicmPhaseTab.DISSECT_IPB,
    val selectedAgentProfile: TaskAgentProfile? = null,
    val selectedPlatformNode: PlatformNodeProfile? = null,
    val selectedStrategicFramework: String = "SWOT",
    val selectedCoaCode: String = "MLCOA",
    val snaMatrixMode: Boolean = false,
    val selectedGraphNodeId: String? = "n_aut_c2",
    val isolatedGraphNodeIds: Set<String> = emptySet(),
    val snaNetworkReport: SnaNetworkReport = DicmSnaEngine.analyzeNetwork(emptySet()),
    val deployedNistIds: Set<String> = setOf("nist_id"),
    val checkedGuardrailIds: Set<String> = setOf("grd_quiday", "grd_rod"),
    // Hawaiʻi Island Airspace & PTA Military Drill Monitor
    val airspaceLoadState: AirspaceLoadState = AirspaceLoadState.Idle,
    val airspaceSnapshot: HawaiiAirspaceSnapshot = HawaiiAirspaceSnapshot(
        updatedAtHst = HawaiiAirspaceReference.formatCurrentHst()
    ),
    val selectedAirspaceSite: HawaiiAirspaceSite = HawaiiAirspaceReference.keySites.first(),
    val ptaAdvisories: List<PtaDrillAdvisory> = emptyList(),
    // Live NOAA/NWS NEXRAD Doppler Radar + NASA GIBS Satellite Overlay & Map Scroll Controls
    val dopplerEnabled: Boolean = true,
    val activeDopplerLayer: HawaiiDopplerLayer = HawaiiDopplerLayer.HAWAII_BREF,
    val dopplerOpacity: Float = 0.82f,
    val dopplerRefreshNonce: Long = 0L,
    val airspaceSatEnabled: Boolean = true,
    val airspaceSatLayer: NasaGibsLayer = NasaGibsLayer.VIIRS_TRUE_COLOR,
    val airspaceSatOpacity: Float = 0.72f,
    val airspaceSatDateIso: String = NasaGibsUrlBuilder.yesterdayUtcIso(),
    val airspaceSatRefreshNonce: Long = 0L,
    val airspaceMapZoom: Float = 1.0f,
    val airspaceMapScrollLocked: Boolean = false,
    val airspaceMapExpanded: Boolean = false,
    val airspaceShowGridAndRoads: Boolean = true,
    val airspaceShowSpecialZones: Boolean = true,
    val airspaceShowTfrs: Boolean = true,
    val airspaceShowRadarRings: Boolean = true,
    val airspaceShowSitePins: Boolean = true,
    val airspaceShowWebcamPins: Boolean = true,
    // Hawaiʻi Live Still Cams (FAA WeatherCams + USGS HVO Volcanoes + UH System)
    val webcamsLoadState: WebcamsLoadState = WebcamsLoadState.Idle,
    val webcamsSnapshot: HawaiiWebcamsSnapshot = HawaiiWebcamsSnapshot(
        updatedAtHst = HawaiiAirspaceReference.formatCurrentHst(),
        usgsCams = HawaiiWebcamsReference.usgsHvoCams,
        uhCams = HawaiiWebcamsReference.uhSystemCams
    ),
    val selectedWebcamFilter: WebcamFilter = WebcamFilter.ALL,
    val selectedWebcamStill: HawaiiWebcamStill? = null,
    val webcamRefreshNonce: Long = 0L,
    // Hilo Downtown Cam Monitor (Kalakaua Park & 154 Waianuenue Ave)
    val hiloCamStatusState: HiloCamStatusState = HiloCamStatusState.Idle,
    val hiloCamStatusQuery: String = "Live downtown Hilo view — Kalakaua Park & 154 Waianuenue Ave",
    // Narrative Control & Social Connector Queue (FB Graph v21.0 + Telegram Bot API)
    val narrativeIncidents: List<NarrativeIncidentRecord> = emptyList(),
    val isNarrativeAnalyzing: Boolean = false,
    val narrativeError: String? = null,
    val narrativeFbConfigured: Boolean = false,
    val narrativeTgConfigured: Boolean = false,
    val webhookVerifyStatus: String? = null,
    // Accessibility: Visual Select Audio & Larger Readable Text
    val textScalePreset: TextScalePreset = TextScalePreset.STANDARD,
    val visualAudioEnabled: Boolean = true,
    val isAccessibilitySheetOpen: Boolean = false,
    // Mapping & Visualization Optimization State (Full-Screen, Gestures, Split, Accessibility, GPS/ADS-B, Cache)
    val isFullScreenMapActive: Boolean = false,
    val fullScreenMapDomain: FullScreenMapDomain = FullScreenMapDomain.AIRSPACE,
    val isInSystemPip: Boolean = false,
    val isInAppMiniPipActive: Boolean = false,
    val mapSplitMode: MapSplitMode = MapSplitMode.SINGLE,
    val mapVisualThemeMode: MapVisualThemeMode = MapVisualThemeMode.NIGHT,
    val mapRotationDeg: Float = 0f,
    val mapTiltDeg: Float = 0f,
    val terrainLayerEnabled: Boolean = true,
    val terrainOpacity: Float = 0.65f,
    val airspaceOpacity: Float = 0.85f,
    val airspaceAltitudeBand: AirspaceAltitudeBand = AirspaceAltitudeBand.ALL,
    val enabledAirspaceClasses: Set<AirspaceClassification> = AirspaceClassification.entries.toSet(),
    val selectedAirspaceZone: AirspaceClassificationZone? = null,
    val activeLayerPreset: MapLayerPreset? = null,
    val quickToolbarTools: List<QuickToolbarTool> = PersistedMapPreferences().quickTools,
    val showQuickToolbarConfig: Boolean = false,
    val showMapLegendSheet: Boolean = false,
    val userWaypoints: List<UserWaypoint> = PersistedMapPreferences.defaultWaypoints(),
    val routeSummary: RouteSummary = NavigationRouteEngine.recalculateRoute(
        HawaiiAirspaceReference.ORCHIDLAND_LAT,
        HawaiiAirspaceReference.ORCHIDLAND_LON,
        110.0,
        PersistedMapPreferences.defaultWaypoints()
    ),
    val gpsTelemetry: LiveGpsTelemetry = LiveGpsTelemetry(),
    val isGpsLiveEnabled: Boolean = false,
    val gpsPermissionGranted: Boolean = false,
    val gpsStatusNote: String? = null,
    val adsbEnabled: Boolean = false,
    val adsbAircraft: List<AdsbAircraftTrack> = emptyList(),
    val adsbStatus: AdsbFeedStatus = AdsbFeedStatus.Idle,
    val offlineSectorsCached: Set<String> = emptySet(),
    val tileCacheSizeKb: Long = 0L,
    val isPrefetchingTiles: Boolean = false,
    val hdTileResolution: Boolean = true,
    val externalDisplayCount: Int = 0,
    val mapPerfMetrics: MapPerformanceMetrics = MapPerformanceMetrics(),
    val registeredExternalLayers: List<MapOverlayLayerDescriptor> = MapLayerRegistry.defaultExternalLayers,
    // Startup Lifecycle & Satellite Aspect Calibration
    val startupInitCompleted: Boolean = false,
    val startupDiagnosticNote: String? = null,
    val satAspectCalibrationMode: SatAspectCalibrationMode = SatAspectCalibrationMode.AUTO_AESTHETIC,
    val satZoomCalibrationFactor: Float = 1.0f,
    // VeriScope BTAM Gateway & U.S. Secret Service NTAC Workflow
    val btamSessionUnlocked: Boolean = false,
    val selectedBtamRole: BtamOperationalRole? = null,
    val btamAffiliationPathway: BtamAffiliationPathway = BtamAffiliationPathway.AFFILIATED,
    val btamAffiliationOrgName: String = "",
    val btamGatewayError: String? = null,
    val btamWorkspaceModalOpen: Boolean = false,
    val btamBystanderStage: BtamBystanderStage = BtamBystanderStage.STAGE_1_LIFE_SAFETY,
    val btamLifeSafetyImminentThreat: Boolean? = null,
    val btamSelectedIndicators: Set<NtacIndicatorCategory> = emptySet(),
    val btamLeakageDraft: String = "",
    val btamGrievanceDraft: String = "",
    val btamProbingDraft: String = "",
    val btamContextArtifactsDraft: String = "",
    val btamObsDateTimeLocDraft: String = "",
    val btamWitnessNoteDraft: String = "",
    val btamAntiProfilingAlert: String? = null,
    val btamLatestIssuedToken: String? = null,
    val btamTokenLookupQuery: String = "",
    val btamTokenLookupResult: BtamCaseSubmission? = null,
    val btamTokenLookupMessage: String? = null,
    val btamBystanderFollowUpDraft: String = "",
    val btamSubmissions: List<BtamCaseSubmission> = emptyList(),
    val btamPiiAuditLogs: List<BtamPiiAuditLogEntry> = emptyList(),
    val btamSelectedQueueToken: String? = null,
    val btamInvestigatorQuestionDraft: String = "",
    val btamShow911ConfirmDialog: Boolean = false,
    val btam911StatusNote: String? = null,
    val btamUserAgreementAccepted: Boolean = false,
    val btamPolicyDialogOpen: Boolean = false,
    val btamApiDirectoryOpen: Boolean = false,
    val bystanderPortalOpen: Boolean = false
)

class ForensicViewModel(
    nasaFeedRepositoryFactory: () -> NasaSatFeedRepository = { NasaSatFeedRepository() },
    hawaiiAirspaceRepositoryFactory: () -> HawaiiAirspaceRepository = { HawaiiAirspaceRepository() },
    hawaiiWebcamsRepositoryFactory: () -> HawaiiWebcamsRepository = { HawaiiWebcamsRepository() },
    narrativeAiRepositoryFactory: () -> NarrativeAiRepository = { SampleNarrativeAiRepository() },
    socialDispatchRepositoryFactory: () -> SocialDispatchRepository = { SocialDispatchRepository() },
    openSkyAdsbRepositoryFactory: () -> OpenSkyAdsbRepository = { OpenSkyAdsbRepository() }
) : ViewModel() {

    private val nasaFeedRepository: NasaSatFeedRepository by lazy(nasaFeedRepositoryFactory)
    private val hawaiiAirspaceRepository: HawaiiAirspaceRepository by lazy(hawaiiAirspaceRepositoryFactory)
    private val hawaiiWebcamsRepository: HawaiiWebcamsRepository by lazy(hawaiiWebcamsRepositoryFactory)
    private val narrativeAiRepository: NarrativeAiRepository by lazy(narrativeAiRepositoryFactory)
    private val socialDispatchRepository: SocialDispatchRepository by lazy(socialDispatchRepositoryFactory)
    private val openSkyAdsbRepository: OpenSkyAdsbRepository by lazy(openSkyAdsbRepositoryFactory)

    @Volatile
    private var prefsStore: MapPreferencesStore? = null
    @Volatile
    private var tileCacheManager: MapTileCacheManager? = null
    @Volatile
    private var locationTracker: DeviceLocationTracker? = null
    private var lastAutoPrefetchMs: Long = 0L
    @Volatile
    private var startupWarmupStarted: Boolean = false

    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _uiState.update {
            it.copy(
                startupInitCompleted = true,
                startupDiagnosticNote = "Recovered from background init issue: ${throwable.localizedMessage ?: throwable.javaClass.simpleName}"
            )
        }
    }

    private val _uiState = MutableStateFlow(ForensicUiState())
    val uiState: StateFlow<ForensicUiState> = _uiState.asStateFlow()

    val civicCoordinator: com.example.civic.CivicStateCoordinator = com.example.civic.CivicStateCoordinator()
    val civicState: StateFlow<com.example.civic.CivicUiState> = civicCoordinator.state

    val bystanderReferralCoordinator: BystanderReferralCoordinator = BystanderReferralCoordinator()
    val bystanderPortalState: StateFlow<BystanderPortalUiState> = bystanderReferralCoordinator.state

    fun ensureStartupWarmup() {
        if (startupWarmupStarted) return
        startupWarmupStarted = true
        viewModelScope.launch(startupExceptionHandler) {
            val (fbOk, tgOk) = withContext(Dispatchers.IO) {
                runCatching {
                    Pair(
                        socialDispatchRepository.isFacebookConfigured(),
                        socialDispatchRepository.isTelegramConfigured()
                    )
                }.getOrDefault(Pair(false, false))
            }
            _uiState.update {
                it.copy(
                    narrativeFbConfigured = fbOk,
                    narrativeTgConfigured = tgOk
                )
            }
            refreshNasaFeed()
            refreshHawaiiAirspace()
            refreshHawaiiWebcams()
            seedInitialNarrativeSignal()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun selectIntelSubMode(mode: IntelSubMode) {
        _uiState.update { it.copy(selectedIntelSubMode = mode) }
    }

    fun setScopeFilter(scope: OperationalScope) {
        _uiState.update { it.copy(selectedScopeFilter = scope) }
    }

    fun updateRepositorySearch(query: String) {
        _uiState.update { it.copy(repositorySearchQuery = query) }
    }

    fun selectRepository(repo: ArchivalRepository?) {
        _uiState.update { it.copy(selectedRepository = repo) }
    }

    fun toggleBookmark(id: String) {
        _uiState.update { state ->
            val updated = state.bookmarks.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(bookmarks = updated)
        }
    }

    fun selectCase(caseItem: ForensicCase?) {
        _uiState.update { it.copy(selectedCase = caseItem) }
    }

    fun setCreatingNewCase(creating: Boolean) {
        _uiState.update { it.copy(isCreatingNewCase = creating) }
    }

    // -----------------------------------------------------------------------
    // NASA Satellite Feed Actions
    // -----------------------------------------------------------------------

    fun refreshNasaFeed() {
        val current = _uiState.value
        val category = current.nasaSelectedCategory
        val status = if (current.nasaIncludeClosed) "all" else "open"

        _uiState.update { it.copy(nasaFeedState = NasaFeedLoadState.Loading) }

        viewModelScope.launch {
            val result = nasaFeedRepository.fetchEvents(
                category = category,
                status = status,
                limit = 30
            )
            result.fold(
                onSuccess = { events ->
                    _uiState.update { state ->
                        val refreshedSelected = state.selectedNasaEvent?.let { sel ->
                            events.find { it.id == sel.id } ?: sel
                        }
                        val autoEvent = if (!state.hasCustomGibsTarget) {
                            refreshedSelected ?: events.firstOrNull()
                        } else {
                            refreshedSelected
                        }
                        val updatedTarget = if (!state.hasCustomGibsTarget && autoEvent != null) {
                            GibsTarget(
                                label = autoEvent.title,
                                latitude = autoEvent.latitude,
                                longitude = autoEvent.longitude,
                                dateIso = autoEvent.latestDateIso,
                                eventId = autoEvent.id
                            )
                        } else {
                            state.customGibsTarget
                        }
                        state.copy(
                            nasaFeedState = NasaFeedLoadState.Success(events),
                            selectedNasaEvent = autoEvent,
                            customGibsTarget = updatedTarget
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            nasaFeedState = NasaFeedLoadState.Error(
                                error.localizedMessage ?: "Unable to reach NASA EONET v3 satellite feed."
                            )
                        )
                    }
                }
            )
        }
    }

    fun selectNasaCategory(category: NasaEonetCategory) {
        _uiState.update {
            it.copy(
                nasaSelectedCategory = category,
                selectedNasaEvent = null,
                hasCustomGibsTarget = false
            )
        }
        refreshNasaFeed()
    }

    fun toggleNasaIncludeClosed(includeClosed: Boolean) {
        _uiState.update {
            it.copy(
                nasaIncludeClosed = includeClosed,
                selectedNasaEvent = null,
                hasCustomGibsTarget = false
            )
        }
        refreshNasaFeed()
    }

    fun updateNasaSearchQuery(query: String) {
        _uiState.update { it.copy(nasaSearchQuery = query) }
    }

    fun selectNasaEvent(event: NasaSatEvent) {
        _uiState.update {
            it.copy(
                selectedNasaEvent = event,
                hasCustomGibsTarget = false,
                satVisualZoom = 1.0f,
                customGibsTarget = GibsTarget(
                    label = event.title,
                    latitude = event.latitude,
                    longitude = event.longitude,
                    dateIso = event.latestDateIso,
                    eventId = event.id
                )
            )
        }
    }

    fun selectGibsLayer(layer: NasaGibsLayer) {
        _uiState.update { it.copy(activeGibsLayer = layer, gibsRefreshNonce = System.currentTimeMillis()) }
    }

    fun setGibsSpanDegrees(span: Double) {
        _uiState.update { it.copy(gibsSpanDegrees = span.coerceIn(0.25, 12.0)) }
    }

    fun setSatVisualZoom(zoom: Float) {
        _uiState.update { it.copy(satVisualZoom = zoom.coerceIn(1.0f, 5.0f)) }
    }

    fun toggleSatScrollLocked() {
        _uiState.update { it.copy(satScrollLocked = !it.satScrollLocked) }
    }

    fun toggleSatReticle() {
        _uiState.update { it.copy(satShowReticle = !it.satShowReticle) }
    }

    fun toggleSatCoordGrid() {
        _uiState.update { it.copy(satShowCoordGrid = !it.satShowCoordGrid) }
    }

    fun toggleSatHud() {
        _uiState.update { it.copy(satShowHud = !it.satShowHud) }
    }

    fun reloadGibsTile() {
        _uiState.update { it.copy(gibsRefreshNonce = System.currentTimeMillis()) }
    }

    fun shiftGibsDateDays(deltaDays: Int) {
        val current = _uiState.value.customGibsTarget
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        val todayMillis = System.currentTimeMillis()
        val parsed = runCatching { sdf.parse(current.dateIso.trim().take(10)) }.getOrNull() ?: Date(todayMillis)
        val clampedTime = (parsed.time + deltaDays * 86_400_000L).coerceAtMost(todayMillis)
        val formatted = sdf.format(Date(clampedTime))
        _uiState.update {
            it.copy(
                customGibsTarget = current.copy(dateIso = formatted),
                gibsRefreshNonce = System.currentTimeMillis()
            )
        }
    }

    fun setCustomGibsTarget(latitude: Double, longitude: Double, dateIso: String, label: String) {
        val safeLat = if (latitude.isNaN() || latitude.isInfinite()) 19.5614 else latitude.coerceIn(-89.5, 89.5)
        val safeLon = if (longitude.isNaN() || longitude.isInfinite()) -155.0131 else longitude.coerceIn(-179.5, 179.5)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        val trimmedDate = dateIso.trim().take(10)
        val validDate = if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(trimmedDate) && runCatching { sdf.parse(trimmedDate) }.getOrNull() != null) {
            trimmedDate
        } else {
            "2024-08-14"
        }
        _uiState.update {
            it.copy(
                selectedNasaEvent = null,
                hasCustomGibsTarget = true,
                satVisualZoom = 1.0f,
                customGibsTarget = GibsTarget(
                    label = label.ifBlank { "Custom Sector" },
                    latitude = safeLat,
                    longitude = safeLon,
                    dateIso = validDate
                ),
                gibsRefreshNonce = System.currentTimeMillis()
            )
        }
    }

    fun inspectCaseInSatFeed(forensicCase: ForensicCase) {
        val datePart = forensicCase.incidentDate.take(10)
        val isValidDate = Regex("\\d{4}-\\d{2}-\\d{2}").matches(datePart)
        val gibsSafeDate = if (!isValidDate || datePart > NasaGibsUrlBuilder.todayUtcIso()) {
            NasaGibsUrlBuilder.yesterdayUtcIso()
        } else {
            datePart
        }
        _uiState.update {
            it.copy(
                selectedCase = null,
                selectedNasaEvent = null,
                hasCustomGibsTarget = true,
                satVisualZoom = 1.0f,
                customGibsTarget = GibsTarget(
                    label = "${forensicCase.caseNumber}: ${forensicCase.locationName}",
                    latitude = forensicCase.latitude,
                    longitude = forensicCase.longitude,
                    dateIso = gibsSafeDate
                ),
                gibsRefreshNonce = System.currentTimeMillis(),
                currentTab = NavigationTab.SAT_FEED
            )
        }
    }

    fun ingestNasaEventToCasebook(event: NasaSatEvent) {
        val layer = _uiState.value.activeGibsLayer
        val activeDate = _uiState.value.customGibsTarget.dateIso
        val gibsUrl = NasaGibsUrlBuilder.buildSnapshotUrl(
            latitude = event.latitude,
            longitude = event.longitude,
            dateIso = activeDate,
            layer = layer,
            spanDegrees = _uiState.value.gibsSpanDegrees
        )
        val newCaseNumber = "NASA-${event.id}"
        val existing = _uiState.value.cases.find { it.caseNumber == newCaseNumber }
        if (existing != null) {
            val evKey = "${layer.shortLabel} @ $activeDate"
            val alreadyHasSnapshot = existing.evidenceItems.any {
                it.technicalAttributes["GIBS Layer"] == layer.layersParam &&
                    it.capturedTimestamp.startsWith(activeDate)
            }
            val nowUtc = SocialDispatchRepository.formatUtcTimestamp()
            val updatedEvidence = if (alreadyHasSnapshot) {
                existing.evidenceItems
            } else {
                existing.evidenceItems + EvidenceItem(
                    id = "ev_gibs_${event.id}_${UUID.randomUUID().toString().take(6)}",
                    title = "NASA Worldview GIBS Snapshot ($evKey)",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "eonet-${event.id}-$activeDate-${layer.id}".padEnd(64, '0').take(64),
                    capturedTimestamp = "$activeDate 00:00:00 UTC",
                    sensorOrDevice = layer.instrument,
                    fileSizeBytes = 640_000L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "EONET Event ID" to event.id,
                        "GIBS Layer" to layer.layersParam,
                        "Observation Date" to activeDate,
                        "Snapshot URI" to gibsUrl
                    ),
                    notes = layer.forensicUse
                )
            }
            val updatedCase = existing.copy(
                status = if (event.isClosed) CaseStatus.CORROBORATED else existing.status,
                evidenceItems = updatedEvidence,
                chainOfCustody = existing.chainOfCustody + CustodyLogEvent(
                    timestamp = nowUtc,
                    custodian = "NASA EOSDIS / EONET v3 Sync",
                    organization = event.sourceId,
                    actionTaken = "Updated satellite dossier with $evKey telemetry (${event.trackPointCount} observations).",
                    digitalSignatureHash = "sync-${UUID.randomUUID().toString().take(8)}"
                )
            )
            _uiState.update { state ->
                state.copy(
                    cases = state.cases.map { if (it.id == updatedCase.id) updatedCase else it },
                    currentTab = NavigationTab.INTEL,
                    selectedIntelSubMode = IntelSubMode.CASEBOOK,
                    selectedCase = updatedCase
                )
            }
            return
        }

        val newCase = ForensicCase(
            id = "case_nasa_${event.id}",
            caseNumber = newCaseNumber,
            title = event.title,
            incidentDate = event.latestTimestamp,
            locationName = "NASA EONET ${event.categoryTitle} Telemetry",
            latitude = event.latitude,
            longitude = event.longitude,
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            status = if (event.isClosed) CaseStatus.CORROBORATED else CaseStatus.OPEN,
            corroborationTier = "Tier 1 - NASA EOSDIS / GIBS Satellite Observation",
            casualtyEstimate = event.magnitudeLabel ?: "Remote Sensing Event",
            weaponIndicators = listOf(event.categoryTitle, "Source: ${event.sourceId}"),
            summary = event.description ?: "Live NASA EONET v3 satellite-detected ${event.categoryTitle} event ingested with ${layer.fullTitle} imagery.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_gibs_${event.id}",
                    title = "NASA Worldview GIBS Snapshot (${layer.shortLabel})",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "eonet-${event.id}-$activeDate".padEnd(64, '0').take(64),
                    capturedTimestamp = event.latestTimestamp,
                    sensorOrDevice = layer.instrument,
                    fileSizeBytes = 640_000L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "EONET Event ID" to event.id,
                        "GIBS Layer" to layer.layersParam,
                        "Track Geometries" to "${event.trackPointCount} observations",
                        "Snapshot URI" to gibsUrl
                    ),
                    notes = layer.forensicUse
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = event.latestTimestamp,
                    custodian = "NASA EOSDIS / EONET v3 Feed",
                    organization = event.sourceId,
                    actionTaken = "Ingested live satellite telemetry and GIBS snapshot bounding box into VeriScope Casebook.",
                    digitalSignatureHash = "nasa-eonet-${event.id}"
                )
            )
        )

        _uiState.update { state ->
            state.copy(
                cases = listOf(newCase) + state.cases,
                currentTab = NavigationTab.INTEL,
                selectedIntelSubMode = IntelSubMode.CASEBOOK,
                selectedCase = newCase
            )
        }
    }

    fun ingestActiveGibsTargetToCasebook() {
        val selected = _uiState.value.selectedNasaEvent
        if (selected != null) {
            ingestNasaEventToCasebook(selected)
            return
        }
        val state = _uiState.value
        val target = state.customGibsTarget
        val layer = state.activeGibsLayer
        val gibsUrl = NasaGibsUrlBuilder.buildSnapshotUrl(
            latitude = target.latitude,
            longitude = target.longitude,
            dateIso = target.dateIso,
            layer = layer,
            spanDegrees = state.gibsSpanDegrees
        )
        val suffix = UUID.randomUUID().toString().take(6)
        val newCase = ForensicCase(
            id = "case_gibs_${System.currentTimeMillis()}_$suffix",
            caseNumber = "GIBS-${target.dateIso.replace("-", "").takeLast(4)}-${suffix.uppercase(Locale.US)}",
            title = "Satellite Target: ${target.label}",
            incidentDate = "${target.dateIso} 00:00:00 UTC",
            locationName = target.label,
            latitude = target.latitude,
            longitude = target.longitude,
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            status = CaseStatus.OPEN,
            corroborationTier = "Tier 2 - NASA Worldview GIBS Spectral Swath",
            casualtyEstimate = "Geospatial Sector Observation",
            weaponIndicators = listOf(layer.shortLabel, "BBOX Span ${state.gibsSpanDegrees}°"),
            summary = "Captured ${layer.fullTitle} swath over ${target.label} (${String.format(Locale.US, "%.4f°, %.4f°", target.latitude, target.longitude)}) for UTC ${target.dateIso}.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_sat_$suffix",
                    title = "NASA Worldview GIBS (${layer.shortLabel} • ${target.dateIso})",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "gibs-$suffix-${target.dateIso}".padEnd(64, '0').take(64),
                    capturedTimestamp = "${target.dateIso} 00:00:00 UTC",
                    sensorOrDevice = layer.instrument,
                    fileSizeBytes = 720_000L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "GIBS Layer" to layer.layersParam,
                        "BBOX Span" to "${state.gibsSpanDegrees}°",
                        "Snapshot URI" to gibsUrl
                    ),
                    notes = layer.forensicUse
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = SocialDispatchRepository.formatUtcTimestamp(),
                    custodian = "Satellite Feed Operator",
                    organization = "VeriScope NASA GIBS Desk",
                    actionTaken = "Archived custom satellite sector snapshot into Evidence Casebook.",
                    digitalSignatureHash = "gibs-$suffix"
                )
            )
        )
        _uiState.update {
            it.copy(
                cases = listOf(newCase) + it.cases,
                currentTab = NavigationTab.INTEL,
                selectedIntelSubMode = IntelSubMode.CASEBOOK,
                selectedCase = newCase
            )
        }
    }

    fun sendSatTargetToAiIntel() {
        val state = _uiState.value
        val target = state.customGibsTarget
        val layer = state.activeGibsLayer
        val sitrep = buildString {
            append("NASA GIBS Satellite Telemetry (${target.dateIso}): ${target.label} at ")
            append(String.format(Locale.US, "%.4f, %.4f", target.latitude, target.longitude))
            append(". Active band: ${layer.fullTitle} (${layer.instrument}). ")
            append("Span: ${state.gibsSpanDegrees} deg. ")
            state.selectedNasaEvent?.let { ev ->
                append("EONET ${ev.id} (${ev.categoryTitle}, source ${ev.sourceId}). ")
                if (!ev.magnitudeLabel.isNullOrBlank()) append("Intensity: ${ev.magnitudeLabel}.")
            }
        }
        val query = "${target.label} ${layer.shortLabel} satellite thermal anomaly"
        _uiState.update {
            it.copy(
                vectorQuery = query,
                vectorResults = AiVectorSimulator.search(query),
                lmmInputText = sitrep,
                lmmExtractedStac = AiVectorSimulator.simulateLmmExtraction(sitrep),
                currentTab = NavigationTab.INTEL,
                selectedIntelSubMode = IntelSubMode.AI_VECTOR
            )
        }
    }

    fun addNewCase(
        title: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        operationalScope: OperationalScope,
        weapon: String,
        summary: String
    ) {
        val existingNumbers = _uiState.value.cases.mapTo(mutableSetOf()) { it.caseNumber }
        var newCaseNumber = "VERI-2026-${(1000..9999).random()}"
        while (existingNumbers.contains(newCaseNumber)) {
            newCaseNumber = "VERI-2026-${(1000..9999).random()}"
        }
        val uniqueSuffix = UUID.randomUUID().toString().take(8)
        val newCase = ForensicCase(
            id = "case_${System.currentTimeMillis()}_$uniqueSuffix",
            caseNumber = newCaseNumber,
            title = title.ifBlank { "Untitled Evidence Case" },
            incidentDate = "2026-10-27 18:00:00 UTC",
            locationName = locationName.ifBlank { "Unspecified Coordinates" },
            latitude = latitude.coerceIn(-90.0, 90.0),
            longitude = longitude.coerceIn(-180.0, 180.0),
            operationalScope = operationalScope,
            status = CaseStatus.OPEN,
            corroborationTier = "Tier 3 - Single Source UGC",
            casualtyEstimate = "Under Assessment",
            weaponIndicators = if (weapon.isBlank()) listOf("Unconfirmed munition type") else listOf(weapon),
            summary = summary.ifBlank { "Investigative evidence collection initiated. Chain of custody opened." },
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_$uniqueSuffix",
                    title = "Initial Intake Photographic Evidence",
                    assetType = EvidenceAssetType.IMAGE_EXIF,
                    hashSha256 = "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0",
                    capturedTimestamp = "2026-10-27 18:05:00 UTC",
                    sensorOrDevice = "Field Operator Mobile Capture",
                    fileSizeBytes = 4500120L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Ingestion Protocol" to "Secure Cryptographic Escrow",
                        "Resolution" to "4032 x 3024 px",
                        "Original Color Space" to "Display P3"
                    ),
                    notes = "Intake file verified against tampering; awaiting second corroborating sensor."
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = "2026-10-27 18:10:00 UTC",
                    custodian = "Investigator",
                    organization = "VeriScope Intelligence Cell",
                    actionTaken = "Initiated digital evidence locker and recorded cryptographic hash.",
                    digitalSignatureHash = "f0e1d2...c3b4"
                )
            )
        )

        _uiState.update { state ->
            state.copy(
                cases = listOf(newCase) + state.cases,
                isCreatingNewCase = false,
                selectedCase = newCase
            )
        }
    }

    fun updateCaseSearchQuery(query: String) {
        _uiState.update { it.copy(caseSearchQuery = query) }
    }

    fun setCaseStatusFilter(status: CaseStatus?) {
        _uiState.update { it.copy(caseStatusFilter = status) }
    }

    fun updateCaseStatus(caseId: String, newStatus: CaseStatus) {
        val nowUtc = SocialDispatchRepository.formatUtcTimestamp()
        _uiState.update { state ->
            val updatedList = state.cases.map { c ->
                if (c.id != caseId || c.status == newStatus) {
                    c
                } else {
                    c.copy(
                        status = newStatus,
                        chainOfCustody = c.chainOfCustody + CustodyLogEvent(
                            timestamp = nowUtc,
                            custodian = "Lead Investigator",
                            organization = "VeriScope Casebook Desk",
                            actionTaken = "Updated dossier status from ${c.status.label} to ${newStatus.label}.",
                            digitalSignatureHash = "stat-${UUID.randomUUID().toString().take(8)}"
                        )
                    )
                }
            }
            val updatedSelected = updatedList.find { it.id == caseId } ?: state.selectedCase
            state.copy(cases = updatedList, selectedCase = updatedSelected)
        }
    }

    fun appendCaseIntelUpdate(
        caseId: String,
        title: String,
        note: String,
        sensor: String = "Field SITREP / Cross-Feed Telemetry",
        assetType: EvidenceAssetType = EvidenceAssetType.SATELLITE_SAR
    ) {
        val cleanTitle = title.trim().ifBlank { "Corroborating Intel Update" }
        val cleanNote = note.trim()
        if (cleanNote.isEmpty()) return
        val nowUtc = SocialDispatchRepository.formatUtcTimestamp()
        val evId = "ev_upd_${UUID.randomUUID().toString().take(8)}"
        _uiState.update { state ->
            val updatedList = state.cases.map { c ->
                if (c.id != caseId) {
                    c
                } else {
                    val newEv = EvidenceItem(
                        id = evId,
                        title = cleanTitle,
                        assetType = assetType,
                        hashSha256 = "upd-$evId-$nowUtc".hashCode().toString(16).padEnd(64, 'a').take(64),
                        capturedTimestamp = nowUtc,
                        sensorOrDevice = sensor.trim().ifBlank { "Multi-Source Intel Feed" },
                        fileSizeBytes = 256_000L,
                        verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                        technicalAttributes = mapOf(
                            "Update Channel" to "Casebook Intel Append",
                            "Reference Case" to c.caseNumber
                        ),
                        notes = cleanNote
                    )
                    c.copy(
                        evidenceItems = c.evidenceItems + newEv,
                        chainOfCustody = c.chainOfCustody + CustodyLogEvent(
                            timestamp = nowUtc,
                            custodian = "Case Analyst",
                            organization = "VeriScope Intel Update",
                            actionTaken = "Appended intel item '$cleanTitle': ${cleanNote.take(120)}",
                            digitalSignatureHash = "intel-${evId.takeLast(6)}"
                        )
                    )
                }
            }
            val updatedSelected = updatedList.find { it.id == caseId } ?: state.selectedCase
            state.copy(cases = updatedList, selectedCase = updatedSelected)
        }
    }

    fun sendCaseToAiIntel(forensicCase: ForensicCase) {
        val sitrep = buildString {
            append("${forensicCase.caseNumber} (${forensicCase.locationName}) at ")
            append(String.format(Locale.US, "%.4f, %.4f", forensicCase.latitude, forensicCase.longitude))
            append(". Status: ${forensicCase.status.label}. ")
            append(forensicCase.summary)
            if (forensicCase.weaponIndicators.isNotEmpty()) {
                append(" Indicators: ${forensicCase.weaponIndicators.joinToString(", ")}.")
            }
        }
        val query = "${forensicCase.title} ${forensicCase.weaponIndicators.firstOrNull().orEmpty()}"
        _uiState.update {
            it.copy(
                selectedCase = null,
                vectorQuery = query.trim(),
                vectorResults = AiVectorSimulator.search(query),
                lmmInputText = sitrep,
                lmmExtractedStac = AiVectorSimulator.simulateLmmExtraction(sitrep),
                currentTab = NavigationTab.INTEL,
                selectedIntelSubMode = IntelSubMode.AI_VECTOR
            )
        }
    }

    fun ingestStacIntelToCasebook(sitrepText: String) {
        val clean = sitrepText.trim()
        if (clean.isEmpty()) return
        val extracted = AiVectorSimulator.simulateLmmExtraction(clean)
        val coordMatch = Regex("(-?\\d{1,2}\\.\\d{2,6})\\s*,\\s*(-?\\d{1,3}\\.\\d{2,6})").find(clean)
        val isHawaii = clean.lowercase(Locale.US).let {
            it.contains("orchidland") || it.contains("puna") || it.contains("hilo") ||
                it.contains("pohakuloa") || it.contains("saddle")
        }
        val lat = coordMatch?.groupValues?.get(1)?.toDoubleOrNull()?.coerceIn(-89.5, 89.5)
            ?: if (isHawaii) OrchidlandDicmData.ORCHIDLAND_LAT else 46.4825
        val lon = coordMatch?.groupValues?.get(2)?.toDoubleOrNull()?.coerceIn(-179.5, 179.5)
            ?: if (isHawaii) OrchidlandDicmData.ORCHIDLAND_LON else 30.7233
        val suffix = UUID.randomUUID().toString().take(6).uppercase(Locale.US)
        val nowUtc = SocialDispatchRepository.formatUtcTimestamp()
        val newCase = ForensicCase(
            id = "case_stac_${System.currentTimeMillis()}_$suffix",
            caseNumber = "INTL-2026-$suffix",
            title = "AI STAC Synthesis: ${clean.take(54)}${if (clean.length > 54) "..." else ""}",
            incidentDate = nowUtc,
            locationName = if (isHawaii) "Lower Puna / Orchidland Sector, HI" else "AI Vector Georeferenced Sector",
            latitude = lat,
            longitude = lon,
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            status = CaseStatus.OPEN,
            corroborationTier = "Tier 2 - LMM STAC 1.0.0 Extraction",
            casualtyEstimate = "Intel Assessment Logged",
            weaponIndicators = listOf("LMM STAC Feature", "Multi-Sensor Vector Synthesis"),
            summary = clean.take(240),
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_stac_$suffix",
                    title = "Extracted STAC 1.0.0 GeoJSON Feature",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "stac-$suffix-$nowUtc".hashCode().toString(16).padEnd(64, '0').take(64),
                    capturedTimestamp = nowUtc,
                    sensorOrDevice = "VeriScope LMM STAC Extractor",
                    fileSizeBytes = extracted.length.toLong(),
                    verificationStatus = VerificationStatus.C2PA_SIGNED,
                    technicalAttributes = mapOf(
                        "STAC Version" to "1.0.0",
                        "Coordinates" to String.format(Locale.US, "%.4f, %.4f", lat, lon)
                    ),
                    notes = extracted.take(260)
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = nowUtc,
                    custodian = "AI Vector Intelligence",
                    organization = "VeriScope STAC Pipeline",
                    actionTaken = "Promoted synthesized STAC feature and SITREP narrative into Evidence Casebook.",
                    digitalSignatureHash = "stac-$suffix"
                )
            )
        )
        _uiState.update {
            it.copy(
                cases = listOf(newCase) + it.cases,
                lmmInputText = clean,
                lmmExtractedStac = extracted,
                selectedIntelSubMode = IntelSubMode.CASEBOOK,
                selectedCase = newCase
            )
        }
    }

    // Forensic Lab Actions
    fun selectLabTool(tool: LabTool) {
        _uiState.update { it.copy(activeLabTool = tool) }
    }

    fun updateChronolocation(
        height: Double = _uiState.value.chronoObjectHeight,
        shadow: Double = _uiState.value.chronoShadowLength,
        lat: Double = _uiState.value.chronoLatitude,
        lon: Double = _uiState.value.chronoLongitude,
        day: Int = _uiState.value.chronoDayOfYear
    ) {
        val result = SunChronolocationCalculator.calculate(height, shadow, lat, lon, day)
        _uiState.update {
            it.copy(
                chronoObjectHeight = height,
                chronoShadowLength = shadow,
                chronoLatitude = lat,
                chronoLongitude = lon,
                chronoDayOfYear = day,
                chronoResult = result
            )
        }
    }

    fun selectElaPreset(preset: String) {
        val assessment = ErrorLevelAnalysisEngine.evaluate(preset)
        _uiState.update {
            it.copy(
                selectedElaPreset = preset,
                elaAssessment = assessment
            )
        }
    }

    fun updateBallisticComparison(casingA: String, casingB: String, slider: Float) {
        val comparison = BallisticMetrologyEngine.compare(casingA, casingB)
        _uiState.update {
            it.copy(
                ballisticCasingA = casingA,
                ballisticCasingB = casingB,
                ballisticSplitSlider = slider,
                ballisticComparison = comparison
            )
        }
    }

    fun updateCbrnSearch(query: String) {
        _uiState.update { it.copy(cbrnSearchQuery = query) }
    }

    fun selectCbrnHazard(hazard: CbrnHazard?) {
        _uiState.update { it.copy(selectedCbrnHazard = hazard) }
    }

    fun selectGeointSensor(sensor: GeointSensor?) {
        _uiState.update { it.copy(selectedGeointSensor = sensor) }
    }

    fun executeVectorSearch(query: String) {
        val results = AiVectorSimulator.search(query)
        _uiState.update {
            it.copy(
                vectorQuery = query,
                vectorResults = results
            )
        }
    }

    fun updateLmmInputText(text: String) {
        val extracted = AiVectorSimulator.simulateLmmExtraction(text)
        _uiState.update {
            it.copy(
                lmmInputText = text,
                lmmExtractedStac = extracted
            )
        }
    }

    // -----------------------------------------------------------------------
    // Orchidland Estates DICM & Cyber Operations Actions
    // -----------------------------------------------------------------------

    fun selectDicmTab(tab: DicmPhaseTab) {
        _uiState.update { it.copy(selectedDicmTab = tab) }
    }

    fun selectAgentProfile(profile: TaskAgentProfile?) {
        _uiState.update { it.copy(selectedAgentProfile = profile) }
    }

    fun selectPlatformNode(platform: PlatformNodeProfile?) {
        _uiState.update { it.copy(selectedPlatformNode = platform) }
    }

    fun selectStrategicFramework(code: String) {
        _uiState.update { it.copy(selectedStrategicFramework = code) }
    }

    fun selectCoaCode(code: String) {
        _uiState.update { it.copy(selectedCoaCode = code) }
    }

    fun setSnaMatrixMode(matrix: Boolean) {
        _uiState.update { it.copy(snaMatrixMode = matrix) }
    }

    fun selectGraphNode(nodeId: String?) {
        _uiState.update { it.copy(selectedGraphNodeId = nodeId) }
    }

    fun toggleIsolateGraphNode(nodeId: String) {
        _uiState.update { state ->
            val updated = state.isolatedGraphNodeIds.toMutableSet()
            if (updated.contains(nodeId)) updated.remove(nodeId) else updated.add(nodeId)
            val report = DicmSnaEngine.analyzeNetwork(updated)
            state.copy(
                isolatedGraphNodeIds = updated,
                snaNetworkReport = report
            )
        }
    }

    fun applySnaPresetTrapVsBridge(isolateSwarmTrap: Boolean) {
        val presetIds = if (isolateSwarmTrap) {
            setOf("n_srx_1", "n_srx_2", "n_srx_3")
        } else {
            setOf("n_aut_c2", "n_hum_opr")
        }
        val report = DicmSnaEngine.analyzeNetwork(presetIds)
        _uiState.update {
            it.copy(
                isolatedGraphNodeIds = presetIds,
                snaNetworkReport = report,
                selectedGraphNodeId = if (isolateSwarmTrap) "n_srx_4" else "n_aut_c2"
            )
        }
    }

    fun resetSnaGraph() {
        _uiState.update {
            it.copy(
                isolatedGraphNodeIds = emptySet(),
                snaNetworkReport = DicmSnaEngine.analyzeNetwork(emptySet()),
                selectedGraphNodeId = "n_aut_c2"
            )
        }
    }

    fun toggleNistCountermeasure(id: String) {
        _uiState.update { state ->
            val updated = state.deployedNistIds.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(deployedNistIds = updated)
        }
    }

    fun toggleGuardrailCheck(id: String) {
        _uiState.update { state ->
            val updated = state.checkedGuardrailIds.toMutableSet()
            if (updated.contains(id)) updated.remove(id) else updated.add(id)
            state.copy(checkedGuardrailIds = updated)
        }
    }

    fun focusOrchidlandInSatFeed() {
        _uiState.update {
            it.copy(
                selectedNasaEvent = null,
                hasCustomGibsTarget = true,
                customGibsTarget = GibsTarget(
                    label = "Orchidland Estates (Lower Puna, HI)",
                    latitude = OrchidlandDicmData.ORCHIDLAND_LAT,
                    longitude = OrchidlandDicmData.ORCHIDLAND_LON,
                    dateIso = NasaGibsUrlBuilder.yesterdayUtcIso()
                ),
                gibsSpanDegrees = 1.5,
                satVisualZoom = 1.0f,
                gibsRefreshNonce = System.currentTimeMillis(),
                currentTab = NavigationTab.SAT_FEED
            )
        }
    }

    // -----------------------------------------------------------------------
    // Hawaiʻi Island Airspace & PTA Military Drill Monitor Actions
    // -----------------------------------------------------------------------

    fun refreshHawaiiAirspace() {
        _uiState.update {
            it.copy(
                airspaceLoadState = AirspaceLoadState.Loading,
                dopplerRefreshNonce = System.currentTimeMillis()
            )
        }
        viewModelScope.launch {
            val snapshot = hawaiiAirspaceRepository.fetchSnapshot()
            _uiState.update {
                it.copy(
                    airspaceLoadState = AirspaceLoadState.Ready,
                    airspaceSnapshot = snapshot
                )
            }
        }
    }

    fun toggleDopplerEnabled(enabled: Boolean) {
        val start = System.currentTimeMillis()
        _uiState.update {
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            it.copy(
                dopplerEnabled = enabled,
                activeLayerPreset = null,
                mapPerfMetrics = it.mapPerfMetrics.copy(lastLayerSwitchMs = elapsed)
            )
        }
    }

    fun selectDopplerLayer(layer: HawaiiDopplerLayer) {
        val start = System.currentTimeMillis()
        _uiState.update {
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            it.copy(
                dopplerEnabled = true,
                activeDopplerLayer = layer,
                activeLayerPreset = null,
                mapPerfMetrics = it.mapPerfMetrics.copy(lastLayerSwitchMs = elapsed)
            )
        }
    }

    fun setDopplerOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.2f, 1.0f)
        _uiState.update {
            prefsStore?.saveOpacities(it.airspaceSatOpacity, clamped, it.airspaceOpacity)
            it.copy(dopplerOpacity = clamped)
        }
    }

    fun setAirspaceMapZoom(zoom: Float) {
        _uiState.update { it.copy(airspaceMapZoom = zoom.coerceIn(1.0f, 8.0f)) }
    }

    fun toggleAirspaceMapScrollLocked() {
        _uiState.update { it.copy(airspaceMapScrollLocked = !it.airspaceMapScrollLocked) }
    }

    fun toggleAirspaceMapExpanded() {
        _uiState.update { it.copy(airspaceMapExpanded = !it.airspaceMapExpanded) }
    }

    fun toggleAirspaceGridAndRoads() {
        _uiState.update { it.copy(airspaceShowGridAndRoads = !it.airspaceShowGridAndRoads) }
    }

    fun toggleAirspaceSpecialZones() {
        _uiState.update { it.copy(airspaceShowSpecialZones = !it.airspaceShowSpecialZones) }
    }

    fun toggleAirspaceTfrs() {
        _uiState.update { it.copy(airspaceShowTfrs = !it.airspaceShowTfrs) }
    }

    fun toggleAirspaceRadarRings() {
        _uiState.update { it.copy(airspaceShowRadarRings = !it.airspaceShowRadarRings) }
    }

    fun toggleAirspaceSitePins() {
        _uiState.update { it.copy(airspaceShowSitePins = !it.airspaceShowSitePins) }
    }

    fun toggleAirspaceWebcamPins() {
        _uiState.update { it.copy(airspaceShowWebcamPins = !it.airspaceShowWebcamPins) }
    }

    fun reloadDopplerOverlay() {
        _uiState.update { it.copy(dopplerRefreshNonce = System.currentTimeMillis()) }
    }

    fun toggleAirspaceSatEnabled(enabled: Boolean = !_uiState.value.airspaceSatEnabled) {
        val start = System.currentTimeMillis()
        _uiState.update {
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            it.copy(
                airspaceSatEnabled = enabled,
                activeLayerPreset = null,
                mapPerfMetrics = it.mapPerfMetrics.copy(lastLayerSwitchMs = elapsed)
            )
        }
    }

    fun selectAirspaceSatLayer(layer: NasaGibsLayer) {
        val start = System.currentTimeMillis()
        _uiState.update {
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            it.copy(
                airspaceSatEnabled = true,
                airspaceSatLayer = layer,
                airspaceSatRefreshNonce = System.currentTimeMillis(),
                activeLayerPreset = null,
                mapPerfMetrics = it.mapPerfMetrics.copy(lastLayerSwitchMs = elapsed)
            )
        }
    }

    fun setAirspaceSatOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.25f, 1.0f)
        _uiState.update {
            prefsStore?.saveOpacities(clamped, it.dopplerOpacity, it.airspaceOpacity)
            it.copy(airspaceSatOpacity = clamped)
        }
    }

    fun shiftAirspaceSatDateDays(deltaDays: Int) {
        val current = _uiState.value.airspaceSatDateIso
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }
        val todayMillis = System.currentTimeMillis()
        val parsed = runCatching { sdf.parse(current.trim().take(10)) }.getOrNull() ?: Date(todayMillis)
        val clampedTime = (parsed.time + deltaDays * 86_400_000L).coerceAtMost(todayMillis)
        val formatted = sdf.format(Date(clampedTime))
        _uiState.update {
            it.copy(
                airspaceSatEnabled = true,
                airspaceSatDateIso = formatted,
                airspaceSatRefreshNonce = System.currentTimeMillis()
            )
        }
    }

    fun reloadAirspaceSatOverlay() {
        _uiState.update { it.copy(airspaceSatRefreshNonce = System.currentTimeMillis()) }
    }

    fun openAirspaceSiteInFullSatFeed(site: HawaiiAirspaceSite = _uiState.value.selectedAirspaceSite) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                selectedNasaEvent = null,
                hasCustomGibsTarget = true,
                activeGibsLayer = state.airspaceSatLayer,
                gibsSpanDegrees = 1.0,
                satVisualZoom = 1.0f,
                customGibsTarget = GibsTarget(
                    label = "${site.shortCode}: ${site.name}",
                    latitude = site.lat,
                    longitude = site.lon,
                    dateIso = state.airspaceSatDateIso
                ),
                gibsRefreshNonce = System.currentTimeMillis(),
                currentTab = NavigationTab.SAT_FEED
            )
        }
    }

    fun selectAirspaceSite(site: HawaiiAirspaceSite) {
        _uiState.update { it.copy(selectedAirspaceSite = site) }
    }

    fun refreshHawaiiWebcams() {
        _uiState.update {
            it.copy(
                webcamsLoadState = WebcamsLoadState.Loading,
                webcamRefreshNonce = System.currentTimeMillis()
            )
        }
        viewModelScope.launch {
            val snapshot = hawaiiWebcamsRepository.fetchSnapshot()
            _uiState.update {
                it.copy(
                    webcamsLoadState = WebcamsLoadState.Ready,
                    webcamsSnapshot = snapshot
                )
            }
        }
    }

    fun selectWebcamFilter(filter: WebcamFilter) {
        _uiState.update { it.copy(selectedWebcamFilter = filter) }
    }

    fun selectWebcamStill(still: HawaiiWebcamStill?) {
        _uiState.update { it.copy(selectedWebcamStill = still) }
    }

    fun reloadWebcamStills() {
        _uiState.update { it.copy(webcamRefreshNonce = System.currentTimeMillis()) }
    }

    fun parseAndAddPtaAdvisory(subject: String, sender: String, bodyText: String) {
        val parsed = PtaAdvisoryParser.parseAdvisory(
            subject = subject,
            sender = sender,
            bodyText = bodyText
        )
        _uiState.update { state ->
            val duplicate = state.ptaAdvisories.any {
                it.subject.equals(parsed.subject, ignoreCase = true) && it.excerpt == parsed.excerpt
            }
            if (duplicate) state else state.copy(ptaAdvisories = listOf(parsed) + state.ptaAdvisories)
        }
    }

    fun openAirspaceTab() {
        _uiState.update {
            it.copy(
                currentTab = NavigationTab.DASHBOARD,
                selectedDicmTab = DicmPhaseTab.AIRSPACE_PTA
            )
        }
    }

    // -----------------------------------------------------------------------
    // Hilo Downtown Cam Monitor Controls
    // -----------------------------------------------------------------------

    fun openHiloCamMonitorTab() {
        _uiState.update {
            it.copy(
                currentTab = NavigationTab.DASHBOARD,
                selectedDicmTab = DicmPhaseTab.HILO_CAM_MONITOR
            )
        }
    }

    fun updateHiloCamStatusQuery(query: String) {
        _uiState.update { it.copy(hiloCamStatusQuery = query) }
    }

    fun checkHiloCamOfficialPageStatus() {
        _uiState.update { it.copy(hiloCamStatusState = HiloCamStatusState.Checking) }
        viewModelScope.launch {
            val result = hawaiiWebcamsRepository.checkHiloCamOfficialPageStatus()
            _uiState.update { it.copy(hiloCamStatusState = HiloCamStatusState.Checked(result)) }
        }
    }

    fun openHiloDowntownInSatFeed() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                selectedNasaEvent = null,
                hasCustomGibsTarget = true,
                activeGibsLayer = NasaGibsLayer.VIIRS_TRUE_COLOR,
                gibsSpanDegrees = 0.5,
                satVisualZoom = 1.0f,
                customGibsTarget = GibsTarget(
                    label = "Downtown Hilo: Kalakaua Park / 154 Waianuenue Ave",
                    latitude = HiloDowntownCamReference.CAMERA_LAT,
                    longitude = HiloDowntownCamReference.CAMERA_LON,
                    dateIso = state.airspaceSatDateIso
                ),
                gibsRefreshNonce = System.currentTimeMillis(),
                currentTab = NavigationTab.SAT_FEED
            )
        }
    }

    // -----------------------------------------------------------------------
    // Narrative Control & Social Connector Operations (Facebook & Telegram)
    // -----------------------------------------------------------------------

    private fun seedInitialNarrativeSignal() {
        viewModelScope.launch {
            val seedSignal = IncomingSocialSignal(
                platform = SocialPlatform.FACEBOOK,
                replyType = "comment",
                targetId = "10294857102_994812",
                senderId = "puna_chatter_01",
                senderName = "Puna Rant & Watch",
                text = "BLIND ITEM: Heard a convoy is blocking Highway 130 near Orchidland 16th Ave and shutting down access tonight—anyone know who is behind the road closed alert?"
            )
            val triagePkg = narrativeAiRepository.triageAndDraft(seedSignal)
            val record = NarrativeIncidentRecord(
                id = "seed_01",
                timestampIso = SocialDispatchRepository.formatUtcTimestamp(),
                signal = seedSignal,
                triagePackage = triagePkg
            )
            _uiState.update { state ->
                if (state.narrativeIncidents.any { it.id == record.id }) state
                else state.copy(narrativeIncidents = listOf(record) + state.narrativeIncidents)
            }
        }
    }

    fun openNarrativeOpsTab() {
        _uiState.update {
            it.copy(
                currentTab = NavigationTab.INTEL,
                selectedIntelSubMode = IntelSubMode.NARRATIVE_OPS
            )
        }
    }

    fun clearNarrativeError() {
        _uiState.update { it.copy(narrativeError = null) }
    }

    fun verifyFacebookWebhookChallenge(mode: String, token: String, challenge: String) {
        val res = FacebookConnectorParser.verifyWebhookHandshake(
            mode = mode.trim(),
            verifyToken = token.trim(),
            challenge = challenge.trim()
        )
        val statusText = if (res.statusCode == 200) {
            "HTTP 200 OK • Verified handshake; echoed challenge=\"${res.challengeResponse}\""
        } else {
            "HTTP 403 FORBIDDEN • Token mismatch or mode != 'subscribe' against Secrets FB_VERIFY_TOKEN."
        }
        _uiState.update { it.copy(webhookVerifyStatus = statusText) }
    }

    fun ingestSocialSignal(
        platform: SocialPlatform,
        senderName: String,
        text: String,
        replyType: String = if (platform == SocialPlatform.FACEBOOK) "comment" else "reply",
        targetId: String = if (platform == SocialPlatform.FACEBOOK) "POST_ORCHIDLAND_101" else "-100192837465"
    ) {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) {
            _uiState.update { it.copy(narrativeError = "Enter a message or blind item before ingesting.") }
            return
        }
        val cleanSender = senderName.trim().ifBlank {
            if (platform == SocialPlatform.FACEBOOK) "Local FB Monitor" else "Telegram Tip Channel"
        }
        _uiState.update { it.copy(isNarrativeAnalyzing = true, narrativeError = null) }
        viewModelScope.launch {
            val signal = IncomingSocialSignal(
                platform = platform,
                replyType = replyType,
                targetId = targetId,
                senderName = cleanSender,
                text = cleanText
            )
            val triagePkg = narrativeAiRepository.triageAndDraft(signal)
            val record = NarrativeIncidentRecord(
                id = SocialDispatchRepository.nextRecordId(),
                timestampIso = SocialDispatchRepository.formatUtcTimestamp(),
                signal = signal,
                triagePackage = triagePkg
            )
            _uiState.update {
                it.copy(
                    narrativeIncidents = listOf(record) + it.narrativeIncidents,
                    isNarrativeAnalyzing = false
                )
            }
        }
    }

    fun ingestRawWebhookJson(platform: SocialPlatform, rawJson: String) {
        val cleanJson = rawJson.trim()
        if (cleanJson.isEmpty()) {
            _uiState.update { it.copy(narrativeError = "Paste raw webhook JSON payload first.") }
            return
        }
        _uiState.update { it.copy(isNarrativeAnalyzing = true, narrativeError = null) }
        viewModelScope.launch {
            try {
                val signals = when (platform) {
                    SocialPlatform.FACEBOOK -> FacebookConnectorParser.parseFacebookWebhookPayload(cleanJson)
                    SocialPlatform.TELEGRAM -> listOfNotNull(TelegramConnectorParser.parseTelegramUpdatePayload(cleanJson))
                }
                if (signals.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isNarrativeAnalyzing = false,
                            narrativeError = "No actionable ${platform.label} text events extracted from webhook JSON."
                        )
                    }
                    return@launch
                }
                val newRecords = signals.map { sig ->
                    val pkg = narrativeAiRepository.triageAndDraft(sig)
                    NarrativeIncidentRecord(
                        id = SocialDispatchRepository.nextRecordId(),
                        timestampIso = SocialDispatchRepository.formatUtcTimestamp(),
                        signal = sig,
                        triagePackage = pkg
                    )
                }
                _uiState.update {
                    it.copy(
                        narrativeIncidents = newRecords + it.narrativeIncidents,
                        isNarrativeAnalyzing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isNarrativeAnalyzing = false,
                        narrativeError = "Webhook JSON parse error: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun approveAndDispatchNarrative(incidentId: String, editedReply: String) {
        val target = _uiState.value.narrativeIncidents.find { it.id == incidentId } ?: return
        val replyToSend = editedReply.trim().ifBlank { target.triagePackage.drafts.communityReply }

        _uiState.update { state ->
            state.copy(
                narrativeIncidents = state.narrativeIncidents.map {
                    if (it.id == incidentId) it.copy(status = IncidentQueueStatus.DISPATCHING) else it
                }
            )
        }

        viewModelScope.launch {
            val result = socialDispatchRepository.dispatchReply(target, replyToSend)
            _uiState.update { state ->
                state.copy(
                    narrativeIncidents = state.narrativeIncidents.map { rec ->
                        if (rec.id != incidentId) {
                            rec
                        } else {
                            result.fold(
                                onSuccess = { note ->
                                    rec.copy(
                                        status = IncidentQueueStatus.DISPATCHED,
                                        finalSentText = replyToSend,
                                        dispatchResultNote = note
                                    )
                                },
                                onFailure = { err ->
                                    rec.copy(
                                        status = IncidentQueueStatus.DISPATCH_ERROR,
                                        dispatchResultNote = err.localizedMessage ?: "Dispatch failed"
                                    )
                                }
                            )
                        }
                    }
                )
            }
        }
    }

    // -----------------------------------------------------------------------
    // Accessibility: Visual Select Audio & Larger Readable Text Controls
    // -----------------------------------------------------------------------

    fun setTextScalePreset(preset: TextScalePreset) {
        _uiState.update { it.copy(textScalePreset = preset) }
    }

    fun cycleTextScalePreset() {
        _uiState.update { it.copy(textScalePreset = it.textScalePreset.next()) }
    }

    fun toggleVisualAudioEnabled() {
        _uiState.update { it.copy(visualAudioEnabled = !it.visualAudioEnabled) }
    }

    fun setAccessibilitySheetOpen(open: Boolean) {
        _uiState.update { it.copy(isAccessibilitySheetOpen = open) }
    }

    // -----------------------------------------------------------------------
    // Mapping, Airspace, GPS/ADS-B, Tile Cache & Persistence Controls
    // -----------------------------------------------------------------------

    /**
     * Non-blocking startup hydration executed from LaunchedEffect after frame 1 renders.
     * Safely isolates any storage, preference, Coil, or system service error so the app never crashes on boot.
     */
    suspend fun hydrateRuntimeServicesAsync(context: android.content.Context): MapTileCacheManager? {
        val appContext = context.applicationContext ?: context
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val manager = tileCacheManager ?: MapTileCacheManager(appContext).also { tileCacheManager = it }
                val store = prefsStore ?: MapPreferencesStore(appContext).also { prefsStore = it }
                val saved = store.load()
                val tracker = locationTracker ?: DeviceLocationTracker(appContext).also { locationTracker = it }
                val displays = runCatching { tracker.countExternalDisplays() }.getOrDefault(0)
                val hasLocPerm = runCatching { tracker.hasLocationPermission() }.getOrDefault(false)
                val cacheKb = runCatching { manager.getCacheSizeKb() }.getOrDefault(0L)
                Triple(manager, saved, Triple(displays, hasLocPerm, cacheKb))
            }
        }
        return result.fold(
            onSuccess = { (manager, saved, telemetry) ->
                val (displays, hasLocPerm, cacheKb) = telemetry
                _uiState.update { state ->
                    val recalc = NavigationRouteEngine.recalculateRoute(
                        state.gpsTelemetry.latitude,
                        state.gpsTelemetry.longitude,
                        state.gpsTelemetry.speedKts.coerceAtLeast(110.0),
                        saved.waypoints
                    )
                    state.copy(
                        mapVisualThemeMode = saved.visualThemeMode,
                        mapSplitMode = saved.splitMode,
                        airspaceAltitudeBand = saved.altitudeBand,
                        dopplerOpacity = saved.dopplerOpacity,
                        airspaceSatOpacity = saved.satOpacity,
                        airspaceOpacity = saved.airspaceOpacity,
                        hdTileResolution = saved.hdTileResolution,
                        quickToolbarTools = saved.quickTools,
                        offlineSectorsCached = saved.offlineSectorIds,
                        userWaypoints = saved.waypoints,
                        routeSummary = recalc,
                        gpsPermissionGranted = hasLocPerm,
                        externalDisplayCount = displays,
                        tileCacheSizeKb = cacheKb,
                        startupInitCompleted = true,
                        mapPerfMetrics = state.mapPerfMetrics.copy(
                            tileResolutionLabel = if (saved.hdTileResolution) "1600×1200 HD" else "960×620 Std"
                        )
                    )
                }
                ensureStartupWarmup()
                manager
            },
            onFailure = { err ->
                _uiState.update {
                    it.copy(
                        startupInitCompleted = true,
                        startupDiagnosticNote = "Safe default config active (${err.localizedMessage ?: err.javaClass.simpleName})"
                    )
                }
                ensureStartupWarmup()
                null
            }
        )
    }

    fun attachRuntimeServices(context: android.content.Context): MapTileCacheManager? {
        val appContext = context.applicationContext ?: context
        val outcome = runCatching {
            val manager = tileCacheManager ?: MapTileCacheManager(appContext).also { tileCacheManager = it }
            if (prefsStore == null) {
                val store = MapPreferencesStore(appContext)
                prefsStore = store
                val saved = store.load()
                val tracker = DeviceLocationTracker(appContext)
                locationTracker = tracker
                val displays = runCatching { tracker.countExternalDisplays() }.getOrDefault(0)
                val hasLocPerm = runCatching { tracker.hasLocationPermission() }.getOrDefault(false)
                val cacheKb = runCatching { manager.getCacheSizeKb() }.getOrDefault(0L)
                _uiState.update { state ->
                    val recalc = NavigationRouteEngine.recalculateRoute(
                        state.gpsTelemetry.latitude,
                        state.gpsTelemetry.longitude,
                        state.gpsTelemetry.speedKts.coerceAtLeast(110.0),
                        saved.waypoints
                    )
                    state.copy(
                        mapVisualThemeMode = saved.visualThemeMode,
                        mapSplitMode = saved.splitMode,
                        airspaceAltitudeBand = saved.altitudeBand,
                        dopplerOpacity = saved.dopplerOpacity,
                        airspaceSatOpacity = saved.satOpacity,
                        airspaceOpacity = saved.airspaceOpacity,
                        hdTileResolution = saved.hdTileResolution,
                        quickToolbarTools = saved.quickTools,
                        offlineSectorsCached = saved.offlineSectorIds,
                        userWaypoints = saved.waypoints,
                        routeSummary = recalc,
                        gpsPermissionGranted = hasLocPerm,
                        externalDisplayCount = displays,
                        tileCacheSizeKb = cacheKb,
                        startupInitCompleted = true,
                        mapPerfMetrics = state.mapPerfMetrics.copy(
                            tileResolutionLabel = if (saved.hdTileResolution) "1600×1200 HD" else "960×620 Std"
                        )
                    )
                }
            }
            manager
        }
        return outcome.getOrElse { err ->
            _uiState.update {
                it.copy(
                    startupInitCompleted = true,
                    startupDiagnosticNote = "Runtime service fallback: ${err.localizedMessage ?: err.javaClass.simpleName}"
                )
            }
            null
        }
    }

    fun openFullScreenMap(domain: FullScreenMapDomain) {
        _uiState.update { state ->
            val syncedAirspaceZoom = if (domain == FullScreenMapDomain.SATELLITE &&
                state.mapSplitMode != MapSplitMode.SINGLE
            ) {
                state.satVisualZoom.coerceIn(1.0f, 8.0f)
            } else {
                state.airspaceMapZoom
            }
            state.copy(
                isFullScreenMapActive = true,
                fullScreenMapDomain = domain,
                isInAppMiniPipActive = false,
                airspaceMapZoom = syncedAirspaceZoom
            )
        }
    }

    fun closeFullScreenMap() {
        _uiState.update { it.copy(isFullScreenMapActive = false) }
    }

    fun setSystemPipActive(inPip: Boolean) {
        _uiState.update { it.copy(isInSystemPip = inPip) }
    }

    fun toggleInAppMiniPip(active: Boolean = !_uiState.value.isInAppMiniPipActive) {
        _uiState.update {
            it.copy(
                isInAppMiniPipActive = active,
                isFullScreenMapActive = if (active) false else it.isFullScreenMapActive
            )
        }
    }

    fun setMapSplitMode(mode: MapSplitMode) {
        prefsStore?.saveSplitMode(mode)
        _uiState.update { state ->
            val syncedZoom = if (state.fullScreenMapDomain == FullScreenMapDomain.SATELLITE &&
                state.mapSplitMode == MapSplitMode.SINGLE &&
                mode != MapSplitMode.SINGLE
            ) {
                state.satVisualZoom.coerceIn(1.0f, 8.0f)
            } else {
                state.airspaceMapZoom
            }
            state.copy(mapSplitMode = mode, airspaceMapZoom = syncedZoom)
        }
    }

    fun cycleMapSplitMode() {
        val all = MapSplitMode.entries
        val next = all[(all.indexOf(_uiState.value.mapSplitMode) + 1) % all.size]
        setMapSplitMode(next)
    }

    fun setMapVisualThemeMode(mode: MapVisualThemeMode) {
        prefsStore?.saveVisualMode(mode)
        _uiState.update { it.copy(mapVisualThemeMode = mode) }
    }

    fun cycleMapVisualThemeMode() {
        val all = MapVisualThemeMode.entries
        val next = all[(all.indexOf(_uiState.value.mapVisualThemeMode) + 1) % all.size]
        setMapVisualThemeMode(next)
    }

    fun updateMapTransform(
        zoom: Float? = null,
        rotationDeg: Float? = null,
        tiltDeg: Float? = null,
        gestureLatencyMs: Long? = null
    ) {
        _uiState.update { state ->
            val nextZoom = zoom?.coerceIn(1.0f, 8.0f) ?: state.airspaceMapZoom
            val nextRot = rotationDeg?.let { ((it % 360f) + 360f) % 360f } ?: state.mapRotationDeg
            val nextTilt = tiltDeg?.coerceIn(0f, 55f) ?: state.mapTiltDeg
            state.copy(
                airspaceMapZoom = nextZoom,
                mapRotationDeg = nextRot,
                mapTiltDeg = nextTilt,
                mapPerfMetrics = if (gestureLatencyMs != null) {
                    state.mapPerfMetrics.copy(lastGestureLatencyMs = gestureLatencyMs.coerceIn(1L, 95L))
                } else {
                    state.mapPerfMetrics
                }
            )
        }
    }

    fun resetMapToNorthUp() {
        _uiState.update {
            it.copy(
                mapRotationDeg = 0f,
                mapTiltDeg = 0f,
                mapPerfMetrics = it.mapPerfMetrics.copy(lastGestureLatencyMs = 8L)
            )
        }
    }

    fun toggleTerrainLayer() {
        _uiState.update { it.copy(terrainLayerEnabled = !it.terrainLayerEnabled) }
    }

    fun setTerrainOpacity(opacity: Float) {
        _uiState.update { it.copy(terrainOpacity = opacity.coerceIn(0.2f, 1.0f)) }
    }

    fun setAirspaceOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.2f, 1.0f)
        _uiState.update {
            prefsStore?.saveOpacities(it.airspaceSatOpacity, it.dopplerOpacity, clamped)
            it.copy(airspaceOpacity = clamped)
        }
    }

    fun setAirspaceAltitudeBand(band: AirspaceAltitudeBand) {
        prefsStore?.saveAltitudeBand(band)
        _uiState.update { it.copy(airspaceAltitudeBand = band) }
    }

    fun toggleAirspaceClassification(cls: AirspaceClassification) {
        _uiState.update { state ->
            val updated = state.enabledAirspaceClasses.toMutableSet()
            if (updated.contains(cls)) updated.remove(cls) else updated.add(cls)
            state.copy(enabledAirspaceClasses = updated)
        }
    }

    fun selectAirspaceZone(zone: AirspaceClassificationZone?) {
        _uiState.update { it.copy(selectedAirspaceZone = zone) }
    }

    fun applyMapLayerPreset(preset: MapLayerPreset) {
        val start = System.currentTimeMillis()
        prefsStore?.saveAltitudeBand(preset.altitudeBand)
        _uiState.update { state ->
            val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
            state.copy(
                activeLayerPreset = preset,
                airspaceSatEnabled = preset.satEnabled,
                airspaceSatLayer = preset.satLayer,
                dopplerEnabled = preset.dopplerEnabled,
                activeDopplerLayer = preset.dopplerLayer,
                airspaceAltitudeBand = preset.altitudeBand,
                mapPerfMetrics = state.mapPerfMetrics.copy(lastLayerSwitchMs = elapsed)
            )
        }
    }

    fun toggleQuickToolbarConfig(open: Boolean = !_uiState.value.showQuickToolbarConfig) {
        _uiState.update { it.copy(showQuickToolbarConfig = open) }
    }

    fun toggleQuickToolbarTool(tool: QuickToolbarTool) {
        _uiState.update { state ->
            val updated = state.quickToolbarTools.toMutableList()
            if (updated.contains(tool)) {
                if (updated.size > 2) updated.remove(tool)
            } else {
                updated.add(tool)
            }
            prefsStore?.saveQuickTools(updated)
            state.copy(quickToolbarTools = updated)
        }
    }

    fun toggleMapLegendSheet(open: Boolean = !_uiState.value.showMapLegendSheet) {
        _uiState.update { it.copy(showMapLegendSheet = open) }
    }

    fun toggleHdTileResolution() {
        _uiState.update { state ->
            val next = !state.hdTileResolution
            prefsStore?.saveHdResolution(next)
            state.copy(
                hdTileResolution = next,
                mapPerfMetrics = state.mapPerfMetrics.copy(
                    tileResolutionLabel = if (next) "1600×1200 HD" else "960×620 Std"
                )
            )
        }
    }

    fun addUserWaypoint(label: String, lat: Double, lon: Double, altitudeFt: Int = 1500) {
        _uiState.update { state ->
            val wp = UserWaypoint(
                id = "wp_${System.currentTimeMillis()}_${state.userWaypoints.size}",
                label = label.ifBlank { "WP-${state.userWaypoints.size + 1}" }.uppercase(Locale.US),
                lat = lat.coerceIn(-89.5, 89.5),
                lon = lon.coerceIn(-179.5, 179.5),
                altitudeFt = altitudeFt.coerceIn(0, 60000)
            )
            val updated = state.userWaypoints + wp
            prefsStore?.saveWaypoints(updated)
            val route = NavigationRouteEngine.recalculateRoute(
                currentLat = state.gpsTelemetry.latitude,
                currentLon = state.gpsTelemetry.longitude,
                groundSpeedKts = state.gpsTelemetry.speedKts.coerceAtLeast(110.0),
                waypoints = updated
            )
            state.copy(userWaypoints = updated, routeSummary = route)
        }
    }

    fun removeUserWaypoint(waypointId: String) {
        _uiState.update { state ->
            val updated = state.userWaypoints.filterNot { it.id == waypointId }
            prefsStore?.saveWaypoints(updated)
            val route = NavigationRouteEngine.recalculateRoute(
                currentLat = state.gpsTelemetry.latitude,
                currentLon = state.gpsTelemetry.longitude,
                groundSpeedKts = state.gpsTelemetry.speedKts.coerceAtLeast(110.0),
                waypoints = updated
            )
            state.copy(userWaypoints = updated, routeSummary = route)
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                gpsPermissionGranted = granted,
                gpsStatusNote = if (granted) null else "Location permission denied — grant permission for live GPS tracking."
            )
        }
        if (granted) {
            startLiveGpsTracking()
        }
    }

    fun startLiveGpsTracking() {
        val tracker = locationTracker ?: return
        val outcome = tracker.startTrackingWithDiagnostics { telemetry ->
            _uiState.update { state ->
                val recalc = NavigationRouteEngine.recalculateRoute(
                    currentLat = telemetry.latitude,
                    currentLon = telemetry.longitude,
                    groundSpeedKts = telemetry.speedKts.coerceAtLeast(95.0),
                    waypoints = state.userWaypoints
                )
                state.copy(
                    isGpsLiveEnabled = true,
                    gpsTelemetry = telemetry,
                    gpsStatusNote = "Live fix (${telemetry.providerName}) • ±${telemetry.accuracyMeters.toInt()}m",
                    routeSummary = recalc,
                    mapPerfMetrics = state.mapPerfMetrics.copy(
                        gpsRefreshIntervalMs = telemetry.fixIntervalMs.coerceAtLeast(1L)
                    )
                )
            }
            val nowMs = System.currentTimeMillis()
            if (telemetry.speedKts > 5.0 && nowMs - lastAutoPrefetchMs >= 15_000L) {
                lastAutoPrefetchMs = nowMs
                prefetchPredictiveMapTiles()
            }
        }
        _uiState.update {
            it.copy(
                isGpsLiveEnabled = outcome.started,
                gpsPermissionGranted = tracker.hasLocationPermission(),
                gpsStatusNote = outcome.diagnosticNote
            )
        }
    }

    fun stopLiveGpsTracking() {
        locationTracker?.stopTracking()
        _uiState.update {
            it.copy(
                isGpsLiveEnabled = false,
                gpsTelemetry = it.gpsTelemetry.copy(isLive = false),
                gpsStatusNote = "Live GPS paused — using last fix / Orchidland reference."
            )
        }
    }

    fun toggleAdsbTraffic() {
        val next = !_uiState.value.adsbEnabled
        _uiState.update { it.copy(adsbEnabled = next) }
        if (next) refreshAdsbTraffic()
    }

    fun refreshAdsbTraffic() {
        _uiState.update { it.copy(adsbEnabled = true, adsbStatus = AdsbFeedStatus.Loading) }
        viewModelScope.launch {
            val result = openSkyAdsbRepository.fetchHawaiiAircraft()
            _uiState.update { state ->
                result.fold(
                    onSuccess = { tracks ->
                        state.copy(
                            adsbAircraft = tracks,
                            adsbStatus = AdsbFeedStatus.Live(
                                updatedAtHst = HawaiiAirspaceReference.formatCurrentHst(),
                                count = tracks.size
                            )
                        )
                    },
                    onFailure = { err ->
                        state.copy(
                            adsbStatus = AdsbFeedStatus.Unavailable(
                                err.localizedMessage ?: "OpenSky ADS-B feed rate-limited or offline"
                            )
                        )
                    }
                )
            }
        }
    }

    fun prefetchPredictiveMapTiles() {
        val manager = tileCacheManager ?: return
        if (_uiState.value.isPrefetchingTiles) return
        val snapshot = _uiState.value
        _uiState.update { it.copy(isPrefetchingTiles = true) }
        viewModelScope.launch {
            val count = manager.prefetchPredictiveTiles(
                currentLat = snapshot.gpsTelemetry.latitude,
                currentLon = snapshot.gpsTelemetry.longitude,
                headingDeg = snapshot.gpsTelemetry.headingDeg,
                speedKts = snapshot.gpsTelemetry.speedKts.coerceAtLeast(90.0),
                satLayer = snapshot.airspaceSatLayer,
                dopplerLayer = snapshot.activeDopplerLayer,
                dateIso = snapshot.airspaceSatDateIso,
                highRes = snapshot.hdTileResolution
            )
            _uiState.update {
                it.copy(
                    isPrefetchingTiles = false,
                    tileCacheSizeKb = manager.getCacheSizeKb(),
                    mapPerfMetrics = it.mapPerfMetrics.copy(
                        cacheHitNote = "Prefetched $count predictive tiles (${manager.getCacheSizeKb()} KB)"
                    )
                )
            }
        }
    }

    fun cacheOfflineSector(sector: OfflineSectorSpec) {
        val manager = tileCacheManager ?: return
        val snapshot = _uiState.value
        _uiState.update { it.copy(isPrefetchingTiles = true) }
        viewModelScope.launch {
            val stored = manager.cacheOfflineSector(
                sector = sector,
                satLayer = snapshot.airspaceSatLayer,
                dopplerLayer = snapshot.activeDopplerLayer,
                dateIso = snapshot.airspaceSatDateIso,
                highRes = snapshot.hdTileResolution
            )
            _uiState.update { state ->
                val updated = if (stored > 0) state.offlineSectorsCached + sector.id else state.offlineSectorsCached
                prefsStore?.saveOfflineSectors(updated)
                state.copy(
                    isPrefetchingTiles = false,
                    offlineSectorsCached = updated,
                    tileCacheSizeKb = manager.getCacheSizeKb(),
                    mapPerfMetrics = state.mapPerfMetrics.copy(
                        cacheHitNote = if (stored > 0) {
                            "${sector.title} cached offline ($stored layers • ${manager.getCacheSizeKb()} KB)"
                        } else {
                            "Offline sync deferred (check connection)"
                        }
                    )
                )
            }
        }
    }

    fun toggleRegisteredExternalLayer(layerId: String) {
        _uiState.update { state ->
            state.copy(
                registeredExternalLayers = state.registeredExternalLayers.map {
                    if (it.id == layerId) it.copy(enabled = !it.enabled) else it
                }
            )
        }
    }

    // -----------------------------------------------------------------------
    // Satellite Fixed-Aspect Zoom Calibration Controls
    // -----------------------------------------------------------------------

    fun setSatAspectCalibrationMode(mode: SatAspectCalibrationMode) {
        _uiState.update {
            it.copy(
                satAspectCalibrationMode = mode,
                satZoomCalibrationFactor = 1.0f
            )
        }
    }

    fun setSatZoomCalibrationFactor(factor: Float) {
        _uiState.update { it.copy(satZoomCalibrationFactor = factor.coerceIn(0.75f, 2.50f)) }
    }

    fun resetSatCalibration() {
        _uiState.update {
            it.copy(
                satAspectCalibrationMode = SatAspectCalibrationMode.AUTO_AESTHETIC,
                satZoomCalibrationFactor = 1.0f,
                satVisualZoom = 1.0f
            )
        }
    }

    // -----------------------------------------------------------------------
    // VeriScope BTAM Gateway, 5-Stage NTAC Workflow & 911 Pocket-Dial Guard
    // -----------------------------------------------------------------------

    fun selectBtamRoleOption(role: BtamOperationalRole) {
        _uiState.update { state ->
            val autoPathway = when (role) {
                BtamOperationalRole.BTAM_INVESTIGATOR,
                BtamOperationalRole.TEAM_LEAD -> BtamAffiliationPathway.AFFILIATED
                BtamOperationalRole.PUBLIC_BYSTANDER ->
                    if (state.btamAffiliationOrgName.trim().isEmpty()) {
                        BtamAffiliationPathway.UNAFFILIATED_PUBLIC
                    } else {
                        state.btamAffiliationPathway
                    }
                BtamOperationalRole.COMPLIANCE_AUDITOR -> state.btamAffiliationPathway
            }
            state.copy(
                selectedBtamRole = role,
                btamAffiliationPathway = autoPathway,
                btamGatewayError = null
            )
        }
    }

    fun setBtamAffiliationPathway(pathway: BtamAffiliationPathway) {
        _uiState.update { it.copy(btamAffiliationPathway = pathway, btamGatewayError = null) }
    }

    fun updateBtamAffiliationOrgName(orgName: String) {
        _uiState.update { it.copy(btamAffiliationOrgName = orgName, btamGatewayError = null) }
    }

    fun setBtamUserAgreementAccepted(accepted: Boolean) {
        _uiState.update { it.copy(btamUserAgreementAccepted = accepted, btamGatewayError = null) }
    }

    fun setBtamPolicyDialogOpen(open: Boolean) {
        _uiState.update { it.copy(btamPolicyDialogOpen = open) }
    }

    fun acceptPolicyFromDialog() {
        _uiState.update {
            it.copy(
                btamUserAgreementAccepted = true,
                btamPolicyDialogOpen = false,
                btamGatewayError = null
            )
        }
    }

    fun setBtamApiDirectoryOpen(open: Boolean) {
        _uiState.update { it.copy(btamApiDirectoryOpen = open) }
    }

    fun unlockBtamSession() {
        val current = _uiState.value
        val role = current.selectedBtamRole
        if (role == null) {
            _uiState.update { it.copy(btamGatewayError = "Step 1 Required: Select an operational role before unlocking VeriScope tools.") }
            return
        }
        if (current.btamAffiliationPathway == BtamAffiliationPathway.AFFILIATED &&
            current.btamAffiliationOrgName.trim().length < 2
        ) {
            _uiState.update {
                it.copy(btamGatewayError = "Step 2 Required: Enter your participating organization, workplace, school, or venue name (or choose Unaffiliated Public).")
            }
            return
        }
        if (!current.btamUserAgreementAccepted) {
            _uiState.update {
                it.copy(btamGatewayError = "Step 3 Required: Accept the Non-Affiliation, Non-Endorsement & Use-At-Own-Risk User Agreement to continue.")
            }
            return
        }
        _uiState.update {
            it.copy(
                btamSessionUnlocked = true,
                btamGatewayError = null,
                btamWorkspaceModalOpen = false,
                currentTab = NavigationTab.DASHBOARD
            )
        }
        ensureStartupWarmup()
    }

    fun lockBtamSessionToGateway() {
        _uiState.update {
            it.copy(
                btamSessionUnlocked = false,
                btamWorkspaceModalOpen = false,
                isFullScreenMapActive = false,
                bystanderPortalOpen = false
            )
        }
    }

    fun openBystanderPortalOption(option: BystanderSideMenuOption? = null) {
        if (option != null) {
            bystanderReferralCoordinator.selectOption(option)
        }
        _uiState.update { it.copy(bystanderPortalOpen = true) }
    }

    fun setBystanderPortalOpen(open: Boolean) {
        _uiState.update { it.copy(bystanderPortalOpen = open) }
    }

    fun setBtamWorkspaceModalOpen(open: Boolean) {
        _uiState.update { it.copy(btamWorkspaceModalOpen = open) }
    }

    fun setBtamBystanderStage(stage: BtamBystanderStage) {
        _uiState.update { it.copy(btamBystanderStage = stage) }
    }

    fun setBtamLifeSafetyImminentThreat(isImminent: Boolean) {
        _uiState.update {
            it.copy(
                btamLifeSafetyImminentThreat = isImminent,
                btamBystanderStage = if (isImminent) BtamBystanderStage.STAGE_1_LIFE_SAFETY else BtamBystanderStage.STAGE_2_AFFILIATION
            )
        }
    }

    fun toggleNtacIndicator(indicator: NtacIndicatorCategory) {
        _uiState.update { state ->
            val updated = state.btamSelectedIndicators.toMutableSet()
            if (updated.contains(indicator)) updated.remove(indicator) else updated.add(indicator)
            state.copy(btamSelectedIndicators = updated, btamAntiProfilingAlert = null)
        }
    }

    fun updateBtamIntakeDrafts(
        leakage: String? = null,
        grievance: String? = null,
        probing: String? = null,
        contextArtifacts: String? = null,
        obsDateTimeLoc: String? = null,
        witnessNote: String? = null
    ) {
        _uiState.update { state ->
            val nextLeak = leakage ?: state.btamLeakageDraft
            val nextGriev = grievance ?: state.btamGrievanceDraft
            val nextProbe = probing ?: state.btamProbingDraft
            val nextCtx = contextArtifacts ?: state.btamContextArtifactsDraft
            val (passed, note) = BtamAntiProfilingGuard.evaluateComplianceNote(
                listOf(nextLeak, nextGriev, nextProbe, nextCtx)
            )
            state.copy(
                btamLeakageDraft = nextLeak,
                btamGrievanceDraft = nextGriev,
                btamProbingDraft = nextProbe,
                btamContextArtifactsDraft = nextCtx,
                btamObsDateTimeLocDraft = obsDateTimeLoc ?: state.btamObsDateTimeLocDraft,
                btamWitnessNoteDraft = witnessNote ?: state.btamWitnessNoteDraft,
                btamAntiProfilingAlert = if (passed) null else note
            )
        }
    }

    fun computePlannedBtamUrgencyTier(state: ForensicUiState = _uiState.value): BtamUrgencyTier {
        if (state.btamLifeSafetyImminentThreat == true) return BtamUrgencyTier.EMERGENCY
        val hasHighRiskIndicator = state.btamSelectedIndicators.contains(NtacIndicatorCategory.PROBES_PREATTACK) ||
            (state.btamSelectedIndicators.contains(NtacIndicatorCategory.LEAKAGE_COMMUNICATIONS) &&
                state.btamSelectedIndicators.contains(NtacIndicatorCategory.GRIEVANCE_FIXATION))
        if (hasHighRiskIndicator || state.btamSelectedIndicators.size >= 3) return BtamUrgencyTier.PRIORITY
        return BtamUrgencyTier.STANDARD
    }

    fun submitBtamBystanderReport() {
        val state = _uiState.value
        val (antiProfilingOk, guardMsg) = BtamAntiProfilingGuard.evaluateComplianceNote(
            listOf(
                state.btamLeakageDraft,
                state.btamGrievanceDraft,
                state.btamProbingDraft,
                state.btamContextArtifactsDraft
            )
        )
        if (!antiProfilingOk) {
            _uiState.update { it.copy(btamAntiProfilingAlert = guardMsg) }
            return
        }

        val isUnaffiliated = state.btamAffiliationPathway == BtamAffiliationPathway.UNAFFILIATED_PUBLIC
        val scrubLeak = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamLeakageDraft) else BtamPiiScrubber.ScrubResult(state.btamLeakageDraft.trim(), 0, emptyList())
        val scrubGriev = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamGrievanceDraft) else BtamPiiScrubber.ScrubResult(state.btamGrievanceDraft.trim(), 0, emptyList())
        val scrubProbe = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamProbingDraft) else BtamPiiScrubber.ScrubResult(state.btamProbingDraft.trim(), 0, emptyList())
        val scrubCtx = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamContextArtifactsDraft) else BtamPiiScrubber.ScrubResult(state.btamContextArtifactsDraft.trim(), 0, emptyList())
        val scrubObs = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamObsDateTimeLocDraft) else BtamPiiScrubber.ScrubResult(state.btamObsDateTimeLocDraft.trim(), 0, emptyList())
        val scrubWitness = if (isUnaffiliated) BtamPiiScrubber.scrub(state.btamWitnessNoteDraft) else BtamPiiScrubber.ScrubResult(state.btamWitnessNoteDraft.trim(), 0, emptyList())

        val totalRedactions = scrubLeak.redactionCount + scrubGriev.redactionCount +
            scrubProbe.redactionCount + scrubCtx.redactionCount + scrubObs.redactionCount + scrubWitness.redactionCount
        val redactedCats = (scrubLeak.redactedCategories + scrubGriev.redactedCategories +
            scrubProbe.redactedCategories + scrubCtx.redactedCategories + scrubObs.redactedCategories + scrubWitness.redactedCategories).distinct()

        val tier = computePlannedBtamUrgencyTier(state)
        val token = BtamTokenGenerator.generateCaseToken()
        val nowUtc = BtamTokenGenerator.formatUtcNow()
        val orgLabel = if (isUnaffiliated) {
            "Unaffiliated Public Volunteer Prevention Pool"
        } else {
            state.btamAffiliationOrgName.ifBlank { "Participating Organization" }
        }
        val statusLabel = when {
            tier == BtamUrgencyTier.EMERGENCY -> "Tier 1 Emergency Expedited — 911 + Responder Alert"
            isUnaffiliated -> "Archived in PII-Scrubbed Volunteer Prevention Pool"
            else -> "Queued for Authorized BTAM Investigator Review"
        }
        val submission = BtamCaseSubmission(
            token = token,
            createdAtUtc = nowUtc,
            urgencyTier = tier,
            isLifeSafetyEmergency = state.btamLifeSafetyImminentThreat == true,
            pathway = state.btamAffiliationPathway,
            organizationName = orgLabel,
            indicators = state.btamSelectedIndicators.ifEmpty { setOf(NtacIndicatorCategory.ACUTE_STRESSORS) },
            leakageSummary = scrubLeak.cleanedText.ifBlank { "No leakage statements reported." },
            grievanceSummary = scrubGriev.cleanedText.ifBlank { "No fixation/grievance narrative provided." },
            probingSummary = scrubProbe.cleanedText.ifBlank { "No probing/weapon acquisition reported." },
            contextAndArtifacts = scrubCtx.cleanedText.ifBlank { "Observable context logged for baseline review." },
            observationDateTimeLocation = scrubObs.cleanedText.ifBlank { "Time/location not specified" },
            witnessCountNote = scrubWitness.cleanedText.ifBlank { "No additional witness notes" },
            statusLabel = statusLabel,
            piiScrubApplied = isUnaffiliated,
            piiRedactionCount = totalRedactions,
            distressSupportFlagged = state.btamSelectedIndicators.contains(NtacIndicatorCategory.ACUTE_STRESSORS)
        )
        val auditEntry = BtamPiiAuditLogEntry(
            id = "audit_${System.currentTimeMillis()}",
            token = token,
            timestampUtc = nowUtc,
            pathway = state.btamAffiliationPathway,
            redactedCategories = if (isUnaffiliated && redactedCats.isEmpty()) listOf("Device Metadata Stripped") else redactedCats,
            redactionCount = totalRedactions,
            antiProfilingPassed = true
        )

        _uiState.update {
            it.copy(
                btamLatestIssuedToken = token,
                btamTokenLookupQuery = token,
                btamTokenLookupResult = submission,
                btamTokenLookupMessage = "Anonymous Case Token issued. Save $token to check status or reply asynchronously.",
                btamSubmissions = listOf(submission) + it.btamSubmissions,
                btamPiiAuditLogs = listOf(auditEntry) + it.btamPiiAuditLogs,
                btamBystanderStage = BtamBystanderStage.STAGE_5_CASE_TOKEN,
                btamSelectedQueueToken = token
            )
        }
    }

    fun updateBtamTokenLookupQuery(query: String) {
        _uiState.update { it.copy(btamTokenLookupQuery = query, btamTokenLookupMessage = null) }
    }

    fun lookupBtamCaseByToken(tokenInput: String = _uiState.value.btamTokenLookupQuery) {
        val normalized = tokenInput.trim().uppercase(Locale.US)
        val match = _uiState.value.btamSubmissions.firstOrNull { it.token.equals(normalized, ignoreCase = true) }
        _uiState.update {
            if (match != null) {
                it.copy(
                    btamTokenLookupQuery = normalized,
                    btamTokenLookupResult = match,
                    btamTokenLookupMessage = "Token verified — anonymous two-way channel active."
                )
            } else {
                it.copy(
                    btamTokenLookupQuery = normalized,
                    btamTokenLookupResult = null,
                    btamTokenLookupMessage = "No active case found for token '$normalized'."
                )
            }
        }
    }

    fun updateBtamBystanderFollowUpDraft(text: String) {
        _uiState.update { it.copy(btamBystanderFollowUpDraft = text) }
    }

    fun sendAnonymousBystanderFollowUp() {
        val state = _uiState.value
        val target = state.btamTokenLookupResult ?: return
        val raw = state.btamBystanderFollowUpDraft.trim()
        if (raw.isEmpty()) return
        val cleaned = if (target.pathway == BtamAffiliationPathway.UNAFFILIATED_PUBLIC) {
            BtamPiiScrubber.scrub(raw).cleanedText
        } else raw
        val msg = BtamFollowUpMessage(
            id = "fu_${System.currentTimeMillis()}",
            senderRole = "Anonymous Bystander (${target.token})",
            timestampUtc = BtamTokenGenerator.formatUtcNow(),
            body = cleaned
        )
        _uiState.update { current ->
            val updatedList = current.btamSubmissions.map { sub ->
                if (sub.token == target.token) sub.copy(followUpMessages = sub.followUpMessages + msg) else sub
            }
            val updatedMatch = updatedList.firstOrNull { it.token == target.token }
            current.copy(
                btamSubmissions = updatedList,
                btamTokenLookupResult = updatedMatch,
                btamBystanderFollowUpDraft = ""
            )
        }
    }

    fun selectBtamQueueCase(token: String) {
        _uiState.update { it.copy(btamSelectedQueueToken = token) }
    }

    fun updateBtamInvestigatorQuestionDraft(text: String) {
        _uiState.update { it.copy(btamInvestigatorQuestionDraft = text) }
    }

    fun postInvestigatorFollowUpQuestion(token: String) {
        val state = _uiState.value
        val q = state.btamInvestigatorQuestionDraft.trim()
        if (q.isEmpty()) return
        val roleTitle = state.selectedBtamRole?.title ?: "BTAM Investigator"
        val msg = BtamFollowUpMessage(
            id = "iq_${System.currentTimeMillis()}",
            senderRole = roleTitle,
            timestampUtc = BtamTokenGenerator.formatUtcNow(),
            body = q
        )
        _uiState.update { current ->
            val updatedList = current.btamSubmissions.map { sub ->
                if (sub.token == token) {
                    sub.copy(
                        statusLabel = "Investigator Follow-Up Requested",
                        followUpMessages = sub.followUpMessages + msg
                    )
                } else sub
            }
            val refreshedLookup = if (current.btamTokenLookupResult?.token == token) {
                updatedList.firstOrNull { it.token == token }
            } else current.btamTokenLookupResult
            current.copy(
                btamSubmissions = updatedList,
                btamTokenLookupResult = refreshedLookup,
                btamInvestigatorQuestionDraft = ""
            )
        }
    }

    fun toggleBtamCaseDiscipline(token: String, discipline: String) {
        _uiState.update { current ->
            val updatedList = current.btamSubmissions.map { sub ->
                if (sub.token == token) {
                    val nextSet = sub.assignedDisciplines.toMutableSet()
                    if (nextSet.contains(discipline)) nextSet.remove(discipline) else nextSet.add(discipline)
                    sub.copy(assignedDisciplines = nextSet)
                } else sub
            }
            current.copy(btamSubmissions = updatedList)
        }
    }

    fun recordHumanAuthorityDecision(token: String, decisionStatus: String, authorityNote: String) {
        _uiState.update { current ->
            val updatedList = current.btamSubmissions.map { sub ->
                if (sub.token == token) {
                    sub.copy(
                        statusLabel = decisionStatus,
                        humanAuthorityNote = authorityNote
                    )
                } else sub
            }
            val refreshedLookup = if (current.btamTokenLookupResult?.token == token) {
                updatedList.firstOrNull { it.token == token }
            } else current.btamTokenLookupResult
            current.copy(btamSubmissions = updatedList, btamTokenLookupResult = refreshedLookup)
        }
    }

    fun request911CallConfirmation() {
        _uiState.update { it.copy(btamShow911ConfirmDialog = true, btam911StatusNote = null) }
    }

    fun dismiss911CallConfirmation() {
        _uiState.update {
            it.copy(
                btamShow911ConfirmDialog = false,
                btam911StatusNote = "911 dial cancelled — accidental tap / pocket-call guard active."
            )
        }
    }

    fun confirmAndLaunch911Dialer(context: android.content.Context) {
        val res = EmergencyDialerBridge.openDefaultDialerFor911(context)
        _uiState.update {
            it.copy(
                btamShow911ConfirmDialog = false,
                btam911StatusNote = res.fold(
                    onSuccess = { msg -> msg },
                    onFailure = { err -> "Unable to open phone dialer (${err.localizedMessage ?: "no dialer available"}). Dial 911 manually if in immediate danger." }
                )
            )
        }
    }

    override fun onCleared() {
        locationTracker?.stopTracking()
        super.onCleared()
    }
}
