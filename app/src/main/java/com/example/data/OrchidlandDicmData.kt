package com.example.data

import com.example.R
import com.example.model.*
import java.util.ArrayDeque
import java.util.Locale

object OrchidlandDicmData {

    const val ORCHIDLAND_LAT = 19.5614
    const val ORCHIDLAND_LON = -155.0131
    const val ORCHIDLAND_AREA_SQ_MI = "9.71 sq mi (25.14 km²)"
    const val ORCHIDLAND_DEFAULT_DATE = "2024-08-14"

    val taskAgentProfiles: List<TaskAgentProfile> = listOf(
        TaskAgentProfile(
            id = "agent_srx",
            cryptoId = "AI-SRX-AG",
            name = "Simple Reflex AI",
            subtitle = "Stateless Facebook Group Swarm",
            category = TaskAgentCategory.AI_SWARM,
            emblemResId = R.drawable.ic_profile_ai_srx,
            coreCharacteristics = "Deterministic stimulus-response (if-then); stateless with zero world model or memory. Cheaply spun up in 10x-100x disposable swarms across Hawaiʻi Tracker & Puna pages.",
            typicalTasks = "High-frequency community spamming, phishing link injection, automated rumor replies, and keyword-triggered crisis scraping (0.18 → 33.75 posts/day crisis spike baseline).",
            orchidlandSignature = "Mass-replies on Hawaiʻi Tracker (118k subscribers) & Puna road/flood threads; mimics storm/lava inquiries (62 posts/day storm spike model).",
            snaRole = "HIGH-DEGREE TRAP (High Degree / Low Betweenness)",
            isHighDegreeTrap = true,
            isPrimaryTarget = false,
            degreeScore = 0.94f,
            betweennessScore = 0.08f,
            closenessScore = 0.56f,
            eigenvectorScore = 0.24f,
            primaryVulnerabilities = listOf(
                "No memory beyond immediate prompt; instantly stalled by malformed query injection",
                "Predictable burst cadence fails Meta behavioral anomaly classifiers",
                "High degree creates an illusion of strategic importance — terminating individual bots fails to degrade C2"
            ),
            pinOrUtilityProfile = "PIN/Utility: Maximizes raw volume (k) per dollar via deterministic regex triggers; zero adaptation to unexpected semantic tokens.",
            interdictionAction = "DO NOT whack-a-mole takedown. Deploy passive honeypot rules, malformed query injection, and poison world-model ingestion feeds."
        ),
        TaskAgentProfile(
            id = "agent_gdv",
            cryptoId = "AI-GDV-AG",
            name = "Goal-Driven AI",
            subtitle = "Aged Snapchat Persona & Funnel Router",
            category = TaskAgentCategory.AI_SWARM,
            emblemResId = R.drawable.ic_profile_ai_gdv,
            coreCharacteristics = "Tracks multi-turn dialogue state against explicit sub-goals; runs persistent aged personas tuned to transition targets across platforms.",
            typicalTasks = "Cross-platform migration (Facebook thread → Snapchat DM → WhatsApp Business), multi-turn trust grooming, and automated scheduling.",
            orchidlandSignature = "Operates aged Puna-local Snap accounts probing Hwy 130 traffic or bush-lot parcel listings before handing leads to encrypted WhatsApp endpoints.",
            snaRole = "Broadcaster & Funnel Aggregator (High Closeness)",
            isHighDegreeTrap = false,
            isPrimaryTarget = false,
            degreeScore = 0.58f,
            betweennessScore = 0.44f,
            closenessScore = 0.86f,
            eigenvectorScore = 0.52f,
            primaryVulnerabilities = listOf(
                "Context-window drift & persona break under adversarial Socratic questioning",
                "Deterministic funnel-goal weights cause predictable loops around Snapchat→WhatsApp migration links",
                "Subject to Snap Inc. 24/7 Emergency Disclosure turnaround (~30 min for imminent threats)"
            ),
            pinOrUtilityProfile = "Utility Function: Optimizes conversion rate P(WhatsApp_handoff | turn_count <= 8); rigid objective creates exploitable social-engineering friction.",
            interdictionAction = "Deploy persona-consistency probes & Context-Setter Snap Trust & Safety escalation + poison routing prompts."
        ),
        TaskAgentProfile(
            id = "agent_aut",
            cryptoId = "AI-AUT-AG",
            name = "Autonomous / Agentic AI",
            subtitle = "High-Betweenness Cross-Platform Broker",
            category = TaskAgentCategory.AI_SWARM,
            emblemResId = R.drawable.ic_profile_ai_aut,
            coreCharacteristics = "Adaptive tool-use agent maintaining persistent vector memory, dynamic API webhook orchestration, and multi-platform C2 bridging.",
            typicalTasks = "Orchestrates AI-SRX-AG swarms, rotates 96749/96760 residential proxy pools, links Snapchat leads to WhatsApp Business tokens, and dispatches HUM-OPR-AG.",
            orchidlandSignature = "Central algorithmic bridge connecting public OSINT/SOCMINT Puna chatter to encrypted WhatsApp Business queues and residential proxy nodes.",
            snaRole = "PRIMARY INTERDICTION TARGET (Low Degree / High Betweenness)",
            isHighDegreeTrap = false,
            isPrimaryTarget = true,
            degreeScore = 0.34f,
            betweennessScore = 0.96f,
            closenessScore = 0.89f,
            eigenvectorScore = 0.94f,
            primaryVulnerabilities = listOf(
                "API/Compute Starvation via malformed webhook payloads & rate-limit exhaustion",
                "Meta WhatsApp Business enforcement escalation (Oct 2025 / May 21 2026 regime: portfolio termination)",
                "Shared C2 webhook/token entity resolution leaks entire downstream swarm & proxy table"
            ),
            pinOrUtilityProfile = "Utility Function: Balances stealth (low active edges k=3..5) against operational throughput; single point of failure across 4 network partitions.",
            interdictionAction = "PRIORITY SEVER: Revoke WhatsApp Business portfolio via Meta Context-Setter report, sinkhole webhook tokens, and isolate 96749/96760 proxy gateways."
        ),
        TaskAgentProfile(
            id = "agent_hum",
            cryptoId = "HUM-OPR-AG",
            name = "Human Operative",
            subtitle = "Hwy 130 Courier & Bush-Lot Custodian",
            category = TaskAgentCategory.PHYSICAL_RF,
            emblemResId = R.drawable.ic_profile_hum_opr,
            coreCharacteristics = "Adaptive human node governed by cognitive biases, financial stress, and physical movement chokepoints; bridges encrypted C2 to ground parcels.",
            typicalTasks = "Manages physical drop locations across unbuilt 49%–52% bush lots (30th–42nd Aves), services residential router proxies, and transits Hwy 130.",
            orchidlandSignature = "Funneled through Hwy 130 MM 4–6 (>20,000 vpd bottleneck) & 29 mi of unpaved OLCA gravel roads; susceptible to Pohaku/39th–40th Ave flood isolation.",
            snaRole = "NON-REDUNDANT GROUND BROKER (Low Degree / High Betweenness)",
            isHighDegreeTrap = false,
            isPrimaryTarget = true,
            degreeScore = 0.32f,
            betweennessScore = 0.88f,
            closenessScore = 0.72f,
            eigenvectorScore = 0.81f,
            primaryVulnerabilities = listOf(
                "Hwy 130 sole-arterial chokepoint & 72.5% unpaved road wear/flood vulnerability",
                "PIN/PIDI pressure points: OLCA MRMA financial strain ($850k+ arrears) vs. personal security fear",
                "Strict legal boundary protection required (HRS §§ 708-813–815 trespass; judicial warrant for curtilage)"
            ),
            pinOrUtilityProfile = "PIN/PIDI Profile: Motivated by off-grid income/anonymity; fears property seizure, neighbor reporting, and HPD Pāhoa intercept on Hwy 130.",
            interdictionAction = "Correlate WhatsApp dispatch timestamps with Hwy 130 ingress windows; route lawful HPD Area I Pāhoa referrals."
        ),
        TaskAgentProfile(
            id = "agent_drone",
            cryptoId = "DRONE-FCE-AG",
            name = "Drone Force (sUAS)",
            subtitle = "Part 107 / Remote ID Aerial Node",
            category = TaskAgentCategory.PHYSICAL_RF,
            emblemResId = R.drawable.ic_profile_drone_fce,
            coreCharacteristics = "Physical aerial sensor/payload platform constrained by battery endurance, 35+ mph albizia shear winds, heavy rain (125–200 in/yr), and Class G airspace rules.",
            typicalTasks = "Wide-area canopy reconnaissance over 30–80 ft ʻōhiʻa / ≤200 ft albizia canopy, thermal/GIS cueing, or adversary illicit bush-lot overwatch.",
            orchidlandSignature = "Operates inside Class G (<400 ft AGL, outside PHTO 4.3 NM Class D); adversarial flights risk collision with brittle albizia or HELCO poles 2–5 ft inside ROW.",
            snaRole = "Physical Edge Relay & Vertical Sensor",
            isHighDegreeTrap = false,
            isPrimaryTarget = false,
            degreeScore = 0.28f,
            betweennessScore = 0.42f,
            closenessScore = 0.51f,
            eigenvectorScore = 0.48f,
            primaryVulnerabilities = listOf(
                "FAA Part 107 Remote ID broadcast fingerprinting exposes operator launch coordinates",
                "Weather & canopy fragility: ≤200 ft invasive albizia fails at ≥35 mph winds; rain attenuates optics",
                "State v. Quiday (Haw. Const. art. I, § 7) bars warrantless friendly drone curtilage surveillance"
            ),
            pinOrUtilityProfile = "Physical Telemetry Envelope: 18–28 min endurance under windward rain; Remote ID MAC/serial binds operator directly to 915 MHz / 2.4 GHz controller.",
            interdictionAction = "Capture FAA Remote ID passive telemetry within public ROW; obtain warrant before any curtilage overhead inspection."
        ),
        TaskAgentProfile(
            id = "agent_sigint",
            cryptoId = "SIGINT-AG",
            name = "SIGINT / RF Relay Agent",
            subtitle = "915 MHz LoRa Meshtastic & Cellular Shadow",
            category = TaskAgentCategory.PHYSICAL_RF,
            emblemResId = R.drawable.ic_profile_sigint,
            coreCharacteristics = "Automated or solar-mounted RF relay bridging cellular dead zones and compromised 96749/96760 fiber endpoints using ISM-band packet telemetry.",
            typicalTasks = "Maintains 915 MHz LoRa (Meshtastic) / VHF mesh hops across southern Orchidland cellular shadows (65.5% T-Mobile / patchy 5G coverage).",
            orchidlandSignature = "Solar nodes concealed inside dense ʻōhiʻa/albizia bush lots bridge off-grid couriers back to Starlink/Hawaiian Telcom Fiber ingress.",
            snaRole = "RF Shadow Bridge (High Closeness / Localized Betweenness)",
            isHighDegreeTrap = false,
            isPrimaryTarget = true,
            degreeScore = 0.40f,
            betweennessScore = 0.74f,
            closenessScore = 0.83f,
            eigenvectorScore = 0.69f,
            primaryVulnerabilities = listOf(
                "Passive RF direction-finding (DF) along 60 ft / 40 ft public OLCA ROW isolates unencrypted LoRa beacons",
                "Shared Meshtastic channel PSK keys & hop-count headers reveal full physical node topology",
                "Solar panel canopy shading during 125–200 in/yr rainfall induces predictable battery dropouts"
            ),
            pinOrUtilityProfile = "RF Signature: 915 MHz ISM packet bursts (SF7-SF12) every 15–60 min; deterministic hop timestamps correlate with WhatsApp webhook spikes.",
            interdictionAction = "Conduct passive ROW spectrum sweep from public corridors (Orchidland Dr / Pohaku); correlate LoRa timestamps against C2 egress."
        )
    )

