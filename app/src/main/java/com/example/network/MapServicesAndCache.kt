package com.example.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// 1. Tile-Based Disk Cache, Offline Sector Storage & Predictive Prefetching
// ---------------------------------------------------------------------------

class MapTileCacheManager(context: Context) {

    private val appContext = context.applicationContext ?: context
    private val cacheDir = runCatching { File(appContext.cacheDir, "map_tile_cache") }.getOrNull()
    private val httpCache = runCatching {
        cacheDir?.let { Cache(it, 64L * 1024L * 1024L) }
    }.getOrNull()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .apply { if (httpCache != null) cache(httpCache) }
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(16, TimeUnit.SECONDS)
        .build()

    val imageLoader: ImageLoader = ImageLoader.Builder(appContext)
        .okHttpClient(okHttpClient)
        .memoryCache {
            MemoryCache.Builder(appContext)
                .maxSizePercent(0.25)
                .build()
        }
        .apply {
            val coilDir = runCatching { File(appContext.cacheDir, "coil_map_disk_cache") }.getOrNull()
            if (coilDir != null) {
                diskCache {
                    DiskCache.Builder()
                        .directory(coilDir)
                        .maxSizeBytes(96L * 1024L * 1024L)
                        .build()
                }
            }
        }
        .respectCacheHeaders(false)
        .build()

    fun getCacheSizeKb(): Long {
        val httpBytes = runCatching { httpCache?.size() ?: 0L }.getOrDefault(0L)
        val coilBytes = runCatching { imageLoader.diskCache?.size ?: 0L }.getOrDefault(0L)
        return ((httpBytes + coilBytes) / 1024L).coerceAtLeast(0L)
    }

    suspend fun prefetchPredictiveTiles(
        currentLat: Double,
        currentLon: Double,
        headingDeg: Int,
        speedKts: Double,
        satLayer: NasaGibsLayer,
        dopplerLayer: HawaiiDopplerLayer,
        dateIso: String,
        highRes: Boolean
    ): Int = withContext(Dispatchers.IO) {
        val width = if (highRes) 1440 else 960
        val height = if (highRes) 960 else 620
        val leadNm = (speedKts / 60.0 * 5.0).coerceIn(4.0, 25.0)
        val forwardPoint = NavigationRouteEngine.projectPointNm(
            lat = currentLat,
            lon = currentLon,
            bearingDeg = headingDeg.toDouble(),
            distanceNm = leadNm
        )

        val urls = listOf(
            NasaGibsUrlBuilder.buildSnapshotUrl(
                latitude = currentLat,
                longitude = currentLon,
                dateIso = dateIso,
                layer = satLayer,
                spanDegrees = 1.0,
                width = width,
                height = height
            ),
            NasaGibsUrlBuilder.buildSnapshotUrl(
                latitude = forwardPoint.lat,
                longitude = forwardPoint.lon,
                dateIso = dateIso,
                layer = satLayer,
                spanDegrees = 1.0,
                width = width,
                height = height
            ),
            HawaiiDopplerUrlBuilder.buildWmsUrl(
                layer = dopplerLayer,
                minLon = (forwardPoint.lon - 0.6).coerceIn(-161.0, -154.0),
                minLat = (forwardPoint.lat - 0.5).coerceIn(18.5, 22.5),
                maxLon = (forwardPoint.lon + 0.6).coerceIn(-161.0, -154.0),
                maxLat = (forwardPoint.lat + 0.5).coerceIn(18.5, 22.5),
                width = width,
                height = height
            )
        )

        var prefetched = 0
        urls.forEach { url ->
            val ok = fetchAndCacheUrl(url)
            if (ok) prefetched++
        }
        prefetched
    }

