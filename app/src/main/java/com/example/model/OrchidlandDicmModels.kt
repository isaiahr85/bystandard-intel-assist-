package com.example.model

import androidx.annotation.DrawableRes

enum class DicmPhaseTab(val label: String, val code: String) {
    DISSECT_IPB("1. Dissect & IPB", "DISSECT"),
    PROFILES_PLATFORMS("2. Profiles & Platforms", "IDENTIFY"),
    CORRELATE_SNA("3. Correlate & SNA", "CORRELATE"),
    MITIGATE_ROE("4. Mitigate & ROE", "MITIGATE"),
    AIRSPACE_PTA("5. Airspace & PTA", "AIRSPACE"),
    HILO_CAM_MONITOR("6. Hilo Cam Monitor", "HILO_CAM")
}

enum class TaskAgentCategory(val label: String) {
    AI_SWARM("AI Agent"),
    PHYSICAL_RF("Human / RF / sUAS")
}

data class TaskAgentProfile(
    val id: String,
    val cryptoId: String,
    val name: String,
    val subtitle: String,
    val category: TaskAgentCategory,
    @DrawableRes val emblemResId: Int,
    val coreCharacteristics: String,
    val typicalTasks: String,
    val orchidlandSignature: String,
    val snaRole: String,
    val isHighDegreeTrap: Boolean,
    val isPrimaryTarget: Boolean,
    val degreeScore: Float,
    val betweennessScore: Float,
    val closenessScore: Float,
    val eigenvectorScore: Float,
    val primaryVulnerabilities: List<String>,
    val pinOrUtilityProfile: String,
    val interdictionAction: String
)

data class PlatformNodeProfile(
    val id: String,
    val name: String,
    val categoryBadge: String,
    @DrawableRes val emblemResId: Int,
    val funnelStage: String,
    val orchidlandTelemetry: String,
    val agentIds: List<String>,
    val extractionIndicators: List<String>,
    val contextSetterMechanism: String,
    val mitigationStatusLabel: String
)

data class OrchidlandIpbDimension(
    val id: String,
    val dimension: String,
    val verifiedBaseline: String,
    val cyberPhysicalImplication: String,
    val sourceCaveat: String
)

data class InvertedMdmpStep(
    val stepNumber: Int,
    val title: String,
    val summary: String,
    val orchidlandApplication: String
)

data class CoaScenario(
    val code: String,
    val title: String,
    val description: String,
    val triggerIndicators: List<String>
)

data class CogComparison(
    val domain: String,
    val primaryCog: String,
    val criticalCapabilities: List<String>,
    val criticalRequirements: List<String>,
    val criticalVulnerabilities: List<String>
)

data class StrategicFrameworkBlock(
    val frameworkCode: String,
    val title: String,
    val items: List<Pair<String, String>>
)

data class StakeholderQuadrant(
    val id: String,
    val actorName: String,
    val powerLevel: String,
    val interestLevel: String,
    val salienceClass: String,
    val attributes: String,
    val posture: String
)

enum class GraphNodeClass(val label: String) {
    CYBER_AGENT("Cyber / Bot Node"),
    HUMAN_CELL("Human Operative"),
    TELECOM_PROXY("Proxy / C2 Infrastructure"),
    TERRAIN_RF("Orchidland Terrain / RF")
}

enum class GraphEdgeClass(val label: String) {
    COMMS("COMMS"),
    COMMAND("C2"),
    FINANCE("FIN"),
    PHYSICAL("PHYS"),
    SOCMINT("SOCMINT")
}

data class DicmGraphNode(
    val id: String,
    val label: String,
    val cryptoId: String,
    val nodeClass: GraphNodeClass,
    val agentProfileId: String?,
    val xNorm: Float,
    val yNorm: Float,
    val roleSummary: String
)

data class DicmGraphEdge(
    val fromId: String,
    val toId: String,
    val edgeClass: GraphEdgeClass,
    val weight: Float,
    val channelNote: String
)

data class SnaNodeComputedMetric(
    val node: DicmGraphNode,
    val activeDegree: Int,
    val degreeNorm: Float,
    val betweennessNorm: Float,
    val closenessNorm: Float,
    val diagnosticTag: String
)

data class SnaNetworkReport(
    val totalActiveNodes: Int,
    val totalActiveEdges: Int,
    val connectedComponents: Int,
    val fragmentationPercent: Float,
    val largestComponentRatio: Float,
    val isolatedNodeIds: Set<String>,
    val nodeMetrics: List<SnaNodeComputedMetric>,
    val tacticalAssessment: String
)

data class NistCountermeasure(
    val id: String,
    val nistPhase: String,
    val title: String,
    val targetClass: String,
    val mechanism: String,
    val impactScore: Int
)

data class JointTargetingStep(
    val phaseNumber: Int,
    val title: String,
    val leadRole: String,
    val cyberAction: String,
    val orchidlandGuardrail: String
)

data class ComplianceGuardrailItem(
    val id: String,
    val statuteOrStandard: String,
    val title: String,
    val requirement: String
)