    val platformNodes: List<PlatformNodeProfile> = listOf(
        PlatformNodeProfile(
            id = "plat_facebook",
            name = "Facebook / Hawaiʻi Tracker",
            categoryBadge = "SOCMINT ENTRY",
            emblemResId = R.drawable.ic_platform_facebook,
            funnelStage = "Stage 1: Top-of-Funnel Swarm & Crisis Scraping",
            orchidlandTelemetry = "Hawaiʻi Tracker (~118,000 subscribers); Volcanica 2018 baseline 0.18 → 33.75 posts/day eruption surge; 62 posts/day storm spike (Aug 24, 2018).",
            agentIds = listOf("AI-SRX-AG", "AI-GDV-AG"),
            extractionIndicators = listOf(
                "Template reply bursts on Puna flood/road/parcel posts",
                "Comment redirects pushing targets into ephemeral off-platform DMs",
                "High-Degree Trap topology: dozens of disposable profiles sharing copy-paste syntax"
            ),
            contextSetterMechanism = "CrowdTangle-style / Meta Coordinated Inauthentic Behavior (CIB) & automated spam burst reporting after graph cluster mapping.",
            mitigationStatusLabel = "PASSIVE OBSERVE & CLUSTER"
        ),
        PlatformNodeProfile(
            id = "plat_snapchat",
            name = "Snapchat Migration Bridge",
            categoryBadge = "EPHEMERAL HOP",
            emblemResId = R.drawable.ic_platform_snapchat,
            funnelStage = "Stage 2: Ephemeral Grooming & Target Migration",
            orchidlandTelemetry = "Aged local-persona accounts operated by AI-GDV-AG; Snap Inc. 24/7 Emergency Disclosure SLA ~30 minutes for imminent physical threats.",
            agentIds = listOf("AI-GDV-AG", "AI-AUT-AG"),
            extractionIndicators = listOf(
                "Sudden persona transitions from Puna community chatter to urgent encrypted off-ramping",
                "Repeated geolocation spoofing centered near Keaʻau / Kurtistown boundary",
                "Consistent DM script trees funneling victims toward verified WhatsApp Business numbers"
            ),
            contextSetterMechanism = "Snap Trust & Safety Law Enforcement Portal (imminent-harm 30-min disclosure SLA + account preservation requests).",
            mitigationStatusLabel = "PERSONA PROBE & PRESERVE"
        ),
        PlatformNodeProfile(
            id = "plat_whatsapp",
            name = "WhatsApp Business Platform",
            categoryBadge = "C2 END-OF-FUNNEL",
            emblemResId = R.drawable.ic_platform_whatsapp,
            funnelStage = "Stage 3: Encrypted Execution, Grooming & Dispatch",
            orchidlandTelemetry = "Meta enforcement transition (legacy tiers retired Oct 7, 2025 → portfolio regime effective May 21, 2026): template pause → 1–30d block → permanent portfolio ban.",
            agentIds = listOf("AI-AUT-AG", "HUM-OPR-AG"),
            extractionIndicators = listOf(
                "Non-opted-in promotional/phishing templates triggered by AI-AUT-AG webhook",
                "Linked phone-number / Business Manager ID shared across multiple Puna sub-cells",
                "Dispatch bursts aligned within ±5 min of Hwy 130 / LoRa courier movements"
            ),
            contextSetterMechanism = "Document opt-in violations + spoofed WABA portfolio IDs to trigger Meta full-portfolio termination instead of single-number bans.",
            mitigationStatusLabel = "PRIMARY SEVER POINT"
        ),
        PlatformNodeProfile(
            id = "plat_telecom",
            name = "96749/96760 Fiber & Proxy Farms",
            categoryBadge = "TELECOM / PROXY",
            emblemResId = R.drawable.ic_platform_telecom_proxy,
            funnelStage = "Infrastructure: Residential Proxy Exit & Cloud C2",
            orchidlandTelemetry = "Aggregators: 96.0%–99.2% HT Fiber (≤2,850 Mbps), 60% Spectrum Cable (1 Gbps), 100% Starlink; conservative baseline 52% fiber / 36% cable across 1,197–1,264 homes.",
            agentIds = listOf("AI-AUT-AG", "AI-SRX-AG"),
            extractionIndicators = listOf(
                "Compromised consumer routers in ZIP 96749 (Keaʻau) & 96760 (Kurtistown) reselling residential IP egress",
                "High-bandwidth overnight API polling inconsistent with household diurnal curve",
                "Multi-tenant Starlink / fiber fallback when HELCO poles fail in wind storms"
            ),
            contextSetterMechanism = "ISP abuse/ASN correlation + router compromise telemetry sharing to sinkhole residential proxy exit nodes.",
            mitigationStatusLabel = "ASN / PROXY SINKHOLE"
        ),
        PlatformNodeProfile(
            id = "plat_lora",
            name = "915 MHz LoRa & sUAS RF",
            categoryBadge = "RF / PHYSICAL",
            emblemResId = R.drawable.ic_platform_lora_rf,
            funnelStage = "Last-Mile: Southern Cellular Shadow & Canopy Corridor",
            orchidlandTelemetry = "T-Mobile 65.5% coverage + southern cellular shadow drives 915 MHz LoRa (Meshtastic) / VHF mesh & Class G sUAS (<400 ft AGL outside PHTO 4.3 NM).",
            agentIds = listOf("SIGINT-AG", "DRONE-FCE-AG", "HUM-OPR-AG"),
            extractionIndicators = listOf(
                "Recurring 915 MHz LoRa packet hops between bush-lot relays and Hwy 130 egress",
                "FAA Remote ID broadcasts intersecting 30th–42nd Ave unpaved 40 ft ROW",
                "Solar node voltage dropouts during sustained windward heavy rain"
            ),
            contextSetterMechanism = "Lawful passive spectrum monitoring from public ROW + FAA Remote ID correlation (strictly honoring State v. Quiday & HRS 708-814).",
            mitigationStatusLabel = "ROW PASSIVE DF"
        ),
        PlatformNodeProfile(
            id = "plat_toolstack",
            name = "6-Tool Multi-INT Fusion Stack",
            categoryBadge = "ANALYTIC CORE",
            emblemResId = R.drawable.ic_platform_toolstack,
            funnelStage = "Analytic Engine: OSINT+SOCMINT+SIGINT+COMINT+HUMINT+DARKINT",
            orchidlandTelemetry = "Unified stack: Maltego (transform graphs), NetworkX (Python SNA), Gephi (modular clustering), Palantir Gotham (multi-INT), Linkurious (graph UI), ArcGIS (Puna GIS).",
            agentIds = listOf("AI-AUT-AG", "AI-GDV-AG", "SIGINT-AG"),
            extractionIndicators = listOf(
                "Entity resolution linking C2 webhooks, WABA tokens, and proxy ASNs in Maltego/Gotham",
                "NetworkX Betweenness vs. Degree scatter exposing AI-SRX-AG traps vs. AI-AUT-AG bridges",
                "ArcGIS overlay of Lava Zone 3, UH 125–200 in/yr rain, and OLCA 60 ft/40 ft ROWs"
            ),
            contextSetterMechanism = "Produces court-admissible, warrant-ready intelligence packages linking digital C2 artifacts to lawful physical enforcement.",
            mitigationStatusLabel = "ACTIVE FUSION"
        )
    )