    suspend fun cacheOfflineSector(
        sector: OfflineSectorSpec,
        satLayer: NasaGibsLayer,
        dopplerLayer: HawaiiDopplerLayer,
        dateIso: String,
        highRes: Boolean
    ): Int = withContext(Dispatchers.IO) {
        val width = if (highRes) 1600 else 1024
        val height = if (highRes) 1200 else 768
        val urls = listOf(
            NasaGibsUrlBuilder.buildBboxSnapshotUrl(
                south = sector.minLat,
                west = sector.minLon,
                north = sector.maxLat,
                east = sector.maxLon,
                dateIso = dateIso,
                layer = satLayer,
                width = width,
                height = height
            ),
            NasaGibsUrlBuilder.buildBboxSnapshotUrl(
                south = sector.minLat,
                west = sector.minLon,
                north = sector.maxLat,
                east = sector.maxLon,
                dateIso = dateIso,
                layer = NasaGibsLayer.VIIRS_THERMAL,
                width = width,
                height = height
            ),
            HawaiiDopplerUrlBuilder.buildWmsUrl(
                layer = dopplerLayer,
                minLon = sector.minLon,
                minLat = sector.minLat,
                maxLon = sector.maxLon,
                maxLat = sector.maxLat,
                width = width,
                height = height
            )
        )

        var stored = 0
        urls.forEach { url ->
            if (fetchAndCacheUrl(url)) stored++
        }
        stored
    }

    private fun fetchAndCacheUrl(url: String): Boolean = runCatching {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "VeriScopeMapPrefetch/2.6")
            .get()
            .build()
        okHttpClient.newCall(req).execute().use { resp ->
            if (resp.isSuccessful) {
                resp.body?.bytes()
                true
            } else {
                false
            }
        }
    }.getOrDefault(false)
}

// ---------------------------------------------------------------------------
// 2. Live OpenSky ADS-B Air Traffic Integration (Hawaiʻi Island Bounds)
// ---------------------------------------------------------------------------

interface OpenSkyAdsbApi {
    @GET("api/states/all")
    suspend fun getStatesInBbox(
        @Query("lamin") laMin: Double,
        @Query("lomin") loMin: Double,
        @Query("lamax") laMax: Double,
        @Query("lomax") loMax: Double
    ): ResponseBody
}

class OpenSkyAdsbRepository(
    private val api: OpenSkyAdsbApi = createDefaultApi()
) {
    suspend fun fetchHawaiiAircraft(): Result<List<AdsbAircraftTrack>> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = api.getStatesInBbox(
                laMin = 18.6,
                loMin = -156.5,
                laMax = 20.5,
                loMax = -154.5
            ).string()
            parseOpenSkyStates(raw)
        }
    }

    companion object {
        fun createDefaultApi(): OpenSkyAdsbApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://opensky-network.org/")
                .client(client)
                .build()
                .create(OpenSkyAdsbApi::class.java)
        }

        internal fun parseOpenSkyStates(rawJson: String): List<AdsbAircraftTrack> {
            val trimmed = rawJson.trim()
            if (trimmed.isEmpty() || !trimmed.startsWith("{")) return emptyList()
            val root = JSONObject(trimmed)
            val states: JSONArray = root.optJSONArray("states") ?: return emptyList()
            val tracks = mutableListOf<AdsbAircraftTrack>()
            for (i in 0 until states.length()) {
                val row = states.optJSONArray(i) ?: continue
                if (row.length() < 11) continue
                val lon = row.optDouble(5, Double.NaN)
                val lat = row.optDouble(6, Double.NaN)
                if (lat.isNaN() || lon.isNaN()) continue
                val icao24 = row.optString(0, "").trim().uppercase(Locale.US)
                val callsign = row.optString(1, "").trim().ifBlank { icao24.ifBlank { "TARGET" } }
                val baroAltMeters = row.optDouble(7, Double.NaN)
                val geoAltMeters = row.optDouble(13, Double.NaN)
                val altMeters = when {
                    !baroAltMeters.isNaN() -> baroAltMeters
                    !geoAltMeters.isNaN() -> geoAltMeters
                    else -> 0.0
                }
                val altFt = (altMeters * 3.28084).roundToInt().coerceAtLeast(0)
                val onGround = row.optBoolean(8, false)
                val velMs = row.optDouble(9, 0.0).let { if (it.isNaN()) 0.0 else it }
                val velKts = (velMs * 1.94384).roundToInt().coerceAtLeast(0)
                val trackDeg = row.optDouble(10, 0.0).let { if (it.isNaN()) 0 else it.roundToInt() }

                tracks.add(
                    AdsbAircraftTrack(
                        icao24 = icao24,
                        callsign = callsign,
                        latitude = lat,
                        longitude = lon,
                        altitudeFt = altFt,
                        velocityKts = velKts,
                        headingDeg = ((trackDeg % 360) + 360) % 360,
                        onGround = onGround
                    )
                )
            }
            return tracks
        }
    }
}

