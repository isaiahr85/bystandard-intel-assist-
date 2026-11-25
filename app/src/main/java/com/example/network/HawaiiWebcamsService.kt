package com.example.network

import android.content.Context
import coil.request.ImageRequest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Request

// ---------------------------------------------------------------------------
// Shared Coil ImageRequest Builder with Proper Agency Headers
// ---------------------------------------------------------------------------

private const val FEED_BROWSER_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; VeriScope) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

fun buildFeedImageRequest(
    context: Context,
    url: String,
    referer: String? = null
): ImageRequest {
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .addHeader("User-Agent", FEED_BROWSER_USER_AGENT)
        .addHeader("Accept", "image/avif,image/webp,image/png,image/jpeg,image/*,*/*;q=0.8")
        .apply {
            if (!referer.isNullOrBlank()) {
                addHeader("Referer", referer)
            }
        }
        .build()
}

// ---------------------------------------------------------------------------
// Hawaiʻi Live Still Cams Domain Models (FAA WeatherCams + USGS HVO + UH)
// ---------------------------------------------------------------------------

enum class WebcamFeedSource(
    val id: String,
    val badgeLabel: String,
    val fullAgencyName: String
) {
    FAA_WEATHERCAM("faa", "FAA CAM", "FAA WeatherCams"),
    USGS_VOLCANO("usgs", "USGS HVO", "USGS Hawaiian Volcano Observatory"),
    UH_SYSTEM("uh", "UH SYSTEM", "University of Hawaiʻi / Mauna Kea")
}

enum class WebcamFilter(
    val id: String,
    val label: String
) {
    ALL("all", "All Cams"),
    FAA("faa", "FAA WeatherCams"),
    USGS("usgs", "USGS Volcanoes"),
    UH("uh", "UH System")
}

data class HawaiiWebcamStill(
    val id: String,
    val stationCode: String,
    val title: String,
    val subtitle: String,
    val source: WebcamFeedSource,
    val direction: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val refererUrl: String? = null,
    val portalUrl: String = "",
    val captureTimestamp: String? = null,
    val isThermal: Boolean = false
) {
    fun buildCacheBustedUrl(nonce: Long): String {
        if (nonce <= 0L) return imageUrl
        val sep = if (imageUrl.contains("?")) "&" else "?"
        return "$imageUrl${sep}_t=$nonce"
    }

    fun distanceToOrchidlandNm(): Double = HawaiiAirspaceReference.distanceNauticalMiles(
        HawaiiAirspaceReference.ORCHIDLAND_LAT,
        HawaiiAirspaceReference.ORCHIDLAND_LON,
        latitude,
        longitude
    )
}

data class FaaSiteInfo(
    val siteId: String,
    val siteIdentifier: String,
    val siteName: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val elevationFt: Int? = null
)

data class HawaiiWebcamsSnapshot(
    val updatedAtHst: String,
    val faaCams: List<HawaiiWebcamStill> = emptyList(),
    val usgsCams: List<HawaiiWebcamStill> = emptyList(),
    val uhCams: List<HawaiiWebcamStill> = emptyList(),
    val faaError: String? = null,
    val faaSiteCount: Int = 0
) {
    val allCamsSorted: List<HawaiiWebcamStill>
        get() = (usgsCams + uhCams + faaCams)
            .sortedBy { it.distanceToOrchidlandNm() }

    fun filtered(filter: WebcamFilter): List<HawaiiWebcamStill> = when (filter) {
        WebcamFilter.ALL -> allCamsSorted
        WebcamFilter.FAA -> faaCams.sortedBy { it.distanceToOrchidlandNm() }
        WebcamFilter.USGS -> usgsCams.sortedBy { it.distanceToOrchidlandNm() }
        WebcamFilter.UH -> uhCams.sortedBy { it.distanceToOrchidlandNm() }
    }
}

sealed interface WebcamsLoadState {
    data object Idle : WebcamsLoadState
    data object Loading : WebcamsLoadState
    data object Ready : WebcamsLoadState
}

// ---------------------------------------------------------------------------
// Hilo Downtown Cam Monitor Domain Models & Verified Official Reference
// ---------------------------------------------------------------------------

data class HiloCamStatusProbeResult(
    val checkedAtHst: String,
    val officialUrl: String,
    val isReachable: Boolean,
    val httpStatusCode: Int?,
    val latencyMs: Long?,
    val summary: String
)

