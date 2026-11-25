package com.example.tools

import java.util.Locale
import kotlin.math.*

object SunChronolocationCalculator {

    data class ChronoResult(
        val sunElevationDeg: Double,
        val sunZenithDeg: Double,
        val morningEstimateUtc: String,
        val afternoonEstimateUtc: String,
        val estimatedHourDecimal: Double,
        val shadowRatio: Double,
        val azimuthDirection: String,
        val solarTimeConfidence: Double
    )

    fun calculate(
        objectHeightMeters: Double,
        shadowLengthMeters: Double,
        latitudeDeg: Double,
        longitudeDeg: Double,
        dayOfYear: Int = 226 // August 14 default
    ): ChronoResult {
        val safeHeight = if (objectHeightMeters <= 0) 1.0 else objectHeightMeters
        val safeShadow = if (shadowLengthMeters <= 0) 0.1 else shadowLengthMeters

        val elevationRad = atan(safeHeight / safeShadow)
        val elevationDeg = Math.toDegrees(elevationRad)
        val zenithDeg = 90.0 - elevationDeg
        val shadowRatio = safeShadow / safeHeight

        // Approximate solar declination: delta = 23.45 * sin(360/365 * (d - 81))
        val declinationDeg = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81.0)))
        val decRad = Math.toRadians(declinationDeg)
        val phiRad = Math.toRadians(latitudeDeg.coerceIn(-89.5, 89.5))

        // Hour angle H from cos(zenith) = sin(phi)*sin(delta) + cos(phi)*cos(delta)*cos(H)
        val denom = (cos(phiRad) * cos(decRad)).let { if (abs(it) < 1e-6) 1e-6 else it }
        val cosH = (cos(elevationRad) - sin(phiRad) * sin(decRad)) / denom
        val clampedCosH = if (cosH.isNaN()) 0.0 else cosH.coerceIn(-1.0, 1.0)
        val hourAngleDeg = Math.toDegrees(acos(clampedCosH))
        val hoursFromSolarNoon = hourAngleDeg / 15.0

        // Approximate solar noon at this longitude, wrapped cleanly into [0, 24) UTC
        val solarNoonUtc = 12.0 - (longitudeDeg / 15.0)
        fun wrapHour(h: Double): Double = ((h % 24.0) + 24.0) % 24.0
        val morningHour = wrapHour(solarNoonUtc - hoursFromSolarNoon)
        val afternoonHour = wrapHour(solarNoonUtc + hoursFromSolarNoon)

        fun formatHour(h: Double): String {
            val totalSeconds = ((wrapHour(h) * 3600.0).roundToInt()) % 86400
            val hour = totalSeconds / 3600
            val minute = (totalSeconds % 3600) / 60
            val second = totalSeconds % 60
            return String.format(Locale.US, "%02d:%02d:%02d UTC", hour, minute, second)
        }

        return ChronoResult(
            sunElevationDeg = (elevationDeg * 10).roundToInt() / 10.0,
            sunZenithDeg = (zenithDeg * 10).roundToInt() / 10.0,
            morningEstimateUtc = formatHour(morningHour),
            afternoonEstimateUtc = formatHour(afternoonHour),
            estimatedHourDecimal = morningHour,
            shadowRatio = (shadowRatio * 100).roundToInt() / 100.0,
            azimuthDirection = if (elevationDeg > 45) "High Solar Altitude (Nadir Proximity)" else "Low Solar Angle (Morning / Late Afternoon)",
            solarTimeConfidence = (96.5 - (abs(latitudeDeg) * 0.05)).coerceIn(80.0, 99.0)
        )
    }
}

object ErrorLevelAnalysisEngine {

    data class ElaAssessment(
        val globalCompressionRatio: String,
        val anomalyScorePercent: Int,
        val quantizationUniformity: String,
        val isTamperingSuspected: Boolean,
        val flaggedRegions: List<String>,
        val containerAnalysis: ContainerAnalysis,
        val c2paAudit: C2paAudit
    )

    data class ContainerAnalysis(
        val atomStructure: String,
        val isRawUnedited: Boolean,
        val encoderSignature: String,
        val audioVideoSyncOffsetMs: Int
    )

    data class C2paAudit(
        val isManifestPresent: Boolean,
        val signerIdentity: String,
        val certValidity: String,
        val assertionCount: Int
    )

