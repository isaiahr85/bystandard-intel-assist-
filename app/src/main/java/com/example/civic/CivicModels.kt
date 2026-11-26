package com.example.civic

import androidx.compose.runtime.Immutable

/**
 * Mandatory Epistemic Classification separating ground truth, user reports,
 * AI inference, probabilistic forecasts, and long-horizon speculative projections.
 */
enum class EpistemicTier(
    val code: String,
    val label: String,
    val badgeSummary: String
) {
    VERIFIED_DATA(
        code = "VERIFIED",
        label = "Verified Data",
        badgeSummary = "Authoritative public-record dataset with citation & revision log"
    ),
    USER_GENERATED(
        code = "USER_UGC",
        label = "User-Generated Info",
        badgeSummary = "Community-submitted observation subject to moderation & human verification"
    ),
    AI_GENERATED_INSIGHT(
        code = "AI_INSIGHT",
        label = "AI-Generated Insight",
        badgeSummary = "Explainable on-device / studio-aligned model synthesis (non-authoritative)"
    ),
    FORECAST(
        code = "FORECAST",
        label = "Forecast",
        badgeSummary = "Bounded quantitative trend projection with confidence interval"
    ),
    SPECULATIVE_PROJECTION(
        code = "SPECULATIVE",
        label = "Speculative Projection",
        badgeSummary = "5–10 year strategic horizon scenario requiring periodic human review"
    )
}

enum class PlatformMilestoneVersion(
    val versionTag: String,
    val title: String,
    val horizonLabel: String,
    val datasetsActivated: List<String>,
    val aiModelsActivated: List<String>,
    val reportingSystems: List<String>,
    val engagementFeatures: List<String>,
    val epistemicTier: EpistemicTier
) {
    V1_EARLY_BETA(
        versionTag = "VERSION 1.0",
        title = "Early Beta Launch",
        horizonLabel = "Months 1–4",
        datasetsActivated = listOf("Local 211 Service Directory", "Verified Municipal Notices"),
        aiModelsActivated = listOf("Rule-Based Content Safety Filter", "On-Device Keyword Indexer"),
        reportingSystems = listOf("Moderated Community Observation Form"),
        engagementFeatures = listOf("Issue Subscriptions", "WCAG Accessibility Controls"),
        epistemicTier = EpistemicTier.VERIFIED_DATA
    ),
    V2_REGIONAL_EXPANSION(
        versionTag = "VERSION 2.0",
        title = "Regional Community Expansion",
        horizonLabel = "Months 5–10",
        datasetsActivated = listOf("County Council Agendas", "Transit & Food Security Feeds"),
        aiModelsActivated = listOf("On-Device Cultural Framing Adapter"),
        reportingSystems = listOf("Reputation-Weighted Verification Queue", "Outreach Case Tracking"),
        engagementFeatures = listOf("Multilingual Glossary", "Indigenous Place-Name Context"),
        epistemicTier = EpistemicTier.VERIFIED_DATA
    ),
    V3_AI_ASSISTED_RECS(
        versionTag = "VERSION 3.0",
        title = "AI Assisted Recommendations",
        horizonLabel = "Year 1–2",
        datasetsActivated = listOf("Workforce & Housing Program Metrics"),
        aiModelsActivated = listOf("On-Device Contextual Bandit RL", "Explainable Feature Attribution"),
        reportingSystems = listOf("AI-Assisted Duplicate & Bias Screening (Human-Gated)"),
        engagementFeatures = listOf("Transparent 'Why Shown' Cards", "Opt-In Federated Weight Sync"),
        epistemicTier = EpistemicTier.AI_GENERATED_INSIGHT
    ),
    V4_POLICY_INTELLIGENCE(
        versionTag = "VERSION 4.0",
        title = "Policy Intelligence Integration",
        horizonLabel = "Year 2–3",
        datasetsActivated = listOf("State Legislative Trackers", "Historical Revision Ledger"),
        aiModelsActivated = listOf("Neutral Policy Impact Summarizer", "Election-Cycle Transition Detector"),
        reportingSystems = listOf("Longitudinal Budget & Service Continuity Audits"),
        engagementFeatures = listOf("Informational Policy Timeline", "Non-Partisan Effective-Date Alerts"),
        epistemicTier = EpistemicTier.FORECAST
    ),
    V5_MULTI_REGION_SCALING(
        versionTag = "VERSION 5.0",
        title = "Multi-Region Scaling",
        horizonLabel = "Years 3–5",
        datasetsActivated = listOf("Cross-County Disaster & Housing Interop Catalog"),
        aiModelsActivated = listOf("Cross-Region Federated Aggregation + DP Accounting"),
        reportingSystems = listOf("Authorized Multi-Agency Field Coordinator Workflows"),
        engagementFeatures = listOf("Regional Resource Gap Heatmaps", "Offline Sector Bundles"),
        epistemicTier = EpistemicTier.FORECAST
    ),
    V10_LONG_TERM_EVOLUTION(
        versionTag = "VERSION 10.0",
        title = "Long-Term Strategic Evolution",
        horizonLabel = "Years 5–10",
        datasetsActivated = listOf("Decadal Socio-Infrastructure & Climate Adaptation Archive"),
        aiModelsActivated = listOf("Multimodal Edge Civic Reasoning & Human-Governed Self-Refinement"),
        reportingSystems = listOf("10-Year Longitudinal Policy Outcomes & Budget Equity Ledger"),
        engagementFeatures = listOf("Participatory Resource Co-Design & Accessibility Co-Pilot"),
        epistemicTier = EpistemicTier.SPECULATIVE_PROJECTION
    )
}