    val ipbDimensions: List<OrchidlandIpbDimension> = listOf(
        OrchidlandIpbDimension(
            id = "ipb_terrain",
            dimension = "Terrain, Lots & Roadways",
            verifiedBaseline = "9.71 sq mi (25.14 km²) Lower Puna; 518 ft CDP rising ≥781 ft mauka; ~49%–52% of 2,490 deeded parcels are dense unbuilt bush lots; ~40 mi private OLCA roads (72.5% unpaved gravel/dirt); 60 ft ROW (Orchidland Dr, 40th Ave, Pohaku) / 40 ft elsewhere; Hwy 130 MM 4–6 (>20,000 vpd) sole arterial bottleneck.",
            cyberPhysicalImplication = "Unbuilt bush lots conceal LoRa solar relays & stash points, while Hwy 130 acts as a deterministic single-point chokepoint for HUM-OPR-AG ingress/egress.",
            sourceCaveat = "Private OLCA road maintenance lags due to 49% parcel owner MRMA arrears (>$850k)."
        ),
        OrchidlandIpbDimension(
            id = "ipb_climate",
            dimension = "Weather, Hydrology & Canopy",
            verifiedBaseline = "UH Rainfall Atlas 125–200 in/yr windward rain; Aug 24, 2018 4 in/hr flood cut Pohaku Dr (39th–40th Ave); 30–80 ft ʻōhiʻa & ≤200 ft brittle albizia fail at ≥35 mph winds (2014 Iselle: $20M damage, 90% albizia, $15M HELCO).",
            cyberPhysicalImplication = "Heavy rainfall and 35+ mph albizia limb falls simultaneously degrade optical sUAS, solar LoRa charging, and overhead HELCO fiber/power — triggering adversary failover to Starlink.",
            sourceCaveat = "Coarse PWS models understate local rain (~57 in/yr vs. 125–200 in/yr actual UH contour)."
        ),
        OrchidlandIpbDimension(
            id = "ipb_telecom",
            dimension = "Telecom, Fiber & RF Coverage",
            verifiedBaseline = "Dual ZIPs 96749 (Keaʻau) & 96760 (Kurtistown); BestNeighborhood reports 96%–99.2% Hawaiian Telcom Fiber (≤2,850 Mbps), 60% Spectrum Cable (1 Gbps), 100% Starlink, 100% AT&T/VZW LTE, 65.5% T-Mobile; Reviews.org flags ~52% fiber / ~36% cable.",
            cyberPhysicalImplication = "High residential fiber penetration makes 96749/96760 attractive for compromised-router proxy farms hosting AI-AUT-AG, while southern T-Mobile dead zones force 915 MHz LoRa Meshtastic fallback.",
            sourceCaveat = "Aggregators diverge (96% vs. 52% fiber) because ~half of parcels are unbuilt lots without drops."
        ),
        OrchidlandIpbDimension(
            id = "ipb_utilities",
            dimension = "Water, Power & Fire Response",
            verifiedBaseline = "Zero municipal water, zero fire hydrants, zero sewer (5,000–10,000 gal catchment + septic); HELCO poles sit 2–5 ft inside ROW; HFD Stn 5 Keaʻau & Stn 18 HPP lack tankers; Stn 10 Pāhoa Tanker 10 holds 3,000 gal (750 GPM = 4 min flow).",
            cyberPhysicalImplication = "Kinetic/fire incidents or downed HELCO lines inside narrow unpaved corridors isolate sectors rapidly; battery backup & tanker sustainment limit field dwell time.",
            sourceCaveat = "HPD Area I Pāhoa Station (15-2615 Keaʻau-Pāhoa Rd) carries primary law enforcement jurisdiction."
        ),
        OrchidlandIpbDimension(
            id = "ipb_demo",
            dimension = "Demographics & Socioeconomic Stress",
            verifiedBaseline = "2020 Census: 3,165 pop / 1,264 housing units (6.8% vacant); ACS 2024: 3,027 pop / 1,197 units; Median HH income $73,936, per capita $27,428, 13.9% poverty.",
            cyberPhysicalImplication = "Economic vulnerability + high vacant bush-lot isolation create recruitment surface for proxy router hosting, social engineering, and illicit courier tasking.",
            sourceCaveat = "ACS 2024 small-area tracts carry ≥10% margin of error (MOE)."
        ),
        OrchidlandIpbDimension(
            id = "ipb_airspace",
            dimension = "Volcanic & Class G Airspace",
            verifiedBaseline = "USGS Lava Zone 3 (1%–5% inundation probability since 1800) with porous basalt & subsurface collapse pukas; Class G uncontrolled airspace (<400 ft AGL, safely outside PHTO 4.3 NM Class D).",
            cyberPhysicalImplication = "Lava pukas present extreme ankle/vehicle hazards during dismounted bush entry; Class G permits Part 107 sUAS flights above 80–200 ft canopy ONLY with continuous VLOS & warrant if curtilage.",
            sourceCaveat = "Rapid ʻŌhiʻa Death (ROD) quarantine strictly prohibits moving ʻōhiʻa/soil & mandates decon."
        )
    )

