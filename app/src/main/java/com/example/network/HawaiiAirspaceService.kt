package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.*

// ---------------------------------------------------------------------------
// Domain Models for Hawaiʻi Island Airspace & PTA Monitor
// ---------------------------------------------------------------------------

data class GeoPoint(val lat: Double, val lon: Double)

data class HawaiiAirspaceSite(
    val id: String,
    val name: String,
    val shortCode: String,
    val lat: Double,
    val lon: Double,
    val type: String,
    val airspaceNote: String
)

data class HawaiiTfrPolygon(
    val notamKey: String,
    val title: String,
    val rings: List<List<GeoPoint>>
)

data class HawaiiTfrRecord(
    val notamId: String,
    val type: String,
    val facility: String,
    val state: String,
    val description: String
)

data class HawaiiNwsAlert(
    val id: String,
    val event: String,
    val headline: String,
    val areaDesc: String,
    val severity: String,
    val sent: String
)

data class PtaDrillAdvisory(
    val id: String,
    val dateHst: String,
    val sender: String,
    val subject: String,
    val keywords: List<String>,
    val excerpt: String,
    val impactsSaddleRoad: Boolean,
    val impactsAviationOrUas: Boolean
)

data class HawaiiAirspaceSnapshot(
    val updatedAtHst: String,
    val tfrPolygons: List<HawaiiTfrPolygon> = emptyList(),
    val tfrList: List<HawaiiTfrRecord> = emptyList(),
    val nwsAlerts: List<HawaiiNwsAlert> = emptyList(),
    val tfrFeedError: String? = null,
    val nwsFeedError: String? = null
)

sealed interface AirspaceLoadState {
    data object Idle : AirspaceLoadState
    data object Loading : AirspaceLoadState
    data object Ready : AirspaceLoadState
}

enum class HawaiiDopplerLayer(
    val id: String,
    val shortLabel: String,
    val fullTitle: String,
    val wmsEndpointPath: String,
    val wmsLayerName: String,
    val isVelocityProduct: Boolean,
    val operationalNote: String
) {
    HAWAII_BREF(
        id = "hi_bref",
        shortLabel = "HI Base dBZ",
        fullTitle = "NWS Hawaiʻi MRMS Base Reflectivity (QCD)",
        wmsEndpointPath = "hawaii/hawaii_bref_qcd/ows",
        wmsLayerName = "hawaii_bref_qcd",
        isVelocityProduct = false,
        operationalNote = "Quality-controlled composite precipitation echo across Hawaiʻi Island (alerts for 4 in/hr Pohaku flooding)."
    ),
    HAWAII_CREF(
        id = "hi_cref",
        shortLabel = "HI Comp dBZ",
        fullTitle = "NWS Hawaiʻi MRMS Composite Reflectivity",
        wmsEndpointPath = "hawaii/hawaii_cref_qcd/ows",
        wmsLayerName = "hawaii_cref_qcd",
        isVelocityProduct = false,
        operationalNote = "Maximum column reflectivity highlighting convective storm cores & heavy rain cells."
    ),
    PHWA_BREF(
        id = "phwa_bref",
        shortLabel = "PHWA dBZ",
        fullTitle = "NEXRAD PHWA (South Point) Base Reflectivity",
        wmsEndpointPath = "phwa/phwa_sr_bref/ows",
        wmsLayerName = "phwa_sr_bref",
        isVelocityProduct = false,
        operationalNote = "WSR-88D South Shore Puna/Kaʻū 0.5° beam scanning directly over Orchidland & Kīlauea."
    ),
    PHWA_VEL(
        id = "phwa_vel",
        shortLabel = "PHWA Wind Vel",
        fullTitle = "NEXRAD PHWA (South Point) Base Radial Velocity",
        wmsEndpointPath = "phwa/phwa_sr_bvel/ows",
        wmsLayerName = "phwa_sr_bvel",
        isVelocityProduct = true,
        operationalNote = "Inbound/outbound Doppler radial wind speed — tracks ≥35 mph albizia windfall gusts & sUAS shear."
    ),
    PHKM_BREF(
        id = "phkm_bref",
        shortLabel = "PHKM dBZ",
        fullTitle = "NEXRAD PHKM (Kamuela/Kohala) Base Reflectivity",
        wmsEndpointPath = "phkm/phkm_sr_bref/ows",
        wmsLayerName = "phkm_sr_bref",
        isVelocityProduct = false,
        operationalNote = "North Hawaiʻi WSR-88D coverage across Waimea, Saddle Road & PTA R-3103."
    ),
    PHKM_VEL(
        id = "phkm_vel",
        shortLabel = "PHKM Wind Vel",
        fullTitle = "NEXRAD PHKM (Kamuela/Kohala) Base Radial Velocity",
        wmsEndpointPath = "phkm/phkm_sr_bvel/ows",
        wmsLayerName = "phkm_sr_bvel",
        isVelocityProduct = true,
        operationalNote = "Saddle corridor & R-3103 Doppler wind shear affecting rotary-wing, artillery & sUAS ops."
    )
}