enum class SocialIssueTopic(
    val id: String,
    val title: String,
    val categoryCode: String,
    val summary: String
) {
    HOUSING("housing", "Housing", "HSG", "Affordable rentals, zoning updates, tenant protections, and shelter supply."),
    HOMELESSNESS("homelessness", "Homelessness", "HML", "Outreach navigation, emergency shelter beds, and transitional housing."),
    EMPLOYMENT("employment", "Employment", "EMP", "Local job openings, wage standards, and labor support centers."),
    WORKFORCE_TRAINING("workforce", "Workforce Training", "WFT", "Apprenticeships, vocational certificates, and digital upskilling grants."),
    DISASTER_RECOVERY("disaster", "Disaster Recovery", "DRS", "FEMA/county recovery centers, hazard mitigation, and utility restoration."),
    PUBLIC_TRANSIT("transit", "Public Transportation", "TRN", "Bus route changes, paratransit access, and rural mobility corridors."),
    FOOD_SECURITY("food", "Food Security", "FOD", "Food pantries, SNAP/WIC eligibility clinics, and community agriculture."),
    SMALL_BUSINESS("small_biz", "Small Business Dev", "SBD", "Micro-grants, permitting clinics, and local procurement workshops."),
    MENTAL_HEALTH("mental_health", "Mental Health Services", "MHS", "Crisis lines, community clinics, and peer support networks."),
    CIVIC_RESOURCES("civic", "Civic Resources", "CVC", "Public hearings, legal aid clinics, library services, and open records.")
}

enum class UpdateCadence(
    val id: String,
    val label: String,
    val scope: String
) {
    DAILY("daily", "Daily Updates", "Sync public service statuses, emergency recovery notices, and moderation queue."),
    WEEKLY("weekly", "Weekly Updates", "Refresh legislative feed diffs, community sentiment summaries, and local directories."),
    MONTHLY("monthly", "Monthly Updates", "Recalibrate contextual bandit exploration epsilon and accessibility telemetry."),
    QUARTERLY("quarterly", "Quarterly Reviews", "Audit cultural sensitivity compliance, retention trends, and model drift."),
    ELECTION_CYCLE("election_cycle", "Election-Cycle Reviews", "Detect transitions, recompute policy relevance, and publish cross-cycle comparisons."),
    FIVE_YEAR_STRATEGIC("five_year", "5-Year Strategic Reviews", "Evaluate multi-region infrastructure outcomes and milestone roadmap readiness.")
}

@Immutable
data class RecommendationWeights(
    val topicAffinities: Map<String, Float>,
    val localityBoost: Float = 0.25f,
    val urgencyBoost: Float = 0.20f,
    val accessibilityBoost: Float = 0.15f,
    val explorationEpsilon: Float = 0.12f,
    val updatesCount: Int = 0
)

@Immutable
data class ExplainableRecommendation(
    val id: String,
    val topic: SocialIssueTopic,
    val title: String,
    val plainLanguageSummary: String,
    val score: Float,
    val attributionBreakdown: List<String>,
    val culturalFramingNote: String,
    val epistemicTier: EpistemicTier = EpistemicTier.AI_GENERATED_INSIGHT
)