    val invertedMdmpSteps: List<InvertedMdmpStep> = listOf(
        InvertedMdmpStep(
            stepNumber = 1,
            title = "Mission Analysis (Inverted Adversarial Frame)",
            summary = "Deconstruct adversary objectives across Specified, Implied, and Mission-Essential tasks rather than assuming friendly intent.",
            orchidlandApplication = "Specified: Phish/groom Lower Puna targets & stage rural logistics. Implied: Bridge FB→Snap→WhatsApp + lease 96749/96760 proxies. Essential: Preserve anonymity across Hwy 130 & bush lots."
        ),
        InvertedMdmpStep(
            stepNumber = 2,
            title = "IPB & Operational Intelligence Integration",
            summary = "Fuse digital SOCMINT/DARKINT terrain with Orchidland physical terrain (roads, weather, power, cellular shadows).",
            orchidlandApplication = "Maps 96749/96760 fiber ingress against southern Orchidland 915 MHz LoRa dead zones and Aug-style 4 in/hr Pohaku flood choke points."
        ),
        InvertedMdmpStep(
            stepNumber = 3,
            title = "Enemy COA Development (MLCOA vs. MDCOA)",
            summary = "Model Most Likely Course of Action vs. Most Dangerous Course of Action to pre-stage mitigation triggers.",
            orchidlandApplication = "Predicts shift from routine FB swarm harvesting (MLCOA) to crisis-surge exploitation + off-grid LoRa/Starlink C2 during storm outages (MDCOA)."
        ),
        InvertedMdmpStep(
            stepNumber = 4,
            title = "Target-Centric COA Comparison & Selection",
            summary = "Prioritize non-redundant high-betweenness brokers (`AI-AUT-AG`, `HUM-OPR-AG`) over noisy high-degree swarms (`AI-SRX-AG`).",
            orchidlandApplication = "Executes WhatsApp Business portfolio revocation + proxy ASN sinkhole + lawful HPD Hwy 130 timing correlation."
        )
    )

    val coaScenarios: List<CoaScenario> = listOf(
        CoaScenario(
            code = "MLCOA",
            title = "Persistent Digital Funnel + Opportunistic Bush Staging",
            description = "Routine AI-SRX-AG swarms harvest leads on Hawaiʻi Tracker/Puna groups, AI-GDV-AG migrates targets via Snapchat to WhatsApp Business, while 1–2 local HUM-OPR-AG couriers service 96749 proxies and Hwy 130 drops.",
            triggerIndicators = listOf(
                "0.18–2.0 posts/day baseline spam comments on parcel/storm threads",
                "Aged Snap persona handoffs containing wa.me / WhatsApp Business links",
                "Overnight proxy egress spikes on 96749 Hawaiian Telcom residential IPs"
            )
        ),
        CoaScenario(
            code = "MDCOA",
            title = "Crisis-Surge Disinfo + Off-Grid LoRa/Starlink Autonomous Ops",
            description = "During a Kīlauea crisis or 35+ mph albizia storm outage (Pohaku flooded, HELCO down), swarms spike to 33.75–62 posts/day poisoning emergency info while AI-AUT-AG fails over to Starlink + 915 MHz LoRa Meshtastic relays in dense bush lots.",
            triggerIndicators = listOf(
                "Sudden 30–60x burst of automated disaster/evacuation misinformation on FB",
                "HELCO feeder drop paired with persistent Starlink + 915 MHz LoRa burst sync",
                "Unauthorized Part 107 sUAS flights without Remote ID over 30th–42nd Ave bush corridors"
            )
        )
    )