object HawaiiDopplerUrlBuilder {
    fun buildWmsUrl(
        layer: HawaiiDopplerLayer,
        minLon: Double = HawaiiAirspaceReference.MAP_MIN_LON,
        minLat: Double = HawaiiAirspaceReference.MAP_MIN_LAT,
        maxLon: Double = HawaiiAirspaceReference.MAP_MAX_LON,
        maxLat: Double = HawaiiAirspaceReference.MAP_MAX_LAT,
        width: Int = 800,
        height: Int = 600,
        cacheBustNonce: Long = 0L
    ): String {
        val bbox = String.format(Locale.US, "%.4f,%.4f,%.4f,%.4f", minLon, minLat, maxLon, maxLat)
        val base = "https://opengeo.ncep.noaa.gov/geoserver/${layer.wmsEndpointPath}" +
            "?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap" +
            "&LAYERS=${layer.wmsLayerName}" +
            "&STYLES=" +
            "&FORMAT=image/png" +
            "&TRANSPARENT=true" +
            "&SRS=EPSG:4326" +
            "&BBOX=$bbox" +
            "&WIDTH=$width" +
            "&HEIGHT=$height"
        return if (cacheBustNonce > 0L) "$base&_t=$cacheBustNonce" else base
    }
}

// ---------------------------------------------------------------------------
// NWS Response DTOs (api.weather.gov/alerts/active?area=HI)
// ---------------------------------------------------------------------------

data class NwsAlertCollectionDto(
    val features: List<NwsAlertFeatureDto> = emptyList()
)

data class NwsAlertFeatureDto(
    val id: String? = null,
    val properties: NwsAlertPropertiesDto? = null
)

data class NwsAlertPropertiesDto(
    val event: String? = null,
    val headline: String? = null,
    val areaDesc: String? = null,
    val severity: String? = null,
    val sent: String? = null
)

// ---------------------------------------------------------------------------
// Retrofit Interfaces (FAA TFR WFS / JSON & NWS Hawaiʻi)
// ---------------------------------------------------------------------------

interface FaaTfrApi {
    @GET
    suspend fun getWfsGeoJson(@Url url: String): ResponseBody

    @GET("tfrapi/exportTfrList")
    suspend fun getExportTfrList(): ResponseBody
}

interface NwsAlertsApi {
    @GET("alerts/active")
    suspend fun getActiveAlerts(
        @Query("area") area: String = "HI",
        @Header("User-Agent") userAgent: String = "VeriScopeHawaiiAirspaceMonitor/2.6",
        @Header("Accept") accept: String = "application/geo+json, application/json"
    ): NwsAlertCollectionDto
}

// ---------------------------------------------------------------------------
// Repository & Reference Geometry / Keyword Parser
// ---------------------------------------------------------------------------

object HawaiiAirspaceReference {

    const val ORCHIDLAND_LAT = 19.5614
    const val ORCHIDLAND_LON = -155.0131
    const val PHTO_CLASS_D_RADIUS_NM = 4.3
    const val MAP_MIN_LAT = 18.80
    const val MAP_MAX_LAT = 20.38
    const val MAP_MIN_LON = -156.20
    const val MAP_MAX_LON = -154.68

    // Exact Restricted Area R-3103 (Pōhakuloa) boundary from dashboard specification
    val r3103Boundary: List<GeoPoint> = listOf(
        GeoPoint(19.83, -155.68),
        GeoPoint(19.83, -155.45),
        GeoPoint(19.68, -155.45),
        GeoPoint(19.68, -155.68)
    )

    // Simplified Hawaiʻi Island coastline polygon for tactical canvas rendering
    val hawaiiIslandCoastline: List<GeoPoint> = listOf(
        GeoPoint(20.27, -155.83), // Upolu Pt
        GeoPoint(20.14, -155.56), // Waipiʻo
        GeoPoint(20.02, -155.31), // Honokaʻa
        GeoPoint(19.90, -155.12), // Laupāhoehoe
        GeoPoint(19.74, -155.07), // Hilo Bay
        GeoPoint(19.52, -154.81), // Kapoho / Kumukahi
        GeoPoint(19.35, -154.98), // Kalapana
        GeoPoint(19.16, -155.47), // Punaluʻu
        GeoPoint(18.91, -155.68), // Ka Lae (South Point)
        GeoPoint(19.18, -155.90), // Miloliʻi
        GeoPoint(19.48, -155.94), // Kealakekua
        GeoPoint(19.64, -155.99), // Kailua-Kona
        GeoPoint(19.84, -156.01), // Keāhole / PHKO
        GeoPoint(20.03, -155.83), // Kawaihae
        GeoPoint(20.27, -155.83)
    )