@Immutable
data class FederatedGradientUpdate(
    val epochId: String,
    val epsilonBudget: Float,
    val clippedL2Norm: Float,
    val noisedTopicDeltas: Map<String, Float>,
    val containsRawUserData: Boolean = false
)

@Immutable
data class CulturalContextProfile(
    val preferredLanguage: String = "English / ʻŌlelo Hawaiʻi Terms",
    val geographicRegion: String = "Hawaiʻi Island (Puna / Hilo)",
    val accessibilityFocus: String = "High-contrast readable cards + plain-language briefs",
    val indigenousProtocolRespect: Boolean = true,
    val communityNormSummary: String = "Prioritize respectful kūpuna-accessible phrasing, ʻāina stewardship context, and local ʻōlelo place names without demographic targeting."
)

@Immutable
data class BiasAuditResult(
    val passed: Boolean,
    val guardReason: String,
    val checkedAtIso: String
)

@Immutable
data class PolicyRevision(
    val revisionDateIso: String,
    val summary: String,
    val sourceCitation: String
)

@Immutable
data class PolicyRecord(
    val id: String,
    val topic: SocialIssueTopic,
    val title: String,
    val jurisdiction: String,
    val effectiveDateIso: String,
    val status: String,
    val informationalSummary: String,
    val documentationUrl: String,
    val relevanceScore: Float,
    val revisions: List<PolicyRevision>,
    val epistemicTier: EpistemicTier = EpistemicTier.VERIFIED_DATA
)

@Immutable
data class AdministrationTransitionEvent(
    val id: String,
    val cycleLabel: String,
    val transitionType: String,
    val effectiveDateIso: String,
    val declaredPriorities: List<String>,
    val detectedSource: String
)

@Immutable
data class ElectionCycleComparison(
    val id: String,
    val cyclesCompared: String,
    val policyContinuityNote: String,
    val programChangesNote: String,
    val serviceImpactForecast: String,
    val budgetTrendSummary: String,
    val verifiedBasis: EpistemicTier = EpistemicTier.VERIFIED_DATA,
    val forecastBasis: EpistemicTier = EpistemicTier.FORECAST
)

@Immutable
data class ServiceResourceItem(
    val id: String,
    val topic: SocialIssueTopic,
    val organizationName: String,
    val serviceSummary: String,
    val accessChannel: String,
    val verifiedDateIso: String,
    val epistemicTier: EpistemicTier = EpistemicTier.VERIFIED_DATA
)

enum class ModerationStatus(val label: String) {
    PENDING_HUMAN_REVIEW("Pending Human Review"),
    HUMAN_VERIFIED("Human Verified"),
    HELD_FOR_POLICY_CHECK("Held — Neutrality / PII Check")
}

@Immutable
data class CommunityReport(
    val id: String,
    val topic: SocialIssueTopic,
    val title: String,
    val observableDetail: String,
    val locationLabel: String,
    val reporterReputation: Float,
    val moderationStatus: ModerationStatus,
    val piiRedacted: Boolean,
    val epistemicTier: EpistemicTier = EpistemicTier.USER_GENERATED
)

@Immutable
data class FieldInvestigatorCase(
    val id: String,
    val title: String,
    val assignedRole: String,
    val taskStatus: String,
    val locationLoggingConsentGranted: Boolean,
    val consentBoundaryNote: String,
    val observations: List<String>,
    val outcomeSummary: String,
    val nonSurveillanceVerified: Boolean = true,
    val epistemicTier: EpistemicTier = EpistemicTier.USER_GENERATED
)

@Immutable
data class SelfImprovementProposal(
    val id: String,
    val area: String,
    val recommendation: String,
    val measuredSignal: String,
    val humanApproved: Boolean? = null,
    val epistemicTier: EpistemicTier = EpistemicTier.AI_GENERATED_INSIGHT
)

@Immutable
data class OptimizationReport(
    val id: String,
    val cadence: UpdateCadence,
    val generatedAtIso: String,
    val retentionRateEstimate: Float,
    val engagementQualityScore: Float,
    val accessibilityAdoptionRate: Float,
    val summary: String,
    val epistemicTier: EpistemicTier = EpistemicTier.FORECAST
)
