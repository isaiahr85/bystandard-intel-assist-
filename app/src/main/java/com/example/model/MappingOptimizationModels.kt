package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.network.GeoPoint
import com.example.network.HawaiiAirspaceReference
import com.example.network.HawaiiDopplerLayer
import com.example.network.NasaGibsLayer
import java.util.Locale
import kotlin.math.*

// ---------------------------------------------------------------------------
// 1. Visual Accessibility & Human Factors Display Modes (WCAG Compliant)
// ---------------------------------------------------------------------------

enum class MapVisualThemeMode(
    val id: String,
    val label: String,
    val shortLabel: String,
    val description: String
) {
    NIGHT(
        id = "night",
        label = "Night Cockpit",
        shortLabel = "Night",
        description = "Low-glare obsidian cockpit theme preserving dark adaptation."
    ),
    DAY(
        id = "day",
        label = "Day Chart",
        shortLabel = "Day",
        description = "High-luminance daylight sectional palette for bright sun visibility."
    ),
    HIGH_CONTRAST(
        id = "high_contrast",
        label = "High Contrast AAA",
        shortLabel = "Hi-Contrast",
        description = "WCAG AAA >= 7:1 contrast with bold outlines for direct sunlight."
    ),
    COLOR_BLIND(
        id = "color_blind",
        label = "Color-Blind Safe",
        shortLabel = "Okabe-Ito",
        description = "CVD-safe Okabe-Ito hues paired with distinct dash patterns & glyphs."
    )
}

@Immutable
data class MapColorPalette(
    val mode: MapVisualThemeMode,
    val canvasBackground: Color,
    val panelSurface: Color,
    val panelBorder: Color,
    val landFill: Color,
    val landOutline: Color,
    val terrainContour: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    // Semantic Alert Hierarchy (Critical=Red, Warning=Amber, Advisory=Blue, Normal=Green)
    val criticalRed: Color,
    val warningAmber: Color,
    val advisoryBlue: Color,
    val normalGreen: Color,
    // Airspace Classification Colors
    val classAColor: Color,
    val classBColor: Color,
    val classCColor: Color,
    val classDColor: Color,
    val restrictedColor: Color,
    val tfrColor: Color,
    val routeLineColor: Color,
    val adsbTargetColor: Color,
    val strokeMultiplier: Float
)