// ---------------------------------------------------------------------------
// 3. Persistent User Mapping Preferences Store (SharedPreferences)
// ---------------------------------------------------------------------------

data class PersistedMapPreferences(
    val visualThemeMode: MapVisualThemeMode = MapVisualThemeMode.NIGHT,
    val splitMode: MapSplitMode = MapSplitMode.SINGLE,
    val altitudeBand: AirspaceAltitudeBand = AirspaceAltitudeBand.ALL,
    val dopplerOpacity: Float = 0.82f,
    val satOpacity: Float = 0.72f,
    val airspaceOpacity: Float = 0.85f,
    val hdTileResolution: Boolean = true,
    val quickTools: List<QuickToolbarTool> = listOf(
        QuickToolbarTool.FULLSCREEN,
        QuickToolbarTool.NORTH_UP,
        QuickToolbarTool.SPLIT_VIEW,
        QuickToolbarTool.GPS_TRACK,
        QuickToolbarTool.ADD_WAYPOINT,
        QuickToolbarTool.PIP_MODE,
        QuickToolbarTool.VISUAL_MODE
    ),
    val offlineSectorIds: Set<String> = emptySet(),
    val waypoints: List<UserWaypoint> = defaultWaypoints()
) {
    companion object {
        fun defaultWaypoints(): List<UserWaypoint> = listOf(
            UserWaypoint(
                id = "wp_orchidland",
                label = "ORCHID",
                lat = HawaiiAirspaceReference.ORCHIDLAND_LAT,
                lon = HawaiiAirspaceReference.ORCHIDLAND_LON,
                altitudeFt = 600,
                createdAtHst = "Baseline"
            ),
            UserWaypoint(
                id = "wp_phto",
                label = "PHTO",
                lat = 19.720,
                lon = -155.048,
                altitudeFt = 1500,
                createdAtHst = "Baseline"
            ),
            UserWaypoint(
                id = "wp_pta",
                label = "PTA-R3103",
                lat = 19.756,
                lon = -155.533,
                altitudeFt = 9500,
                createdAtHst = "Baseline"
            )
        )
    }
}

class MapPreferencesStore(context: Context) {
    private val appContext = context.applicationContext ?: context
    private val prefs = runCatching {
        appContext.getSharedPreferences("veriscope_map_prefs_v1", Context.MODE_PRIVATE)
    }.getOrNull()