    val cogComparisons: List<CogComparison> = listOf(
        CogComparison(
            domain = "Strategic COG",
            primaryCog = "Multi-Platform Funnel Trust & Off-Island Compute/C2 Orchestration",
            criticalCapabilities = listOf(
                "Rapidly re-seed disposable FB/Snap personas at near-zero marginal cost",
                "Maintain persistent WhatsApp Business API & cloud LLM inference endpoints"
            ),
            criticalRequirements = listOf(
                "Verified Meta Business Manager portfolios & active WABA phone tokens",
                "Clean 96749/96760 residential IP reputation to bypass anti-bot WAFs"
            ),
            criticalVulnerabilities = listOf(
                "Meta May 2026 portfolio-level enforcement terminating entire verified WABA tree",
                "Shared webhook API tokens & ASN telemetry resolving seemingly separate cells"
            )
        ),
        CogComparison(
            domain = "Operational COG (Orchidland)",
            primaryCog = "Hwy 130 Logistics Corridor & Unbuilt Bush-Lot RF Sanctuary",
            criticalCapabilities = listOf(
                "Conceal LoRa/router infrastructure inside 49%–52% unbuilt dense bush parcels",
                "Move physical couriers/equipment rapidly between Keaʻau, Orchidland, and Pāhoa"
            ),
            criticalRequirements = listOf(
                "Uninterrupted transit through Hwy 130 MM 4–6 (>20,000 vpd)",
                "Continuous power/solar availability & clear 915 MHz ISM line-of-sight hops"
            ),
            criticalVulnerabilities = listOf(
                "Single-arterial Hwy 130 choke point & 72.5% unpaved gravel wear/flooding",
                "Passive RF DF from public 60 ft/40 ft ROWs & 125–200 in/yr canopy solar attenuation"
            )
        )
    )

    val strategicFrameworks: List<StrategicFrameworkBlock> = listOf(
        StrategicFrameworkBlock(
            frameworkCode = "SWOT",
            title = "Adversarial vs. Defender SWOT Matrix",
            items = listOf(
                "Strengths (Adv)" to "96%–99% HT Fiber availability for stealth proxies; 49%–52% dense bush cover; 30x–100x bot scale.",
                "Weaknesses (Adv)" to "Zero municipal redundancy; single Hwy 130 chokepoint; Meta May 2026 WABA portfolio ban exposure.",
                "Opportunities (Def)" to "High-betweenness broker decapitation (`AI-AUT-AG`); Snap 30-min disclosure SLA; ROW passive RF DF.",
                "Threats (Def)" to "Albizia 35+ mph windfalls; Pohaku 4 in/hr flash floods; ROD biosecurity & State v. Quiday suppression risk."
            )
        ),
        StrategicFrameworkBlock(
            frameworkCode = "PESTLE",
            title = "Orchidland PESTLE Environment",
            items = listOf(
                "Political / Legal" to "State v. Quiday warrant mandate for curtilage drones; HRS §§ 708-813–815 trespass; HRS Ch. 6E SHPD.",
                "Economic" to "$73,936 median HH income, 13.9% poverty, 49% OLCA MRMA arrears (>$850k) fueling proxy/courier temptation.",
                "Social" to "Hawaiʻi Tracker 118k community trust; crisis rumors spread rapidly across isolated catchment homes.",
                "Technological" to "Dual ZIP 96749/96760 fiber vs. southern 915 MHz LoRa Meshtastic & Starlink off-grid failovers.",
                "Environmental" to "Lava Zone 3 pukas, 125–200 in/yr UH rainfall, brittle ≤200 ft albizia, Rapid ʻŌhiʻa Death (ROD) quarantine."
            )
        ),
        StrategicFrameworkBlock(
            frameworkCode = "BSC / OKRs",
            title = "Balanced Scorecard & Operational OKRs",
            items = listOf(
                "OKR 1 (Graph Disruption)" to "Reduce Largest Connected Component ratio by ≥55% by severing AI-AUT-AG & HUM-OPR-AG brokers rather than AI-SRX-AG swarms.",
                "OKR 2 (Platform Enforcement)" to "Achieve <24h WABA portfolio escalation under Meta Context-Setter opt-in rules & <30m Snap imminent-harm triage.",
                "OKR 3 (Zero Legal Suppression)" to "100% compliance on ROD 70% alcohol decon, FAA Part 107 VLOS (<400 ft), and zero warrantless curtilage trespass."
            )
        )
    )

    val stakeholderQuadrants: List<StakeholderQuadrant> = listOf(
        StakeholderQuadrant(
            id = "stk_aut",
            actorName = "Autonomous Orchestrator (AI-AUT-AG) & Off-Island Handler",
            powerLevel = "HIGH POWER",
            interestLevel = "HIGH INTEREST",
            salienceClass = "Definitive Stakeholder (Power + Legitimacy + Urgency)",
            attributes = "Controls WABA tokens, proxy rotation, and tasking pipelines across all platforms.",
            posture = "Priority 1 Counter-Network Sever Target (Webhook sinkhole + WABA portfolio ban)."
        ),
        StakeholderQuadrant(
            id = "stk_hpd_meta",
            actorName = "HPD Area I Pāhoa, Snap 24/7 & Meta Trust/Safety",
            powerLevel = "HIGH POWER",
            interestLevel = "MODERATE / CONTEXTUAL",
            salienceClass = "Dormant / Expectant Institutional Lever",
            attributes = "Holds statutory arrest authority (Hwy 130) and platform-wide account termination authority.",
            posture = "Deliver court-grade Multi-INT evidence & opt-in violation packets (Context-Setter Role)."
        ),
        StakeholderQuadrant(
            id = "stk_olca",
            actorName = "OLCA Community & Hawaiʻi Tracker Moderators",
            powerLevel = "MODERATE POWER",
            interestLevel = "HIGH INTEREST",
            salienceClass = "Dependent / Ally Force Multiplier",
            attributes = "Controls private ROW road maintenance awareness & 118k-member local rumor inoculation.",
            posture = "Coordinate passive group moderation rules & flood/albizia hazard advisories."
        ),
        StakeholderQuadrant(
            id = "stk_srx",
            actorName = "Disposable Reflex Swarms (AI-SRX-AG)",
            powerLevel = "LOW STRATEGIC POWER",
            interestLevel = "HIGH NOISE VOLUME",
            salienceClass = "Demanding / Non-Salient Trap",
            attributes = "High degree count in Facebook threads, zero decision authority, regenerated in minutes.",
            posture = "Do NOT expend interdiction bandwidth on individual takedowns; monitor & poison inputs."
        )
    )