fun paletteForMode(mode: MapVisualThemeMode): MapColorPalette = when (mode) {
    MapVisualThemeMode.NIGHT -> MapColorPalette(
        mode = mode,
        canvasBackground = Color(0xFF04070E),
        panelSurface = Color(0xFF0B1221),
        panelBorder = Color(0xFF33496B),
        landFill = Color(0xFF132038),
        landOutline = Color(0xFF1CE8FF),
        terrainContour = Color(0xFF33496B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFFD5E1F0),
        criticalRed = Color(0xFFFF5263),
        warningAmber = Color(0xFFFFC22B),
        advisoryBlue = Color(0xFF60A5FA),
        normalGreen = Color(0xFF22E39B),
        classAColor = Color(0xFFA78BFA),
        classBColor = Color(0xFF38BDF8),
        classCColor = Color(0xFFE879F9),
        classDColor = Color(0xFF60A5FA),
        restrictedColor = Color(0xFFFFC22B),
        tfrColor = Color(0xFFFF5263),
        routeLineColor = Color(0xFF22E39B),
        adsbTargetColor = Color(0xFF1CE8FF),
        strokeMultiplier = 1.0f
    )
    MapVisualThemeMode.DAY -> MapColorPalette(
        mode = mode,
        canvasBackground = Color(0xFFE2E8F0),
        panelSurface = Color(0xFFF8FAFC),
        panelBorder = Color(0xFF64748B),
        landFill = Color(0xFFF1F5F9),
        landOutline = Color(0xFF0F172A),
        terrainContour = Color(0xFF94A3B8),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF334155),
        criticalRed = Color(0xFFDC2626),
        warningAmber = Color(0xFFD97706),
        advisoryBlue = Color(0xFF1D4ED8),
        normalGreen = Color(0xFF047857),
        classAColor = Color(0xFF6D28D9),
        classBColor = Color(0xFF0369A1),
        classCColor = Color(0xFFA21CAF),
        classDColor = Color(0xFF1D4ED8),
        restrictedColor = Color(0xFFB45309),
        tfrColor = Color(0xFFDC2626),
        routeLineColor = Color(0xFF047857),
        adsbTargetColor = Color(0xFF0F172A),
        strokeMultiplier = 1.15f
    )
    MapVisualThemeMode.HIGH_CONTRAST -> MapColorPalette(
        mode = mode,
        canvasBackground = Color(0xFF000000),
        panelSurface = Color(0xFF000000),
        panelBorder = Color(0xFFFFFFFF),
        landFill = Color(0xFF0F172A),
        landOutline = Color(0xFFFFFFFF),
        terrainContour = Color(0xFF94A3B8),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFF8FAFC),
        criticalRed = Color(0xFFFF3B30),
        warningAmber = Color(0xFFFFD60A),
        advisoryBlue = Color(0xFF40C8FF),
        normalGreen = Color(0xFF30D158),
        classAColor = Color(0xFFDF8BFF),
        classBColor = Color(0xFF40C8FF),
        classCColor = Color(0xFFFF64F2),
        classDColor = Color(0xFF64D2FF),
        restrictedColor = Color(0xFFFFD60A),
        tfrColor = Color(0xFFFF3B30),
        routeLineColor = Color(0xFF30D158),
        adsbTargetColor = Color(0xFFFFFFFF),
        strokeMultiplier = 1.55f
    )
    MapVisualThemeMode.COLOR_BLIND -> MapColorPalette(
        mode = mode,
        canvasBackground = Color(0xFF080E1A),
        panelSurface = Color(0xFF10192D),
        panelBorder = Color(0xFF88A3C7),
        landFill = Color(0xFF16243E),
        landOutline = Color(0xFF56B4E9), // Okabe-Ito Sky Blue
        terrainContour = Color(0xFF64748B),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFE2E8F0),
        criticalRed = Color(0xFFD55E00),   // Vermilion (CVD safe)
        warningAmber = Color(0xFFE69F00),  // Orange (CVD safe)
        advisoryBlue = Color(0xFF0072B2),  // Blue (CVD safe)
        normalGreen = Color(0xFF009E73),   // Bluish Green (CVD safe)
        classAColor = Color(0xFFCC79A7),   // Reddish Purple
        classBColor = Color(0xFF56B4E9),   // Sky Blue
        classCColor = Color(0xFFF0E442),   // Yellow
        classDColor = Color(0xFF0072B2),   // Deep Blue
        restrictedColor = Color(0xFFE69F00),
        tfrColor = Color(0xFFD55E00),
        routeLineColor = Color(0xFF009E73),
        adsbTargetColor = Color(0xFFF0E442),
        strokeMultiplier = 1.35f
    )
}

// ---------------------------------------------------------------------------
// 2. Split-Screen & Multi-Map Views
// ---------------------------------------------------------------------------

enum class MapSplitMode(
    val id: String,
    val label: String,
    val leftPaneTitle: String,
    val rightPaneTitle: String
) {
    SINGLE("single", "Unified View", "Unified Tactical Map", ""),
    SAT_AIRSPACE("sat_airspace", "Sat + Airspace", "NASA GIBS Satellite", "Airspace & TFR Sectors"),
    TERRAIN_NAV("terrain_nav", "Terrain + Nav", "Elevation & Terrain Grid", "Navigation & Waypoints"),
    WEATHER_ROUTES("weather_routes", "Weather + Routes", "NEXRAD Doppler Radar", "Flight Corridors & ADS-B")
}

// ---------------------------------------------------------------------------
// 3. Airspace Classifications, Altitude Filter & Touch Selection Zones
// ---------------------------------------------------------------------------

enum class AirspaceClassification(
    val id: String,
    val code: String,
    val label: String,
    val symbolGlyph: String
) {
    CLASS_A("class_a", "CLASS A", "Class A (FL180–FL600)", "▲"),
    CLASS_B("class_b", "CLASS B", "Class B (Terminal Corridor)", "●"),
    CLASS_C("class_c", "CLASS C", "Class C (Approach Sector)", "◆"),
    CLASS_D("class_d", "CLASS D", "Class D (Towered Surface)", "○"),
    RESTRICTED("restricted", "R-AREA", "Restricted SUA (R-3103)", "■"),
    TFR("tfr", "TFR", "Temp Flight Restriction", "⚠")
}