    fun load(): PersistedMapPreferences = runCatching {
        val p = prefs ?: return@runCatching PersistedMapPreferences()
        val modeId = runCatching { p.getString("visual_mode", MapVisualThemeMode.NIGHT.id) }.getOrNull()
        val visualMode = MapVisualThemeMode.entries.find { it.id == modeId } ?: MapVisualThemeMode.NIGHT

        val splitId = runCatching { p.getString("split_mode", MapSplitMode.SINGLE.id) }.getOrNull()
        val splitMode = MapSplitMode.entries.find { it.id == splitId } ?: MapSplitMode.SINGLE

        val altId = runCatching { p.getString("altitude_band", AirspaceAltitudeBand.ALL.id) }.getOrNull()
        val altBand = AirspaceAltitudeBand.entries.find { it.id == altId } ?: AirspaceAltitudeBand.ALL

        val quickRaw = runCatching { p.getString("quick_tools", null) }.getOrNull()
        val quickTools = if (quickRaw.isNullOrBlank()) {
            PersistedMapPreferences().quickTools
        } else {
            quickRaw.split(",")
                .mapNotNull { id -> QuickToolbarTool.entries.find { it.id == id } }
                .ifEmpty { PersistedMapPreferences().quickTools }
        }

        val offlineIds = runCatching { p.getStringSet("offline_sectors", emptySet())?.toSet() }.getOrNull() ?: emptySet()
        val wpJson = runCatching { p.getString("waypoints_json", null) }.getOrNull()
        val waypoints = if (wpJson.isNullOrBlank()) {
            PersistedMapPreferences.defaultWaypoints()
        } else {
            decodeWaypoints(wpJson)
        }

        PersistedMapPreferences(
            visualThemeMode = visualMode,
            splitMode = splitMode,
            altitudeBand = altBand,
            dopplerOpacity = runCatching { p.getFloat("doppler_opacity", 0.82f) }.getOrDefault(0.82f).coerceIn(0.2f, 1.0f),
            satOpacity = runCatching { p.getFloat("sat_opacity", 0.72f) }.getOrDefault(0.72f).coerceIn(0.25f, 1.0f),
            airspaceOpacity = runCatching { p.getFloat("airspace_opacity", 0.85f) }.getOrDefault(0.85f).coerceIn(0.2f, 1.0f),
            hdTileResolution = runCatching { p.getBoolean("hd_tile_res", true) }.getOrDefault(true),
            quickTools = quickTools,
            offlineSectorIds = offlineIds,
            waypoints = waypoints
        )
    }.getOrElse {
        runCatching { prefs?.edit()?.clear()?.apply() }
        PersistedMapPreferences()
    }

    fun saveVisualMode(mode: MapVisualThemeMode) {
        prefs?.edit()?.putString("visual_mode", mode.id)?.apply()
    }

    fun saveSplitMode(mode: MapSplitMode) {
        prefs?.edit()?.putString("split_mode", mode.id)?.apply()
    }

    fun saveAltitudeBand(band: AirspaceAltitudeBand) {
        prefs?.edit()?.putString("altitude_band", band.id)?.apply()
    }

    fun saveOpacities(sat: Float, doppler: Float, airspace: Float) {
        prefs?.edit()
            ?.putFloat("sat_opacity", sat)
            ?.putFloat("doppler_opacity", doppler)
            ?.putFloat("airspace_opacity", airspace)
            ?.apply()
    }

    fun saveHdResolution(enabled: Boolean) {
        prefs?.edit()?.putBoolean("hd_tile_res", enabled)?.apply()
    }

    fun saveQuickTools(tools: List<QuickToolbarTool>) {
        prefs?.edit()?.putString("quick_tools", tools.joinToString(",") { it.id })?.apply()
    }

    fun saveOfflineSectors(ids: Set<String>) {
        prefs?.edit()?.putStringSet("offline_sectors", ids)?.apply()
    }

    fun saveWaypoints(waypoints: List<UserWaypoint>) {
        val arr = JSONArray()
        waypoints.forEach { wp ->
            arr.put(
                JSONObject().apply {
                    put("id", wp.id)
                    put("label", wp.label)
                    put("lat", wp.lat)
                    put("lon", wp.lon)
                    put("alt", wp.altitudeFt)
                    put("ts", wp.createdAtHst)
                }
            )
        }
        prefs?.edit()?.putString("waypoints_json", arr.toString())?.apply()
    }

    private fun decodeWaypoints(json: String): List<UserWaypoint> = runCatching {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(
                    UserWaypoint(
                        id = obj.optString("id", "wp_$i"),
                        label = obj.optString("label", "WP-${i + 1}"),
                        lat = obj.optDouble("lat", HawaiiAirspaceReference.ORCHIDLAND_LAT),
                        lon = obj.optDouble("lon", HawaiiAirspaceReference.ORCHIDLAND_LON),
                        altitudeFt = obj.optInt("alt", 1500),
                        createdAtHst = obj.optString("ts", "Saved")
                    )
                )
            }
        }
    }.getOrElse { PersistedMapPreferences.defaultWaypoints() }
}

