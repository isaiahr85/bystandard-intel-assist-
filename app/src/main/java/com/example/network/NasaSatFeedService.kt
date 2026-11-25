package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// NASA EONET v3 API DTOs
// ---------------------------------------------------------------------------

data class EonetEventsResponse(
    val title: String = "",
    val description: String = "",
    val link: String = "",
    val events: List<EonetEventDto> = emptyList()
)

data class EonetEventDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val link: String = "",
    val closed: String? = null,
    val categories: List<EonetCategoryDto> = emptyList(),
    val sources: List<EonetSourceDto> = emptyList(),
    val geometry: List<EonetGeometryDto> = emptyList()
)

data class EonetCategoryDto(
    val id: String,
    val title: String
)

data class EonetSourceDto(
    val id: String,
    val url: String
)

data class EonetGeometryDto(
    val magnitudeValue: Double? = null,
    val magnitudeUnit: String? = null,
    val magnitudeDescription: String? = null,
    val date: String = "",
    val type: String = "Point",
    val coordinates: Any? = null
)

// ---------------------------------------------------------------------------
// Domain Model for NASA Satellite Feed Events
// ---------------------------------------------------------------------------

data class NasaSatEvent(
    val id: String,
    val title: String,
    val description: String?,
    val isClosed: Boolean,
    val closedDate: String?,
    val categoryId: String,
    val categoryTitle: String,
    val sourceId: String,
    val sourceUrl: String,
    val latestTimestamp: String,
    val latestDateIso: String,
    val latitude: Double,
    val longitude: Double,
    val magnitudeLabel: String?,
    val trackPointCount: Int
)

enum class NasaEonetCategory(val apiId: String?, val label: String) {
    ALL(null, "All Feeds"),
    WILDFIRES("wildfires", "Thermal / Fires"),
    SEVERE_STORMS("severeStorms", "Storms"),
    VOLCANOES("volcanoes", "Volcanoes"),
    FLOODS("floods", "Floods"),
    DUST_HAZE("dustHaze", "Dust & Plumes"),
    SEA_LAKE_ICE("seaLakeIce", "Sea / Ice")
}

enum class NasaGibsLayer(
    val id: String,
    val shortLabel: String,
    val fullTitle: String,
    val instrument: String,
    val layersParam: String,
    val forensicUse: String
) {
    VIIRS_TRUE_COLOR(
        id = "viirs_true",
        shortLabel = "VIIRS RGB",
        fullTitle = "VIIRS SNPP Corrected Reflectance (True Color)",
        instrument = "Suomi NPP / VIIRS (375m-750m)",
        layersParam = "VIIRS_SNPP_CorrectedReflectance_TrueColor,Coastlines_15m",
        forensicUse = "Visible optical baseline, smoke plumes, flood boundaries, and surface change."
    ),
    VIIRS_THERMAL(
        id = "viirs_thermal",
        shortLabel = "Thermal 375m",
        fullTitle = "VIIRS True Color + 375m Thermal Anomalies",
        instrument = "Suomi NPP / VIIRS I-Band Thermal",
        layersParam = "VIIRS_SNPP_CorrectedReflectance_TrueColor,VIIRS_SNPP_ThermalAnomalies_375m_Day,Coastlines_15m",
        forensicUse = "Active fire fronts, industrial explosions, thermal flashes, and exothermic events."
    ),
    VIIRS_FALSE_COLOR(
        id = "viirs_swir",
        shortLabel = "SWIR Burn",
        fullTitle = "VIIRS False Color SWIR (Bands M11-I2-I1)",
        instrument = "Suomi NPP / VIIRS SWIR + NIR",
        layersParam = "VIIRS_SNPP_CorrectedReflectance_BandsM11I2I1,Coastlines_15m",
        forensicUse = "Burn scar delineation, bare soil disruption, and moisture contrast through haze."
    ),
    MODIS_TERRA(
        id = "modis_terra",
        shortLabel = "MODIS Terra",
        fullTitle = "MODIS Terra Corrected Reflectance (True Color)",
        instrument = "EOS Terra / MODIS (250m)",
        layersParam = "MODIS_Terra_CorrectedReflectance_TrueColor,Coastlines_15m",
        forensicUse = "Morning overpass optical imagery for shadow chronolocation cross-checks."
    ),
    MODIS_AQUA(
        id = "modis_aqua",
        shortLabel = "MODIS Aqua",
        fullTitle = "MODIS Aqua Corrected Reflectance (True Color)",
        instrument = "EOS Aqua / MODIS (250m)",
        layersParam = "MODIS_Aqua_CorrectedReflectance_TrueColor,Coastlines_15m",
        forensicUse = "Afternoon overpass optical verification and multi-sensor temporal corroboration."
    ),
    VIIRS_NIGHT(
        id = "viirs_dnb",
        shortLabel = "Day/Night",
        fullTitle = "VIIRS Day/Night Band (Low-Light Radiance)",
        instrument = "Suomi NPP / VIIRS DNB",
        layersParam = "VIIRS_SNPP_DayNightBand_ENCC,Coastlines_15m",
        forensicUse = "Power grid blackouts, nighttime flare plumes, and infrastructure illumination loss."
    )
}