    fun evaluate(preset: String): ElaAssessment {
        return when (preset) {
            "SPLICED_MUNITION" -> ElaAssessment(
                globalCompressionRatio = "JPEG Q=75 (Re-saved with inconsistent 8x8 DCT matrix)",
                anomalyScorePercent = 84,
                quantizationUniformity = "Non-uniform: High error delta detected around projectile casing",
                isTamperingSuspected = true,
                flaggedRegions = listOf(
                    "Bounding box [x:140, y:220, w:310, h:180]: DCT luminance variance exceeds 4.8 sigma",
                    "Edge boundary artifact: Ghost halo indicating lasso cut-and-paste compositing",
                    "Quantization mismatch: Munition element compressed at Q=92 while background is Q=70"
                ),
                containerAnalysis = ContainerAnalysis(
                    atomStructure = "moov atom positioned at end of file; secondary encoder metadata present",
                    isRawUnedited = false,
                    encoderSignature = "Lavf / ffmpeg re-encode stream detected",
                    audioVideoSyncOffsetMs = 120
                ),
                c2paAudit = C2paAudit(
                    isManifestPresent = false,
                    signerIdentity = "None (Cryptographic provenance absent)",
                    certValidity = "N/A - Unsigned Media",
                    assertionCount = 0
                )
            )
            "STRIPPED_EXIF" -> ElaAssessment(
                globalCompressionRatio = "WebP / JPEG Q=82 (Standard social media transcode)",
                anomalyScorePercent = 38,
                quantizationUniformity = "Uniform re-compression across all channels",
                isTamperingSuspected = false,
                flaggedRegions = listOf(
                    "All original EXIF GPS & Shutter metadata completely stripped",
                    "Color profile normalized to sRGB IEC61966-2.1",
                    "Chroma subsampling downsampled to 4:2:0"
                ),
                containerAnalysis = ContainerAnalysis(
                    atomStructure = "Single web container stream; metadata stripped by social CDN",
                    isRawUnedited = false,
                    encoderSignature = "Cloudflare / CDN Transcoder Signature",
                    audioVideoSyncOffsetMs = 0
                ),
                c2paAudit = C2paAudit(
                    isManifestPresent = false,
                    signerIdentity = "None",
                    certValidity = "Untrusted Origin",
                    assertionCount = 0
                )
            )
            else -> ElaAssessment(
                globalCompressionRatio = "Direct Camera Raw / JPEG Fine Q=98",
                anomalyScorePercent = 6,
                quantizationUniformity = "Pristine Uniformity: Consistent noise floor across all pixel blocks",
                isTamperingSuspected = false,
                flaggedRegions = listOf(
                    "No anomalous error clustering detected",
                    "Sensor PRNU (Photo-Response Non-Uniformity) perfectly matches camera lens signature",
                    "EXIF timestamp matches GPS atomic clock synchronizer (+/- 0.2s)"
                ),
                containerAnalysis = ContainerAnalysis(
                    atomStructure = "Pristine MP4/MOV: ftyp -> moov -> mvhd -> trak -> mdat hierarchy",
                    isRawUnedited = true,
                    encoderSignature = "Native Hardware Camera ISP Engine",
                    audioVideoSyncOffsetMs = 0
                ),
                c2paAudit = C2paAudit(
                    isManifestPresent = true,
                    signerIdentity = "Verified Press Agency / Secure Hardware Enclave (Cert ID: C2PA-9981-EU)",
                    certValidity = "Valid Cryptographic Signature (SHA-256 with Ed25519)",
                    assertionCount = 5
                )
            )
        }
    }
}

object BallisticMetrologyEngine {

    data class ComparisonResult(
        val correlationScorePercent: Double,
        val congruentMatchingCells: Int,
        val totalCellsEvaluated: Int,
        val meanRoughnessRaDeltaUm: Double,
        val conclusion: String,
        val isPositiveMatch: Boolean
    )

    fun compare(casingTypeA: String, casingTypeB: String): ComparisonResult {
        return if (casingTypeA == casingTypeB) {
            ComparisonResult(
                correlationScorePercent = 94.8,
                congruentMatchingCells = 48,
                totalCellsEvaluated = 52,
                meanRoughnessRaDeltaUm = 0.04,
                conclusion = "Positive Identification: High-density congruent striations exceed AFTE criteria for unique toolmark origin.",
                isPositiveMatch = true
            )
        } else {
            ComparisonResult(
                correlationScorePercent = 38.2,
                congruentMatchingCells = 5,
                totalCellsEvaluated = 52,
                meanRoughnessRaDeltaUm = 0.82,
                conclusion = "Exclusion: Incongruent breech face milling pattern and discordant firing pin shear geometries.",
                isPositiveMatch = false
            )
        }
    }