// ---------------------------------------------------------------------------
// 4. Real Foreground GPS Location Tracker + Multi-Display Detector
// ---------------------------------------------------------------------------

class DeviceLocationTracker(context: Context) {
    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val displayManager =
        appContext.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager

    private var activeListener: LocationListener? = null
    private var lastUpdateMs: Long = 0L

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun countExternalDisplays(): Int {
        val displays = runCatching { displayManager?.displays }.getOrNull() ?: return 0
        return (displays.size - 1).coerceAtLeast(0)
    }

    data class GpsStartOutcome(
        val started: Boolean,
        val diagnosticNote: String
    )

    @SuppressLint("MissingPermission")
    fun startTracking(onTelemetry: (LiveGpsTelemetry) -> Unit): Boolean =
        startTrackingWithDiagnostics(onTelemetry).started

    @SuppressLint("MissingPermission")
    fun startTrackingWithDiagnostics(onTelemetry: (LiveGpsTelemetry) -> Unit): GpsStartOutcome {
        val lm = locationManager
            ?: return GpsStartOutcome(false, "LocationManager service unavailable on this device.")
        if (!hasLocationPermission()) {
            return GpsStartOutcome(false, "Location permission denied — using Orchidland reference coordinates.")
        }
        stopTracking()

        // Emit immediate last known fix if available
        val lastKnown = listOfNotNull(
            runCatching { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull(),
            runCatching { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()
        ).maxByOrNull { it.time }

        if (lastKnown != null) {
            onTelemetry(toTelemetry(lastKnown, intervalMs = 0L))
        }

        val listener = LocationListener { loc ->
            val now = System.currentTimeMillis()
            val interval = if (lastUpdateMs > 0L) (now - lastUpdateMs).coerceAtLeast(1L) else 800L
            lastUpdateMs = now
            onTelemetry(toTelemetry(loc, intervalMs = interval))
        }
        activeListener = listener

        var registered = false
        val enabledProviders = mutableListOf<String>()
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).forEach { provider ->
            runCatching {
                if (lm.isProviderEnabled(provider)) {
                    lm.requestLocationUpdates(
                        provider,
                        800L, // < 1 second target refresh
                        1f,
                        listener,
                        Looper.getMainLooper()
                    )
                    registered = true
                    enabledProviders.add(provider.uppercase(Locale.US))
                }
            }
        }
        val started = registered || lastKnown != null
        val note = when {
            registered && lastKnown != null ->
                "Live tracking via ${enabledProviders.joinToString("/")} (<1s refresh)."
            registered ->
                "Waiting for first satellite fix via ${enabledProviders.joinToString("/")}..."
            lastKnown != null ->
                "Showing last known fix; enable device Location / GPS for live streaming."
            else ->
                "Device GPS & Network location providers are turned off. Enable Location in Android Settings."
        }
        return GpsStartOutcome(started = started, diagnosticNote = note)
    }

    fun stopTracking() {
        val listener = activeListener ?: return
        runCatching { locationManager?.removeUpdates(listener) }
        activeListener = null
    }

    private fun toTelemetry(loc: Location, intervalMs: Long): LiveGpsTelemetry {
        val speedKts = if (loc.hasSpeed()) (loc.speed * 1.94384) else 0.0
        val heading = if (loc.hasBearing()) loc.bearing.roundToInt() else 0
        val altFt = if (loc.hasAltitude()) (loc.altitude * 3.28084).roundToInt() else 0
        return LiveGpsTelemetry(
            isLive = true,
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeFt = altFt,
            speedKts = speedKts,
            headingDeg = ((heading % 360) + 360) % 360,
            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else 0f,
            providerName = loc.provider?.uppercase(Locale.US) ?: "GPS",
            lastFixTimestampMs = loc.time,
            fixIntervalMs = intervalMs
        )
    }
}