object NasaGibsUrlBuilder {
    fun buildSnapshotUrl(
        latitude: Double,
        longitude: Double,
        dateIso: String,
        layer: NasaGibsLayer,
        spanDegrees: Double = 3.0,
        width: Int = 960,
        height: Int = 620,
        cacheBustNonce: Long = 0L
    ): String {
        val halfSpan = (spanDegrees / 2.0).coerceIn(0.05, 25.0)
        val south = (latitude - halfSpan).coerceIn(-89.5, 89.5)
        val north = (latitude + halfSpan).coerceIn(-89.5, 89.5)
        val west = (longitude - halfSpan).coerceIn(-179.5, 179.5)
        val east = (longitude + halfSpan).coerceIn(-179.5, 179.5)

        return buildBboxSnapshotUrl(
            south = south,
            west = west,
            north = north,
            east = east,
            dateIso = dateIso,
            layer = layer,
            width = width,
            height = height,
            cacheBustNonce = cacheBustNonce
        )
    }

    fun buildBboxSnapshotUrl(
        south: Double,
        west: Double,
        north: Double,
        east: Double,
        dateIso: String,
        layer: NasaGibsLayer,
        width: Int = 960,
        height: Int = 620,
        cacheBustNonce: Long = 0L
    ): String {
        val cleanDate = dateIso.trim().take(10).let {
            if (Regex("\\d{4}-\\d{2}-\\d{2}").matches(it)) it else yesterdayUtcIso()
        }
        val safeSouth = south.coerceIn(-89.5, 89.5)
        val safeNorth = north.coerceIn(-89.5, 89.5)
        val safeWest = west.coerceIn(-179.5, 179.5)
        val safeEast = east.coerceIn(-179.5, 179.5)
        val bbox = String.format(Locale.US, "%.4f,%.4f,%.4f,%.4f", safeSouth, safeWest, safeNorth, safeEast)

        val base = "https://wvs.earthdata.nasa.gov/api/v1/snapshot" +
            "?REQUEST=GetSnapshot" +
            "&TIME=${cleanDate}T00:00:00Z" +
            "&BBOX=$bbox" +
            "&CRS=EPSG:4326" +
            "&LAYERS=${layer.layersParam}" +
            "&FORMAT=image/jpeg" +
            "&WIDTH=$width" +
            "&HEIGHT=$height"
        return if (cacheBustNonce > 0L) "$base&_t=$cacheBustNonce" else base
    }

    fun todayUtcIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }

    fun yesterdayUtcIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(System.currentTimeMillis() - 86_400_000L))
    }

    fun isCurrentOrFutureUtcDate(dateIso: String): Boolean {
        return dateIso.trim().take(10) >= todayUtcIso()
    }

    fun isRecentCompositeWindow(dateIso: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val twoDaysAgo = sdf.format(Date(System.currentTimeMillis() - 2L * 86_400_000L))
        return dateIso.trim().take(10) >= twoDaysAgo
    }
}

// ---------------------------------------------------------------------------
// Retrofit Service Interface & Client
// ---------------------------------------------------------------------------