    fun generateWaveformPoints(isMatch: Boolean): Pair<List<Float>, List<Float>> {
        val pointsA = mutableListOf<Float>()
        val pointsB = mutableListOf<Float>()
        for (i in 0 until 50) {
            val base = sin(i * 0.4f) * 0.5f + cos(i * 0.8f) * 0.3f
            pointsA.add(base + (sin(i * 1.5f) * 0.15f))
            if (isMatch) {
                // Highly similar with subtle sensor noise
                pointsB.add(base + (sin(i * 1.5f) * 0.12f) + (cos(i * 2f) * 0.04f))
            } else {
                // Completely discordant waveform
                pointsB.add(cos(i * 0.6f) * 0.6f + sin(i * 1.2f) * 0.4f)
            }
        }
        return Pair(pointsA, pointsB)
    }
}

object AiVectorSimulator {

    data class VectorSearchResult(
        val title: String,
        val category: String,
        val similarityScore: Double,
        val snippet: String,
        val metadataMatch: String
    )

    fun search(query: String): List<VectorSearchResult> {
        val q = query.lowercase().trim()
        val allResults = listOf(
            VectorSearchResult(
                title = "UNOSAT Structural Damage Archive - Grade 4 Infrastructure",
                category = "Damage Polygons & GIS",
                similarityScore = if (q.contains("bridge") || q.contains("collapse") || q.contains("urban")) 0.96 else 0.78,
                snippet = "Classified vector GIS polygons detailing structural catastrophic failure, span fracture, and secondary crater analysis.",
                metadataMatch = "GDACS / UNOSAT Damage Schema, STAC item bbox: [30.71, 46.47, 30.74, 46.50]"
            ),
            VectorSearchResult(
                title = "Copernicus Sentinel-1 SAR Coherence Loss Mapping",
                category = "GEOINT & Radar",
                similarityScore = if (q.contains("sar") || q.contains("fire") || q.contains("damage") || q.contains("satellite")) 0.94 else 0.72,
                snippet = "Interferometric SAR phase change identifying non-visible structural damage and debris distribution across urban grid.",
                metadataMatch = "C-band 5.405GHz, Descending Pass 142, InSAR coherence drop delta: 0.76"
            ),
            VectorSearchResult(
                title = "NIST Ballistics Toolmark (NBTRD) - 3D Surface Striations",
                category = "Ballistics & Metrology",
                similarityScore = if (q.contains("ballistic") || q.contains("casing") || q.contains("bullet") || q.contains("firearm")) 0.95 else 0.64,
                snippet = "Laser confocal 3D topography scans of fired cartridge cases and bullet lands with micrometer-scale surface roughness.",
                metadataMatch = "ISO 5436-2 X3P Point Cloud, Ra: 1.42 µm, AFTE standard criteria"
            ),
            VectorSearchResult(
                title = "Bellingcat Open-Source Chronolocation & UGC Repository",
                category = "OSINT & Incident Documentation",
                similarityScore = if (q.contains("shadow") || q.contains("video") || q.contains("osint") || q.contains("strike")) 0.93 else 0.69,
                snippet = "Multi-source video atom analysis, shadow angle ephemeris calculations, and flight trajectory corroboration dossiers.",
                metadataMatch = "Dublin Core, Six-decimal coordinate logs, SunCalc solar elevation matching"
            ),
            VectorSearchResult(
                title = "CAS Chemical Safety Library (CSL) - Hazardous Reactivity",
                category = "CBRN & Chemical Safety",
                similarityScore = if (q.contains("chemical") || q.contains("chlorine") || q.contains("hazard") || q.contains("gas")) 0.97 else 0.61,
                snippet = "Thermal runaway threshold matrices, SMILES notation, and toxic inhalation dispersion modeling.",
                metadataMatch = "CAS RN: 7782-50-5, InChIKey: ZLMJMSJWJFRGJR-UHFFFAOYSA-N, GHS Hazard 2.3"
            )
        )

        return allResults.sortedByDescending { it.similarityScore }
    }