enum class AirspaceAltitudeBand(
    val id: String,
    val label: String,
    val minFilterFt: Int,
    val maxFilterFt: Int
) {
    ALL("all", "All Altitudes", 0, 60000),
    LOW_SURFACE("low", "SFC – 4,000 ft", 0, 4000),
    MID_ENROUTE("mid", "4,000 – 18,000 ft", 4000, 18000),
    HIGH_FL180("high", "FL180+ High", 18000, 60000);

    fun matchesAltitudeRange(floorFt: Int, ceilingFt: Int): Boolean =
        ceilingFt >= minFilterFt && floorFt <= maxFilterFt
}

data class AirspaceClassificationZone(
    val id: String,
    val name: String,
    val classification: AirspaceClassification,
    val centerLat: Double,
    val centerLon: Double,
    val radiusNm: Double,
    val polygon: List<GeoPoint> = emptyList(),
    val floorFtMsl: Int,
    val ceilingFtMsl: Int,
    val altitudeDisplay: String,
    val controllingAgency: String,
    val operationalDetail: String
)

object HawaiiAirspaceClassificationsCatalog {
    val staticZones: List<AirspaceClassificationZone> = listOf(
        AirspaceClassificationZone(
            id = "zone_zhn_class_a",
            name = "ZHN Honolulu Oceanic High Sector",
            classification = AirspaceClassification.CLASS_A,
            centerLat = 19.62,
            centerLon = -155.44,
            radiusNm = 52.0,
            floorFtMsl = 18000,
            ceilingFtMsl = 60000,
            altitudeDisplay = "18,000 ft MSL – FL600",
            controllingAgency = "Honolulu ARTCC (ZHN)",
            operationalDetail = "IFR-only jet route airspace spanning Hawaiʻi Island jet airways V15/V16/A332."
        ),
        AirspaceClassificationZone(
            id = "zone_phnl_class_b_ref",
            name = "Hawaiʻi Inter-Island Class B Feeder Corridor",
            classification = AirspaceClassification.CLASS_B,
            centerLat = 20.10,
            centerLon = -155.85,
            radiusNm = 16.0,
            floorFtMsl = 4000,
            ceilingFtMsl = 10000,
            altitudeDisplay = "4,000 – 10,000 ft MSL",
            controllingAgency = "Honolulu Control Facility (HCF)",
            operationalDetail = "High-density commercial jet arrival/departure corridor linking PHKO/PHTO to PHNL Class B."
        ),
        AirspaceClassificationZone(
            id = "zone_phko_class_c",
            name = "Kona Approach Sector (PHKO TRSA)",
            classification = AirspaceClassification.CLASS_C,
            centerLat = 19.738,
            centerLon = -156.045,
            radiusNm = 10.0,
            floorFtMsl = 1200,
            ceilingFtMsl = 4000,
            altitudeDisplay = "1,200 – 4,000 ft MSL",
            controllingAgency = "HCF / Kona Approach",
            operationalDetail = "Mode-C transponder & two-way radio approach shelf surrounding Ellison Onizuka Kona Intl."
        ),
        AirspaceClassificationZone(
            id = "zone_phto_class_d",
            name = "Hilo International Class D (PHTO)",
            classification = AirspaceClassification.CLASS_D,
            centerLat = 19.720,
            centerLon = -155.048,
            radiusNm = 4.3,
            floorFtMsl = 0,
            ceilingFtMsl = 2500,
            altitudeDisplay = "SFC – 2,500 ft MSL",
            controllingAgency = "Hilo ATCT (118.1 MHz)",
            operationalDetail = "4.3 NM towered surface area; Part 107 sUAS operations require active LAANC authorization."
        ),
        AirspaceClassificationZone(
            id = "zone_phko_class_d",
            name = "Kona International Class D (PHKO)",
            classification = AirspaceClassification.CLASS_D,
            centerLat = 19.738,
            centerLon = -156.045,
            radiusNm = 4.3,
            floorFtMsl = 0,
            ceilingFtMsl = 2500,
            altitudeDisplay = "SFC – 2,500 ft MSL",
            controllingAgency = "Kona ATCT (120.3 MHz)",
            operationalDetail = "West Hawaiʻi Class D surface ring; active daytime towered traffic."
        ),
        AirspaceClassificationZone(
            id = "zone_phsf_class_d",
            name = "Bradshaw Army Airfield Class D (PHSF)",
            classification = AirspaceClassification.CLASS_D,
            centerLat = 19.760,
            centerLon = -155.554,
            radiusNm = 4.0,
            floorFtMsl = 6190,
            ceilingFtMsl = 8700,
            altitudeDisplay = "SFC (6,190 ft) – 8,700 ft MSL",
            controllingAgency = "Bradshaw Tower / PTA Range Control",
            operationalDetail = "High-elevation military airfield embedded inside PTA Saddle corridor."
        ),
        AirspaceClassificationZone(
            id = "zone_r3103_restricted",
            name = "Restricted Area R-3103 (Pōhakuloa)",
            classification = AirspaceClassification.RESTRICTED,
            centerLat = 19.755,
            centerLon = -155.565,
            radiusNm = 8.5,
            polygon = HawaiiAirspaceReference.r3103Boundary,
            floorFtMsl = 0,
            ceilingFtMsl = 30000,
            altitudeDisplay = "SFC – 30,000 ft MSL",
            controllingAgency = "Honolulu Center (ZHN) / USAG PTA",
            operationalDetail = "Active live-fire artillery, mortar, and military UAS Special Use Airspace when hot."
        ),
        AirspaceClassificationZone(
            id = "zone_hvnp_tfr_advisory",
            name = "Kīlauea / HVNP Volcanic Hazard TFR Zone",
            classification = AirspaceClassification.TFR,
            centerLat = 19.406,
            centerLon = -155.283,
            radiusNm = 5.0,
            floorFtMsl = 0,
            ceilingFtMsl = 10000,
            altitudeDisplay = "SFC – 10,000 ft MSL",
            controllingAgency = "FAA ZHN / HVO",
            operationalDetail = "Volcanic eruption plume, low-level flight exclusion & HVO monitoring advisory ring."
        )
    )
}