    val graphNodes: List<DicmGraphNode> = listOf(
        DicmGraphNode("n_aut_c2", "AI-AUT-AG Core", "AI-AUT-AG", GraphNodeClass.CYBER_AGENT, "agent_aut", 0.50f, 0.22f, "High-Betweenness bridge linking social funnels, proxies, and couriers."),
        DicmGraphNode("n_waba", "WhatsApp Biz API", "WABA-TKN", GraphNodeClass.TELECOM_PROXY, null, 0.72f, 0.34f, "Encrypted end-of-funnel Business Manager portfolio token."),
        DicmGraphNode("n_proxy96749", "96749 Fiber Proxy", "ASN-96749", GraphNodeClass.TELECOM_PROXY, null, 0.28f, 0.22f, "Compromised residential HT Fiber exit router in Keaʻau."),
        DicmGraphNode("n_gdv_snap", "AI-GDV-AG Snap", "AI-GDV-AG", GraphNodeClass.CYBER_AGENT, "agent_gdv", 0.72f, 0.14f, "Aged Snapchat persona steering Puna leads into WhatsApp."),
        DicmGraphNode("n_srx_1", "FB Swarm #1", "AI-SRX-01", GraphNodeClass.CYBER_AGENT, "agent_srx", 0.12f, 0.44f, "Hawaiʻi Tracker flood-thread reflex bot (Trap node)."),
        DicmGraphNode("n_srx_2", "FB Swarm #2", "AI-SRX-02", GraphNodeClass.CYBER_AGENT, "agent_srx", 0.16f, 0.62f, "Puna parcel spam reflex bot (Trap node)."),
        DicmGraphNode("n_srx_3", "FB Swarm #3", "AI-SRX-03", GraphNodeClass.CYBER_AGENT, "agent_srx", 0.30f, 0.56f, "Crisis-rumor reflex reply bot (Trap node)."),
        DicmGraphNode("n_srx_4", "FB Swarm #4", "AI-SRX-04", GraphNodeClass.CYBER_AGENT, "agent_srx", 0.10f, 0.24f, "Keyword scraper reflex bot (Trap node)."),
        DicmGraphNode("n_sigint", "915MHz LoRa Node", "SIGINT-AG", GraphNodeClass.TERRAIN_RF, "agent_sigint", 0.52f, 0.52f, "Southern Orchidland Meshtastic solar relay inside bush canopy."),
        DicmGraphNode("n_hum_opr", "HUM-OPR-AG Courier", "HUM-OPR-AG", GraphNodeClass.HUMAN_CELL, "agent_hum", 0.68f, 0.68f, "Non-redundant human broker transiting Hwy 130 MM 4-6."),
        DicmGraphNode("n_bush_stash", "38th Ave Bush Lot", "TER-38AVE", GraphNodeClass.TERRAIN_RF, null, 0.46f, 0.84f, "Unbuilt dense ʻōhiʻa/albizia parcel staging ground."),
        DicmGraphNode("n_hwy130", "Hwy 130 MM 4-6", "TER-HWY130", GraphNodeClass.TERRAIN_RF, null, 0.86f, 0.82f, "Sole 20,000+ vpd arterial chokepoint to Keaʻau/Pāhoa."),
        DicmGraphNode("n_drone", "Part 107 sUAS", "DRONE-FCE", GraphNodeClass.TERRAIN_RF, "agent_drone", 0.86f, 0.54f, "Remote ID aerial overwatch node below 400 ft AGL Class G.")
    )

    val graphEdges: List<DicmGraphEdge> = listOf(
        // High-Degree Trap local clique among SRX bots + single uplink
        DicmGraphEdge("n_srx_1", "n_srx_2", GraphEdgeClass.SOCMINT, 0.8f, "FB thread co-reply swarm"),
        DicmGraphEdge("n_srx_2", "n_srx_3", GraphEdgeClass.SOCMINT, 0.8f, "FB thread co-reply swarm"),
        DicmGraphEdge("n_srx_3", "n_srx_1", GraphEdgeClass.SOCMINT, 0.8f, "FB thread co-reply swarm"),
        DicmGraphEdge("n_srx_4", "n_srx_1", GraphEdgeClass.SOCMINT, 0.7f, "Shared copy-paste prompt"),
        DicmGraphEdge("n_srx_4", "n_srx_3", GraphEdgeClass.SOCMINT, 0.7f, "Shared copy-paste prompt"),
        DicmGraphEdge("n_srx_4", "n_proxy96749", GraphEdgeClass.COMMS, 0.9f, "96749 residential proxy egress"),
        DicmGraphEdge("n_srx_3", "n_gdv_snap", GraphEdgeClass.SOCMINT, 0.6f, "Lead scrape handoff"),
        // Core C2 backbone (AI-AUT-AG bridges proxy/Snap funnel into WABA & LoRa)
        DicmGraphEdge("n_proxy96749", "n_aut_c2", GraphEdgeClass.COMMS, 1.0f, "Rotating residential IP tunnel"),
        DicmGraphEdge("n_gdv_snap", "n_aut_c2", GraphEdgeClass.COMMAND, 0.95f, "Snap lead -> AI-AUT-AG routing webhook"),
        DicmGraphEdge("n_aut_c2", "n_waba", GraphEdgeClass.COMMAND, 1.0f, "WhatsApp Business API token C2"),
        DicmGraphEdge("n_aut_c2", "n_sigint", GraphEdgeClass.COMMS, 0.85f, "Starlink/Fiber -> LoRa gateway"),
        // Cyber-to-Physical bridge
        DicmGraphEdge("n_waba", "n_hum_opr", GraphEdgeClass.FINANCE, 0.95f, "Encrypted dispatch & payout"),
        DicmGraphEdge("n_sigint", "n_hum_opr", GraphEdgeClass.COMMS, 0.9f, "915 MHz Meshtastic off-grid cue"),
        DicmGraphEdge("n_hum_opr", "n_bush_stash", GraphEdgeClass.PHYSICAL, 1.0f, "40 ft unpaved gravel access"),
        DicmGraphEdge("n_hum_opr", "n_hwy130", GraphEdgeClass.PHYSICAL, 1.0f, "Sole ingress/egress chokepoint"),
        DicmGraphEdge("n_hum_opr", "n_drone", GraphEdgeClass.COMMAND, 0.75f, "2.4 GHz Remote ID controller"),
        DicmGraphEdge("n_drone", "n_bush_stash", GraphEdgeClass.PHYSICAL, 0.65f, "Canopy overwatch")
    )