sealed interface HiloCamStatusState {
    data object Idle : HiloCamStatusState
    data object Checking : HiloCamStatusState
    data class Checked(val result: HiloCamStatusProbeResult) : HiloCamStatusState
}

object HiloDowntownCamReference {
    const val PRIMARY_CAMERA_NAME = "Pacific Tsunami Museum Hilo Bay Webcam"
    const val PRIMARY_OFFICIAL_URL = "https://tsunami.org/hilobaycam/"
    const val CAMERA_LAT = 19.726
    const val CAMERA_LON = -155.0866
    const val CAMERA_VIEW_DESCRIPTION =
        "Hilo Bay from rooftop near downtown waterfront (closest public feed to the park/post office block)."

    const val TARGET_AREA_TITLE = "Kalakaua Park & Federal Building / Main Post Office"
    const val TARGET_STREET_ADDRESS = "154 Waianuenue Ave, Downtown Hilo, HI 96720"
    const val KALAKAUA_PARK_LAT = 19.7248
    const val KALAKAUA_PARK_LON = -155.0875
    const val FEDERAL_BLDG_LAT = 19.7241
    const val FEDERAL_BLDG_LON = -155.0870

    const val BANDWIDTH_OFFLINE_NOTE =
        "Feeds are frequently bandwidth-limited or offline. No direct raw stream URL is publicly documented—never use unverified or guessed stream links."
    const val SEARCH_OFFER_NOTE =
        "Use 'Check Status via Search' below to inspect recent public outage reports or museum notices."
    const val SEARCH_STATUS_URL =
        "https://www.google.com/search?q=%22Pacific+Tsunami+Museum%22+%22Hilo+Bay+Webcam%22+status+tsunami.org"

    fun distanceToPostOfficeMeters(): Int {
        val nm = HawaiiAirspaceReference.distanceNauticalMiles(
            CAMERA_LAT,
            CAMERA_LON,
            FEDERAL_BLDG_LAT,
            FEDERAL_BLDG_LON
        )
        return (nm * 1852.0).toInt()
    }

    fun buildOperationalBrief(
        probeResult: HiloCamStatusProbeResult?,
        userQuery: String = ""
    ): String = buildString {
        append("1. Primary Public Camera (Official Link First): ")
        append(PRIMARY_CAMERA_NAME)
        append(" — ")
        append(PRIMARY_OFFICIAL_URL)
        append(" (Approx. ")
        append(CAMERA_LAT)
        append(", ")
        append(CAMERA_LON)
        append("). ")
        append("View: ")
        append(CAMERA_VIEW_DESCRIPTION)
        append(" Target Area: ")
        append(TARGET_AREA_TITLE)
        append(" (")
        append(TARGET_STREET_ADDRESS)
        append("), approx. ")
        append(distanceToPostOfficeMeters())
        append(" meters from museum rooftop camera. ")
        append("2. Feed Reliability Notice: ")
        append(BANDWIDTH_OFFLINE_NOTE)
        append(" ")
        append("3. Status & Search Check: ")
        if (probeResult != null) {
            append(probeResult.summary)
            append(" ")
        }
        append(SEARCH_OFFER_NOTE)
        if (userQuery.isNotBlank()) {
            append(" Active query context: ")
            append(userQuery.trim())
            append(".")
        }
    }
}

// ---------------------------------------------------------------------------
// Verified USGS HVO & UH System Direct HTTPS Still Feeds
// ---------------------------------------------------------------------------

object HawaiiWebcamsReference {

    private fun usgsStillUrl(camCode: String): String =
        "https://volcanoes.usgs.gov/cams/$camCode/images/M.jpg"

