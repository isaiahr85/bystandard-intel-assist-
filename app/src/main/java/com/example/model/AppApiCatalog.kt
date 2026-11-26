package com.example.model

/**
 * Authoritative registry of every external REST/WMS/WFS endpoint, public telemetry source,
 * optional social connector, and Android system API integrated into VeriScope, along with
 * its app relevance, feature usage, and non-affiliation disclaimer.
 */
data class AppApiDescriptor(
    val id: String,
    val name: String,
    val provider: String,
    val endpointOrInterface: String,
    val appRelevance: String,
    val howUsedInApp: String,
    val disclaimerNote: String
)

object AppApiCatalog {
    const val NON_AFFILIATION_HEADER =
        "NON-AFFILIATION & NON-ENDORSEMENT NOTICE"

    const val NON_AFFILIATION_BODY =
        "VeriScope is an independent, non-partisan public-interest information, situational awareness, and behavioral threat assessment workflow application. " +
            "VeriScope is NOT affiliated with, endorsed by, sponsored by, authorized by, or acting on behalf of the U.S. Secret Service (NTAC), " +
            "U.S. Department of Homeland Security, NASA, NOAA / National Weather Service, FAA, USGS, FEMA, Hawaiʻi County, University of Hawaiʻi, " +
            "OpenSky Network, Meta / Facebook, Telegram, or any federal, state, local, or foreign government entity."

    const val USE_AT_OWN_RISK_BODY =
        "USE AT YOUR OWN RISK & INFORMATIONAL-ONLY POLICY: All external API feeds, satellite imagery, Doppler radar overlays, ADS-B flight tracks, " +
            "weather/volcano still cameras, civic policy summaries, community reports, and on-device AI outputs are provided strictly 'AS IS' and " +
            "'AS AVAILABLE' for informational awareness only. Data may be delayed, incomplete, cached, or unavailable. Do NOT use this application " +
            "for primary aviation navigation, ATC compliance, emergency dispatch, clinical/medical diagnosis, legal advice, or political persuasion. " +
            "In any life-safety emergency, immediately call 911 or local emergency services directly. By entering a workspace role, you acknowledge " +
            "and accept sole responsibility for how you interpret or act upon any content displayed."