    val nistCountermeasures: List<NistCountermeasure> = listOf(
        NistCountermeasure(
            id = "nist_id",
            nistPhase = "1. IDENTIFY",
            title = "Multi-INT Entity Resolution & Bot Class Profiling",
            targetClass = "AI-AUT-AG / 96749 Proxies",
            mechanism = "Fuse OSINT, SOCMINT, SIGINT, COMINT, HUMINT, and DARKINT in Maltego/NetworkX to separate High-Degree Traps (AI-SRX-AG) from High-Betweenness Bridges.",
            impactScore = 22
        ),
        NistCountermeasure(
            id = "nist_pr",
            nistPhase = "2. PROTECT",
            title = "Defensive Digital Terrain & Context-Setter Opt-In Audits",
            targetClass = "Hawaiʻi Tracker / OLCA Residents",
            mechanism = "Harden community group posting throttles, inoculate Puna storm/lava threads, and enforce 96749/96760 ISP router hygiene.",
            impactScore = 18
        ),
        NistCountermeasure(
            id = "nist_de",
            nistPhase = "3. DETECT",
            title = "Adversarial Anomaly & 915 MHz LoRa DF Monitoring",
            targetClass = "AI-GDV-AG / SIGINT-AG",
            mechanism = "Flag storm-spike post bursts (>33.75 posts/day), detect Socratic persona-break loops on Snap, and log LoRa bursts along 60 ft public ROW.",
            impactScore = 20
        ),
        NistCountermeasure(
            id = "nist_rs",
            nistPhase = "4. RESPOND",
            title = "Algorithmic Poisoning, Compute Starvation & Meta Portfolio Ban",
            targetClass = "AI-AUT-AG / WhatsApp Business",
            mechanism = "Inject malformed queries into AI-SRX-AG, starve AI-AUT-AG webhooks, file Meta May 2026 WABA portfolio abuse & Snap 30-min emergency requests.",
            impactScore = 28
        ),
        NistCountermeasure(
            id = "nist_rc",
            nistPhase = "5. RECOVER",
            title = "Post-Interdiction Re-Constitution Watch & Legal Review",
            targetClass = "HUM-OPR-AG / 38th Ave Corridor",
            mechanism = "Monitor for backup Starlink/VHF re-seeding, verify zero suppression risk under State v. Quiday, and document chain of custody for HPD Pāhoa.",
            impactScore = 12
        )
    )

    val jointTargetingSteps: List<JointTargetingStep> = listOf(
        JointTargetingStep(
            phaseNumber = 1,
            title = "End State & Commander's Objectives",
            leadRole = "DICM Strategic Lead + Legal Counsel",
            cyberAction = "Define measurable fragmentation target (sever AI-AUT-AG & WABA funnel while preserving SOCMINT visibility on FB swarms).",
            orchidlandGuardrail = "Strict adherence to Haw. Const. art. I, § 7; zero private-property trespass."
        ),
        JointTargetingStep(
            phaseNumber = 2,
            title = "Target Development & SNA Prioritization",
            leadRole = "NetworkX / Gephi SNA Cell",
            cyberAction = "Compute Betweenness vs. Degree; reject High-Degree AI-SRX-AG trap; nominate AI-AUT-AG, WABA tokens, and HUM-OPR-AG.",
            orchidlandGuardrail = "Corroborate at least 2 independent INT streams before nominating physical nodes."
        ),
        JointTargetingStep(
            phaseNumber = 3,
            title = "Capabilities Analysis (Cyber & Context-Setter)",
            leadRole = "Cyber Ops & Trust/Safety Liaison",
            cyberAction = "Select non-kinetic Context-Setter levers: Meta Business Portfolio takedown, Snap 30-min disclosure, ISP proxy sinkhole, malformed query traps.",
            orchidlandGuardrail = "HFD Tanker 10 capacity (3,000 gal / 4 min flow) & windward albizia hazard check before any field movement."
        ),
        JointTargetingStep(
            phaseNumber = 4,
            title = "Commander's Decision & ROE Allocation",
            leadRole = "Operations Commander + HPD Area I Liaison",
            cyberAction = "Synchronize digital WABA severance window with Hwy 130 MM 4–6 traffic observation window.",
            orchidlandGuardrail = "FAA Part 107 (<400 ft, Remote ID, continuous VLOS above 80–200 ft canopy)."
        ),
        JointTargetingStep(
            phaseNumber = 5,
            title = "Mission Planning & Synchronized Execution",
            leadRole = "Cyber Task Force + Field Biosecurity Lead",
            cyberAction = "Execute API starvation + Meta/Snap escalation packets simultaneously so HUM-OPR-AG cannot migrate C2 channels.",
            orchidlandGuardrail = "Mandatory ROD decon (70% isopropyl ≥15s on boots; pressure-wash undercarriage) + HRS Ch. 6E archaeological check."
        ),
        JointTargetingStep(
            phaseNumber = 6,
            title = "Combat Assessment (BDA & Re-attack)",
            leadRole = "Multi-INT Fusion Cell (Palantir / ArcGIS)",
            cyberAction = "Verify drop in connected-component ratio, measure FB→WhatsApp funnel collapse, and detect Starlink/LoRa re-constitution attempts.",
            orchidlandGuardrail = "Archive cryptographic SHA-256 audit log to VeriScope Casebook for judicial admissibility."
        )
    )

    val complianceGuardrails: List<ComplianceGuardrailItem> = listOf(
        ComplianceGuardrailItem(
            id = "grd_quiday",
            statuteOrStandard = "State v. Quiday / Haw. Const. art. I, § 7",
            title = "Warrant Mandate for Curtilage Drone Surveillance",
            requirement = "Zero warrantless sUAS overflight or optical inspection of residential curtilage. Friendly aerial sensors restricted strictly to public corridors or judicial warrant."
        ),
        ComplianceGuardrailItem(
            id = "grd_faa",
            statuteOrStandard = "FAA Part 107 (Class G Airspace)",
            title = "Altitude <400 ft AGL, Remote ID & VLOS",
            requirement = "Orchidland sits outside PHTO 4.3 NM Class D in Class G; operations require continuous VLOS above 80–200 ft albizia/ʻōhiʻa canopy & active Remote ID."
        ),
        ComplianceGuardrailItem(
            id = "grd_trespass",
            statuteOrStandard = "HRS §§ 708-813 to 708-815",
            title = "Private Road & Unbuilt Parcel Trespass Boundaries",
            requirement = "OLCA ~40 mi roads are private; remain inside verified public/authorized 60 ft & 40 ft ROWs unless executing explicit HPD warrant."
        ),
        ComplianceGuardrailItem(
            id = "grd_shpd",
            statuteOrStandard = "HRS Chapter 6E (SHPD)",
            title = "Burial & Archaeological Site Preservation",
            requirement = "Consult State Historic Preservation Division records prior to any physical ground disturbance or excavation near lava pukas/historical walls."
        ),
        ComplianceGuardrailItem(
            id = "grd_rod",
            statuteOrStandard = "Haw. Code R. § 4-72-13 (Rapid ʻŌhiʻa Death)",
            title = "Mandatory Biosecurity Decontamination Protocol",
            requirement = "Spray boot soles with ≥70% isopropyl alcohol for ≥15 seconds; detergent pressure-wash vehicle tires & undercarriages; zero off-island transport of ʻōhiʻa or soil."
        )
    )
}

/**
 * Pure Kotlin Social Network Analysis (SNA) Engine.
 * Computes exact BFS shortest-path Betweenness Centrality, Closeness Centrality,
 * Degree Centrality, and Connected-Component Fragmentation % as nodes are isolated.
 */
object DicmSnaEngine {

