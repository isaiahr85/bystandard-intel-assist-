package com.example.model

enum class AccessTier(val label: String) {
    PUBLIC_OPEN("Public / Open"),
    RESTRICTED_LEGAL("Restricted (Legal / Humanitarian)"),
    GOVERNMENTAL("Public / Governmental"),
    ACADEMIC("Academic / Research")
}

enum class MetadataQuality(val label: String) {
    VERY_HIGH("Very High (Cryptographic / Metrology)"),
    HIGH("High (ISO / STAC Compliant)"),
    STANDARD("Standard (Dublin Core / EXIF)")
}

enum class ResearchValue(val label: String) {
    CRITICAL("Critical (Baseline Standard)"),
    HIGH("High (Admissible / Specialized)"),
    MODERATE("Moderate (Contextual / Secondary)")
}

enum class OperationalScope(val label: String, val shortName: String) {
    ALL("All Scopes", "All"),
    METADATA_DISCOVERY("Metadata & Archival Discovery", "Discovery"),
    BALLISTICS("Ballistic & Toolmark Forensics", "Ballistics"),
    URBAN_CONFLICT("Urban Conflict & Incident Logs", "Conflict"),
    CBRN_HAZARDS("CBRN & Hazardous Materials", "CBRN"),
    GEOINT_IMAGERY("GEOINT, SAR & Multispectral", "GEOINT"),
    AI_VECTOR("AI Vector & Multimodal Discovery", "AI Vector"),
    VERIFICATION("Reliability, ELA & Provenance", "Verification")
}

enum class CaseStatus(val label: String) {
    OPEN("Active Investigation"),
    CORROBORATED("Corroborated"),
    COURT_ADMISSIBLE("Court Admissible"),
    UNDER_REVIEW("Under Review")
}

enum class EvidenceAssetType(val label: String) {
    IMAGE_EXIF("Digital Still / EXIF"),
    VIDEO_CONTAINER("Video Stream / Atom Structure"),
    SATELLITE_SAR("SAR / Multispectral Tile"),
    BALLISTIC_TOPOGRAPHY("3D Toolmark Surface Topography"),
    CHEMICAL_SAMPLE("CBRN Environmental Sample"),
    SENSOR_TELEM("Seismic / Thermal Telemetry")
}

enum class VerificationStatus(val label: String) {
    AUTHENTIC_VERIFIED("Verified Authentic"),
    FLAGGED_ANOMALY("Tampering / ELA Anomaly"),
    METADATA_STRIPPED("EXIF Stripped / Re-encoded"),
    C2PA_SIGNED("C2PA Cryptographically Signed")
}

data class ArchivalRepository(
    val id: String,
    val name: String,
    val organization: String,
    val accessTier: AccessTier,
    val primaryAssetClass: String,
    val metadataQuality: MetadataQuality,
    val metadataSchema: String,
    val searchCapabilities: String,
    val researchValue: ResearchValue,
    val summary: String,
    val indexingStructure: String,
    val operationalScope: OperationalScope,
    val sampleQueries: List<String>,
    val technicalSpecs: Map<String, String>
)

data class ForensicCase(
    val id: String,
    val caseNumber: String,
    val title: String,
    val incidentDate: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val operationalScope: OperationalScope,
    val status: CaseStatus,
    val corroborationTier: String,
    val casualtyEstimate: String,
    val weaponIndicators: List<String>,
    val summary: String,
    val evidenceItems: List<EvidenceItem>,
    val chainOfCustody: List<CustodyLogEvent>
)

data class EvidenceItem(
    val id: String,
    val title: String,
    val assetType: EvidenceAssetType,
    val hashSha256: String,
    val capturedTimestamp: String,
    val sensorOrDevice: String,
    val fileSizeBytes: Long,
    val verificationStatus: VerificationStatus,
    val technicalAttributes: Map<String, String>,
    val notes: String
)

data class CustodyLogEvent(
    val timestamp: String,
    val custodian: String,
    val organization: String,
    val actionTaken: String,
    val digitalSignatureHash: String
)

data class CbrnHazard(
    val casNumber: String,
    val commonName: String,
    val molecularFormula: String,
    val smiles: String,
    val hazardClass: String,
    val thermalRunawayRisk: String,
    val exposureLimit: String,
    val environmentalFate: String,
    val samplingStandard: String
)

data class BallisticProfile(
    val id: String,
    val firearmModel: String,
    val caliber: String,
    val riflingTwist: String,
    val striatedSurfaceRaUm: Double,
    val peakValleyVarianceUm: Double,
    val firingPinImpression: String,
    val matchConfidencePercent: Double,
    val nistDatabaseReference: String
)

data class GeointSensor(
    val name: String,
    val sensorType: String,
    val constellation: String,
    val resolutionMeters: String,
    val primarySpectralBands: String,
    val bestUseCases: String,
    val accessModel: String
)