    // Key arterial & military transit routes (Saddle Rd / DKI Hwy & Hwy 130)
    val saddleRoadPath: List<GeoPoint> = listOf(
        GeoPoint(19.72, -155.09),
        GeoPoint(19.69, -155.34),
        GeoPoint(19.75, -155.54),
        GeoPoint(19.83, -155.72),
        GeoPoint(20.02, -155.67)
    )

    val hwy130Path: List<GeoPoint> = listOf(
        GeoPoint(19.71, -155.06),
        GeoPoint(19.62, -155.04),
        GeoPoint(19.5614, -155.0131), // Orchidland Estates
        GeoPoint(19.49, -154.95)
    )

    val keySites: List<HawaiiAirspaceSite> = listOf(
        HawaiiAirspaceSite(
            id = "site_r3103",
            name = "Pōhakuloa Training Area (R-3103)",
            shortCode = "R-3103",
            lat = 19.756,
            lon = -155.533,
            type = "Military SUA",
            airspaceNote = "Active live-fire Special Use Airspace (surface to 30,000 ft MSL when hot)."
        ),
        HawaiiAirspaceSite(
            id = "site_phsf",
            name = "Bradshaw Army Airfield (PHSF)",
            shortCode = "PHSF",
            lat = 19.760,
            lon = -155.554,
            type = "Military Airfield",
            airspaceNote = "Rotary-wing, UAS & cargo operations supporting PTA training."
        ),
        HawaiiAirspaceSite(
            id = "site_phto",
            name = "Hilo International (PHTO)",
            shortCode = "PHTO",
            lat = 19.720,
            lon = -155.048,
            type = "Commercial Airport",
            airspaceNote = "Class D surface area (4.3 NM radius); Part 107 requires LAANC inside ring."
        ),
        HawaiiAirspaceSite(
            id = "site_phko",
            name = "Ellison Onizuka Kona Intl (PHKO)",
            shortCode = "PHKO",
            lat = 19.738,
            lon = -156.045,
            type = "Commercial Airport",
            airspaceNote = "West Hawaiʻi Class D / TRSA commercial hub."
        ),
        HawaiiAirspaceSite(
            id = "site_phmu",
            name = "Waimea-Kohala (PHMU)",
            shortCode = "PHMU",
            lat = 20.001,
            lon = -155.668,
            type = "Airport",
            airspaceNote = "North Hawaiʻi commuter & military transit corridor."
        ),
        HawaiiAirspaceSite(
            id = "site_hvo",
            name = "Kīlauea Summit / HVO",
            shortCode = "KĪLAUEA",
            lat = 19.406,
            lon = -155.283,
            type = "Volcano Observatory",
            airspaceNote = "HVNP TFR & USGS volcanic plume / eruption monitoring zone."
        ),
        HawaiiAirspaceSite(
            id = "site_orchidland",
            name = "Orchidland Estates (Puna)",
            shortCode = "ORCHID",
            lat = ORCHIDLAND_LAT,
            lon = ORCHIDLAND_LON,
            type = "DICM Ops Sector",
            airspaceNote = "Class G uncontrolled airspace (<400 ft AGL); outside PHTO 4.3 NM Class D ring."
        ),
        HawaiiAirspaceSite(
            id = "site_phwa",
            name = "NEXRAD PHWA (South Point)",
            shortCode = "PHWA",
            lat = 19.0947,
            lon = -155.5689,
            type = "WSR-88D Doppler",
            airspaceNote = "Primary South Hawaiʻi WSR-88D Doppler station (1,381 ft MSL) scanning Puna & Kīlauea."
        ),
        HawaiiAirspaceSite(
            id = "site_phkm",
            name = "NEXRAD PHKM (Kamuela/Kohala)",
            shortCode = "PHKM",
            lat = 20.1253,
            lon = -155.7781,
            type = "WSR-88D Doppler",
            airspaceNote = "North Hawaiʻi WSR-88D Doppler station (3,816 ft MSL) covering PTA R-3103 & Saddle."
        )
    )