    fun analyzeNetwork(isolatedNodeIds: Set<String>): SnaNetworkReport {
        val activeNodes = OrchidlandDicmData.graphNodes.filter { !isolatedNodeIds.contains(it.id) }
        val activeIds = activeNodes.map { it.id }.toSet()
        val activeEdges = OrchidlandDicmData.graphEdges.filter {
            activeIds.contains(it.fromId) && activeIds.contains(it.toId)
        }

        val adj = mutableMapOf<String, MutableList<String>>()
        activeNodes.forEach { adj[it.id] = mutableListOf() }
        activeEdges.forEach { edge ->
            adj[edge.fromId]?.add(edge.toId)
            adj[edge.toId]?.add(edge.fromId)
        }

        val n = activeNodes.size
        val betweenness = mutableMapOf<String, Double>().apply {
            activeNodes.forEach { put(it.id, 0.0) }
        }
        val closeness = mutableMapOf<String, Double>().apply {
            activeNodes.forEach { put(it.id, 0.0) }
        }

        // Brandes-style unweighted BFS for exact Betweenness & Closeness
        for (s in activeNodes) {
            val stack = ArrayDeque<String>()
            val pred = mutableMapOf<String, MutableList<String>>().apply {
                activeNodes.forEach { put(it.id, mutableListOf()) }
            }
            val sigma = mutableMapOf<String, Double>().apply {
                activeNodes.forEach { put(it.id, 0.0) }
                put(s.id, 1.0)
            }
            val dist = mutableMapOf<String, Int>().apply {
                activeNodes.forEach { put(it.id, -1) }
                put(s.id, 0)
            }
            val queue = ArrayDeque<String>()
            queue.add(s.id)

            var distSum = 0
            var reachable = 0
            while (queue.isNotEmpty()) {
                val v = queue.removeFirst()
                stack.addFirst(v)
                val dv = dist[v] ?: 0
                if (v != s.id) {
                    distSum += dv
                    reachable++
                }
                for (w in adj[v].orEmpty()) {
                    if ((dist[w] ?: -1) < 0) {
                        queue.addLast(w)
                        dist[w] = dv + 1
                    }
                    if (dist[w] == dv + 1) {
                        sigma[w] = (sigma[w] ?: 0.0) + (sigma[v] ?: 0.0)
                        pred[w]?.add(v)
                    }
                }
            }

            if (distSum > 0 && n > 1) {
                val reachScale = reachable.toDouble() / (n - 1).toDouble()
                closeness[s.id] = (reachable.toDouble() / distSum.toDouble()) * reachScale
            }

            val delta = mutableMapOf<String, Double>().apply {
                activeNodes.forEach { put(it.id, 0.0) }
            }
            while (stack.isNotEmpty()) {
                val w = stack.removeFirst()
                val sw = sigma[w] ?: 1.0
                for (v in pred[w].orEmpty()) {
                    val sv = sigma[v] ?: 0.0
                    val c = (sv / sw) * (1.0 + (delta[w] ?: 0.0))
                    delta[v] = (delta[v] ?: 0.0) + c
                }
                if (w != s.id) {
                    betweenness[w] = (betweenness[w] ?: 0.0) + (delta[w] ?: 0.0)
                }
            }
        }

        // Connected components & fragmentation
        val visited = mutableSetOf<String>()
        val componentSizes = mutableListOf<Int>()
        for (node in activeNodes) {
            if (visited.contains(node.id)) continue
            var compSize = 0
            val q = ArrayDeque<String>()
            q.add(node.id)
            visited.add(node.id)
            while (q.isNotEmpty()) {
                val cur = q.removeFirst()
                compSize++
                for (nxt in adj[cur].orEmpty()) {
                    if (!visited.contains(nxt)) {
                        visited.add(nxt)
                        q.addLast(nxt)
                    }
                }
            }
            componentSizes.add(compSize)
        }

        val totalBaseNodes = OrchidlandDicmData.graphNodes.size.coerceAtLeast(1)
        val largestComp = componentSizes.maxOrNull() ?: 0
        val largestRatio = largestComp.toFloat() / totalBaseNodes.toFloat()
        val fragmentationPct = ((1f - largestRatio) * 100f).coerceIn(0f, 100f)

        val maxBetw = betweenness.values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
        val maxDeg = activeNodes.maxOfOrNull { adj[it.id]?.size ?: 0 }?.coerceAtLeast(1) ?: 1

        val metrics = activeNodes.map { node ->
            val deg = adj[node.id]?.size ?: 0
            val degNorm = (deg.toFloat() / maxDeg.toFloat()).coerceIn(0f, 1f)
            val betwNorm = ((betweenness[node.id] ?: 0.0) / maxBetw).toFloat().coerceIn(0f, 1f)
            val closeNorm = (closeness[node.id] ?: 0.0).toFloat().coerceIn(0f, 1f)

            val tag = when {
                betwNorm >= 0.60f -> "PRIMARY BRIDGE (High Betweenness)"
                node.agentProfileId == "agent_srx" || (degNorm >= 0.65f && betwNorm < 0.25f) -> "HIGH-DEGREE TRAP"
                closeNorm >= 0.55f -> "BROADCASTER / RELAY"
                else -> "PERIPHERAL NODE"
            }

            SnaNodeComputedMetric(
                node = node,
                activeDegree = deg,
                degreeNorm = degNorm,
                betweennessNorm = betwNorm,
                closenessNorm = closeNorm,
                diagnosticTag = tag
            )
        }.sortedByDescending { it.betweennessNorm }

        val assessment = when {
            isolatedNodeIds.isEmpty() ->
                "Baseline Topology: FB swarms (AI-SRX-01..04) show high local degree but near-zero betweenness. Target AI-AUT-AG, WABA-TKN, or HUM-OPR-AG bridges."
            isolatedNodeIds.all { it.startsWith("n_srx") } ->
                String.format(
                    Locale.US,
                    "HIGH-DEGREE TRAP CONFIRMED: Isolating FB swarm bots yielded only %.0f%% network disruption — C2 & physical bridges remain intact!",
                    fragmentationPct
                )
            fragmentationPct >= 50f ->
                String.format(
                    Locale.US,
                    "CRITICAL C2 FRAGMENTATION (%.0f%%): Severing high-betweenness brokers split the hybrid graph into %d isolated components.",
                    fragmentationPct,
                    componentSizes.size
                )
            else ->
                String.format(
                    Locale.US,
                    "Partial Disruption (%.0f%% fragmented, %d components). Isolate remaining cyber/ground brokers for full separation.",
                    fragmentationPct,
                    componentSizes.size
                )
        }

        return SnaNetworkReport(
            totalActiveNodes = activeNodes.size,
            totalActiveEdges = activeEdges.size,
            connectedComponents = componentSizes.size,
            fragmentationPercent = fragmentationPct,
            largestComponentRatio = largestRatio,
            isolatedNodeIds = isolatedNodeIds,
            nodeMetrics = metrics,
            tacticalAssessment = assessment
        )
    }
}
