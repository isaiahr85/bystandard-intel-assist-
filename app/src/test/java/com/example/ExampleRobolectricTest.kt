package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.DicmSnaEngine
import com.example.data.OrchidlandDicmData
import com.example.model.*
import com.example.network.*
import com.example.tools.AccessibilityAudioManager
import com.example.tools.AiVectorSimulator
import com.example.tools.AudioSpeechRate
import com.example.tools.BallisticMetrologyEngine
import com.example.tools.ErrorLevelAnalysisEngine
import com.example.tools.SunChronolocationCalculator
import com.example.tools.TextScalePreset
import com.example.ui.ForensicViewModel
import com.example.ui.FullScreenMapDomain
import com.example.ui.IntelSubMode
import com.example.ui.NavigationTab
import com.example.ui.theme.contrastContentColor
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `app launches on Orchidland DICM Dashboard and navigates all tabs`() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("main_navigation_bar").assertExists()
    composeTestRule.onNodeWithTag("orchidland_hero_card").assertExists()
    composeTestRule.onNodeWithTag("hero_profile_agent_aut").assertExists()
    composeTestRule.onNodeWithTag("hero_platform_plat_whatsapp").assertExists()

    // Navigate all 4 Orchidland DICM Dashboard phase tabs
    composeTestRule.onNodeWithTag("dicm_tab_identify").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("agent_profile_agent_srx").assertExists()

    composeTestRule.onNodeWithTag("dicm_tab_correlate").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sna_metrics_card").assertExists()
    composeTestRule.onNodeWithTag("sna_sever_bridge_btn").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("dicm_tab_mitigate").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nist_toggle_nist_rs").assertExists()

    composeTestRule.onNodeWithTag("dicm_tab_airspace").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_header_card").assertExists()
    composeTestRule.onNodeWithTag("hawaii_airspace_map_card").assertExists()
    composeTestRule.onNodeWithTag("airspace_zoom_in_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_zoom_readout").assertExists()
    composeTestRule.onNodeWithTag("airspace_overlay_options_row").assertExists()
    composeTestRule.onNodeWithTag("airspace_scroll_lock_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_scroll_lock_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("doppler_wms_overlay").assertExists()
    composeTestRule.onNodeWithTag("doppler_status_badge").assertExists()
    composeTestRule.onNodeWithTag("airspace_sat_overlay").assertExists()
    composeTestRule.onNodeWithTag("airspace_sat_status_badge").assertExists()
    composeTestRule.onNodeWithTag("overlay_opt_satellite").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("overlay_opt_webcams").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("airspace_sat_layers_row").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("airspace_sat_layer_viirs_thermal").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_sat_date_-1").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_sat_toggle_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_sat_toggle_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_open_full_sat_btn").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("doppler_layer_phwa_vel").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("doppler_color_scale").assertExists()
    composeTestRule.onNodeWithTag("doppler_toggle_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Live Hawaiʻi Still Cams (FAA + USGS HVO + UH System) filter & lightbox verification
    composeTestRule.onNodeWithTag("hawaii_webcams_section").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("cam_filter_usgs").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("webcams_carousel_row").performScrollTo()
    composeTestRule.onNodeWithTag("webcams_carousel_row")
      .performScrollToNode(hasTestTag("webcam_card_usgs_pgcam"))
    composeTestRule.onNodeWithTag("webcam_card_usgs_pgcam").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("webcam_lightbox_dialog").assertExists()
    composeTestRule.onNodeWithTag("webcam_lightbox_close").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("cam_filter_uh").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("webcams_carousel_row").performScrollTo()
    composeTestRule.onNodeWithTag("webcams_carousel_row")
      .performScrollToNode(hasTestTag("webcam_card_uh_irtf_c166"))
    composeTestRule.onNodeWithTag("webcam_card_uh_irtf_c166").assertExists()
    composeTestRule.onNodeWithTag("cam_filter_all").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Navigate remaining 4 bottom navigation tabs
    composeTestRule.onNodeWithTag("nav_sat_feed").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gibs_imagery_card").assertExists()
    composeTestRule.onNodeWithTag("gibs_orchidland_preset_btn").assertExists()
    composeTestRule.onNodeWithTag("sat_open_hawaii_cams_btn").assertExists()
    composeTestRule.onNodeWithTag("sat_zoom_in_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sat_zoom_reset_btn").assertExists()
    composeTestRule.onNodeWithTag("sat_overlay_options_row").assertExists()
    composeTestRule.onNodeWithTag("sat_overlay_grid").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sat_scroll_lock_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sat_scroll_lock_btn").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_catalog").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_lab").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_intel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("intel_mode_narrative_ops").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("narrative_firebase_notice").assertExists()
    composeTestRule.onNodeWithTag("narrative_readiness_card").assertExists()
    composeTestRule.onNodeWithTag("narrative_control_list")
      .performScrollToNode(hasTestTag("narrative_ingest_btn"))
    composeTestRule.onNodeWithTag("narrative_ingest_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("narrative_control_list")
      .performScrollToNode(hasTestTag("narrative_approve_btn_seed_01"))
    composeTestRule.onNodeWithTag("narrative_approve_btn_seed_01").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("narrative_dispatch_status_seed_01").assertExists()

    composeTestRule.onNodeWithTag("intel_mode_cbrn_hazard").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("intel_mode_ai_vector").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ai_tab_lmm").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ai_lmm_list")
      .performScrollToNode(hasTestTag("stac_promote_to_casebook_btn"))
    composeTestRule.onNodeWithTag("stac_promote_to_casebook_btn").assertExists()
    composeTestRule.onNodeWithTag("intel_mode_casebook").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("casebook_search_input").assertExists()

    composeTestRule.onNodeWithTag("nav_dashboard").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test Orchidland SNA engine proves High-Degree Trap vs High-Betweenness Bridge`() {
    val baseline = DicmSnaEngine.analyzeNetwork(emptySet())
    assertEquals(13, baseline.totalActiveNodes)
    assertEquals(1, baseline.connectedComponents)

    // AI-AUT-AG and HUM-OPR-AG are top structural bridges, strictly higher betweenness than FB swarm traps
    val metricsById = baseline.nodeMetrics.associateBy { it.node.id }
    val autBetw = metricsById["n_aut_c2"]!!.betweennessNorm
    val humBetw = metricsById["n_hum_opr"]!!.betweennessNorm
    val srxTrapMaxBetw = listOf("n_srx_1", "n_srx_2", "n_srx_3")
      .maxOf { metricsById[it]!!.betweennessNorm }

    val topFourIds = baseline.nodeMetrics.take(4).map { it.node.id }
    assertTrue(topFourIds.contains("n_hum_opr"))
    assertTrue(topFourIds.contains("n_aut_c2"))
    assertTrue(autBetw > srxTrapMaxBetw)
    assertTrue(humBetw > srxTrapMaxBetw)

    // Isolating Facebook High-Degree Swarm Traps yields low fragmentation and leaves C2 connected
    val trapReport = DicmSnaEngine.analyzeNetwork(setOf("n_srx_1", "n_srx_2", "n_srx_3"))
    assertEquals(1, trapReport.connectedComponents)
    assertTrue(trapReport.fragmentationPercent < 30f)
    assertTrue(trapReport.tacticalAssessment.contains("HIGH-DEGREE TRAP"))

    // Severing Low-Degree / High-Betweenness Bridges severely fragments the hybrid network
    val bridgeReport = DicmSnaEngine.analyzeNetwork(setOf("n_aut_c2", "n_hum_opr"))
    assertTrue(bridgeReport.connectedComponents >= 4)
    assertTrue(bridgeReport.fragmentationPercent >= 50f)
  }

  @Test
  fun `test Orchidland profile and platform drawable resources resolve`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals(6, OrchidlandDicmData.taskAgentProfiles.size)
    assertEquals(6, OrchidlandDicmData.platformNodes.size)
    OrchidlandDicmData.taskAgentProfiles.forEach { profile ->
      assertNotNull(context.getDrawable(profile.emblemResId))
    }
    OrchidlandDicmData.platformNodes.forEach { platform ->
      assertNotNull(context.getDrawable(platform.emblemResId))
    }
  }

  @Test
  fun `test NASA GIBS snapshot URL builder`() {
    val url = NasaGibsUrlBuilder.buildSnapshotUrl(
      latitude = 46.4825,
      longitude = 30.7233,
      dateIso = "2024-08-14T06:42:15Z",
      layer = NasaGibsLayer.VIIRS_THERMAL,
      spanDegrees = 2.0
    )
    assertTrue(url.startsWith("https://wvs.earthdata.nasa.gov/api/v1/snapshot"))
    assertTrue(url.contains("TIME=2024-08-14T00:00:00Z"))
    assertTrue(url.contains("BBOX=45.4825,29.7233,47.4825,31.7233"))
    assertTrue(url.contains("VIIRS_SNPP_ThermalAnomalies_375m_Day"))

    val zoomedUrl = NasaGibsUrlBuilder.buildSnapshotUrl(
      latitude = 19.5614,
      longitude = -155.0131,
      dateIso = "2024-08-14",
      layer = NasaGibsLayer.VIIRS_TRUE_COLOR,
      spanDegrees = 0.25,
      cacheBustNonce = 777L
    )
    assertTrue(zoomedUrl.contains("BBOX=19.4364,-155.1381,19.6864,-154.8881"))
    assertTrue(zoomedUrl.endsWith("&_t=777"))
  }

  @Test
  fun `test NASA EONET DTO mapping for Point and Polygon`() {
    val pointDto = EonetEventDto(
      id = "EONET_9901",
      title = "Wildfire Sector Test",
      categories = listOf(EonetCategoryDto("wildfires", "Wildfires")),
      sources = listOf(EonetSourceDto("InciWeb", "https://inciweb.wildfire.gov")),
      geometry = listOf(
        EonetGeometryDto(
          magnitudeValue = 1200.0,
          magnitudeUnit = "acres",
          date = "2025-07-10T14:00:00Z",
          type = "Point",
          coordinates = listOf(-120.5, 38.75)
        )
      )
    )
    val mappedPoint = NasaSatFeedRepository.mapDtoToDomain(pointDto)
    assertNotNull(mappedPoint)
    assertEquals(38.75, mappedPoint!!.latitude, 0.001)
    assertEquals(-120.5, mappedPoint.longitude, 0.001)
    assertEquals("2025-07-10", mappedPoint.latestDateIso)
    assertEquals("1200.0 acres", mappedPoint.magnitudeLabel)

    val polyDto = EonetEventDto(
      id = "EONET_9902",
      title = "Flood Extent Test",
      categories = listOf(EonetCategoryDto("floods", "Floods")),
      geometry = listOf(
        EonetGeometryDto(
          date = "2025-08-01T00:00:00Z",
          type = "Polygon",
          coordinates = listOf(listOf(listOf(10.0, 20.0), listOf(12.0, 22.0)))
        )
      )
    )
    val mappedPoly = NasaSatFeedRepository.mapDtoToDomain(polyDto)
    assertNotNull(mappedPoly)
    assertEquals(21.0, mappedPoly!!.latitude, 0.001)
    assertEquals(11.0, mappedPoly.longitude, 0.001)
  }

  @Test
  fun `read string from context matches VeriScope`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VeriScope", appName)
  }

  @Test
  fun `test sun chronolocation trigonometry`() {
    val result = SunChronolocationCalculator.calculate(
      objectHeightMeters = 10.0,
      shadowLengthMeters = 10.0,
      latitudeDeg = 45.0,
      longitudeDeg = 30.0
    )
    assertEquals(45.0, result.sunElevationDeg, 0.5)
    assertEquals(45.0, result.sunZenithDeg, 0.5)
    assertEquals(1.0, result.shadowRatio, 0.05)
    assertTrue(result.morningEstimateUtc.isNotEmpty())
  }

  @Test
  fun `test error level analysis engine tamper detection`() {
    val pristine = ErrorLevelAnalysisEngine.evaluate("PRISTINE_CAMERA")
    assertFalse(pristine.isTamperingSuspected)
    assertTrue(pristine.c2paAudit.isManifestPresent)

    val spliced = ErrorLevelAnalysisEngine.evaluate("SPLICED_MUNITION")
    assertTrue(spliced.isTamperingSuspected)
    assertTrue(spliced.anomalyScorePercent > 70)
  }

  @Test
  fun `test ballistic metrology congruence`() {
    val match = BallisticMetrologyEngine.compare("Glock 19 Gen 5 (9x19mm)", "Glock 19 Gen 5 (9x19mm)")
    assertTrue(match.isPositiveMatch)
    assertTrue(match.correlationScorePercent > 90.0)

    val mismatch = BallisticMetrologyEngine.compare("Glock 19 Gen 5 (9x19mm)", "Colt M4A1 (5.56mm)")
    assertFalse(mismatch.isPositiveMatch)
    assertTrue(mismatch.correlationScorePercent < 50.0)
  }

  @Test
  fun `test ai vector similarity search`() {
    val results = AiVectorSimulator.search("bridge collapse")
    assertTrue(results.isNotEmpty())
    assertTrue(results.first().similarityScore > 0.8)
  }

  @Test
  fun `test Hawaii FAA TFR filtering, GeoJSON parsing, and PTA keyword extractor`() {
    val tfrJson = """
      [
        {"notam_id": "4/1234", "type": "HAZARDS", "facility": "ZHN", "state": "HI", "description": "Kilauea Volcanic Plume TFR"},
        {"notam_id": "4/9999", "type": "VIP", "facility": "ZLA", "state": "CA", "description": "Los Angeles Basin"}
      ]
    """.trimIndent()
    val filtered = HawaiiAirspaceRepository.filterHawaiiTfrs(tfrJson)
    assertEquals(1, filtered.size)
    assertEquals("4/1234", filtered.first().notamId)
    assertEquals("ZHN", filtered.first().facility)

    val geoJson = """
      {
        "type": "FeatureCollection",
        "features": [{
          "type": "Feature",
          "properties": {"NOTAM_KEY": "FDC 4/1234", "TITLE": "Pōhakuloa SUA Alert"},
          "geometry": {
            "type": "Polygon",
            "coordinates": [[[-155.68, 19.68], [-155.45, 19.68], [-155.45, 19.83], [-155.68, 19.83], [-155.68, 19.68]]]
          }
        }]
      }
    """.trimIndent()
    val polys = HawaiiAirspaceRepository.parseTfrGeoJsonPolygons(geoJson)
    assertEquals(1, polys.size)
    assertEquals("FDC 4/1234", polys.first().notamKey)
    assertEquals(5, polys.first().rings.first().size)

    val pta = PtaAdvisoryParser.parseAdvisory(
      subject = "PTA Training Advisory",
      sender = "usag.hawaii.comrel@army.mil",
      bodyText = "Units will conduct artillery and mortar live-fire inside R-3103 with convoy traffic along Daniel K. Inouye / Saddle Road and UAS flights near Bradshaw."
    )
    assertTrue(pta.keywords.contains("ARTILLERY"))
    assertTrue(pta.keywords.contains("LIVE-FIRE"))
    assertTrue(pta.keywords.contains("CONVOY"))
    assertTrue(pta.keywords.contains("R-3103"))
    assertTrue(pta.impactsSaddleRoad)
    assertTrue(pta.impactsAviationOrUas)

    // Verify Orchidland Estates sits outside PHTO 4.3 NM Class D ring
    val phtoDistNm = HawaiiAirspaceReference.distanceNauticalMiles(
      HawaiiAirspaceReference.ORCHIDLAND_LAT,
      HawaiiAirspaceReference.ORCHIDLAND_LON,
      19.720,
      -155.048
    )
    assertTrue(phtoDistNm > HawaiiAirspaceReference.PHTO_CLASS_D_RADIUS_NM)

    // Verify NOAA/NWS Hawaii MRMS & NEXRAD PHWA/PHKM Doppler WMS URL builder
    val brefUrl = HawaiiDopplerUrlBuilder.buildWmsUrl(HawaiiDopplerLayer.HAWAII_BREF)
    assertTrue(brefUrl.startsWith("https://opengeo.ncep.noaa.gov/geoserver/hawaii/hawaii_bref_qcd/ows"))
    assertTrue(brefUrl.contains("LAYERS=hawaii_bref_qcd"))
    assertTrue(brefUrl.contains("SRS=EPSG:4326"))
    assertTrue(brefUrl.contains("BBOX=-156.2000,18.8000,-154.6800,20.3800"))
    assertTrue(brefUrl.contains("TRANSPARENT=true"))

    val velUrl = HawaiiDopplerUrlBuilder.buildWmsUrl(HawaiiDopplerLayer.PHWA_VEL, cacheBustNonce = 12345L)
    assertTrue(velUrl.contains("phwa/phwa_sr_bvel/ows"))
    assertTrue(velUrl.contains("LAYERS=phwa_sr_bvel"))
    assertTrue(velUrl.endsWith("&_t=12345"))
    assertTrue(HawaiiAirspaceReference.keySites.any { it.id == "site_phwa" })
    assertTrue(HawaiiAirspaceReference.keySites.any { it.id == "site_phkm" })
  }

  @Test
  fun `test FAA WeatherCams HI parsing, USGS HVO volcanic cams, UH stills, and HTML error rejection`() {
    val sitesPayload = """
      {
        "payload": [
          {"siteId": 101, "siteIdentifier": "PHTO", "siteName": "Hilo Intl", "state": "HI", "latitude": 19.72, "longitude": -155.05, "elevation": 38, "siteActive": true},
          {"siteId": 102, "siteIdentifier": "PHSF", "siteName": "Bradshaw AAF", "state": "HI", "latitude": 19.76, "longitude": -155.55, "elevation": 6190, "siteActive": true},
          {"siteId": 205, "siteIdentifier": "PAFA", "siteName": "Fairbanks", "state": "AK", "latitude": 64.81, "longitude": -147.85, "siteActive": true},
          {"siteId": 103, "siteIdentifier": "PHXX", "siteName": "Inactive Site", "state": "HI", "latitude": 19.50, "longitude": -155.00, "siteActive": false}
        ]
      }
    """.trimIndent()

    val hiSites = HawaiiWebcamsRepository.parseFaaHawaiiSites(sitesPayload)
    assertEquals(2, hiSites.size)
    assertEquals("PHTO", hiSites[0].siteIdentifier)
    assertEquals("PHSF", hiSites[1].siteIdentifier)

    val summaryPayload = """
      {
        "payload": {
          "cameras": [
            {"cameraId": 1, "direction": "East", "imageUri": "https://weathercams.faa.gov/images/phto_east.jpg", "lastImageTimestamp": "2025-05-01T18:10:00Z"},
            {"cameraId": 2, "direction": "Southwest", "imageUri": "/images/phto_sw.jpg"}
          ]
        }
      }
    """.trimIndent()

    val faaCams = HawaiiWebcamsRepository.parseFaaSiteSummaryCameras(hiSites[0], summaryPayload)
    assertEquals(2, faaCams.size)
    assertEquals("https://weathercams.faa.gov/images/phto_east.jpg", faaCams[0].imageUrl)
    assertEquals("https://weathercams.faa.gov/images/phto_sw.jpg", faaCams[1].imageUrl)
    assertEquals(WebcamFeedSource.FAA_WEATHERCAM, faaCams[0].source)

    // Verify USGS HVO volcano cams & thermal F1cam & UH System HTTPS stills
    val usgs = HawaiiWebcamsReference.usgsHvoCams
    val uh = HawaiiWebcamsReference.uhSystemCams
    assertTrue(usgs.any { it.stationCode == "PGcam" && it.imageUrl == "https://volcanoes.usgs.gov/cams/PGcam/images/M.jpg" })
    assertTrue(usgs.any { it.stationCode == "F1cam" && it.isThermal })
    assertTrue(usgs.any { it.stationCode == "MLcam" })
    assertEquals(6, uh.size)
    assertTrue(uh.all { it.imageUrl.startsWith("https://") })
    assertTrue(uh.any { it.imageUrl.contains("irtf.ifa.hawaii.edu") })
    assertTrue(uh.any { it.imageUrl.contains("cfht.hawaii.edu") })

    val snap = HawaiiWebcamsSnapshot(
      updatedAtHst = "HST",
      faaCams = faaCams,
      usgsCams = usgs,
      uhCams = uh
    )
    assertEquals(faaCams.size + usgs.size + uh.size, snap.filtered(WebcamFilter.ALL).size)
    assertEquals(faaCams.size, snap.filtered(WebcamFilter.FAA).size)
    assertEquals(usgs.size, snap.filtered(WebcamFilter.USGS).size)
    assertEquals(uh.size, snap.filtered(WebcamFilter.UH).size)

    // Verify HTML/XML FAA error payloads throw instead of masquerading as empty CLEAR TFR status
    assertThrows(IllegalStateException::class.java) {
      HawaiiAirspaceRepository.filterHawaiiTfrs("<html><body>503 Service Unavailable</body></html>")
    }
    assertThrows(IllegalStateException::class.java) {
      HawaiiAirspaceRepository.parseTfrGeoJsonPolygons("<ServiceExceptionReport>Error</ServiceExceptionReport>")
    }
  }

  @Test
  fun `test Facebook and Telegram webhook parsers, sample triage engine, and dispatch dry-run vs API`() = runBlocking {
    // 1. Facebook webhook handshake verification
    val okVerify = FacebookConnectorParser.verifyWebhookHandshake(
      mode = "subscribe",
      verifyToken = "secret_abc",
      challenge = "991122",
      expectedToken = "secret_abc"
    )
    assertEquals(200, okVerify.statusCode)
    assertEquals("991122", okVerify.challengeResponse)

    val badVerify = FacebookConnectorParser.verifyWebhookHandshake(
      mode = "subscribe",
      verifyToken = "wrong",
      challenge = "991122",
      expectedToken = "secret_abc"
    )
    assertEquals(403, badVerify.statusCode)
    assertNull(badVerify.challengeResponse)

    // 2. Facebook webhook Page feed + Messenger (skipping echoes)
    val fbWebhookJson = """
      {
        "object": "page",
        "entry": [{
          "changes": [{
            "field": "feed",
            "value": {
              "comment_id": "post_1_comment_9",
              "from": {"id": "u_44", "name": "Puna Rant"},
              "message": "BLIND ITEM: Who closed the gate?"
            }
          }],
          "messaging": [
            {"sender": {"id": "psid_11"}, "message": {"text": "Is Hwy 130 open?", "is_echo": false}},
            {"sender": {"id": "psid_99"}, "message": {"text": "Echoed bot text", "is_echo": true}}
          ]
        }]
      }
    """.trimIndent()
    val fbSignals = FacebookConnectorParser.parseFacebookWebhookPayload(fbWebhookJson)
    assertEquals(2, fbSignals.size)
    assertEquals("comment", fbSignals[0].replyType)
    assertEquals("post_1_comment_9", fbSignals[0].targetId)
    assertEquals("dm", fbSignals[1].replyType)
    assertEquals("PSID_psid_11", fbSignals[1].senderName)

    // 3. Telegram webhook Update parsing (message + channel_post)
    val tgJson = """
      {
        "update_id": 1001,
        "message": {
          "message_id": 77,
          "from": {"id": 900, "username": "orchidland_watch"},
          "chat": {"id": -100555},
          "text": "Urgent share: convoy resbak alert near Pahoa"
        }
      }
    """.trimIndent()
    val tgSignal = TelegramConnectorParser.parseTelegramUpdatePayload(tgJson)
    assertNotNull(tgSignal)
    assertEquals(SocialPlatform.TELEGRAM, tgSignal!!.platform)
    assertEquals("-100555", tgSignal.targetId)
    assertEquals(77L, tgSignal.replyToMessageId)
    assertEquals("orchidland_watch", tgSignal.senderName)

    // 4. Sample AI triage repository outputs clearly labeled placeholders
    val sampleAi = SampleNarrativeAiRepository()
    assertFalse(sampleAi.isFirebaseBacked)
    val pkg = sampleAi.triageAndDraft(fbSignals[0])
    assertTrue(pkg.isSamplePlaceholder)
    assertEquals(TriageClassification.ACTIONABLE_BLIND_ITEM, pkg.triage.classification)
    assertTrue(pkg.triage.decodedSummary.contains("[SAMPLE RESPONSE]"))
    assertTrue(pkg.drafts.communityReply.contains("[SAMPLE DRAFT]"))
    assertTrue(pkg.triage.detectedKeywords.contains("blind item"))

    // 5. Dispatch dry-run when placeholder tokens vs fake Graph/Bot API when configured
    val incidentFb = NarrativeIncidentRecord(
      id = "rec_1",
      timestampIso = "2025-05-01 00:00:00 UTC",
      signal = fbSignals[0],
      triagePackage = pkg
    )
    val dryRunRepo = SocialDispatchRepository(
      fbTokenProvider = { "MY_FB_PAGE_ACCESS_TOKEN" },
      tgTokenProvider = { "MY_TELEGRAM_BOT_TOKEN" }
    )
    assertFalse(dryRunRepo.isFacebookConfigured())
    assertFalse(dryRunRepo.isTelegramConfigured())
    val dryRes = dryRunRepo.dispatchReply(incidentFb, "Verified update")
    assertTrue(dryRes.isSuccess)
    assertTrue(dryRes.getOrThrow().startsWith("[DRY-RUN]"))

    var sentFbCommentTarget: String? = null
    var sentTgChatId: String? = null
    val liveRepo = SocialDispatchRepository(
      facebookApi = object : FacebookGraphApi {
        override suspend fun sendDirectMessage(accessToken: String, payload: FacebookDmRequest): ResponseBody {
          return "{\"message_id\":\"m1\"}".toResponseBody("application/json".toMediaType())
        }
        override suspend fun postCommentReply(targetId: String, accessToken: String, payload: FacebookCommentRequest): ResponseBody {
          sentFbCommentTarget = targetId
          return "{\"id\":\"c1\"}".toResponseBody("application/json".toMediaType())
        }
      },
      telegramApi = object : TelegramBotApi {
        override suspend fun sendMessage(token: String, payload: TelegramSendMessageRequest): ResponseBody {
          sentTgChatId = payload.chat_id
          return "{\"ok\":true}".toResponseBody("application/json".toMediaType())
        }
      },
      fbTokenProvider = { "real_fb_token" },
      tgTokenProvider = { "real_tg_token" }
    )
    assertTrue(liveRepo.isFacebookConfigured())
    assertTrue(liveRepo.isTelegramConfigured())
    val fbLiveRes = liveRepo.dispatchReply(incidentFb, "Official clarification")
    assertTrue(fbLiveRes.isSuccess)
    assertEquals("post_1_comment_9", sentFbCommentTarget)

    val incidentTg = incidentFb.copy(id = "rec_2", signal = tgSignal)
    val tgLiveRes = liveRepo.dispatchReply(incidentTg, "Telegram clarification")
    assertTrue(tgLiveRes.isSuccess)
    assertEquals("-100555", sentTgChatId)
  }

  @Test
  fun `test bug regressions for Hawaii solar wrapping, dynamic LMM STAC extraction, and Inouye PTA parser`() {
    // 1. SunChronolocationCalculator wraps Hawaii longitude (-155.0) cleanly past 00:00:00 UTC instead of clamping to 23:59:24 UTC
    val hiSolar = SunChronolocationCalculator.calculate(
      objectHeightMeters = 10.0,
      shadowLengthMeters = 10.0,
      latitudeDeg = 19.5614,
      longitudeDeg = -155.0131
    )
    assertNotEquals("23:59:24 UTC", hiSolar.morningEstimateUtc)
    assertNotEquals("23:59:24 UTC", hiSolar.afternoonEstimateUtc)
    assertTrue(hiSolar.morningEstimateUtc.endsWith(" UTC"))
    assertTrue(hiSolar.afternoonEstimateUtc.endsWith(" UTC"))
    assertTrue(hiSolar.estimatedHourDecimal in 0.0..24.0)

    // 2. AiVectorSimulator.simulateLmmExtraction dynamically reflects input narrative instead of static Odessa output
    val punaStac = AiVectorSimulator.simulateLmmExtraction(
      "Orchidland Puna UAS flight and VIIRS thermal anomaly near Hwy 130."
    )
    assertTrue(punaStac.contains("HYBRID_CYBER_PHYSICAL_OPS"))
    assertTrue(punaStac.contains("Unmanned Aircraft System (sUAS) Telemetry"))
    assertTrue(punaStac.contains("-155.0"))
    assertTrue(punaStac.contains("NASA VIIRS 375m Thermal / GIBS"))

    // 3. PtaAdvisoryParser matches "Daniel K. Inouye" (without "convoy" or "Saddle Road") and emits unique IDs
    val inouyeOnly = PtaAdvisoryParser.parseAdvisory(
      subject = "Traffic Notice",
      sender = "pta-pao@army.mil",
      bodyText = "Heavy military transport scheduled near Daniel K. Inouye Highway milepost 35."
    )
    val secondParse = PtaAdvisoryParser.parseAdvisory(
      subject = "Traffic Notice",
      sender = "pta-pao@army.mil",
      bodyText = "Heavy military transport scheduled near Daniel K. Inouye Highway milepost 35."
    )
    assertTrue(inouyeOnly.impactsSaddleRoad)
    assertNotEquals(inouyeOnly.id, secondParse.id)
  }

  @Test
  fun `test Big Island GIBS BBOX URL and ViewModel Case, Intel, and Airspace Satellite updates`() {
    // 1. Exact Hawaiʻi Island bounding-box NASA GIBS snapshot URL
    val hiSatUrl = NasaGibsUrlBuilder.buildBboxSnapshotUrl(
      south = 18.80,
      west = -156.20,
      north = 20.38,
      east = -154.68,
      dateIso = "2025-05-01",
      layer = NasaGibsLayer.VIIRS_THERMAL,
      width = 800,
      height = 600,
      cacheBustNonce = 55L
    )
    assertTrue(hiSatUrl.contains("BBOX=18.8000,-156.2000,20.3800,-154.6800"))
    assertTrue(hiSatUrl.contains("TIME=2025-05-01T00:00:00Z"))
    assertTrue(hiSatUrl.contains("WIDTH=800&HEIGHT=600"))
    assertTrue(hiSatUrl.endsWith("&_t=55"))

    // 2. ViewModel Big Island Airspace Satellite options & full-sat handoff
    val vm = ForensicViewModel()
    val initialDate = vm.uiState.value.airspaceSatDateIso
    vm.selectAirspaceSatLayer(NasaGibsLayer.VIIRS_THERMAL)
    assertEquals(NasaGibsLayer.VIIRS_THERMAL, vm.uiState.value.airspaceSatLayer)
    assertTrue(vm.uiState.value.airspaceSatEnabled)

    vm.setAirspaceSatOpacity(0.1f)
    assertEquals(0.25f, vm.uiState.value.airspaceSatOpacity, 0.001f)

    vm.shiftAirspaceSatDateDays(-1)
    assertNotEquals(initialDate, vm.uiState.value.airspaceSatDateIso)

    val orchidlandSite = HawaiiAirspaceReference.keySites.first { it.id == "site_orchidland" }
    vm.openAirspaceSiteInFullSatFeed(orchidlandSite)
    assertEquals(NavigationTab.SAT_FEED, vm.uiState.value.currentTab)
    assertTrue(vm.uiState.value.hasCustomGibsTarget)
    assertEquals(NasaGibsLayer.VIIRS_THERMAL, vm.uiState.value.activeGibsLayer)
    assertTrue(vm.uiState.value.customGibsTarget.label.contains("ORCHID"))

    // 3. Case updates: NASA EONET ingestion creates & updates existing dossier with new GIBS layer
    val testEvent = NasaSatEvent(
      id = "EONET_HI_77",
      title = "Kilauea East Rift Thermal Anomaly",
      description = "VIIRS 375m radiant anomaly near Puna corridor",
      isClosed = false,
      closedDate = null,
      categoryId = "volcanoes",
      categoryTitle = "Volcanoes",
      sourceId = "HVO",
      sourceUrl = "https://volcanoes.usgs.gov/",
      latestTimestamp = "2025-05-01T12:00:00Z",
      latestDateIso = "2025-05-01",
      latitude = 19.4069,
      longitude = -155.2834,
      magnitudeLabel = "45.0 MW",
      trackPointCount = 2
    )
    vm.selectGibsLayer(NasaGibsLayer.VIIRS_TRUE_COLOR)
    vm.ingestNasaEventToCasebook(testEvent)
    val firstCase = vm.uiState.value.selectedCase
    assertNotNull(firstCase)
    assertEquals("NASA-EONET_HI_77", firstCase!!.caseNumber)
    assertEquals(1, firstCase.evidenceItems.size)

    vm.selectGibsLayer(NasaGibsLayer.VIIRS_THERMAL)
    vm.ingestNasaEventToCasebook(testEvent)
    val updatedNasaCase = vm.uiState.value.selectedCase!!
    assertEquals(firstCase.id, updatedNasaCase.id)
    assertEquals(2, updatedNasaCase.evidenceItems.size)
    assertEquals(2, updatedNasaCase.chainOfCustody.size)

    // 4. Case status transition & Field Intel append update
    vm.updateCaseStatus(updatedNasaCase.id, CaseStatus.CORROBORATED)
    vm.appendCaseIntelUpdate(
      caseId = updatedNasaCase.id,
      title = "PGcam Thermal Cross-Check",
      note = "Confirmed radiant plume above Puʻuʻōʻō ridge via USGS PGcam still.",
      sensor = "USGS HVO PGcam",
      assetType = EvidenceAssetType.IMAGE_EXIF
    )
    val afterUpdate = vm.uiState.value.selectedCase!!
    assertEquals(CaseStatus.CORROBORATED, afterUpdate.status)
    assertEquals(3, afterUpdate.evidenceItems.size)
    assertEquals(4, afterUpdate.chainOfCustody.size)

    // 5. Case -> AI Intel handoff & STAC -> Casebook promotion
    vm.sendCaseToAiIntel(afterUpdate)
    assertEquals(NavigationTab.INTEL, vm.uiState.value.currentTab)
    assertEquals(IntelSubMode.AI_VECTOR, vm.uiState.value.selectedIntelSubMode)
    assertTrue(vm.uiState.value.lmmInputText.contains("NASA-EONET_HI_77"))

    val caseCountBeforeStac = vm.uiState.value.cases.size
    vm.ingestStacIntelToCasebook("Orchidland 16th Ave UAS telemetry corridor 19.5614, -155.0131 near Hwy 130.")
    assertEquals(caseCountBeforeStac + 1, vm.uiState.value.cases.size)
    assertEquals(IntelSubMode.CASEBOOK, vm.uiState.value.selectedIntelSubMode)
    val stacCase = vm.uiState.value.selectedCase!!
    assertTrue(stacCase.caseNumber.startsWith("INTL-2026-"))
    assertEquals(19.5614, stacCase.latitude, 0.001)
    assertEquals(-155.0131, stacCase.longitude, 0.001)
  }

  @Test
  fun `test AccessibilityAudioManager chunking, TextScalePreset, and ViewModel accessibility state`() {
    val longReport = buildString {
      append("Orchidland Estates Sector Update. ")
      append("Highway 130 remains clear with zero active FAA TFRs in lower Puna airspace. ")
      append("Social Network Analysis indicates high-betweenness bridges at AI-AUT-AG and HUM-OPR-AG. ")
      append("NIST countermeasures deployed at 70 percent readiness with full legal warrant boundaries active.")
    }
    val chunks = AccessibilityAudioManager.splitIntoChunks(longReport, maxChunkChars = 110)
    assertTrue(chunks.size >= 2)
    chunks.forEach { (slice, range) ->
      assertTrue(slice.isNotBlank())
      assertTrue(slice.length <= 115)
      assertTrue(range.first >= 0 && range.last < longReport.length)
    }

    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = AccessibilityAudioManager(context)
    try {
      manager.playOrToggle("unit_report_1", "Unit Test Brief", longReport)
      assertTrue(manager.state.value.isActive)
      assertTrue(manager.state.value.isPlaying)
      assertEquals("unit_report_1", manager.state.value.activeSourceId)

      manager.playOrToggle("unit_report_1", "Unit Test Brief", longReport)
      assertTrue(manager.state.value.isPaused)
      assertFalse(manager.state.value.isPlaying)

      manager.cycleSpeechRate()
      assertEquals(AudioSpeechRate.FAST, manager.state.value.speechRate)

      manager.stop()
      assertFalse(manager.state.value.isActive)
      assertNull(manager.state.value.activeSourceId)
    } finally {
      manager.shutdown()
    }

    val vm = ForensicViewModel()
    assertEquals(TextScalePreset.STANDARD, vm.uiState.value.textScalePreset)
    assertTrue(vm.uiState.value.visualAudioEnabled)

    vm.cycleTextScalePreset()
    assertEquals(TextScalePreset.LARGE, vm.uiState.value.textScalePreset)
    assertEquals(1.22f, vm.uiState.value.textScalePreset.scaleFactor, 0.001f)

    vm.cycleTextScalePreset()
    assertEquals(TextScalePreset.EXTRA_LARGE, vm.uiState.value.textScalePreset)
    assertEquals(1.42f, vm.uiState.value.textScalePreset.scaleFactor, 0.001f)

    vm.toggleVisualAudioEnabled()
    assertFalse(vm.uiState.value.visualAudioEnabled)

    vm.setAccessibilitySheetOpen(true)
    assertTrue(vm.uiState.value.isAccessibilitySheetOpen)
  }

  @Test
  fun `test visual select audio playback and readable text accessibility UI flow`() {
    composeTestRule.waitForIdle()

    // 1. Trigger visual select-audio on Dashboard Command Hero report
    composeTestRule.onNodeWithTag("audio_btn_orchidland_hero")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    // Floating audio player bar should appear with speed, scale, pause/play, and stop controls
    composeTestRule.onNodeWithTag("floating_audio_player_bar").assertExists()
    composeTestRule.onNodeWithTag("audio_bar_speed_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_bar_play_pause_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_bar_text_scale_btn").performClick()
    composeTestRule.waitForIdle()

    // 2. Open Accessibility Options sheet and select Extra Large readable text + test audio
    composeTestRule.onNodeWithTag("topbar_a11y_settings_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("accessibility_options_sheet").assertExists()
    composeTestRule.onNodeWithTag("a11y_scale_extra_large").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("a11y_speed_fast").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("a11y_test_audio_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Close Accessibility sheet cleanly before interacting with root TopBar
    composeTestRule.onNodeWithTag("a11y_sheet_close_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("accessibility_options_sheet").assertDoesNotExist()

    // Stop active audio so turning Select Audio OFF hides audio buttons cleanly
    composeTestRule.onNodeWithTag("audio_bar_stop_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("floating_audio_player_bar").assertDoesNotExist()

    // 3. Toggle Visual Select Audio OFF then ON via TopBar
    composeTestRule.onNodeWithTag("topbar_visual_audio_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_btn_orchidland_hero").assertDoesNotExist()

    composeTestRule.onNodeWithTag("topbar_visual_audio_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("audio_btn_orchidland_hero").assertExists()

    // 4. Verify text-box audio reader strip in Narrative Control screen
    composeTestRule.onNodeWithTag("nav_intel").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("intel_mode_narrative_ops").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("narrative_control_list")
      .performScrollToNode(hasTestTag("audio_btn_narrative_text_input"))
    composeTestRule.onNodeWithTag("audio_btn_narrative_text_input")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("floating_audio_player_bar").assertExists()

    // Stop audio playback cleanly
    composeTestRule.onNodeWithTag("audio_bar_stop_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("floating_audio_player_bar").assertDoesNotExist()
  }

  @Test
  fun `test Hilo Downtown Cam Monitor dashboard module and strict URL rules`() {
    composeTestRule.waitForIdle()

    // Verify primary reference rules: official link first, no invented direct stream URLs
    assertEquals("https://tsunami.org/hilobaycam/", HiloDowntownCamReference.PRIMARY_OFFICIAL_URL)
    assertEquals(19.726, HiloDowntownCamReference.CAMERA_LAT, 0.0001)
    assertEquals(-155.0866, HiloDowntownCamReference.CAMERA_LON, 0.0001)
    val brief = HiloDowntownCamReference.buildOperationalBrief(probeResult = null)
    assertTrue(brief.indexOf("https://tsunami.org/hilobaycam/") < brief.indexOf("bandwidth-limited"))
    assertFalse(brief.contains(".m3u8"))
    assertFalse(brief.contains(".mjpeg"))
    assertFalse(brief.contains("rtsp://"))

    // Select "6. Hilo Cam Monitor" in the Dashboard menu
    composeTestRule.onNodeWithTag("dicm_tab_hilo_cam")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()

    // Target area & primary public camera card
    composeTestRule.onNodeWithTag("hilo_downtown_cam_screen").assertExists()
    composeTestRule.onNodeWithTag("hilo_target_sector_card").assertExists()
    assertTrue(composeTestRule.onAllNodesWithText("Kalakaua Park", substring = true).fetchSemanticsNodes().isNotEmpty())
    assertTrue(composeTestRule.onAllNodesWithText("154 Waianuenue Ave", substring = true).fetchSemanticsNodes().isNotEmpty())

    composeTestRule.onNodeWithTag("dicm_lazy_column")
      .performScrollToNode(hasTestTag("hilo_primary_cam_card"))
    composeTestRule.onNodeWithTag("hilo_official_url_text")
      .assertExists()
      .assertTextContains("https://tsunami.org/hilobaycam/")
    assertTrue(composeTestRule.onAllNodesWithText("19.726, -155.0866", substring = true).fetchSemanticsNodes().isNotEmpty())
    composeTestRule.onNodeWithTag("hilo_bandwidth_warning_card").assertExists()
    composeTestRule.onNodeWithTag("hilo_open_official_cam_btn").assertExists()
    composeTestRule.onNodeWithTag("hilo_check_status_btn").assertExists()
    composeTestRule.onNodeWithTag("hilo_search_status_btn").assertExists()

    // Audio playback trigger on Hilo Downtown Cam status brief
    composeTestRule.onNodeWithTag("dicm_lazy_column")
      .performScrollToNode(hasTestTag("hilo_status_brief_card"))
    composeTestRule.onNodeWithTag("audio_btn_hilo_cam_brief")
      .performScrollTo()
      .performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("floating_audio_player_bar").assertExists()
    composeTestRule.onNodeWithTag("audio_bar_stop_btn").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test NavigationRouteEngine, OpenSky ADS-B parser, MapPreferencesStore, and Airspace catalogs`() {
    // 1. Dynamic Route Recalculation & Projection
    val waypoints = listOf(
      UserWaypoint(id = "wp_1", label = "PHTO", lat = 19.720, lon = -155.048, altitudeFt = 1500),
      UserWaypoint(id = "wp_2", label = "PHKO", lat = 19.738, lon = -156.045, altitudeFt = 6500)
    )
    val route = NavigationRouteEngine.recalculateRoute(
      currentLat = HawaiiAirspaceReference.ORCHIDLAND_LAT,
      currentLon = HawaiiAirspaceReference.ORCHIDLAND_LON,
      groundSpeedKts = 120.0,
      waypoints = waypoints
    )
    assertEquals(2, route.legs.size)
    assertEquals("PHTO", route.nextWaypointLabel)
    assertTrue(route.totalDistanceNm > 50.0)
    assertTrue(route.totalEteMinutes > 25.0)
    assertNotNull(route.nextBearingDeg)

    val projected = NavigationRouteEngine.projectPointNm(
      lat = 19.5614,
      lon = -155.0131,
      bearingDeg = 0.0,
      distanceNm = 60.0
    )
    assertEquals(20.56, projected.lat, 0.05)
    assertEquals(-155.0131, projected.lon, 0.05)

    // 2. OpenSky ADS-B JSON parsing (valid + malformed/empty handling)
    val validOpenSky = """
      {
        "time": 1730000000,
        "states": [
          ["a1b2c3", "HAL214  ", "United States", 1730000000, 1730000000, -155.05, 19.72, 1524.0, false, 110.0, 275.0, 0.0, null, 1550.0],
          ["000000", "", "United States", 1730000000, 1730000000, null, 19.60, 500.0, true, 0.0, 0.0]
        ]
      }
    """.trimIndent()
    val tracks = OpenSkyAdsbRepository.parseOpenSkyStates(validOpenSky)
    assertEquals(1, tracks.size)
    assertEquals("A1B2C3", tracks[0].icao24)
    assertEquals("HAL214", tracks[0].callsign)
    assertEquals(5000.0, tracks[0].altitudeFt.toDouble(), 25.0)
    assertEquals(214.0, tracks[0].velocityKts.toDouble(), 5.0)
    assertEquals(275, tracks[0].headingDeg)
    assertFalse(tracks[0].onGround)

    assertTrue(OpenSkyAdsbRepository.parseOpenSkyStates("").isEmpty())
    assertTrue(OpenSkyAdsbRepository.parseOpenSkyStates("<html>502 Bad Gateway</html>").isEmpty())

    // 3. MapPreferencesStore round-trip persistence
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store = MapPreferencesStore(context)
    store.saveVisualMode(MapVisualThemeMode.COLOR_BLIND)
    store.saveSplitMode(MapSplitMode.WEATHER_ROUTES)
    store.saveAltitudeBand(AirspaceAltitudeBand.HIGH_FL180)
    store.saveQuickTools(listOf(QuickToolbarTool.FULLSCREEN, QuickToolbarTool.LEGEND, QuickToolbarTool.PIP_MODE))
    store.saveOfflineSectors(setOf("sector_big_island", "sector_puna_hilo"))
    store.saveWaypoints(waypoints)

    val loaded = store.load()
    assertEquals(MapVisualThemeMode.COLOR_BLIND, loaded.visualThemeMode)
    assertEquals(MapSplitMode.WEATHER_ROUTES, loaded.splitMode)
    assertEquals(AirspaceAltitudeBand.HIGH_FL180, loaded.altitudeBand)
    assertEquals(3, loaded.quickTools.size)
    assertTrue(loaded.offlineSectorIds.contains("sector_puna_hilo"))
    assertEquals(2, loaded.waypoints.size)
    assertEquals("PHKO", loaded.waypoints[1].label)

    // 4. Airspace Altitude Band filtering & WCAG/CVD Palette verification
    val lowZones = HawaiiAirspaceClassificationsCatalog.staticZones.filter {
      AirspaceAltitudeBand.LOW_SURFACE.matchesAltitudeRange(it.floorFtMsl, it.ceilingFtMsl)
    }
    val highZones = HawaiiAirspaceClassificationsCatalog.staticZones.filter {
      AirspaceAltitudeBand.HIGH_FL180.matchesAltitudeRange(it.floorFtMsl, it.ceilingFtMsl)
    }
    assertTrue(lowZones.any { it.classification == AirspaceClassification.CLASS_D })
    assertFalse(lowZones.any { it.classification == AirspaceClassification.CLASS_A })
    assertTrue(highZones.any { it.classification == AirspaceClassification.CLASS_A })
    assertFalse(highZones.any { it.classification == AirspaceClassification.CLASS_D })

    val hcPalette = paletteForMode(MapVisualThemeMode.HIGH_CONTRAST)
    val dayPalette = paletteForMode(MapVisualThemeMode.DAY)
    assertNotEquals(hcPalette.canvasBackground, dayPalette.canvasBackground)
    assertTrue(hcPalette.strokeMultiplier > 1.4f)
  }

  @Test
  fun `test full-screen mapping, split views, airspace filters, visual modes, waypoints, PiP, and legend UI`() {
    composeTestRule.waitForIdle()

    // Navigate to Hawaiʻi Airspace tab
    composeTestRule.onNodeWithTag("dicm_tab_airspace").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // 1. Launch Full-Screen Airspace viewer & verify HUD + North-Up + Split-Screen View + Exit
    composeTestRule.onNodeWithTag("airspace_fullscreen_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_map_viewer").assertExists()
    composeTestRule.onNodeWithTag("fullscreen_hud_telemetry").assertExists()

    composeTestRule.onNodeWithTag("fullscreen_drawer_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_control_drawer").assertExists()
    composeTestRule.onNodeWithTag("drawer_split_sat_airspace").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("fullscreen_drawer_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("split_map_container_sat_airspace").assertExists()
    composeTestRule.onNodeWithTag("split_pane_left").assertExists()
    composeTestRule.onNodeWithTag("split_pane_right").assertExists()
    composeTestRule.onNodeWithTag("fullscreen_bottom_exit_btn").assertExists()

    composeTestRule.onNodeWithTag("fullscreen_north_up_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_exit_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_map_viewer").assertDoesNotExist()

    // 2. Verify Airspace Classification, Altitude Band, Visual Modes, Waypoint Plotting, PiP & Legend
    composeTestRule.onNodeWithTag("airspace_overlay_options_row").performScrollTo()
    composeTestRule.onNodeWithTag("airspace_pip_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pip_mini_map_overlay").assertExists()
    composeTestRule.onNodeWithTag("pip_close_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pip_mini_map_overlay").assertDoesNotExist()

    composeTestRule.onNodeWithTag("airspace_overlay_options_row").performScrollTo()
    composeTestRule.onNodeWithTag("airspace_legend_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("context_map_legend_dialog").assertExists()
    composeTestRule.onNodeWithContentDescription("Close legend").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("map_visual_mode_row").performScrollTo()
    composeTestRule.onNodeWithTag("map_theme_high_contrast").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("map_theme_color_blind").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("map_theme_night").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("airspace_class_chips_row").performScrollTo()
    composeTestRule.onNodeWithTag("airspace_cls_class_d").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("airspace_altitude_row").performScrollTo()
    composeTestRule.onNodeWithTag("altitude_band_low").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("nav_add_waypoint_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("add_waypoint_label_input").performTextReplacement("KILAUEA-W")
    composeTestRule.onNodeWithTag("confirm_add_waypoint_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dynamic_route_summary_box").performScrollTo().assertExists()

    // 3. Switch to Satellite tab and verify sat_fullscreen_btn + split & visual mode chips
    composeTestRule.onNodeWithTag("nav_sat_feed").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sat_overlay_options_row")
      .performScrollTo()
      .performScrollToNode(hasTestTag("sat_visual_mode_btn"))
    composeTestRule.onNodeWithTag("sat_visual_mode_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sat_fullscreen_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_map_viewer").assertExists()
    composeTestRule.onNodeWithTag("fullscreen_bottom_exit_btn").assertExists().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("fullscreen_map_viewer").assertDoesNotExist()
    composeTestRule.onNodeWithTag("sat_overlay_options_row")
      .performScrollTo()
      .performScrollToNode(hasTestTag("sat_split_airspace_btn"))
    composeTestRule.onNodeWithTag("sat_split_airspace_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("split_map_container_sat_airspace").assertExists()
    composeTestRule.onNodeWithTag("fullscreen_exit_btn").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test WCAG contrastContentColor, GPS diagnostics, and satellite zoom sync into split view`() {
    val dayPalette = paletteForMode(MapVisualThemeMode.DAY)
    assertEquals(Color.White, contrastContentColor(dayPalette.advisoryBlue))
    assertEquals(Color.White, contrastContentColor(dayPalette.normalGreen))
    assertEquals(Color.White, contrastContentColor(dayPalette.tfrColor))
    assertEquals(Color.White, contrastContentColor(dayPalette.classAColor))

    val hcPalette = paletteForMode(MapVisualThemeMode.HIGH_CONTRAST)
    assertEquals(Color(0xFF020617), contrastContentColor(hcPalette.warningAmber))
    assertEquals(Color(0xFF020617), contrastContentColor(hcPalette.advisoryBlue))

    val context = ApplicationProvider.getApplicationContext<Context>()
    val tracker = DeviceLocationTracker(context)
    val outcome = tracker.startTrackingWithDiagnostics(onTelemetry = {})
    assertFalse(outcome.started)
    assertTrue(outcome.diagnosticNote.isNotBlank())

    val vm = ForensicViewModel()
    vm.attachRuntimeServices(context)
    vm.onLocationPermissionResult(false)
    assertFalse(vm.uiState.value.isGpsLiveEnabled)
    assertFalse(vm.uiState.value.gpsStatusNote.isNullOrBlank())

    vm.setMapSplitMode(MapSplitMode.SINGLE)
    vm.setSatVisualZoom(2.5f)
    vm.openFullScreenMap(FullScreenMapDomain.SATELLITE)
    vm.setMapSplitMode(MapSplitMode.SAT_AIRSPACE)
    assertEquals(2.5f, vm.uiState.value.airspaceMapZoom, 0.01f)
  }
}
