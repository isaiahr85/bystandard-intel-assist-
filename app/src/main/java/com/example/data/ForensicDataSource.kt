package com.example.data

import com.example.model.*

object ForensicDataSource {

    val repositories = listOf(
        ArchivalRepository(
            id = "repo_bellingcat",
            name = "Bellingcat Open-Source Archives",
            organization = "Bellingcat Investigative Consortium",
            accessTier = AccessTier.PUBLIC_OPEN,
            primaryAssetClass = "OSINT investigations, geolocated video/photo, munitions identification",
            metadataQuality = MetadataQuality.HIGH,
            metadataSchema = "Dublin Core, Attribution Tagging, Open-Source Time-Geolocation",
            searchCapabilities = "Full-text boolean keyword search, geospatial bounding box, weapon taxonomy filters",
            researchValue = ResearchValue.CRITICAL,
            summary = "Methodological benchmark for open-source verification, combining digital forensics, chronolocation, and cross-referenced satellite imagery for accountability.",
            indexingStructure = "Taxonomic tagging with hash validation, shadow ephemeris calculations, and flight-radar correlation archives.",
            operationalScope = OperationalScope.URBAN_CONFLICT,
            sampleQueries = listOf("cluster munition remnants Kharkiv", "shadow angle chronolocation bridge sector", "grad rocket crater azimuth"),
            technicalSpecs = mapOf(
                "Primary Ingestion" to "Social Media UGC, Satellite Bounding Boxes",
                "Verification Method" to "Multi-sensor cross-validation (SunCalc, Sentinel-2)",
                "Standard Output" to "Interactive geolocated case maps, verified video atoms"
            )
        ),
        ArchivalRepository(
            id = "repo_eyewitness",
            name = "eyeWitness to Atrocities",
            organization = "International Bar Association (IBA)",
            accessTier = AccessTier.RESTRICTED_LEGAL,
            primaryAssetClass = "Court-admissible mobile photography, video streams, audio logs",
            metadataQuality = MetadataQuality.VERY_HIGH,
            metadataSchema = "Encrypted EXIF, Cryptographic Hash (SHA-256), Cell-Tower Geolocation",
            searchCapabilities = "Restricted judicial queries, cryptographic tamper verification, spatial-temporal clusters",
            researchValue = ResearchValue.HIGH,
            summary = "Purpose-built evidence capture system preserving uncorrupted chain-of-custody for international criminal tribunals (ICC) and human rights tribunals.",
            indexingStructure = "Client-side private-key cryptographic signing at moment of camera sensor capture, immediate server escrow with immutable audit trail.",
            operationalScope = OperationalScope.VERIFICATION,
            sampleQueries = listOf("ICC Rule 68 authenticated recordings", "unaltered EXIF sensor dump", "corroborated cell tower timestamp"),
            technicalSpecs = mapOf(
                "Cryptographic Standard" to "Ed25519 / SHA-256 Hardware Enclave Binding",
                "Legal Admissibility" to "ICC Rome Statute, ECHR Article 6 compliant",
                "Chain of Custody" to "ISO 27037 Digital Evidence Admissibility"
            )
        ),
        ArchivalRepository(
            id = "repo_usgs_eosdis",
            name = "USGS EarthExplorer / NASA EOSDIS",
            organization = "USGS & NASA Earth Science Data Systems",
            accessTier = AccessTier.GOVERNMENTAL,
            primaryAssetClass = "Multispectral satellite imagery (Landsat 8/9), aerial orthoimagery, DEMs",
            metadataQuality = MetadataQuality.HIGH,
            metadataSchema = "ISO 19115 / ISO 19119 Geographic Metadata Standard",
            searchCapabilities = "Spatial bounding polygon, cloud cover threshold (<10%), sun azimuth/elevation, temporal ranges",
            researchValue = ResearchValue.CRITICAL,
            summary = "Global terrestrial baseline offering decades of calibrated optical and thermal infrared multispectral data for post-incident terrain change detection.",
            indexingStructure = "WRS-2 Path/Row grid indexing, Cloud-Optimized GeoTIFF (COG), STAC API compliance.",
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            sampleQueries = listOf("Landsat-9 SWIR thermal anomaly", "WRS Path 176 Row 38 cloud<5%", "30m SRTM DEM crater elevation"),
            technicalSpecs = mapOf(
                "Spatial Resolution" to "15m Panchromatic / 30m Multispectral",
                "Spectral Bands" to "11 Bands (VNIR, SWIR-1, SWIR-2, TIRS Thermal)",
                "Metadata Standard" to "FGDC-STD-001-1998, ISO 19115:2003"
            )
        ),
        ArchivalRepository(
            id = "repo_nist_nbtrd",
            name = "NIST Ballistics Toolmark Research Database (NBTRD)",
            organization = "National Institute of Standards and Technology (NIST)",
            accessTier = AccessTier.ACADEMIC,
            primaryAssetClass = "3D surface topography of fired bullets and cartridge cases",
            metadataQuality = MetadataQuality.VERY_HIGH,
            metadataSchema = "X3P (ISO 5436-2 XML 3D Surface Topography), Surface Metrology Standards",
            searchCapabilities = "Caliber, firearm make/model, rifling twist rate, surface roughness parameters (Ra, Rq)",
            researchValue = ResearchValue.HIGH,
            summary = "Gold-standard open metrology archive providing high-precision 3D confocal microscopy scans to validate objective ballistic matching algorithms.",
            indexingStructure = "Point cloud micro-topography matrices with calibrated instrument uncertainty logs and open X3P formats.",
            operationalScope = OperationalScope.BALLISTICS,
            sampleQueries = listOf("9x19mm Parabellum Glock 19 breech face", "congruent matching cells 5.56 NATO", "confocal 3D striation land impressions"),
            technicalSpecs = mapOf(
                "Sensor Modality" to "Laser Confocal Microscopy / Focus Variation",
                "Spatial Sampling" to "0.5 µm lateral, 0.05 µm vertical resolution",
                "Data Format" to "Open X3P (ISO 5436-2 compliant)"
            )
        ),
        ArchivalRepository(
            id = "repo_copernicus",
            name = "Copernicus Open Access Hub (Data Space)",
            organization = "European Space Agency (ESA) & European Commission",
            accessTier = AccessTier.GOVERNMENTAL,
            primaryAssetClass = "Synthetic Aperture Radar (Sentinel-1 SAR) & Multispectral (Sentinel-2)",
            metadataQuality = MetadataQuality.HIGH,
            metadataSchema = "STAC (SpatioTemporal Asset Catalog), SAFE (Standard Archive Format for Europe)",
            searchCapabilities = "OData / OpenSearch API, polygon clipping, orbit pass direction (ascending/descending)",
            researchValue = ResearchValue.CRITICAL,
            summary = "Premier open radar and optical data feed enabling all-weather, day/night structural damage assessment through SAR phase coherence (InSAR) and backscatter differential.",
            indexingStructure = "STAC Item collection with datetime, bbox, sensor polarization (VV/VH), and orbit state vectors.",
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            sampleQueries = listOf("Sentinel-1 IW SLC phase coherence loss", "Sentinel-2 10m RGB+NIR normalized burn ratio", "Interferometric SAR urban sector deformation"),
            technicalSpecs = mapOf(
                "SAR Wavelength" to "C-band (5.405 GHz), 20m spatial resolution",
                "Optical Bands" to "13 bands (Visible, Red-Edge, NIR, SWIR 10-60m)",
                "Revisit Period" to "5-6 days with Sentinel-1/2 constellations"
            )
        ),
        ArchivalRepository(
            id = "repo_nibin",
            name = "NIBIN / IBIS Ballistic Network",
            organization = "Bureau of Alcohol, Tobacco, Firearms and Explosives (ATF)",
            accessTier = AccessTier.RESTRICTED_LEGAL,
            primaryAssetClass = "Digitized 2D/3D cartridge casing firing pin & breech face impressions",
            metadataQuality = MetadataQuality.VERY_HIGH,
            metadataSchema = "IBIS Correlation Matrix, Forensic Ballistics Standard",
            searchCapabilities = "Automated correlation score ranking, caliber match, firing pin shape clustering",
            researchValue = ResearchValue.CRITICAL,
            summary = "National inter-agency ballistic intelligence network connecting firearm evidence from disparate crime scenes through automated optical correlation.",
            indexingStructure = "High-speed correlation ranking algorithms with physical comparison microscope confirmation by certified examiners.",
            operationalScope = OperationalScope.BALLISTICS,
            sampleQueries = listOf("Breech face shearing match score > 85%", "Glock semi-rectangular firing pin aperture", "casing ejector mark 45 ACP"),
            technicalSpecs = mapOf(
                "Imaging System" to "IBIS BrassTRAX HD 3D topography sensor",
                "Examiner Protocol" to "AFTE (Association of Firearm and Tool Mark Examiners) Criteria",
                "Chain of Custody" to "NIST Forensic Science Division Audit Standard"
            )
        ),
        ArchivalRepository(
            id = "repo_unosat_damage",
            name = "UNOSAT Structural Damage & Satellite Impact Archive",
            organization = "United Nations Satellite Centre (UNITAR / UNOSAT)",
            accessTier = AccessTier.PUBLIC_OPEN,
            primaryAssetClass = "Vector damage polygons, explosive blast craters, building destruction assessments",
            metadataQuality = MetadataQuality.HIGH,
            metadataSchema = "GDACS / UNOSAT Damage Schema (Destroyed, Severe, Moderate, Possible)",
            searchCapabilities = "Administrative boundary, conflict zone polygon, damage classification tier",
            researchValue = ResearchValue.CRITICAL,
            summary = "Standardized geospatial damage mapping archive supporting international humanitarian law assessments and post-strike infrastructure reconstruction.",
            indexingStructure = "ESRI Shapefile and GeoJSON polygon layers categorizing structural integrity degradation.",
            operationalScope = OperationalScope.URBAN_CONFLICT,
            sampleQueries = listOf("Grade 4 destroyed building cluster", "blast crater diameter > 8 meters", "industrial facility roof breach"),
            technicalSpecs = mapOf(
                "Classification Tiers" to "Destroyed (75-100%), Severe (50-75%), Moderate (25-50%), Minor",
                "Sensor Fusion" to "WorldView-3 (0.3m) + GeoEye-1 + Pleiades Neo",
                "Reporting Format" to "Vector GIS layers + Situation Reports (SITREPs)"
            )
        ),
        ArchivalRepository(
            id = "repo_cas_csl",
            name = "CAS Chemical Safety Library (CSL)",
            organization = "Chemical Abstracts Service (CAS) / ACS",
            accessTier = AccessTier.PUBLIC_OPEN,
            primaryAssetClass = "Chemical incident reports, reactive hazard indices, thermal runaway data",
            metadataQuality = MetadataQuality.VERY_HIGH,
            metadataSchema = "CAS Registry Numbers, SMILES strings, GHS Hazard Classifications",
            searchCapabilities = "CAS Number, chemical structure, reaction runaway temperature, binary hazard matrix",
            researchValue = ResearchValue.HIGH,
            summary = "Peer-reviewed chemical safety repository cataloging exothermic reactions, hazardous incompatibilities, and incident lessons learned.",
            indexingStructure = "Structured chemical graph representation with SMILES identifiers and InChI keys.",
            operationalScope = OperationalScope.CBRN_HAZARDS,
            sampleQueries = listOf("Ammonium Nitrate thermal runaway curve", "CAS 7782-50-5 industrial release", "Chlorine incompatible organophosphates"),
            technicalSpecs = mapOf(
                "Chemical Identifiers" to "CAS RN, IUPAC Name, SMILES, InChIKey",
                "Thermal Metrology" to "Differential Scanning Calorimetry (DSC) curves",
                "Data License" to "Open Access Academic & First Responder Access"
            )
        ),
        ArchivalRepository(
            id = "repo_opcw_iaea",
            name = "OPCW & IAEA Incident and Verification Archives",
            organization = "OPCW & International Atomic Energy Agency",
            accessTier = AccessTier.RESTRICTED_LEGAL,
            primaryAssetClass = "Verified chemical weapons remnants, isotope ratio mass spectrometry, environmental samples",
            metadataQuality = MetadataQuality.VERY_HIGH,
            metadataSchema = "OPCW Continuous Chain of Custody Standard, IAEA Safeguards Nuclear Material Accounting",
            searchCapabilities = "Precursor classification (Schedule 1-3), isotopic signature match, custodial transfer log",
            researchValue = ResearchValue.CRITICAL,
            summary = "Forensic repositories for international treaty compliance, maintaining tamper-evident chain of custody for chemical munition residue and radiological samples.",
            indexingStructure = "Cryptographic seal tracking, gas chromatography-mass spectrometry (GC-MS) spectral libraries.",
            operationalScope = OperationalScope.CBRN_HAZARDS,
            sampleQueries = listOf("Sarin precursor methylphosphonic difluoride (DF)", "Cesium-137 radiological dispersal decay", "OPCW Designated Lab round-robin blind analysis"),
            technicalSpecs = mapOf(
                "Analytical Methods" to "GC-MS, LC-MS/MS, Gamma Spectrometry",
                "Custody Rules" to "Dual-custodian tamper-evident security seal envelopes",
                "Confidence Tier" to "Admissible in UN Security Council tribunals"
            )
        ),
        ArchivalRepository(
            id = "repo_airwars_unidir",
            name = "Airwars & UNIDIR Civilian Impact Monitoring Database",
            organization = "Airwars & UN Institute for Disarmament Research",
            accessTier = AccessTier.PUBLIC_OPEN,
            primaryAssetClass = "Multi-source incident logs, casualty estimates, weapon type indicators, local press archives",
            metadataQuality = MetadataQuality.HIGH,
            metadataSchema = "Six-decimal Lat/Long coordinates, Corroboration Tiers, Weapon System Identifiers",
            searchCapabilities = "Date window, strike perpetrator, casualty range, munitions indicator, geofence radius",
            researchValue = ResearchValue.HIGH,
            summary = "Civilian casualty and strike monitoring archive synthesizing military statements, local journalism, social media, and satellite imagery.",
            indexingStructure = "Event-centric relational database with corroboration scores and archived media snapshots.",
            operationalScope = OperationalScope.URBAN_CONFLICT,
            sampleQueries = listOf("lat:33.513824 lon:36.276538 radius:5km", "confirmed air-delivered FAB-500", "multi-source civilian casualty consensus"),
            technicalSpecs = mapOf(
                "Spatial Precision" to "Latitude / Longitude to 6 decimal places (approx 0.1m)",
                "Corroboration Rating" to "Confirmed / Fair / Weak / Contested",
                "Archive Durability" to "Wayback Machine & perma.cc redundant snapshots"
            )
        )
    )