    fun distanceNauticalMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusNm = 3440.065
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusNm * c
    }

    fun formatCurrentHst(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss 'HST'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Pacific/Honolulu")
        return sdf.format(Date())
    }
}

object PtaAdvisoryParser {
    private val keywordRegex = Regex(
        "(live[- ]fire|artillery|mortar|convoy|Saddle Road|Daniel K\\. Inouye|helicopter|UAS|drone|R-?3103|Bradshaw)",
        RegexOption.IGNORE_CASE
    )

    fun parseAdvisory(
        subject: String,
        sender: String,
        bodyText: String,
        dateHst: String = HawaiiAirspaceReference.formatCurrentHst(),
        id: String = "pta_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
    ): PtaDrillAdvisory {
        val normalized = bodyText.replace(Regex("\\s+"), " ").trim()
        val matches = keywordRegex.findAll(normalized)
            .map { it.value.uppercase(Locale.US) }
            .distinct()
            .toList()
        val finalKeywords = matches.ifEmpty { listOf("GENERAL ADVISORY") }
        val excerpt = if (normalized.length > 280) {
            normalized.substring(0, 280) + "..."
        } else {
            normalized
        }

        val impactsSaddle = finalKeywords.any {
            it.contains("CONVOY") || it.contains("SADDLE") || it.contains("INOUYE")
        }
        val impactsAviation = finalKeywords.any {
            it.contains("HELICOPTER") || it.contains("UAS") || it.contains("DRONE") ||
                it.contains("R-3103") || it.contains("R3103") || it.contains("BRADSHAW")
        }

        return PtaDrillAdvisory(
            id = id,
            dateHst = dateHst,
            sender = sender.ifBlank { "Manual / USAG Hawaiʻi Advisory Intake" },
            subject = subject.ifBlank { "(No Subject)" },
            keywords = finalKeywords,
            excerpt = excerpt.ifBlank { "No body text provided." },
            impactsSaddleRoad = impactsSaddle,
            impactsAviationOrUas = impactsAviation
        )
    }
}