// ---------------------------------------------------------------------------
// 4. Waypoints, Dynamic Route Recalculation, GPS & ADS-B Models
// ---------------------------------------------------------------------------

data class UserWaypoint(
    val id: String,
    val label: String,
    val lat: Double,
    val lon: Double,
    val altitudeFt: Int = 1500,
    val createdAtHst: String = HawaiiAirspaceReference.formatCurrentHst()
)

data class NavigationRouteLeg(
    val fromLabel: String,
    val toLabel: String,
    val distanceNm: Double,
    val bearingDeg: Int,
    val eteMinutes: Double
)

data class RouteSummary(
    val legs: List<NavigationRouteLeg> = emptyList(),
    val totalDistanceNm: Double = 0.0,
    val totalEteMinutes: Double = 0.0,
    val nextWaypointLabel: String? = null,
    val nextBearingDeg: Int? = null,
    val nextDistanceNm: Double? = null
)

object NavigationRouteEngine {
    fun recalculateRoute(
        currentLat: Double,
        currentLon: Double,
        groundSpeedKts: Double,
        waypoints: List<UserWaypoint>
    ): RouteSummary {
        if (waypoints.isEmpty()) return RouteSummary()
        val effectiveSpeed = groundSpeedKts.coerceAtLeast(45.0)
        val legs = mutableListOf<NavigationRouteLeg>()

        var prevLat = currentLat
        var prevLon = currentLon
        var prevLabel = "POS"

        waypoints.forEach { wp ->
            val dist = HawaiiAirspaceReference.distanceNauticalMiles(prevLat, prevLon, wp.lat, wp.lon)
            val brg = bearingDegrees(prevLat, prevLon, wp.lat, wp.lon)
            val ete = (dist / effectiveSpeed) * 60.0
            legs.add(
                NavigationRouteLeg(
                    fromLabel = prevLabel,
                    toLabel = wp.label,
                    distanceNm = dist,
                    bearingDeg = brg,
                    eteMinutes = ete
                )
            )
            prevLat = wp.lat
            prevLon = wp.lon
            prevLabel = wp.label
        }

        val firstLeg = legs.firstOrNull()
        return RouteSummary(
            legs = legs,
            totalDistanceNm = legs.sumOf { it.distanceNm },
            totalEteMinutes = legs.sumOf { it.eteMinutes },
            nextWaypointLabel = firstLeg?.toLabel,
            nextBearingDeg = firstLeg?.bearingDeg,
            nextDistanceNm = firstLeg?.distanceNm
        )
    }

    fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLambda = Math.toRadians(lon2 - lon1)
        val y = sin(dLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        val brng = Math.toDegrees(atan2(y, x))
        return ((brng + 360.0) % 360.0).roundToInt()
    }

    fun projectPointNm(lat: Double, lon: Double, bearingDeg: Double, distanceNm: Double): GeoPoint {
        val angularDist = distanceNm / 3440.065
        val brng = Math.toRadians(bearingDeg)
        val phi1 = Math.toRadians(lat)
        val lambda1 = Math.toRadians(lon)
        val phi2 = asin(
            sin(phi1) * cos(angularDist) + cos(phi1) * sin(angularDist) * cos(brng)
        )
        val lambda2 = lambda1 + atan2(
            sin(brng) * sin(angularDist) * cos(phi1),
            cos(angularDist) - sin(phi1) * sin(phi2)
        )
        return GeoPoint(Math.toDegrees(phi2), Math.toDegrees(lambda2))
    }
}

data class LiveGpsTelemetry(
    val isLive: Boolean = false,
    val latitude: Double = HawaiiAirspaceReference.ORCHIDLAND_LAT,
    val longitude: Double = HawaiiAirspaceReference.ORCHIDLAND_LON,
    val altitudeFt: Int = 340,
    val speedKts: Double = 0.0,
    val headingDeg: Int = 0,
    val accuracyMeters: Float = 0f,
    val providerName: String = "Orchidland Reference",
    val lastFixTimestampMs: Long = 0L,
    val fixIntervalMs: Long = 0L
)

data class AdsbAircraftTrack(
    val icao24: String,
    val callsign: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeFt: Int,
    val velocityKts: Int,
    val headingDeg: Int,
    val onGround: Boolean
)

sealed interface AdsbFeedStatus {
    data object Idle : AdsbFeedStatus
    data object Loading : AdsbFeedStatus
    data class Live(val updatedAtHst: String, val count: Int) : AdsbFeedStatus
    data class Unavailable(val message: String) : AdsbFeedStatus
}

// ---------------------------------------------------------------------------
// 5. Layer Presets, Quick Toolbar, Offline Caching & Performance Telemetry
// ---------------------------------------------------------------------------

enum class MapLayerPreset(
    val id: String,
    val label: String,
    val satEnabled: Boolean,
    val satLayer: NasaGibsLayer,
    val dopplerEnabled: Boolean,
    val dopplerLayer: HawaiiDopplerLayer,
    val altitudeBand: AirspaceAltitudeBand
) {
    VFR_SECTIONAL(
        id = "vfr",
        label = "VFR Sectional",
        satEnabled = true,
        satLayer = NasaGibsLayer.VIIRS_TRUE_COLOR,
        dopplerEnabled = false,
        dopplerLayer = HawaiiDopplerLayer.HAWAII_BREF,
        altitudeBand = AirspaceAltitudeBand.LOW_SURFACE
    ),
    IFR_HIGH_ALT(
        id = "ifr",
        label = "IFR Enroute",
        satEnabled = false,
        satLayer = NasaGibsLayer.VIIRS_TRUE_COLOR,
        dopplerEnabled = true,
        dopplerLayer = HawaiiDopplerLayer.HAWAII_CREF,
        altitudeBand = AirspaceAltitudeBand.HIGH_FL180
    ),
    SAR_THERMAL(
        id = "sar_thermal",
        label = "SAR / Thermal",
        satEnabled = true,
        satLayer = NasaGibsLayer.VIIRS_THERMAL,
        dopplerEnabled = true,
        dopplerLayer = HawaiiDopplerLayer.PHWA_VEL,
        altitudeBand = AirspaceAltitudeBand.ALL
    ),
    WEATHER_RADAR(
        id = "wx_radar",
        label = "Weather & Radar",
        satEnabled = true,
        satLayer = NasaGibsLayer.MODIS_AQUA,
        dopplerEnabled = true,
        dopplerLayer = HawaiiDopplerLayer.PHWA_BREF,
        altitudeBand = AirspaceAltitudeBand.ALL
    )
}

enum class QuickToolbarTool(
    val id: String,
    val shortLabel: String
) {
    FULLSCREEN("fullscreen", "Full Screen"),
    NORTH_UP("north_up", "North-Up"),
    SPLIT_VIEW("split_view", "Split Map"),
    GPS_TRACK("gps_track", "Live GPS"),
    ADSB_TRAFFIC("adsb", "ADS-B"),
    ADD_WAYPOINT("waypoint", "+Waypoint"),
    PIP_MODE("pip", "Mini PiP"),
    LEGEND("legend", "Legend"),
    VISUAL_MODE("theme", "Day/Night")
}