    val preloadedCases = listOf(
        ForensicCase(
            id = "case_2026_01",
            caseNumber = "VERI-2026-0881",
            title = "Urban Munitions Strike on Industrial Grain Silo Complex",
            incidentDate = "2026-08-14 06:42:15 UTC",
            locationName = "Sector 4 Industrial Waterfront",
            latitude = 46.482526,
            longitude = 30.723314,
            operationalScope = OperationalScope.URBAN_CONFLICT,
            status = CaseStatus.COURT_ADMISSIBLE,
            corroborationTier = "Tier 1 - Multi-Sensor Triangulated",
            casualtyEstimate = "0 Fatalities, 4 Moderate Inhalation Injuries",
            weaponIndicators = listOf("Air-delivered glide bomb", "Unified blast fragmentation warhead (250kg)", "Standoff kit guidance fins"),
            summary = "Multi-perspective investigation of early-morning strike on grain storage infrastructure. Verified through shadow geometry chronolocation, Sentinel-1 InSAR coherence loss, and social media video atom container extraction.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_01_video",
                    title = "Civilian Eyewitness High-Resolution Video Capture",
                    assetType = EvidenceAssetType.VIDEO_CONTAINER,
                    hashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    capturedTimestamp = "2026-08-14 06:42:19 UTC",
                    sensorOrDevice = "Apple iPhone 15 Pro Max (Rear 24mm f/1.78)",
                    fileSizeBytes = 48291048L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Container Atoms" to "ftyp, moov, mvhd, trak, mdat (Unmodified MP4 Atom Hierarchy)",
                        "GPS Tag" to "46.482490 N, 30.723180 E (+/- 3.2m accuracy)",
                        "Quantization Table" to "Native iOS AVC H.264 quantization signature",
                        "Shadow Azimuth" to "98.4° East (Matching local solar ephemeris at 06:42:18 UTC)"
                    ),
                    notes = "No secondary transcode artifacts detected. Audio track audio-visual delay calculated at 0.82 seconds from impact flash, placing camera 280 meters from detonation center."
                ),
                EvidenceItem(
                    id = "ev_02_sar",
                    title = "Copernicus Sentinel-1 SAR Interferogram Coherence Loss",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                    capturedTimestamp = "2026-08-14 15:30:00 UTC",
                    sensorOrDevice = "Sentinel-1B C-SAR (Interferometric Wide Swath)",
                    fileSizeBytes = 124800210L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Polarization" to "VV / VH dual polarization",
                        "Orbit" to "Descending Pass 142",
                        "Interferometric Coherence" to "Dropped from 0.88 (Pre-strike) to 0.12 (Post-strike)",
                        "Phase Shift" to "4.2 cm localized structural deformation"
                    ),
                    notes = "Confirms severe structural roof collapse on silo #3 and progressive cracking along elevator tower."
                ),
                EvidenceItem(
                    id = "ev_03_remnant",
                    title = "Munition Guidance Actuator and Serial Plate Photo",
                    assetType = EvidenceAssetType.IMAGE_EXIF,
                    hashSha256 = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                    capturedTimestamp = "2026-08-14 09:15:33 UTC",
                    sensorOrDevice = "Sony Alpha 7 IV (FE 24-70mm F2.8 GM II)",
                    fileSizeBytes = 18459200L,
                    verificationStatus = VerificationStatus.C2PA_SIGNED,
                    technicalAttributes = mapOf(
                        "C2PA Manifest" to "Cryptographic manifest present (Issuer: Reuters Digital Newsroom)",
                        "Error Level Analysis" to "Consistent 8-bit DCT quantization across entire sensor canvas",
                        "PRNU Match Score" to "99.1% lens sensor pattern correlation",
                        "Serial Number Extraction" to "UMPK-500-MOD2-88194"
                    ),
                    notes = "C2PA signature authenticates provenance from ground investigator camera directly into news escrow."
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = "2026-08-14 07:10:00 UTC",
                    custodian = "Investigator A. Chen",
                    organization = "Independent OSINT Verification Cell",
                    actionTaken = "Secured raw video container file, generated initial SHA-256 hash checksum in field.",
                    digitalSignatureHash = "9d4e12...b401"
                ),
                CustodyLogEvent(
                    timestamp = "2026-08-14 11:20:00 UTC",
                    custodian = "Dr. H. Lindqvist",
                    organization = "UN Human Rights Assessment Mission",
                    actionTaken = "Ingested into encrypted evidence repository; performed shadow geometry and InSAR overlay verification.",
                    digitalSignatureHash = "f71a99...3c22"
                ),
                CustodyLogEvent(
                    timestamp = "2026-08-15 14:00:00 UTC",
                    custodian = "Lead Forensic Examiner M. Torres",
                    organization = "Office of the Prosecutor Legal Registry",
                    actionTaken = "Certified court-admissibility dossier under ICC Rule 68(2) standards.",
                    digitalSignatureHash = "88ab01...77e9"
                )
            )
        ),
        ForensicCase(
            id = "case_2026_02",
            caseNumber = "VERI-2026-0419",
            title = "Ballistic Toolmark Matching & Casing Trajectory in Diplomatic District Incident",
            incidentDate = "2026-07-29 23:14:08 UTC",
            locationName = "Embassy Way Checkpoint Alpha",
            latitude = 38.895110,
            longitude = -77.036370,
            operationalScope = OperationalScope.BALLISTICS,
            status = CaseStatus.CORROBORATED,
            corroborationTier = "Tier 1 - Multi-Sensor Triangulated",
            casualtyEstimate = "1 Wounded (Non-life threatening)",
            weaponIndicators = listOf("Semi-automatic firearm", "9x19mm Parabellum caliber", "Rectangular firing pin impression"),
            summary = "Ballistic recovery of spent cartridge cases evaluated against NIST NBTRD metrology scans and NIBIN correlation database. Firing pin striation congruence matched to suspect weapon at 94.6% confidence.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_bal_01",
                    title = "Recovered Spent Cartridge Casing (Item Q-102)",
                    assetType = EvidenceAssetType.BALLISTIC_TOPOGRAPHY,
                    hashSha256 = "6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b",
                    capturedTimestamp = "2026-07-30 02:40:00 UTC",
                    sensorOrDevice = "Sensofar S neox 3D Confocal Profiler (NIST standard calibration)",
                    fileSizeBytes = 8240090L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Surface Metrology Format" to "X3P ISO 5436-2 Point Cloud",
                        "Average Roughness (Ra)" to "1.42 µm",
                        "Root Mean Square (Rq)" to "1.89 µm",
                        "Breech Face Markings" to "Cross-hatched parallel milling marks",
                        "Firing Pin Shape" to "Glock semi-rectangular aperture shear"
                    ),
                    notes = "Micro-topographic striations exhibit 48 congruent matching cells when compared with Glock 19 reference casing."
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = "2026-07-30 01:15:00 UTC",
                    custodian = "Detective S. Vance",
                    organization = "Metropolitan Forensic Ballistics Unit",
                    actionTaken = "Recovered cartridge casing from tarmac, packaged in serialized anti-static container #B-404.",
                    digitalSignatureHash = "1a2b3c...4d5e"
                ),
                CustodyLogEvent(
                    timestamp = "2026-07-30 08:30:00 UTC",
                    custodian = "Ballistics Examiner R. Wu",
                    organization = "National Ballistics Laboratory",
                    actionTaken = "Completed confocal 3D surface topography scan and submitted X3P surface matrix to NIBIN correlation.",
                    digitalSignatureHash = "7f8e9d...0a1b"
                )
            )
        ),
        ForensicCase(
            id = "case_2026_03",
            caseNumber = "VERI-2026-0105",
            title = "Hazardous Chemical Plume & Environmental Soil Contamination",
            incidentDate = "2026-05-18 13:05:00 UTC",
            locationName = "Chemical Storage Depot Warehouse 9",
            latitude = 31.968599,
            longitude = 35.843899,
            operationalScope = OperationalScope.CBRN_HAZARDS,
            status = CaseStatus.UNDER_REVIEW,
            corroborationTier = "Tier 2 - Dual-Source Corroborated",
            casualtyEstimate = "12 Acute Respiratory Exposures, 0 Fatalities",
            weaponIndicators = listOf("Industrial Chlorine pressurized cylinder", "Secondary combustion residue"),
            summary = "Hazard analysis following ruptured containment cylinder. Cross-referenced against CAS Chemical Safety Library and EPA IRIS exposure guidelines to determine thermal runaway risks and dispersion radius.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_chem_01",
                    title = "Environmental Soil and Wipe Sample GC-MS Spectra",
                    assetType = EvidenceAssetType.CHEMICAL_SAMPLE,
                    hashSha256 = "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35",
                    capturedTimestamp = "2026-05-18 16:30:00 UTC",
                    sensorOrDevice = "Agilent 7890B GC / 5977A MSD (OPCW Designated Lab Protocol)",
                    fileSizeBytes = 3405020L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Identified Compound" to "Chlorinated hydrocarbons + Free Chloride ions",
                        "CAS Registry No." to "7782-50-5 (Chlorine Gas)",
                        "Peak Concentration" to "14.8 ppm at 50m radius",
                        "OPCW Protocol" to "Blind split-sample verification passed with Lab 04"
                    ),
                    notes = "Atmospheric dispersion plume modeled with ALOHA software matches video drone smoke dissipation trajectory."
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = "2026-05-18 14:10:00 UTC",
                    custodian = "CBRN First Responder Team Leader",
                    organization = "Civil Protection HAZMAT Unit",
                    actionTaken = "Collected soil core sample at point of release in PTFE-lined stainless container #HZ-91.",
                    digitalSignatureHash = "554433...2211"
                )
            )
        ),
        ForensicCase(
            id = "case_2026_04",
            caseNumber = "VERI-2026-0914",
            title = "Orchidland Estates (Puna) Airspace, Volcanic Thermal & Hybrid C2 Anomaly",
            incidentDate = "2026-08-14 06:42:15 UTC",
            locationName = "Orchidland Estates / Hwy 130 Corridor, HI",
            latitude = 19.561400,
            longitude = -155.013100,
            operationalScope = OperationalScope.GEOINT_IMAGERY,
            status = CaseStatus.OPEN,
            corroborationTier = "Tier 2 - Multi-Feed Satellite & Airspace Sync",
            casualtyEstimate = "Infrastructure / Transit Corridor Monitoring",
            weaponIndicators = listOf("sUAS Reconnaissance Telemetry", "R-3103 PTA Convoy Corridor", "VIIRS Thermal Anomaly"),
            summary = "Correlated Lower Puna cyber-physical chatter with NASA VIIRS 375m GIBS swaths, NOAA PHWA WSR-88D Doppler wind shear, and FAA TFR/PTA Saddle Road advisories.",
            evidenceItems = listOf(
                EvidenceItem(
                    id = "ev_puna_01",
                    title = "NASA Worldview GIBS VIIRS & NOAA PHWA Doppler Overlay",
                    assetType = EvidenceAssetType.SATELLITE_SAR,
                    hashSha256 = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                    capturedTimestamp = "2026-08-14 06:42:15 UTC",
                    sensorOrDevice = "Suomi NPP VIIRS + NEXRAD PHWA South Point",
                    fileSizeBytes = 1_240_500L,
                    verificationStatus = VerificationStatus.AUTHENTIC_VERIFIED,
                    technicalAttributes = mapOf(
                        "Airspace Sector" to "Class G (<400 ft AGL, outside PHTO 4.3 NM)",
                        "Coordinates" to "19.5614°, -155.0131°",
                        "Volcanic Cams" to "USGS HVO PGcam / F1cam Thermal"
                    ),
                    notes = "Baseline Puna/Orchidland digital-physical corridor dossier linked to Hawaiʻi Airspace & Satellite feeds."
                )
            ),
            chainOfCustody = listOf(
                CustodyLogEvent(
                    timestamp = "2026-08-14 07:00:00 UTC",
                    custodian = "Orchidland DICM Analyst",
                    organization = "VeriScope Puna Telemetry Desk",
                    actionTaken = "Opened regional cyber-physical & geospatial corroboration dossier for Lower Puna.",
                    digitalSignatureHash = "9a8b7c...6d5e"
                )
            )
        )
    )

    val cbrnHazards = listOf(
        CbrnHazard(
            casNumber = "7782-50-5",
            commonName = "Chlorine Gas",
            molecularFormula = "Cl2",
            smiles = "ClCl",
            hazardClass = "Toxic Inhalation Hazard (Class 2.3), Oxidizer, Corrosive",
            thermalRunawayRisk = "Reacts violently with hydrocarbons, ammonia, and finely powdered metals; non-flammable but vigorously supports combustion.",
            exposureLimit = "OSHA PEL: 1 ppm (3 mg/m³) Ceiling; NIOSH IDLH: 10 ppm",
            environmentalFate = "Heavy greenish-yellow gas (2.5x heavier than air) pooling in low-lying areas, trenches, and basements; rapid chlorination of organic matter.",
            samplingStandard = "OPCW Handbook Appendix 3, NIOSH Method 6011 (Ion chromatography on silver membrane)."
        ),
        CbrnHazard(
            casNumber = "107-44-8",
            commonName = "Sarin (GB Nerve Agent)",
            molecularFormula = "C4H10FO2P",
            smiles = "CC(C)OP(=O)(C)F",
            hazardClass = "Chemical Weapons Convention Schedule 1, Organophosphate Cholinesterase Inhibitor",
            thermalRunawayRisk = "Decomposes thermally above 150°C releasing hydrogen fluoride (HF) and phosphorus oxides.",
            exposureLimit = "AEGL-3 (10-min): 0.38 mg/m³; Lethal Dose (LCt50): 35-100 mg·min/m³",
            environmentalFate = "Volatile liquid (evaporates similar to water); degrades via hydrolysis to non-toxic isopropyl methylphosphonic acid (IMPA) and methylphosphonic acid (MPA).",
            samplingStandard = "OPCW Standard Operating Procedure for Soil and Surface Wipe Verification (GC-MS / LC-MS/MS)."
        ),
        CbrnHazard(
            casNumber = "7664-41-7",
            commonName = "Ammonia (Anhydrous)",
            molecularFormula = "NH3",
            smiles = "N",
            hazardClass = "Toxic Inhalation, Corrosive, Flammable Gas (LEL 15% - UEL 28%)",
            thermalRunawayRisk = "Forms explosive vapor clouds inside enclosed silos; violent exothermic reaction with halogens and acids.",
            exposureLimit = "OSHA PEL: 50 ppm (35 mg/m³); NIOSH IDLH: 300 ppm",
            environmentalFate = "Lighter than air when dry, but cryogenic liquid leaks form dense aerosol clouds hugging ground level until warmed.",
            samplingStandard = "EPA Method C-001, OSHA PV2145 (Sulfuric acid treated glass fiber filter)."
        ),
        CbrnHazard(
            casNumber = "12185-10-3",
            commonName = "White Phosphorus (Tetraphosphorus)",
            molecularFormula = "P4",
            smiles = "P12P3P1P23",
            hazardClass = "Pyrophoric Solid (Class 4.2), Dermal Corrosive, Systemic Toxicant",
            thermalRunawayRisk = "Spontaneously ignites in air at temperatures above 30°C (86°F), burning at over 800°C with dense white phosphorus pentoxide smoke.",
            exposureLimit = "OSHA PEL: 0.1 mg/m³; NIOSH IDLH: 5 mg/m³",
            environmentalFate = "Forms refractory crust when starved of oxygen; re-ignites violently upon subsequent mechanical disturbance and atmospheric contact.",
            samplingStandard = "Airwars / Human Rights Watch Munition Debris Protocol; inert water submersion transport."
        ),
        CbrnHazard(
            casNumber = "10045-97-3",
            commonName = "Cesium-137 (Radiological Isotope)",
            molecularFormula = "137Cs",
            smiles = "[137Cs]",
            hazardClass = "Beta/Gamma Emitting Radioisotope (Half-life: 30.17 years), Radiological Dispersal Hazard",
            thermalRunawayRisk = "Soluble salt form (CsCl) disperses as fine aerosol powder; highly pyrophoric elemental metal.",
            exposureLimit = "IAEA Basic Safety Standards Annual Limit of Intake: 4 MBq",
            environmentalFate = "Forms water-soluble salts binding tightly to soil clays and concrete matrix, producing persistent gamma contamination.",
            samplingStandard = "IAEA Technical Report Series No. 295; High-Purity Germanium (HPGe) Gamma Spectrometry."
        )
    )

    val ballisticProfiles = listOf(
        BallisticProfile(
            id = "bp_01",
            firearmModel = "Glock 19 Gen 5",
            caliber = "9x19mm Parabellum",
            riflingTwist = "1:9.84 in (250 mm) Right Hand Modified Polygonal",
            striatedSurfaceRaUm = 1.38,
            peakValleyVarianceUm = 5.2,
            firingPinImpression = "Rectangular aperture shear with elliptical center indent",
            matchConfidencePercent = 94.8,
            nistDatabaseReference = "NBTRD-X3P-2024-GLK-9MM-4992"
        ),
        BallisticProfile(
            id = "bp_02",
            firearmModel = "Colt M4A1 Carbine",
            caliber = "5.56x45mm NATO",
            riflingTwist = "1:7 in (178 mm) Right Hand 6 Grooves",
            striatedSurfaceRaUm = 1.85,
            peakValleyVarianceUm = 7.1,
            firingPinImpression = "Hemispherical circular strike with concentric circle breech face milling",
            matchConfidencePercent = 91.2,
            nistDatabaseReference = "NBTRD-X3P-2023-CLT-556-1182"
        ),
        BallisticProfile(
            id = "bp_03",
            firearmModel = "AKM / AK-74 Pattern",
            caliber = "7.62x39mm / 5.45x39mm",
            riflingTwist = "1:9.45 in (240 mm) Right Hand 4 Grooves",
            striatedSurfaceRaUm = 2.45,
            peakValleyVarianceUm = 9.8,
            firingPinImpression = "Deep cratered hemispherical puncture with pronounced ejector rim shear",
            matchConfidencePercent = 88.5,
            nistDatabaseReference = "NBTRD-X3P-2022-AKM-762-0941"
        )
    )

    val geointSensors = listOf(
        GeointSensor(
            name = "Sentinel-1A/1C (SAR)",
            sensorType = "Synthetic Aperture Radar (Active Microwave)",
            constellation = "ESA Copernicus Constellation",
            resolutionMeters = "20m (IW Ground Range Detected) / 5m (Stripmap)",
            primarySpectralBands = "C-band (5.405 GHz, 5.5 cm wavelength)",
            bestUseCases = "All-weather urban damage assessment, InSAR phase coherence loss, building collapse, ground subsidence.",
            accessModel = "Open Data (Creative Commons BY-SA 3.0 IGO)"
        ),
        GeointSensor(
            name = "Sentinel-2A/2B (MSI)",
            sensorType = "Multispectral Optical Scanner",
            constellation = "ESA Copernicus Constellation",
            resolutionMeters = "10m (RGB/NIR) / 20m (Red-Edge/SWIR)",
            primarySpectralBands = "13 bands (Visible, 3 Red-Edge, NIR, 2 SWIR)",
            bestUseCases = "Burn scar mapping (NBR), vegetation disruption, crater thermal reflection, broad-area camouflage contrast.",
            accessModel = "Open Data (Copernicus Open Access)"
        ),
        GeointSensor(
            name = "Maxar WorldView-3",
            sensorType = "Very High Resolution (VHR) Commercial Optical & SWIR",
            constellation = "Maxar Technologies",
            resolutionMeters = "0.31m Panchromatic / 1.24m Multispectral / 3.7m SWIR",
            primarySpectralBands = "Pan + 8 Multispectral + 8 SWIR + 12 CAVIS bands",
            bestUseCases = "Tactical vehicle classification, projectile impact crater depth, individual structural facade spalling.",
            accessModel = "Commercial Tasking / Humanitarian Licensing"
        ),
        GeointSensor(
            name = "PlanetScope (SuperDove)",
            sensorType = "High Revisit CubeSat Constellation",
            constellation = "Planet Labs (200+ Satellites in Orbit)",
            resolutionMeters = "3.0m Orthorectified Surface Reflectance",
            primarySpectralBands = "8-band multispectral (Coastal Blue to NIR)",
            bestUseCases = "Daily cadence change detection, precise strike day/time window narrowing, trench fortification mapping.",
            accessModel = "Commercial / Academic Research Program"
        )
    )
}