    val usgsHvoCams: List<HawaiiWebcamStill> = listOf(
        HawaiiWebcamStill(
            id = "usgs_pgcam",
            stationCode = "PGcam",
            title = "Puʻu Honuaʻula / Leilani LERZ",
            subtitle = "Lower East Rift Zone — closest HVO optical still to Orchidland & Hwy 130.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "East Rift (LERZ)",
            latitude = 19.4620,
            longitude = -154.9060,
            imageUrl = usgsStillUrl("PGcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_f1cam",
            stationCode = "F1cam",
            title = "Halemaʻumaʻu Thermal (West Rim)",
            subtitle = "FLIR infrared thermal camera monitoring Kīlauea summit vents & lava heat signatures.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Thermal E / Summit",
            latitude = 19.4106,
            longitude = -155.2896,
            imageUrl = usgsStillUrl("F1cam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/",
            isThermal = true
        ),
        HawaiiWebcamStill(
            id = "usgs_kwcam",
            stationCode = "KWcam",
            title = "Halemaʻumaʻu West Rim Panorama",
            subtitle = "Wide-angle optical still across Halemaʻumaʻu Crater and summit fumaroles.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "East / Caldera",
            latitude = 19.4108,
            longitude = -155.2892,
            imageUrl = usgsStillUrl("KWcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_v1cam",
            stationCode = "V1cam",
            title = "Kīlauea West Rim Vent Monitor",
            subtitle = "Close-range optical still focused on Halemaʻumaʻu eruptive vent floor.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Caldera Floor",
            latitude = 19.4125,
            longitude = -155.2910,
            imageUrl = usgsStillUrl("V1cam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_kicam",
            stationCode = "KIcam",
            title = "Kaluapele North Rim (Uēkahuna)",
            subtitle = "Kīlauea Caldera North Rim synoptic view including vog & plume ceiling.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "South / Caldera",
            latitude = 19.4208,
            longitude = -155.2878,
            imageUrl = usgsStillUrl("KIcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_kpcam",
            stationCode = "KPcam",
            title = "Kīlauea Summit (Mauna Loa Strip)",
            subtitle = "Long-range plume & SO2 gas dispersion overview toward Puna & Kaʻū.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Southeast",
            latitude = 19.4492,
            longitude = -155.3358,
            imageUrl = usgsStillUrl("KPcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_b1cam",
            stationCode = "B1cam",
            title = "Kīlauea Down-Dropped Block",
            subtitle = "Monitors structural caldera bench and south crater rim visibility.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "North / Rim",
            latitude = 19.4025,
            longitude = -155.2795,
            imageUrl = usgsStillUrl("B1cam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_s1cam",
            stationCode = "S1cam",
            title = "Kīlauea Upper Southwest Rift",
            subtitle = "Optical monitor covering Kīlauea SWRZ fracture corridor.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Southwest Rift",
            latitude = 19.3890,
            longitude = -155.3180,
            imageUrl = usgsStillUrl("S1cam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_mlcam",
            stationCode = "MLcam",
            title = "Mauna Loa Mokuʻāweoweo NW Rim",
            subtitle = "Summit caldera optical still monitoring high-altitude volcanic activity.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Southeast / Summit",
            latitude = 19.4795,
            longitude = -155.6025,
            imageUrl = usgsStillUrl("MLcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_mocam",
            stationCode = "MOcam",
            title = "Mauna Loa Mokuʻāweoweo South Rim",
            subtitle = "South rim view across Mokuʻāweoweo Caldera & summit vents.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "North / Caldera",
            latitude = 19.4565,
            longitude = -155.5955,
            imageUrl = usgsStillUrl("MOcam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        ),
        HawaiiWebcamStill(
            id = "usgs_m2cam",
            stationCode = "M2cam",
            title = "Mauna Loa Middle Southwest Rift",
            subtitle = "Monitors middle SWRZ rift vents and upper Kaʻū slope weather.",
            source = WebcamFeedSource.USGS_VOLCANO,
            direction = "Southwest Rift",
            latitude = 19.3540,
            longitude = -155.7020,
            imageUrl = usgsStillUrl("M2cam"),
            refererUrl = "https://volcanoes.usgs.gov/",
            portalUrl = "https://volcanoes.usgs.gov/observatories/hvo/"
        )
    )

    val uhSystemCams: List<HawaiiWebcamStill> = listOf(
        HawaiiWebcamStill(
            id = "uh_irtf_c166",
            stationCode = "UH-IRTF-166",
            title = "UH IfA / NASA IRTF — Mauna Loa & PTA View",
            subtitle = "UH Institute for Astronomy summit still looking across Saddle Road & Mauna Loa.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "South / Mauna Loa",
            latitude = 19.8263,
            longitude = -155.4720,
            imageUrl = "https://irtf.ifa.hawaii.edu/~irtfcameras/c166/latest.jpg",
            refererUrl = "https://irtf.ifa.hawaii.edu/",
            portalUrl = "https://mkwc.ifa.hawaii.edu/current/cams/"
        ),
        HawaiiWebcamStill(
            id = "uh_irtf_c160",
            stationCode = "UH-IRTF-160",
            title = "UH IfA / NASA IRTF — Summit Front PTZ",
            subtitle = "UH Mauna Kea summit ridge visibility, icing & ceiling conditions.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "Summit Ridge",
            latitude = 19.8265,
            longitude = -155.4725,
            imageUrl = "https://irtf.ifa.hawaii.edu/~irtfcameras/c160/latest.jpg",
            refererUrl = "https://irtf.ifa.hawaii.edu/",
            portalUrl = "https://mkwc.ifa.hawaii.edu/current/cams/"
        ),
        HawaiiWebcamStill(
            id = "uh_cfht_dome",
            stationCode = "UH-CFHT-DOME",
            title = "UH / CFHT Observatory Cloud & Horizon Cam",
            subtitle = "Mauna Kea summit deck still tracking trade-wind cloud deck & Saddle inversion.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "Summit Horizon",
            latitude = 19.8253,
            longitude = -155.4689,
            imageUrl = "https://www.cfht.hawaii.edu/en/gallery/cfhtdome/cfhtdome.jpg",
            refererUrl = "https://www.cfht.hawaii.edu/",
            portalUrl = "https://www.cfht.hawaii.edu/"
        ),
        HawaiiWebcamStill(
            id = "uh_cfht_nanaao",
            stationCode = "UH-NANAAO",
            title = "UH / CFHT Nānā ao All-Sky Cloud Still",
            subtitle = "All-sky optical ceiling monitor for cloud cover, contrails & night glow.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "All-Sky 360°",
            latitude = 19.8255,
            longitude = -155.4687,
            imageUrl = "https://www.cfht.hawaii.edu/images/webcam/nanaao.jpg",
            refererUrl = "https://www.cfht.hawaii.edu/",
            portalUrl = "https://www.cfht.hawaii.edu/"
        ),
        HawaiiWebcamStill(
            id = "uh_cfht_gem",
            stationCode = "UH-MK-GEM",
            title = "UH / Mauna Kea Gemini Ridge Cam",
            subtitle = "Mauna Kea south-facing observatory ridge & Upper PTA airspace visibility.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "South / Ridge",
            latitude = 19.8238,
            longitude = -155.4691,
            imageUrl = "https://www.cfht.hawaii.edu/webcams/gemdome/gemdomesmall.jpg",
            refererUrl = "https://www.cfht.hawaii.edu/",
            portalUrl = "https://mkwc.ifa.hawaii.edu/current/cams/"
        ),
        HawaiiWebcamStill(
            id = "uh_gemini_west",
            stationCode = "MKWC-GEM-W",
            title = "MKWC / Gemini North West-Facing Still",
            subtitle = "Monitors Kohala, Waimea (PHMU) & Saddle western approach weather.",
            source = WebcamFeedSource.UH_SYSTEM,
            direction = "West / Kohala",
            latitude = 19.8240,
            longitude = -155.4694,
            imageUrl = "https://www.gemini.edu/sciops/schedules/obsStatus/currentstill_West.png",
            refererUrl = "https://www.gemini.edu/",
            portalUrl = "https://mkwc.ifa.hawaii.edu/current/cams/"
        )
    )
}

// ---------------------------------------------------------------------------
// FAA WeatherCams Retrofit Service & Repository
// ---------------------------------------------------------------------------

interface FaaWeatherCamsApi {
    @GET("sites")
    suspend fun getSites(): ResponseBody

    @GET("summary")
    suspend fun getSiteSummary(@Query("siteId") siteId: String): ResponseBody
}

class HawaiiWebcamsRepository(
    private val faaWeatherCamsApi: FaaWeatherCamsApi = createDefaultFaaWeatherCamsApi()
) {
    suspend fun fetchSnapshot(): HawaiiWebcamsSnapshot = coroutineScope {
        val usgs = HawaiiWebcamsReference.usgsHvoCams
        val uh = HawaiiWebcamsReference.uhSystemCams

        var faaError: String? = null
        var hiSiteCount = 0
        val faaCams = mutableListOf<HawaiiWebcamStill>()

        val sitesResult = runCatching {
            val raw = faaWeatherCamsApi.getSites().string()
            parseFaaHawaiiSites(raw)
        }

        sitesResult.fold(
            onSuccess = { hiSites ->
                hiSiteCount = hiSites.size
                val sortedSites = hiSites.sortedBy {
                    HawaiiAirspaceReference.distanceNauticalMiles(
                        HawaiiAirspaceReference.ORCHIDLAND_LAT,
                        HawaiiAirspaceReference.ORCHIDLAND_LON,
                        it.latitude,
                        it.longitude
                    )
                }

                val summaryJobs = sortedSites.map { site ->
                    async {
                        runCatching {
                            val summaryJson = faaWeatherCamsApi.getSiteSummary(site.siteId).string()
                            parseFaaSiteSummaryCameras(site, summaryJson)
                        }
                    }
                }
                val summaryResults = summaryJobs.awaitAll()
                var failedSites = 0
                summaryResults.forEach { res ->
                    res.onSuccess { cams -> faaCams.addAll(cams) }
                    res.onFailure { failedSites++ }
                }
                if (faaCams.isEmpty() && failedSites > 0) {
                    faaError = "FAA WeatherCams returned $hiSiteCount HI sites, but summary cameras failed ($failedSites)."
                } else if (failedSites > 0) {
                    faaError = "$failedSites FAA HI station summaries timed out; partial cams loaded."
                }
            },
            onFailure = { err ->
                faaError = err.localizedMessage ?: "Unable to query api.weathercams.faa.gov/sites"
            }
        )

        HawaiiWebcamsSnapshot(
            updatedAtHst = HawaiiAirspaceReference.formatCurrentHst(),
            faaCams = faaCams,
            usgsCams = usgs,
            uhCams = uh,
            faaError = faaError,
            faaSiteCount = hiSiteCount
        )
    }

    /**
     * Checks reachability of the official Pacific Tsunami Museum Hilo Bay Webcam page ONLY.
     * Never probes or invents undocumented direct stream URLs.
     */
    suspend fun checkHiloCamOfficialPageStatus(): HiloCamStatusProbeResult = withContext(Dispatchers.IO) {
        val checkedAt = HawaiiAirspaceReference.formatCurrentHst()
        val targetUrl = HiloDowntownCamReference.PRIMARY_OFFICIAL_URL
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()

        val startMs = System.currentTimeMillis()
        val probe = runCatching {
            val req = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", FEED_BROWSER_USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build()
            client.newCall(req).execute().use { resp ->
                val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                Pair(resp.code, elapsed)
            }
        }

        probe.fold(
            onSuccess = { (code, elapsed) ->
                val ok = code in 200..299
                val summary = if (ok) {
                    "Official page $targetUrl responded HTTP $code in ${elapsed}ms. Note: embedded cam feeds are frequently bandwidth-limited or offline; verify live image in browser or via search."
                } else {
                    "Official page $targetUrl returned HTTP $code (${elapsed}ms). Museum feed may be offline or throttled; check status via web search."
                }
                HiloCamStatusProbeResult(
                    checkedAtHst = checkedAt,
                    officialUrl = targetUrl,
                    isReachable = ok,
                    httpStatusCode = code,
                    latencyMs = elapsed,
                    summary = summary
                )
            },
            onFailure = { err ->
                HiloCamStatusProbeResult(
                    checkedAtHst = checkedAt,
                    officialUrl = targetUrl,
                    isReachable = false,
                    httpStatusCode = null,
                    latencyMs = null,
                    summary = "Unable to reach $targetUrl (${err.localizedMessage ?: err.javaClass.simpleName}). Feeds are frequently bandwidth-limited or offline; try checking status via search."
                )
            }
        )
    }

    companion object {
        fun createDefaultFaaWeatherCamsApi(): FaaWeatherCamsApi {
            val headerInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", FEED_BROWSER_USER_AGENT)
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Referer", "https://weathercams.faa.gov/")
                    .header("Origin", "https://weathercams.faa.gov")
                    .build()
                chain.proceed(req)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://api.weathercams.faa.gov/")
                .client(client)
                .build()
                .create(FaaWeatherCamsApi::class.java)
        }

        internal fun parseFaaHawaiiSites(jsonText: String): List<FaaSiteInfo> {
            val trimmed = jsonText.trim()
            if (trimmed.isEmpty()) return emptyList()
            if (trimmed.startsWith("<")) {
                throw IllegalStateException("FAA WeatherCams /sites returned HTML/XML instead of JSON.")
            }

            val array: JSONArray = when {
                trimmed.startsWith("[") -> JSONArray(trimmed)
                trimmed.startsWith("{") -> {
                    val root = JSONObject(trimmed)
                    root.optJSONArray("payload")
                        ?: root.optJSONArray("sites")
                        ?: root.optJSONArray("data")
                        ?: return emptyList()
                }
                else -> throw IllegalStateException("Unexpected FAA WeatherCams /sites payload.")
            }

            val sites = mutableListOf<FaaSiteInfo>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val state = obj.optString("state", "").trim()
                val icao = obj.optString("icao", obj.optString("siteIdentifier", "")).trim()
                val isHawaii = state.equals("HI", ignoreCase = true) ||
                    icao.startsWith("PH", ignoreCase = true)
                if (!isHawaii) continue

                if (obj.has("siteActive") && !obj.optBoolean("siteActive", true)) continue

                val siteId = obj.opt("siteId")?.toString()?.trim().orEmpty()
                if (siteId.isEmpty() || siteId == "null") continue

                val lat = obj.optDouble("latitude", Double.NaN)
                val lon = obj.optDouble("longitude", Double.NaN)
                if (lat.isNaN() || lon.isNaN()) continue

                val siteName = obj.optString("siteName", icao.ifBlank { "HI Site $siteId" }).trim()
                val identifier = obj.optString("siteIdentifier", icao).ifBlank { "HI-$siteId" }
                val elev = if (obj.has("elevation") && !obj.isNull("elevation")) obj.optInt("elevation") else null

                sites.add(
                    FaaSiteInfo(
                        siteId = siteId,
                        siteIdentifier = identifier,
                        siteName = siteName,
                        state = state.ifBlank { "HI" },
                        latitude = lat,
                        longitude = lon,
                        elevationFt = elev
                    )
                )
            }
            return sites
        }

        internal fun parseFaaSiteSummaryCameras(
            site: FaaSiteInfo,
            jsonText: String
        ): List<HawaiiWebcamStill> {
            val trimmed = jsonText.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("<")) return emptyList()
            val root = JSONObject(trimmed)
            val payload = root.optJSONObject("payload") ?: root
            val cameras = payload.optJSONArray("cameras") ?: return emptyList()

            val results = mutableListOf<HawaiiWebcamStill>()
            for (i in 0 until cameras.length()) {
                val cam = cameras.optJSONObject(i) ?: continue
                val rawUri = listOf("imageUri", "currentImageUri", "stillUri", "url")
                    .firstNotNullOfOrNull { key ->
                        cam.optString(key, "").trim().takeIf { it.isNotEmpty() && it != "null" }
                    } ?: continue

                val fullUrl = when {
                    rawUri.startsWith("https://") -> rawUri
                    rawUri.startsWith("http://") -> rawUri.replaceFirst("http://", "https://")
                    rawUri.startsWith("//") -> "https:$rawUri"
                    rawUri.startsWith("/") -> "https://weathercams.faa.gov$rawUri"
                    else -> "https://weathercams.faa.gov/$rawUri"
                }

                val camId = cam.opt("cameraId")?.toString()?.ifBlank { null } ?: "${site.siteId}_$i"
                val direction = cam.optString("direction", cam.optString("cameraDirection", ""))
                    .ifBlank { cam.optString("cameraName", "Cam ${i + 1}") }
                    .trim()
                val timestamp = listOf("lastImageTimestamp", "imageDatetime", "timestamp", "captureTime")
                    .firstNotNullOfOrNull { key ->
                        cam.optString(key, "").trim().takeIf { it.isNotEmpty() && it != "null" }
                    }

                val subtitleParts = buildList {
                    add("FAA Aviation WeatherCam (${site.siteIdentifier})")
                    site.elevationFt?.let { add("${it} ft MSL") }
                    add(String.format(Locale.US, "%.3f°, %.3f°", site.latitude, site.longitude))
                }

                results.add(
                    HawaiiWebcamStill(
                        id = "faa_${site.siteId}_$camId",
                        stationCode = site.siteIdentifier,
                        title = "${site.siteName} — $direction",
                        subtitle = subtitleParts.joinToString(" • "),
                        source = WebcamFeedSource.FAA_WEATHERCAM,
                        direction = direction,
                        latitude = site.latitude,
                        longitude = site.longitude,
                        imageUrl = fullUrl,
                        refererUrl = "https://weathercams.faa.gov/",
                        portalUrl = "https://weathercams.faa.gov/",
                        captureTimestamp = timestamp
                    )
                )
            }
            return results
        }
    }
}