    fun simulateLmmExtraction(rawText: String): String {
        val clean = rawText.replace(Regex("\\s+"), " ").trim()
        val lower = clean.lowercase(Locale.US)

        // Extract explicit lat, lon pair if provided, else detect Hawaii/Orchidland context or default waterfront
        val coordMatch = Regex("(-?\\d{1,2}\\.\\d{2,6})\\s*,\\s*(-?\\d{1,3}\\.\\d{2,6})").find(clean)
        val (lat, lon) = when {
            coordMatch != null -> {
                val parsedLat = coordMatch.groupValues[1].toDoubleOrNull()?.coerceIn(-89.9, 89.9) ?: 46.4825
                val parsedLon = coordMatch.groupValues[2].toDoubleOrNull()?.coerceIn(-179.9, 179.9) ?: 30.7233
                parsedLat to parsedLon
            }
            lower.contains("orchidland") || lower.contains("puna") || lower.contains("hilo") ||
                lower.contains("pohakuloa") || lower.contains("saddle") -> 19.5614 to -155.0131
            else -> 46.4825 to 30.7233
        }
        val d = 0.0022
        val polyCoords = String.format(
            Locale.US,
            "[[[%.4f, %.4f], [%.4f, %.4f], [%.4f, %.4f], [%.4f, %.4f], [%.4f, %.4f]]]",
            lon - d, lat - d, lon + d, lat - d, lon + d, lat + d, lon - d, lat + d, lon - d, lat - d
        )

        val scope = when {
            lower.contains("chemical") || lower.contains("chlorine") || lower.contains("vog") || lower.contains("plume") -> "CBRN_ATMOSPHERIC_HAZARD"
            lower.contains("flood") || lower.contains("storm") || lower.contains("volcano") || lower.contains("eruption") -> "DISASTER_GEOPHYSICAL_MONITOR"
            lower.contains("convoy") || lower.contains("orchidland") || lower.contains("puna") || lower.contains("uas") -> "HYBRID_CYBER_PHYSICAL_OPS"
            else -> "URBAN_CONFLICT_IMPACT"
        }

        val indicator = when {
            lower.contains("glide") || lower.contains("umpk") -> "Precision Glide Bomb with UMPK guidance"
            lower.contains("artillery") || lower.contains("155mm") || lower.contains("mortar") -> "Artillery / Indirect Live-Fire Signature"
            lower.contains("uas") || lower.contains("drone") -> "Unmanned Aircraft System (sUAS) Telemetry"
            lower.contains("convoy") || lower.contains("road") -> "Ground Convoy / Transit Corridor Disruption"
            lower.contains("lava") || lower.contains("thermal") || lower.contains("fire") -> "Thermal Anomaly / Exothermic Signature"
            else -> "Unclassified Multimodal Field Indicator"
        }

        val sensors = buildList {
            if (lower.contains("sar") || lower.contains("insar") || lower.contains("sentinel")) add("\"Sentinel-1 SAR InSAR Coherence\"")
            if (lower.contains("viirs") || lower.contains("gibs") || lower.contains("thermal")) add("\"NASA VIIRS 375m Thermal / GIBS\"")
            if (lower.contains("video") || lower.contains("atom") || lower.contains("footage")) add("\"Eyewitness Video (Atom Container Audit)\"")
            if (lower.contains("doppler") || lower.contains("radar") || lower.contains("phwa")) add("\"NOAA NEXRAD WSR-88D Doppler\"")
            if (lower.contains("seismic") || lower.contains("hvo")) add("\"USGS Ground Seismic Telemetry\"")
            if (isEmpty()) {
                add("\"Sentinel-1 SAR InSAR Coherence\"")
                add("\"Ground Field SITREP Corroboration\"")
            }
        }

        val escapedSummary = (if (clean.isEmpty()) "No field narrative supplied." else clean.take(180))
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")

        val stacId = "STAC-EXTRACT-${(clean.hashCode().let { if (it == Int.MIN_VALUE) 0 else abs(it) } % 9000) + 1000}"

        return """
{
  "stac_version": "1.0.0",
  "id": "$stacId",
  "type": "Feature",
  "geometry": {
    "type": "Polygon",
    "coordinates": $polyCoords
  },
  "properties": {
    "datetime": "2026-08-14T06:42:15Z",
    "operational_scope": "$scope",
    "signature_indicator": "$indicator",
    "corroboration_score": ${if (sensors.size >= 2) 0.94 else 0.78},
    "narrative_excerpt": "$escapedSummary",
    "sensors_triangulated": [
      ${sensors.joinToString(",\n      ")}
    ]
  },
  "assets": {
    "sar_tile": {
      "href": "https://dataspace.copernicus.eu/stac/tiles/s1_coherence.cog",
      "type": "image/tiff; application=geotiff",
      "roles": ["damage_mapping", "insar_interferogram"]
    }
  }
}
        """.trimIndent()
    }
}