    val entries: List<AppApiDescriptor> = listOf(
        AppApiDescriptor(
            id = "api_nasa_eonet",
            name = "NASA EONET v3 Events API",
            provider = "NASA Earth Observatory (Public Open API — No Endorsement)",
            endpointOrInterface = "https://eonet.gsfc.nasa.gov/api/v3/events",
            appRelevance = "Supplies open natural hazard coordinates (wildfires, volcanoes, severe storms, floods) so analysts and community coordinators can cross-check incident context.",
            howUsedInApp = "Used in Sat Feed tab and Casebook ingestion to list active geospatial hazard events and center NASA satellite snapshots.",
            disclaimerNote = "Informational environmental data only; delayed relative to official emergency alerts."
        ),
        AppApiDescriptor(
            id = "api_nasa_gibs",
            name = "NASA Worldview / GIBS Snapshot API",
            provider = "NASA EOSDIS Global Imagery Browse Services (No Endorsement)",
            endpointOrInterface = "https://wvs.earthdata.nasa.gov/api/v1/snapshot",
            appRelevance = "Provides VIIRS/MODIS true-color, thermal anomaly (375m), and SWIR burn imagery for visual hazard and chronolocation inspection.",
            howUsedInApp = "Used in Sat Feed, Hawaiʻi Airspace satellite overlay, and locked-aspect full-screen/zoom calibration viewer.",
            disclaimerNote = "Orbital swath cadence means passes may lag by hours and include cloud cover."
        ),
        AppApiDescriptor(
            id = "api_faa_tfr",
            name = "FAA TFR GeoServer WFS & Advisory Feed",
            provider = "Federal Aviation Administration Public Feed (No Affiliation)",
            endpointOrInterface = "https://tfr.faa.gov/geoserver/TFR/ows",
            appRelevance = "Helps regional safety observers monitor published Temporary Flight Restrictions and Pohakuloa / Hawaiʻi hazard airspace notices.",
            howUsedInApp = "Used in Dashboard Hawaiʻi Airspace tab and full-screen tactical map overlays.",
            disclaimerNote = "NOT a substitute for official FAA NOTAM briefings or pre-flight legal clearances."
        ),
        AppApiDescriptor(
            id = "api_noaa_nws_alerts",
            name = "NOAA / NWS Weather Alerts API",
            provider = "National Weather Service (api.weather.gov — No Endorsement)",
            endpointOrInterface = "https://api.weather.gov/alerts/active?area=HI",
            appRelevance = "Delivers public severe weather warnings, flood watches, and civil advisories relevant to Hawaiʻi community safety.",
            howUsedInApp = "Used in Hawaiʻi Airspace status cards and disaster recovery policy alerts.",
            disclaimerNote = "Verify life-safety weather actions via official NOAA Weather Radio / Civil Defense broadcasts."
        ),
        AppApiDescriptor(
            id = "api_noaa_nexrad",
            name = "NOAA / NCEP MRMS & NEXRAD Doppler WMS",
            provider = "NOAA NCEP OpenGeo Server (PHWA / PHKM — No Affiliation)",
            endpointOrInterface = "https://opengeo.ncep.noaa.gov/geoserver/",
            appRelevance = "Renders live Hawaiʻi precipitation reflectivity and radial wind velocity over regional maps during storms.",
            howUsedInApp = "Used in Airspace Radar layer toggles and Split-Map Weather/Route overlay views.",
            disclaimerNote = "For situational weather awareness only; never use for tactical flight penetration."
        ),
        AppApiDescriptor(
            id = "api_faa_weathercams",
            name = "FAA WeatherCams Site & Image API",
            provider = "FAA WeatherCams Public Portal (No Endorsement)",
            endpointOrInterface = "https://api.weathercams.faa.gov/",
            appRelevance = "Provides near-real-time airport corridor visibility and cloud-ceiling still frames across Hawaiʻi stations.",
            howUsedInApp = "Used in Dashboard Hawaiʻi Live Still Cams carousel, filter chips, and lightbox modal.",
            disclaimerNote = "Still frames reflect camera timestamp only and do not verify current runway/road safety."
        ),
        AppApiDescriptor(
            id = "api_usgs_uh_hilo_cams",
            name = "USGS HVO, UH Maunakea & Hilo Bay Public Stills",
            provider = "USGS HVO / UH IfA / CFHT / Gemini / Tsunami.org (Independent)",
            endpointOrInterface = "HTTPS static camera endpoints & https://tsunami.org/hilobaycam/",
            appRelevance = "Offers visual surface reference for Kīlauea/Mauna Loa volcanic plumes, Saddle/summit weather, and downtown Hilo Bay.",
            howUsedInApp = "Used in Hilo Cam Monitor module and Live Still Cams tab with official portal links.",
            disclaimerNote = "Third-party bandwidth limits apply; open external portal links directly at your own discretion."
        ),
        AppApiDescriptor(
            id = "api_opensky_adsb",
            name = "OpenSky Network ADS-B State Vector API",
            provider = "OpenSky Network (opensky-network.org — Independent)",
            endpointOrInterface = "https://opensky-network.org/api/states/all",
            appRelevance = "Supplies crowd-sourced aircraft transponder position, altitude, and heading vectors in the regional bounding box.",
            howUsedInApp = "Optional ADS-B toggle overlay inside Airspace & Full-Screen Map views.",
            disclaimerNote = "Crowd-sourced receiver coverage has blind spots and rate limits; informational only."
        ),
        AppApiDescriptor(
            id = "api_meta_telegram",
            name = "Meta Graph API v21.0 & Telegram Bot API",
            provider = "Meta Platforms & Telegram (Optional Operator-Configured Secrets)",
            endpointOrInterface = "https://graph.facebook.com/ & https://api.telegram.org/",
            appRelevance = "Supports participating organization narrative triage queues and human-approved public clarification dispatches.",
            howUsedInApp = "Used in Cases & Intel -> FB / TG Ops tab (defaults to safe local dry-run when secrets are absent).",
            disclaimerNote = "Human approval is strictly mandatory before any outbound reply; no automated mass messaging."
        ),
        AppApiDescriptor(
            id = "api_android_system",
            name = "Android Phone Dialer (911), LocationManager, TTS & DisplayManager",
            provider = "Android OS Platform System APIs",
            endpointOrInterface = "Intent.ACTION_DIAL (tel:911), LocationManager, TextToSpeech, DisplayManager",
            appRelevance = "Provides pocket-dial-guarded 911 dialer handoff, consent-gated GPS site positioning, accessible voice readout, and PiP/external monitor layout.",
            howUsedInApp = "911 buttons require explicit 'Are you sure?' dialog; GPS requires user permission & toggle; TTS powers Select-to-Speak.",
            disclaimerNote = "ACTION_DIAL pre-fills 911 in your phone app without auto-placing calls silently."
        ),
        AppApiDescriptor(
            id = "api_civic_ondevice_ai",
            name = "On-Device Civic AI, RL Personalization & Policy Engine",
            provider = "Local On-Device Kotlin Engines (Zero Raw PII Cloud Sync)",
            endpointOrInterface = "com.example.civic.* & com.example.model.BtamPiiScrubber",
            appRelevance = "Adapts public resource briefings, scrubs PII from bystander/community notes, blocks partisan steering or demographic profiling, and gates refinements behind human sign-off.",
            howUsedInApp = "Powers BTAM Gateway PII scrubbing, anti-profiling checks, and Cases & Intel -> Civic AI.",
            disclaimerNote = "AI-generated insights, forecasts, and speculative projections are explicitly badged as non-authoritative."
        )
    )
}