data class OfflineSectorSpec(
    val id: String,
    val title: String,
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val centerLat: Double,
    val centerLon: Double
)

object HawaiiOfflineSectorCatalog {
    val sectors: List<OfflineSectorSpec> = listOf(
        OfflineSectorSpec(
            id = "sector_big_island",
            title = "Hawaiʻi Island Full",
            minLat = HawaiiAirspaceReference.MAP_MIN_LAT,
            maxLat = HawaiiAirspaceReference.MAP_MAX_LAT,
            minLon = HawaiiAirspaceReference.MAP_MIN_LON,
            maxLon = HawaiiAirspaceReference.MAP_MAX_LON,
            centerLat = 19.59,
            centerLon = -155.44
        ),
        OfflineSectorSpec(
            id = "sector_puna_hilo",
            title = "Orchidland / Hilo (PHTO)",
            minLat = 19.38,
            maxLat = 19.82,
            minLon = -155.20,
            maxLon = -154.78,
            centerLat = 19.62,
            centerLon = -155.02
        ),
        OfflineSectorSpec(
            id = "sector_pta_saddle",
            title = "PTA R-3103 / Saddle Rd",
            minLat = 19.62,
            maxLat = 19.95,
            minLon = -155.78,
            maxLon = -155.35,
            centerLat = 19.755,
            centerLon = -155.56
        ),
        OfflineSectorSpec(
            id = "sector_kilauea_phwa",
            title = "Kīlauea / South Point",
            minLat = 18.88,
            maxLat = 19.52,
            minLon = -155.72,
            maxLon = -155.15,
            centerLat = 19.25,
            centerLon = -155.40
        )
    )
}

data class MapPerformanceMetrics(
    val lastGestureLatencyMs: Long = 12L,
    val lastLayerSwitchMs: Long = 18L,
    val gpsRefreshIntervalMs: Long = 0L,
    val cacheHitNote: String = "Disk & GPU layer cache ready",
    val tileResolutionLabel: String = "1600×1200 HD"
)

// ---------------------------------------------------------------------------
// 6. Scalable API-Driven Overlay Registry (3rd-Party Aviation/Sensor Expansion)
// ---------------------------------------------------------------------------

data class MapOverlayLayerDescriptor(
    val id: String,
    val title: String,
    val category: String,
    val endpointTemplate: String,
    val minAltitudeFt: Int = 0,
    val maxAltitudeFt: Int = 60000,
    val enabled: Boolean = false
)

object MapLayerRegistry {
    val defaultExternalLayers: List<MapOverlayLayerDescriptor> = listOf(
        MapOverlayLayerDescriptor(
            id = "ext_gibs_viirs",
            title = "NASA GIBS Earthdata WMS/Snapshot",
            category = "Satellite",
            endpointTemplate = "https://wvs.earthdata.nasa.gov/api/v1/snapshot",
            enabled = true
        ),
        MapOverlayLayerDescriptor(
            id = "ext_noaa_nexrad",
            title = "NOAA NCEP OpenGeo NEXRAD WMS",
            category = "Weather Radar",
            endpointTemplate = "https://opengeo.ncep.noaa.gov/geoserver/",
            enabled = true
        ),
        MapOverlayLayerDescriptor(
            id = "ext_faa_tfr_wfs",
            title = "FAA GeoServer TFR WFS v1.1.0",
            category = "Airspace",
            endpointTemplate = "https://tfr.faa.gov/geoserver/TFR/ows",
            enabled = true
        ),
        MapOverlayLayerDescriptor(
            id = "ext_opensky_adsb",
            title = "OpenSky Network Live ADS-B State Vectors",
            category = "Air Traffic",
            endpointTemplate = "https://opensky-network.org/api/states/all",
            enabled = false
        ),
        MapOverlayLayerDescriptor(
            id = "ext_usgs_terrain_3dep",
            title = "Terrain Elevation & 3D Pitch Contour Mesh",
            category = "Terrain / AR",
            endpointTemplate = "local://terrain_contour_mesh_v1",
            enabled = true
        )
    )
}