class HawaiiAirspaceRepository(
    private val faaApi: FaaTfrApi = createDefaultFaaApi(),
    private val nwsApi: NwsAlertsApi = createDefaultNwsApi()
) {
    suspend fun fetchSnapshot(): HawaiiAirspaceSnapshot = coroutineScope {
        val wfsDeferred = async {
            runCatching {
                val wfsUrl = "https://tfr.faa.gov/geoserver/TFR/ows" +
                    "?service=WFS&version=1.1.0&request=GetFeature" +
                    "&typeName=TFR:V_TFR_LOC" +
                    "&outputFormat=application/json" +
                    "&bbox=-161.0,18.5,-154.5,22.5,EPSG:4326"
                val raw = faaApi.getWfsGeoJson(wfsUrl).string()
                parseTfrGeoJsonPolygons(raw)
            }
        }
        val listDeferred = async {
            runCatching {
                val raw = faaApi.getExportTfrList().string()
                filterHawaiiTfrs(raw)
            }
        }
        val nwsDeferred = async {
            runCatching {
                val dto = nwsApi.getActiveAlerts(area = "HI")
                dto.features.mapNotNull { feature ->
                    val props = feature.properties ?: return@mapNotNull null
                    HawaiiNwsAlert(
                        id = feature.id ?: "${props.event}-${props.sent}",
                        event = props.event?.ifBlank { "Alert" } ?: "Alert",
                        headline = props.headline ?: "",
                        areaDesc = props.areaDesc ?: "Hawaiʻi",
                        severity = props.severity ?: "Unknown",
                        sent = props.sent ?: ""
                    )
                }
            }
        }

        val wfsRes = wfsDeferred.await()
        val listRes = listDeferred.await()
        val nwsRes = nwsDeferred.await()

        val faaErrors = listOfNotNull(
            listRes.exceptionOrNull()?.let { "TFR List: ${it.localizedMessage ?: it.javaClass.simpleName}" },
            wfsRes.exceptionOrNull()?.let { "WFS Polygons: ${it.localizedMessage ?: it.javaClass.simpleName}" }
        )
        val faaErr = faaErrors.takeIf { it.isNotEmpty() }?.joinToString(" • ")
        val nwsErr = nwsRes.exceptionOrNull()?.localizedMessage

        HawaiiAirspaceSnapshot(
            updatedAtHst = HawaiiAirspaceReference.formatCurrentHst(),
            tfrPolygons = wfsRes.getOrDefault(emptyList()),
            tfrList = listRes.getOrDefault(emptyList()),
            nwsAlerts = nwsRes.getOrDefault(emptyList()),
            tfrFeedError = faaErr,
            nwsFeedError = nwsErr
        )
    }

    companion object {
        fun createDefaultFaaApi(): FaaTfrApi {
            val headerInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; VeriScope) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "application/json, text/javascript, */*; q=0.8")
                    .header("Referer", "https://tfr.faa.gov/")
                    .build()
                chain.proceed(req)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            return Retrofit.Builder()
                .baseUrl("https://tfr.faa.gov/")
                .client(client)
                .build()
                .create(FaaTfrApi::class.java)
        }

        fun createDefaultNwsApi(): NwsAlertsApi {
            val headerInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "VeriScopeHawaiiAirspaceMonitor/2.6")
                    .header("Accept", "application/geo+json, application/json")
                    .build()
                chain.proceed(req)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.weather.gov/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NwsAlertsApi::class.java)
        }

        internal fun filterHawaiiTfrs(jsonArrayText: String): List<HawaiiTfrRecord> {
            val trimmed = jsonArrayText.trim()
            if (trimmed.isEmpty()) return emptyList()
            if (trimmed.startsWith("<")) {
                throw IllegalStateException("FAA TFR API returned HTML/XML error response.")
            }
            if (!trimmed.startsWith("[")) {
                throw IllegalStateException("Unexpected FAA TFR JSON format.")
            }
            val array = JSONArray(trimmed)
            val results = mutableListOf<HawaiiTfrRecord>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val state = obj.optString("state", "").trim()
                val facility = obj.optString("facility", "").trim()
                if (state.equals("HI", ignoreCase = true) || facility.equals("ZHN", ignoreCase = true)) {
                    results.add(
                        HawaiiTfrRecord(
                            notamId = obj.optString("notam_id", "TFR-$i"),
                            type = obj.optString("type", "TFR").ifBlank { "TFR" },
                            facility = facility.ifBlank { "ZHN" },
                            state = state.ifBlank { "HI" },
                            description = obj.optString("description", "")
                        )
                    )
                }
            }
            return results
        }

        internal fun parseTfrGeoJsonPolygons(geoJsonText: String): List<HawaiiTfrPolygon> {
            val trimmed = geoJsonText.trim()
            if (trimmed.isEmpty()) return emptyList()
            if (trimmed.startsWith("<")) {
                throw IllegalStateException("FAA WFS GeoServer returned XML/HTML exception.")
            }
            if (!trimmed.startsWith("{")) {
                throw IllegalStateException("Unexpected FAA WFS GeoJSON payload.")
            }
            val root = JSONObject(trimmed)
            val features = root.optJSONArray("features") ?: return emptyList()
            val polygons = mutableListOf<HawaiiTfrPolygon>()
            for (i in 0 until features.length()) {
                val feat = features.optJSONObject(i) ?: continue
                val props = feat.optJSONObject("properties")
                val notamKey = props?.optString("NOTAM_KEY")?.ifBlank { null } ?: "FAA TFR #${i + 1}"
                val title = props?.optString("TITLE")?.ifBlank { null } ?: "Temporary Flight Restriction"
                val geom = feat.optJSONObject("geometry") ?: continue
                val geomType = geom.optString("type", "")
                val coords = geom.optJSONArray("coordinates") ?: continue

                val rings = mutableListOf<List<GeoPoint>>()
                when {
                    geomType.equals("Polygon", ignoreCase = true) -> {
                        extractRingFromPolygonArray(coords)?.let { rings.add(it) }
                    }
                    geomType.equals("MultiPolygon", ignoreCase = true) -> {
                        for (p in 0 until coords.length()) {
                            val polyArr = coords.optJSONArray(p) ?: continue
                            extractRingFromPolygonArray(polyArr)?.let { rings.add(it) }
                        }
                    }
                }
                if (rings.isNotEmpty()) {
                    polygons.add(HawaiiTfrPolygon(notamKey = notamKey, title = title, rings = rings))
                }
            }
            return polygons
        }

        private fun extractRingFromPolygonArray(polygonCoords: JSONArray): List<GeoPoint>? {
            val outerRing = polygonCoords.optJSONArray(0) ?: return null
            val pts = mutableListOf<GeoPoint>()
            for (i in 0 until outerRing.length()) {
                val pt = outerRing.optJSONArray(i) ?: continue
                if (pt.length() >= 2) {
                    val lon = pt.optDouble(0, Double.NaN)
                    val lat = pt.optDouble(1, Double.NaN)
                    if (!lat.isNaN() && !lon.isNaN()) {
                        pts.add(GeoPoint(lat = lat, lon = lon))
                    }
                }
            }
            return pts.takeIf { it.size >= 3 }
        }
    }
}