interface NasaEonetApi {
    @GET("api/v3/events")
    suspend fun getEvents(
        @Query("status") status: String = "open",
        @Query("limit") limit: Int = 25,
        @Query("category") category: String? = null
    ): EonetEventsResponse
}

class NasaSatFeedRepository(
    private val api: NasaEonetApi = createDefaultApi()
) {
    suspend fun fetchEvents(
        category: NasaEonetCategory = NasaEonetCategory.ALL,
        status: String = "open",
        limit: Int = 25
    ): Result<List<NasaSatEvent>> {
        return try {
            val response = api.getEvents(
                status = status,
                limit = limit,
                category = category.apiId
            )
            val mapped = response.events.mapNotNull { dto -> mapDtoToDomain(dto) }
            Result.success(mapped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val EONET_BASE_URL = "https://eonet.gsfc.nasa.gov/"

        fun createDefaultApi(): NasaEonetApi {
            val headerInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "VeriScopeSatFeed/2.6 (Android)")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            val okHttp = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(EONET_BASE_URL)
                .client(okHttp)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NasaEonetApi::class.java)
        }

        internal fun mapDtoToDomain(dto: EonetEventDto): NasaSatEvent? {
            val latestGeo = dto.geometry.lastOrNull() ?: return null
            val (lon, lat) = extractLonLat(latestGeo.type, latestGeo.coordinates) ?: return null
            if (lat.isNaN() || lon.isNaN()) return null
            val cat = dto.categories.firstOrNull()
            val src = dto.sources.firstOrNull()
            val magLabel = if (latestGeo.magnitudeValue != null) {
                val unit = latestGeo.magnitudeUnit ?: ""
                "${latestGeo.magnitudeValue} $unit".trim()
            } else null

            return NasaSatEvent(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                isClosed = dto.closed != null,
                closedDate = dto.closed,
                categoryId = cat?.id ?: "general",
                categoryTitle = cat?.title ?: "Earth Observation",
                sourceId = src?.id ?: "NASA EONET",
                sourceUrl = src?.url ?: dto.link,
                latestTimestamp = latestGeo.date,
                latestDateIso = latestGeo.date.take(10),
                latitude = lat,
                longitude = lon,
                magnitudeLabel = magLabel,
                trackPointCount = dto.geometry.size
            )
        }

        internal fun extractLonLat(type: String, coordinates: Any?): Pair<Double, Double>? {
            val list = coordinates as? List<*> ?: return null
            return when {
                type.equals("Point", ignoreCase = true) && list.size >= 2 -> {
                    val lon = (list[0] as? Number)?.toDouble() ?: return null
                    val lat = (list[1] as? Number)?.toDouble() ?: return null
                    if (lat.isNaN() || lon.isNaN()) null else Pair(lon, lat)
                }
                type.equals("MultiPoint", ignoreCase = true) -> {
                    val pts = list.mapNotNull { item ->
                        val pair = item as? List<*> ?: return@mapNotNull null
                        val lon = (pair.getOrNull(0) as? Number)?.toDouble() ?: return@mapNotNull null
                        val lat = (pair.getOrNull(1) as? Number)?.toDouble() ?: return@mapNotNull null
                        if (lat.isNaN() || lon.isNaN()) null else Pair(lon, lat)
                    }
                    if (pts.isEmpty()) null else pts.last()
                }
                else -> {
                    // Polygon: [[[lon, lat], ...]] -> average first ring
                    val ring = (list.firstOrNull() as? List<*>) ?: return null
                    val points = ring.mapNotNull { pt ->
                        val pair = pt as? List<*> ?: return@mapNotNull null
                        val lon = (pair.getOrNull(0) as? Number)?.toDouble() ?: return@mapNotNull null
                        val lat = (pair.getOrNull(1) as? Number)?.toDouble() ?: return@mapNotNull null
                        if (lat.isNaN() || lon.isNaN()) null else Pair(lon, lat)
                    }
                    if (points.isEmpty()) null
                    else Pair(points.map { it.first }.average(), points.map { it.second }.average())
                }
            }
        }
    }
}
